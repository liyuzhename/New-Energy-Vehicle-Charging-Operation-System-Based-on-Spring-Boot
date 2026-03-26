package com.charging.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FaultRecordVO {

    private Long id;

    private Long userId;

    private Long pileId;

    private String pileNo;

    private String stationName;

    private String description;

    private String status;

    private String handleNote;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
