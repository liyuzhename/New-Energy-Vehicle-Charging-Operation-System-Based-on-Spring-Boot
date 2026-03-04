package com.charging.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("vehicle")
public class Vehicle {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String plateNo;

    private String brand;

    private String model;

    private BigDecimal batteryCap;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
