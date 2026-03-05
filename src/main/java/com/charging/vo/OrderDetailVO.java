package com.charging.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDetailVO {

    private Long id;

    private String orderNo;

    private Long userId;

    private Long vehicleId;

    private Long gunId;

    private Long pileId;

    private Long stationId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal chargeKwh;

    private BigDecimal chargeFee;

    private BigDecimal serviceFee;

    private BigDecimal totalFee;

    private String status;

    private String payStatus;

    private LocalDateTime createTime;

    private FeeDetailVO feeDetail;
}
