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

    private List<Map<String, Object>> daily;
}
