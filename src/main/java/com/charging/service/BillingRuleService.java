package com.charging.service;

import com.charging.dto.BillingRuleCreateRequest;
import com.charging.dto.BillingRuleUpdateRequest;
import com.charging.vo.BillingRuleVO;
import com.charging.vo.FeeDetailVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface BillingRuleService {

    List<BillingRuleVO> listByStation(Long stationId);

    void create(Long operatorId, BillingRuleCreateRequest request);

    void update(Long operatorId, Long id, BillingRuleUpdateRequest request);

    void delete(Long operatorId, Long id);

    /**
     * 核心计费方法：按峰平谷规则分段计算费用
     * @param stationId 充电站ID
     * @param power 充电桩额定功率(kW)
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    FeeDetailVO calculateFee(Long stationId, BigDecimal power, LocalDateTime startTime, LocalDateTime endTime);
}
