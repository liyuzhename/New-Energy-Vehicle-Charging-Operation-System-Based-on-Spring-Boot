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

    /** 评价用户昵称 */
    private String userName;

    /** 评价用户头像 */
    private String userAvatar;

    /** 充电站名称 */
    private String stationName;
}
