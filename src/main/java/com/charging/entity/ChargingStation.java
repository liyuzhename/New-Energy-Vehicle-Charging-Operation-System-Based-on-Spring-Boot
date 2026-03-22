package com.charging.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("charging_station")
public class ChargingStation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long operatorId;

    private String name;

    private String address;

    private String city;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String businessHours;

    private String parkingFee;

    private String contactPhone;

    private String description;

    private String status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
