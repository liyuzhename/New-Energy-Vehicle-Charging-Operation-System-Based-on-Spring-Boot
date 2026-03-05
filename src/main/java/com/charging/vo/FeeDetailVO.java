package com.charging.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FeeDetailVO {

    private BigDecimal totalChargeKwh;

    private BigDecimal totalChargeFee;

    private BigDecimal totalServiceFee;

    private BigDecimal totalFee;

    private List<FeeSegmentVO> segments;
}
