package com.charging.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {

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

    /** 实时充电时长（秒），仅充电中订单返回 */
    private Long chargingSeconds;

    /** 预估费用，仅充电中订单返回 */
    private BigDecimal estimatedFee;

    /** 充电站名称 */
    private String stationName;

    /** 充电桩编号 */
    private String pileNo;

    /** 充电桩功率（kW） */
    private BigDecimal power;

    /** 车牌号 */
    private String plateNo;

    /** 充电时长（秒） */
    private Long chargeDuration;
}
