package com.charging.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReservationVO {

    private Long id;

    private Long userId;

    private Long pileId;

    private Long gunId;

    private Long orderId;

    private LocalDate reserveDate;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String status;

    private LocalDateTime createTime;

    /** 充电站名称 */
    private String stationName;

    /** 充电桩编号 */
    private String pileNo;

    /** 充电枪编号 */
    private String gunNo;

    /** 车牌号 */
    private String plateNo;
}
