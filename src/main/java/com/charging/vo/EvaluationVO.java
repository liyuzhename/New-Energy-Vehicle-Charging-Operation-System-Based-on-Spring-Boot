package com.charging.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EvaluationVO {

    private Long id;

    private Long userId;

    private Long orderId;

    private Long stationId;

    private Integer rating;

    private String content;

    private String reply;

    private Integer isHidden;

    private LocalDateTime createTime;

    private Double avgRating;
}
