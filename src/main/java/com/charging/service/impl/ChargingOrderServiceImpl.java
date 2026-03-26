package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.exception.BusinessException;
import com.charging.dto.StartChargingRequest;
import com.charging.entity.ChargingGun;
import com.charging.entity.ChargingOrder;
import com.charging.entity.ChargingPile;
import com.charging.entity.ChargingStation;
import com.charging.entity.User;
import com.charging.entity.Vehicle;
import com.charging.mapper.ChargingGunMapper;
import com.charging.mapper.ChargingOrderMapper;
import com.charging.mapper.ChargingPileMapper;
import com.charging.mapper.ChargingStationMapper;
import com.charging.mapper.UserMapper;
import com.charging.mapper.VehicleMapper;
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
    private final ChargingStationMapper stationMapper;
    private final VehicleMapper vehicleMapper;
    private final UserMapper userMapper;

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
        // 无进行中订单时返回 null，前端轮询时不触发错误弹窗
        if (order == null) return null;
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        long seconds = Duration.between(order.getStartTime(), LocalDateTime.now()).getSeconds();
        vo.setChargingSeconds(seconds);
        vo.setChargeDuration(seconds);

        ChargingPile pile = pileMapper.selectById(order.getPileId());
        if (pile != null) {
            vo.setPileNo(pile.getPileNo());
            vo.setPower(pile.getPower());
            // 实时计算已充电量 = 功率(kW) * 时长(h)
            BigDecimal hours = BigDecimal.valueOf(seconds).divide(BigDecimal.valueOf(3600), 6, RoundingMode.HALF_UP);
            vo.setChargeKwh(pile.getPower().multiply(hours).setScale(3, RoundingMode.HALF_UP));
            // 预估费用
            FeeDetailVO fee = billingRuleService.calculateFee(
                    order.getStationId(), pile.getPower(), order.getStartTime(), LocalDateTime.now());
            vo.setEstimatedFee(fee.getTotalFee());
        }
        if (order.getStationId() != null) {
            ChargingStation station = stationMapper.selectById(order.getStationId());
            if (station != null) vo.setStationName(station.getName());
        }
        if (order.getVehicleId() != null) {
            Vehicle vehicle = vehicleMapper.selectById(order.getVehicleId());
            if (vehicle != null) vo.setPlateNo(vehicle.getPlateNo());
        }
        if (order.getUserId() != null) {
            User user = userMapper.selectById(order.getUserId());
            if (user != null) {
                vo.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }
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
        // 填充关联字段
        if (pile != null) vo.setPileNo(pile.getPileNo());
        if (order.getStationId() != null) {
            ChargingStation station = stationMapper.selectById(order.getStationId());
            if (station != null) vo.setStationName(station.getName());
        }
        if (order.getVehicleId() != null) {
            Vehicle vehicle = vehicleMapper.selectById(order.getVehicleId());
            if (vehicle != null) vo.setPlateNo(vehicle.getPlateNo());
        }
        if (order.getStartTime() != null && order.getEndTime() != null) {
            vo.setChargeDuration(java.time.Duration.between(order.getStartTime(), order.getEndTime()).getSeconds());
        }
        return vo;
    }

    @Override
    public OrderDetailVO getDetail(Long orderId) {
        ChargingOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "订单不存在");
        OrderDetailVO vo = new OrderDetailVO();
        BeanUtils.copyProperties(order, vo);
        ChargingPile pile = pileMapper.selectById(order.getPileId());
        if ("CHARGING".equals(order.getStatus())) {
            // 进行中订单：实时计算预估费用
            LocalDateTime now = LocalDateTime.now();
            FeeDetailVO feeDetail = billingRuleService.calculateFee(
                    order.getStationId(), pile != null ? pile.getPower() : new BigDecimal("7.0"),
                    order.getStartTime(), now);
            vo.setTotalFee(feeDetail.getTotalFee());
            vo.setFeeDetail(feeDetail);
            if (order.getStartTime() != null) {
                vo.setChargeDuration(java.time.Duration.between(order.getStartTime(), now).getSeconds());
            }
        } else if (order.getEndTime() != null) {
            // 已完成订单：直接用数据库存储的费用构建 feeDetail，避免重算与历史数据不一致
            FeeDetailVO feeDetail = new FeeDetailVO();
            feeDetail.setTotalChargeKwh(order.getChargeKwh());
            feeDetail.setTotalChargeFee(order.getChargeFee());
            feeDetail.setTotalServiceFee(order.getServiceFee());
            feeDetail.setTotalFee(order.getTotalFee());
            feeDetail.setSegments(java.util.Collections.emptyList());
            vo.setFeeDetail(feeDetail);
            vo.setChargeDuration(java.time.Duration.between(order.getStartTime(), order.getEndTime()).getSeconds());
        }
        // 填充关联字段
        if (pile != null) vo.setPileNo(pile.getPileNo());
        if (order.getStationId() != null) {
            ChargingStation station = stationMapper.selectById(order.getStationId());
            if (station != null) vo.setStationName(station.getName());
        }
        if (order.getVehicleId() != null) {
            Vehicle vehicle = vehicleMapper.selectById(order.getVehicleId());
            if (vehicle != null) vo.setPlateNo(vehicle.getPlateNo());
        }
        return vo;
    }

    @Override
    public Page<OrderVO> listMy(Long userId, String status, String payStatus, int page, int size) {
        LambdaQueryWrapper<ChargingOrder> wrapper = new LambdaQueryWrapper<ChargingOrder>()
                .eq(ChargingOrder::getUserId, userId)
                .orderByDesc(ChargingOrder::getCreateTime);
        if (status != null && !status.isEmpty()) wrapper.eq(ChargingOrder::getStatus, status);
        if (payStatus != null && !payStatus.isEmpty()) wrapper.eq(ChargingOrder::getPayStatus, payStatus);
        Page<ChargingOrder> orderPage = orderMapper.selectPage(new Page<>(page, size), wrapper);
        return toVoPageWithDetails(orderPage);
    }

    @Override
    public Page<OrderVO> listForOperator(Long operatorId, Long stationId, String status, String payStatus, String orderNo, LocalDate startDate, LocalDate endDate, int page, int size) {
        LambdaQueryWrapper<ChargingOrder> wrapper = new LambdaQueryWrapper<ChargingOrder>()
                .eq(ChargingOrder::getOperatorId, operatorId)
                .orderByDesc(ChargingOrder::getCreateTime);
        if (stationId != null) wrapper.eq(ChargingOrder::getStationId, stationId);
        if (status != null && !status.isEmpty()) wrapper.eq(ChargingOrder::getStatus, status);
        if (payStatus != null && !payStatus.isEmpty()) wrapper.eq(ChargingOrder::getPayStatus, payStatus);
        if (orderNo != null && !orderNo.isEmpty()) wrapper.like(ChargingOrder::getOrderNo, orderNo);
        if (startDate != null) wrapper.ge(ChargingOrder::getCreateTime, startDate.atStartOfDay());
        if (endDate != null) wrapper.lt(ChargingOrder::getCreateTime, endDate.plusDays(1).atStartOfDay());
        return toVoPageWithDetails(orderMapper.selectPage(new Page<>(page, size), wrapper));
    }

    @Override
    public Page<OrderVO> listForAdmin(String status, String payStatus, String orderNo, LocalDate startDate, LocalDate endDate, int page, int size) {
        LambdaQueryWrapper<ChargingOrder> wrapper = new LambdaQueryWrapper<ChargingOrder>()
                .orderByDesc(ChargingOrder::getCreateTime);
        if (status != null && !status.isEmpty()) wrapper.eq(ChargingOrder::getStatus, status);
        if (payStatus != null && !payStatus.isEmpty()) wrapper.eq(ChargingOrder::getPayStatus, payStatus);
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
    public IncomeVO getIncome(Long operatorId, Long stationId, LocalDate startDate, LocalDate endDate) {
        LocalDate endExclusive = endDate.plusDays(1);
        // daily：按日聚合，支持 stationId 过滤（用于柱状图）
        List<Map<String, Object>> daily = orderMapper.selectDailyIncome(operatorId, stationId, startDate, endExclusive);
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
        // 按充电站聚合汇总
        vo.setStationList(orderMapper.selectIncomeByStation(operatorId, stationId, startDate, endExclusive));
        // 按充电站+日期双维度明细（含日期字段，用于收益明细列表）
        vo.setDetailList(orderMapper.selectIncomeByStationAndDay(operatorId, stationId, startDate, endExclusive));
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

    private Page<OrderVO> toVoPageWithDetails(Page<ChargingOrder> page) {
        Page<OrderVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(o -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(o, vo);
            // 充电站名称
            if (o.getStationId() != null) {
                ChargingStation station = stationMapper.selectById(o.getStationId());
                if (station != null) vo.setStationName(station.getName());
            }
            // 充电桩编号
            if (o.getPileId() != null) {
                ChargingPile pile = pileMapper.selectById(o.getPileId());
                if (pile != null) {
                    vo.setPileNo(pile.getPileNo());
                    vo.setPower(pile.getPower());
                }
            }
            // 车牌号
            if (o.getVehicleId() != null) {
                Vehicle vehicle = vehicleMapper.selectById(o.getVehicleId());
                if (vehicle != null) vo.setPlateNo(vehicle.getPlateNo());
            }
            // 用户昵称
            if (o.getUserId() != null) {
                User user = userMapper.selectById(o.getUserId());
                if (user != null) {
                    vo.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                }
            }
            // 充电时长（秒）
            if (o.getStartTime() != null && o.getEndTime() != null) {
                vo.setChargeDuration(Duration.between(o.getStartTime(), o.getEndTime()).getSeconds());
            } else if (o.getStartTime() != null && "CHARGING".equals(o.getStatus())) {
                vo.setChargeDuration(Duration.between(o.getStartTime(), LocalDateTime.now()).getSeconds());
            }
            return vo;
        }).toList());
        return voPage;
    }
}
