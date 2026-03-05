package com.charging.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserManageVO {

    private Long id;

    private String username;

    private String phone;

    private String role;

    private String nickname;

    private Integer status;

    private LocalDateTime createTime;

    private Long stationCount;
}
