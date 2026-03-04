package com.charging.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("charging_pile")
public class ChargingPile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String pileNo;

    private Long stationId;

    private Long operatorId;

    private String pileType;

    private BigDecimal power;

    private String status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
