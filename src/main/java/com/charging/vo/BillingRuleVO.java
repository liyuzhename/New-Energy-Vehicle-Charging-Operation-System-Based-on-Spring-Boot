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

    /** 格式化时间段，如 "08:00-18:00"，endHour=24 显示为 24:00 */
    public String getTimeRange() {
        if (startHour == null || endHour == null) return null;
        return String.format("%02d:00-%02d:00", startHour, endHour);
    }

    private BigDecimal electricityPrice;

    private BigDecimal servicePrice;

    private LocalDate effectiveDate;
}
