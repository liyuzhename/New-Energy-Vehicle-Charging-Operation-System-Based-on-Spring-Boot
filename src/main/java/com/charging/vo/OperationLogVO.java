package com.charging.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationLogVO {

    private Long id;

    private Long operatorId;

    private String operatorName;

    private String role;

    private String operation;

    private String method;

    private String ip;

    private Integer status;

    private LocalDateTime createTime;
}
