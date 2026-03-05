package com.charging.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReservationVO {

    private Long id;

    private Long userId;

    private Long pileId;

    private LocalDate reserveDate;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String status;

    private LocalDateTime createTime;
}
