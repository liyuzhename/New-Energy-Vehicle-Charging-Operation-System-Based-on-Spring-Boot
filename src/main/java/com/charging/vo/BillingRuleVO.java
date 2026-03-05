package com.charging.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BillingRuleVO {

    private Long id;

    private Long stationId;

    private Long operatorId;

    private String periodType;

    private Integer startHour;

    private Integer endHour;

    private BigDecimal electricityPrice;

    private BigDecimal servicePrice;

    private LocalDate effectiveDate;
}
