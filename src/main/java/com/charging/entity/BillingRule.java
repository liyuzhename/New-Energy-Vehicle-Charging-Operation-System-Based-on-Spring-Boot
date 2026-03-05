package com.charging.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("billing_rule")
public class BillingRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long stationId;

    private Long operatorId;

    private String periodType;

    private Integer startHour;

    private Integer endHour;

    private BigDecimal electricityPrice;

    private BigDecimal servicePrice;

    private LocalDate effectiveDate;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
