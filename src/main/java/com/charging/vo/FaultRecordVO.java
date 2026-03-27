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

    /** 运营商名称 */
    private String operatorName;

    /** 提报人用户名 */
    private String reporterName;

    private String description;

    private String status;

    private String handleNote;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
