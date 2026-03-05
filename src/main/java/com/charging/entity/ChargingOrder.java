package com.charging.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("charging_order")
public class ChargingOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long vehicleId;

    private Long gunId;

    private Long pileId;

    private Long stationId;

    private Long operatorId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal chargeKwh;

    private BigDecimal chargeFee;

    private BigDecimal serviceFee;

    private BigDecimal totalFee;

    private String status;

    private String payStatus;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
