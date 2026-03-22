package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.exception.BusinessException;
import com.charging.dto.StartChargingRequest;
import com.charging.entity.ChargingGun;
import com.charging.entity.ChargingOrder;
import com.charging.entity.ChargingPile;
import com.charging.mapper.ChargingGunMapper;
import com.charging.mapper.ChargingOrderMapper;
import com.charging.mapper.ChargingPileMapper;
import com.charging.service.BillingRuleService;
import com.charging.service.ChargingOrderService;
import com.charging.service.WalletService;
import com.charging.vo.FeeDetailVO;
import com.charging.vo.IncomeVO;
import com.charging.vo.OrderDetailVO;
import com.charging.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChargingOrderServiceImpl implements ChargingOrderService {

    private final ChargingOrderMapper orderMapper;
    private final ChargingPileMapper pileMapper;
    private final ChargingGunMapper gunMapper;
    private final BillingRuleService billingRuleService;
    private final WalletService walletService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO start(Long userId, StartChargingRequest request) {
        ChargingPile pile = pileMapper.selectOne(
                new LambdaQueryWrapper<ChargingPile>().eq(ChargingPile::getPileNo, request.getPileNo()));
        if (pile == null) throw new BusinessException(404, "充电桩不存在");
        if (!"IDLE".equals(pile.getStatus())) throw new BusinessException(400, "充电桩当前不可用，状态：" + pile.getStatus());

        long existingCount = orderMapper.selectCount(
                new LambdaQueryWrapper<ChargingOrder>()
                        .eq(ChargingOrder::getUserId, userId)
                        .eq(ChargingOrder::getStatus, "CHARGING"));
        if (existingCount > 0) throw new BusinessException(400, "您已有正在充电的订单");

        // 获取第一个可用充电枪
        ChargingGun gun = gunMapper.selectOne(
                new LambdaQueryWrapper<ChargingGun>()
                        .eq(ChargingGun::getPileId, pile.getId())
                        .eq(ChargingGun::getStatus, "IDLE")
                        .last("LIMIT 1"));

        ChargingOrder order = new ChargingOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setVehicleId(request.getVehicleId());
        order.setGunId(gun != null ? gun.getId() : null);
        order.setPileId(pile.getId());
        order.setStationId(pile.getStationId());
        order.setOperatorId(pile.getOperatorId());
        order.setStartTime(LocalDateTime.now());
        order.setChargeKwh(BigDecimal.ZERO);
        order.setChargeFee(BigDecimal.ZERO);
        order.setServiceFee(BigDecimal.ZERO);
        order.setTotalFee(BigDecimal.ZERO);
        order.setStatus("CHARGING");
        order.setPayStatus("UNPAID");
        orderMapper.insert(order);

        pile.setStatus("OCCUPIED");
        pileMapper.updateById(pile);
        if (gun != null) {
            gun.setStatus("OCCUPIED");
            gunMapper.updateById(gun);
        }

        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    @Override
    public OrderVO getCharging(Long userId) {
        ChargingOrder order = orderMapper.selectOne(
                new LambdaQueryWrapper<ChargingOrder>()
                        .eq(ChargingOrder::getUserId, userId)
                        .eq(ChargingOrder::getStatus, "CHARGING"));
        if (order == null) throw new BusinessException(404, "当前没有进行中的充电订单");
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        long seconds = Duration.between(order.getStartTime(), LocalDateTime.now()).getSeconds();
        vo.setChargingSeconds(seconds);
        // 预估费用：按当前时长计算
        ChargingPile pile = pileMapper.selectById(order.getPileId());
        if (pile != null) {
            FeeDetailVO fee = billingRuleService.calculateFee(
                    order.getStationId(), pile.getPower(), order.getStartTime(), LocalDateTime.now());
            vo.setEstimatedFee(fee.getTotalFee());
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO stop(Long userId, Long orderId) {
        ChargingOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (!order.getUserId().equals(userId)) throw new BusinessException(403, "无权操作该订单");
        if (!"CHARGING".equals(order.getStatus())) throw new BusinessException(400, "订单状态异常");

        LocalDateTime endTime = LocalDateTime.now();
        order.setEndTime(endTime);

        ChargingPile pile = pileMapper.selectById(order.getPileId());
        FeeDetailVO feeDetail = billingRuleService.calculateFee(
                order.getStationId(), pile != null ? pile.getPower() : new BigDecimal("7.0"),
                order.getStartTime(), endTime);

        order.setChargeKwh(feeDetail.getTotalChargeKwh());
        order.setChargeFee(feeDetail.getTotalChargeFee());
        order.setServiceFee(feeDetail.getTotalServiceFee());
        order.setTotalFee(feeDetail.getTotalFee());
        order.setStatus("FINISHED");
        orderMapper.updateById(order);

        if (pile != null) {
            pile.setStatus("IDLE");
            pileMapper.updateById(pile);
        }
        if (order.getGunId() != null) {
            ChargingGun gun = gunMapper.selectById(order.getGunId());
            if (gun != null) { gun.setStatus("IDLE"); gunMapper.updateById(gun); }
        }

        OrderDetailVO vo = new OrderDetailVO();
        BeanUtils.copyProperties(order, vo);
        vo.setFeeDetail(feeDetail);
        return vo;
    }

    @Override
    public OrderDetailVO getDetail(Long orderId) {
        ChargingOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "订单不存在");
        OrderDetailVO vo = new OrderDetailVO();
        BeanUtils.copyProperties(order, vo);
        if (order.getEndTime() != null) {
            ChargingPile pile = pileMapper.selectById(order.getPileId());
            FeeDetailVO feeDetail = billingRuleService.calculateFee(
                    order.getStationId(), pile != null ? pile.getPower() : new BigDecimal("7.0"),
                    order.getStartTime(), order.getEndTime());
            vo.setFeeDetail(feeDetail);
        }
        return vo;
    }

    @Override
    public Page<OrderVO> listMy(Long userId, String status, int page, int size) {
        LambdaQueryWrapper<ChargingOrder> wrapper = new LambdaQueryWrapper<ChargingOrder>()
                .eq(ChargingOrder::getUserId, userId)
                .orderByDesc(ChargingOrder::getCreateTime);
        if (status != null && !status.isEmpty()) wrapper.eq(ChargingOrder::getStatus, status);
        return toVoPage(orderMapper.selectPage(new Page<>(page, size), wrapper));
    }

    @Override
    public Page<OrderVO> listForOperator(Long operatorId, String status, String orderNo, LocalDate startDate, LocalDate endDate, int page, int size) {
        LambdaQueryWrapper<ChargingOrder> wrapper = new LambdaQueryWrapper<ChargingOrder>()
                .eq(ChargingOrder::getOperatorId, operatorId)
                .orderByDesc(ChargingOrder::getCreateTime);
        if (status != null && !status.isEmpty()) wrapper.eq(ChargingOrder::getStatus, status);
        if (orderNo != null && !orderNo.isEmpty()) wrapper.like(ChargingOrder::getOrderNo, orderNo);
        if (startDate != null) wrapper.ge(ChargingOrder::getCreateTime, startDate.atStartOfDay());
        if (endDate != null) wrapper.lt(ChargingOrder::getCreateTime, endDate.plusDays(1).atStartOfDay());
        return toVoPage(orderMapper.selectPage(new Page<>(page, size), wrapper));
    }

    @Override
    public Page<OrderVO> listForAdmin(String status, String orderNo, LocalDate startDate, LocalDate endDate, int page, int size) {
        LambdaQueryWrapper<ChargingOrder> wrapper = new LambdaQueryWrapper<ChargingOrder>()
                .orderByDesc(ChargingOrder::getCreateTime);
        if (status != null && !status.isEmpty()) wrapper.eq(ChargingOrder::getStatus, status);
        if (orderNo != null && !orderNo.isEmpty()) wrapper.like(ChargingOrder::getOrderNo, orderNo);
        if (startDate != null) wrapper.ge(ChargingOrder::getCreateTime, startDate.atStartOfDay());
        if (endDate != null) wrapper.lt(ChargingOrder::getCreateTime, endDate.plusDays(1).atStartOfDay());
        return toVoPage(orderMapper.selectPage(new Page<>(page, size), wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(Long userId, Long orderId) {
        ChargingOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (!order.getUserId().equals(userId)) throw new BusinessException(403, "无权操作该订单");
        if (!"PAID".equals(order.getPayStatus())) throw new BusinessException(400, "仅已支付订单可申请退款");
        order.setStatus("REFUNDING");
        order.setPayStatus("REFUNDING");
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRefund(Long orderId) {
        ChargingOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (!"REFUNDING".equals(order.getPayStatus())) throw new BusinessException(400, "订单不在退款审核中");
        walletService.refund(order.getUserId(), order.getTotalFee(), order.getId(), "订单退款 +" + order.getTotalFee() + " 元");
        order.setStatus("REFUNDED");
        order.setPayStatus("REFUNDED");
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRefund(Long orderId) {
        ChargingOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (!"REFUNDING".equals(order.getPayStatus())) throw new BusinessException(400, "订单不在退款审核中");
        order.setStatus("FINISHED");
        order.setPayStatus("PAID");
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(Long userId, Long orderId) {
        ChargingOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "订单不存在");
        if (!order.getUserId().equals(userId)) throw new BusinessException(403, "无权操作该订单");
        if (!"FINISHED".equals(order.getStatus())) throw new BusinessException(400, "订单未完成，无法支付");
        if (!"UNPAID".equals(order.getPayStatus())) throw new BusinessException(400, "订单已支付");
        walletService.deduct(userId, order.getTotalFee(), order.getId(), "充电订单消费 -" + order.getTotalFee() + " 元");
        order.setPayStatus("PAID");
        order.setStatus("FINISHED");
        orderMapper.updateById(order);
    }

    @Override
    public IncomeVO getIncome(Long operatorId, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> daily = orderMapper.selectDailyIncome(operatorId, startDate, endDate.plusDays(1));
        BigDecimal totalCharge = BigDecimal.ZERO;
        BigDecimal totalService = BigDecimal.ZERO;
        for (Map<String, Object> row : daily) {
            Object c = row.get("chargeFee");
            Object s = row.get("serviceFee");
            if (c != null) totalCharge = totalCharge.add(new BigDecimal(c.toString()));
            if (s != null) totalService = totalService.add(new BigDecimal(s.toString()));
        }
        IncomeVO vo = new IncomeVO();
        vo.setTotalChargeFee(totalCharge.setScale(2, RoundingMode.HALF_UP));
        vo.setTotalServiceFee(totalService.setScale(2, RoundingMode.HALF_UP));
        vo.setTotalFee(totalCharge.add(totalService).setScale(2, RoundingMode.HALF_UP));
        vo.setDaily(daily);
        return vo;
    }

    private String generateOrderNo() {
        return "ORD" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private Page<OrderVO> toVoPage(Page<ChargingOrder> page) {
        Page<OrderVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(o -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(o, vo);
            return vo;
        }).toList());
        return voPage;
    }
}
