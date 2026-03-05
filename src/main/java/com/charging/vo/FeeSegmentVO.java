package com.charging.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeeSegmentVO {

    private String periodType;

    private Integer startHour;

    private Integer endHour;

    private BigDecimal chargeKwh;

    private BigDecimal electricityPrice;

    private BigDecimal servicePrice;

    private BigDecimal electricityFee;

    private BigDecimal serviceFee;

    private BigDecimal subtotal;
}
