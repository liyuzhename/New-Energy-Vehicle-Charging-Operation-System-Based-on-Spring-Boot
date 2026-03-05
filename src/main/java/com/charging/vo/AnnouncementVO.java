package com.charging.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnnouncementVO {

    private Long id;

    private String title;

    private String content;

    private String type;

    private String status;

    private Long creatorId;

    private LocalDateTime createTime;
}
