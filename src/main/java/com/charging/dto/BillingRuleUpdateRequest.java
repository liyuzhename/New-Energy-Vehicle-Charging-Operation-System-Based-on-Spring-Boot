package com.charging.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BillingRuleUpdateRequest {

    private String periodType;

    private Integer startHour;

    private Integer endHour;

    private BigDecimal electricityPrice;

    private BigDecimal servicePrice;

    private LocalDate effectiveDate;
}
