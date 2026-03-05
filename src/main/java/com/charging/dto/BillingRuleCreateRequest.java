package com.charging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BillingRuleCreateRequest {

    @NotNull(message = "充电站ID不能为空")
    private Long stationId;

    @NotBlank(message = "时段类型不能为空")
    private String periodType;

    @NotNull(message = "开始小时不能为空")
    private Integer startHour;

    @NotNull(message = "结束小时不能为空")
    private Integer endHour;

    @NotNull(message = "电费单价不能为空")
    private BigDecimal electricityPrice;

    @NotNull(message = "服务费单价不能为空")
    private BigDecimal servicePrice;

    @NotNull(message = "生效日期不能为空")
    private LocalDate effectiveDate;
}
