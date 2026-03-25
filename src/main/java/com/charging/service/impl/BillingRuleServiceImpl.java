package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.charging.common.exception.BusinessException;
import com.charging.dto.BillingRuleCreateRequest;
import com.charging.dto.BillingRuleUpdateRequest;
import com.charging.entity.BillingRule;
import com.charging.entity.ChargingStation;
import com.charging.mapper.BillingRuleMapper;
import com.charging.mapper.ChargingStationMapper;
import com.charging.service.BillingRuleService;
import com.charging.vo.BillingRuleVO;
import com.charging.vo.FeeDetailVO;
import com.charging.vo.FeeSegmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingRuleServiceImpl implements BillingRuleService {

    private final BillingRuleMapper billingRuleMapper;
    private final ChargingStationMapper chargingStationMapper;

    @Override
    public List<BillingRuleVO> listByStation(Long stationId) {
        List<BillingRule> rules = billingRuleMapper.selectList(
                new LambdaQueryWrapper<BillingRule>()
                        .eq(BillingRule::getStationId, stationId)
                        .orderByAsc(BillingRule::getStartHour)
        );
        return rules.stream().map(r -> {
            BillingRuleVO vo = new BillingRuleVO();
            BeanUtils.copyProperties(r, vo);
            return vo;
        }).toList();
    }

    @Override
    public void create(Long operatorId, BillingRuleCreateRequest request) {
        checkStationOwnership(operatorId, request.getStationId());
        // endHour=0 视为 24（当天结束）
        int endHour = (request.getEndHour() == 0) ? 24 : request.getEndHour();
        if (endHour <= request.getStartHour()) {
            throw new BusinessException(400, "结束时间必须晚于开始时间（如需设置到24:00，请选择00:00）");
        }
        request.setEndHour(endHour);
        checkTimeOverlap(request.getStationId(), null, request.getStartHour(), endHour);

        BillingRule rule = new BillingRule();
        BeanUtils.copyProperties(request, rule);
        rule.setOperatorId(operatorId);
        billingRuleMapper.insert(rule);
    }

    @Override
    public void update(Long operatorId, Long id, BillingRuleUpdateRequest request) {
        BillingRule rule = getOwnedRule(operatorId, id);
        if (request.getPeriodType() != null) rule.setPeriodType(request.getPeriodType());
        if (request.getStartHour() != null) rule.setStartHour(request.getStartHour());
        if (request.getEndHour() != null) {
            // endHour=0 视为 24（当天结束）
            int endHour = (request.getEndHour() == 0) ? 24 : request.getEndHour();
            rule.setEndHour(endHour);
        }
        if (request.getElectricityPrice() != null) rule.setElectricityPrice(request.getElectricityPrice());
        if (request.getServicePrice() != null) rule.setServicePrice(request.getServicePrice());
        if (request.getEffectiveDate() != null) rule.setEffectiveDate(request.getEffectiveDate());

        int newStart = rule.getStartHour();
        int newEnd = rule.getEndHour();
        if (newEnd <= newStart) {
            throw new BusinessException(400, "结束时间必须晚于开始时间（如需设置到24:00，请选择00:00）");
        }
        checkTimeOverlap(rule.getStationId(), id, newStart, newEnd);
        billingRuleMapper.updateById(rule);
    }

    @Override
    public void delete(Long operatorId, Long id) {
        getOwnedRule(operatorId, id);
        billingRuleMapper.deleteById(id);
    }

    @Override
    public FeeDetailVO calculateFee(Long stationId, BigDecimal power, LocalDateTime startTime, LocalDateTime endTime) {
        List<BillingRule> rules = billingRuleMapper.selectList(
                new LambdaQueryWrapper<BillingRule>()
                        .eq(BillingRule::getStationId, stationId)
                        .orderByAsc(BillingRule::getStartHour)
        );

        FeeDetailVO detail = new FeeDetailVO();
        List<FeeSegmentVO> segments = new ArrayList<>();
        BigDecimal totalKwh = BigDecimal.ZERO;
        BigDecimal totalChargeFee = BigDecimal.ZERO;
        BigDecimal totalServiceFee = BigDecimal.ZERO;

        // 若无计费规则，按默认费率计算（电费 1.0 元/kWh，服务费 0.5 元/kWh）
        if (rules.isEmpty()) {
            double hours = Duration.between(startTime, endTime).toMinutes() / 60.0;
            BigDecimal kwh = power.multiply(BigDecimal.valueOf(hours)).setScale(3, RoundingMode.HALF_UP);
            BigDecimal eFee = kwh.multiply(new BigDecimal("1.0000")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal sFee = kwh.multiply(new BigDecimal("0.5000")).setScale(2, RoundingMode.HALF_UP);
            FeeSegmentVO seg = new FeeSegmentVO();
            seg.setPeriodType("FLAT");
            seg.setStartHour(0);
            seg.setEndHour(24);
            seg.setChargeKwh(kwh);
            seg.setElectricityPrice(new BigDecimal("1.0000"));
            seg.setServicePrice(new BigDecimal("0.5000"));
            seg.setElectricityFee(eFee);
            seg.setServiceFee(sFee);
            seg.setSubtotal(eFee.add(sFee));
            segments.add(seg);
            detail.setTotalChargeKwh(kwh);
            detail.setTotalChargeFee(eFee);
            detail.setTotalServiceFee(sFee);
            detail.setTotalFee(eFee.add(sFee));
            detail.setSegments(segments);
            return detail;
        }

        // 按分钟遍历充电时段，匹配对应计费规则
        LocalDateTime cursor = startTime;
        while (cursor.isBefore(endTime)) {
            int hour = cursor.getHour();
            BillingRule matched = matchRule(rules, hour);
            if (matched == null) {
                cursor = cursor.plusMinutes(1);
                continue;
            }
            // 找到该规则时段的结束时间点（endHour=24 表示当日结束）
            LocalDateTime ruleEnd;
            if (matched.getEndHour() >= 24) {
                ruleEnd = cursor.toLocalDate().plusDays(1).atStartOfDay();
            } else {
                ruleEnd = cursor.toLocalDate().atTime(matched.getEndHour(), 0);
            }
            if (!ruleEnd.isAfter(cursor)) {
                ruleEnd = ruleEnd.plusDays(1);
            }
            LocalDateTime segEnd = ruleEnd.isBefore(endTime) ? ruleEnd : endTime;

            double segHours = Duration.between(cursor, segEnd).toMinutes() / 60.0;
            BigDecimal kwh = power.multiply(BigDecimal.valueOf(segHours)).setScale(3, RoundingMode.HALF_UP);
            BigDecimal eFee = kwh.multiply(matched.getElectricityPrice()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal sFee = kwh.multiply(matched.getServicePrice()).setScale(2, RoundingMode.HALF_UP);

            // 合并相同时段的分段
            boolean merged = false;
            for (FeeSegmentVO existing : segments) {
                if (existing.getPeriodType().equals(matched.getPeriodType())
                        && existing.getStartHour().equals(matched.getStartHour())) {
                    existing.setChargeKwh(existing.getChargeKwh().add(kwh));
                    existing.setElectricityFee(existing.getElectricityFee().add(eFee));
                    existing.setServiceFee(existing.getServiceFee().add(sFee));
                    existing.setSubtotal(existing.getSubtotal().add(eFee).add(sFee));
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                FeeSegmentVO seg = new FeeSegmentVO();
                seg.setPeriodType(matched.getPeriodType());
                seg.setStartHour(matched.getStartHour());
                seg.setEndHour(matched.getEndHour());
                seg.setChargeKwh(kwh);
                seg.setElectricityPrice(matched.getElectricityPrice());
                seg.setServicePrice(matched.getServicePrice());
                seg.setElectricityFee(eFee);
                seg.setServiceFee(sFee);
                seg.setSubtotal(eFee.add(sFee));
                segments.add(seg);
            }
            totalKwh = totalKwh.add(kwh);
            totalChargeFee = totalChargeFee.add(eFee);
            totalServiceFee = totalServiceFee.add(sFee);
            cursor = segEnd;
        }

        segments.sort(Comparator.comparing(FeeSegmentVO::getStartHour));
        detail.setTotalChargeKwh(totalKwh.setScale(3, RoundingMode.HALF_UP));
        detail.setTotalChargeFee(totalChargeFee.setScale(2, RoundingMode.HALF_UP));
        detail.setTotalServiceFee(totalServiceFee.setScale(2, RoundingMode.HALF_UP));
        detail.setTotalFee(totalChargeFee.add(totalServiceFee).setScale(2, RoundingMode.HALF_UP));
        detail.setSegments(segments);
        return detail;
    }

    private BillingRule matchRule(List<BillingRule> rules, int hour) {
        for (BillingRule r : rules) {
            if (r.getStartHour() <= hour && hour < r.getEndHour()) {
                return r;
            }
        }
        return null;
    }

    private void checkTimeOverlap(Long stationId, Long excludeId, int startHour, int endHour) {
        List<BillingRule> existing = billingRuleMapper.selectList(
                new LambdaQueryWrapper<BillingRule>().eq(BillingRule::getStationId, stationId)
        );
        for (BillingRule r : existing) {
            if (excludeId != null && r.getId().equals(excludeId)) continue;
            if (startHour < r.getEndHour() && endHour > r.getStartHour()) {
                throw new BusinessException(400, "时段与已有规则 [" + r.getStartHour() + ":00-" + r.getEndHour() + ":00] 重叠");
            }
        }
    }

    private void checkStationOwnership(Long operatorId, Long stationId) {
        ChargingStation station = chargingStationMapper.selectById(stationId);
        if (station == null) throw new BusinessException(404, "充电站不存在");
        if (!station.getOperatorId().equals(operatorId)) throw new BusinessException(403, "无权操作该充电站");
    }

    private BillingRule getOwnedRule(Long operatorId, Long id) {
        BillingRule rule = billingRuleMapper.selectById(id);
        if (rule == null) throw new BusinessException(404, "计费规则不存在");
        if (!rule.getOperatorId().equals(operatorId)) throw new BusinessException(403, "无权操作该计费规则");
        return rule;
    }
}
