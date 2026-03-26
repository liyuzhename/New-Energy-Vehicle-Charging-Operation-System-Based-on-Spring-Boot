package com.charging.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class IncomeVO {

    private BigDecimal totalChargeFee;

    private BigDecimal totalServiceFee;

    private BigDecimal totalFee;

    /** 按日聚合明细（用于柱状图，含 stationId 过滤） */
    private List<Map<String, Object>> daily;

    /** 按充电站聚合汇总 */
    private List<Map<String, Object>> stationList;

    /** 按充电站+日期双维度明细（用于收益明细列表，含日期字段） */
    private List<Map<String, Object>> detailList;
}
