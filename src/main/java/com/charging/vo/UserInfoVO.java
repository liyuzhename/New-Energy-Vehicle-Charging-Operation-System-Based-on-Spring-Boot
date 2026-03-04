package com.charging.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfoVO {

    private Long id;

    private String username;

    private String phone;

    private String nickname;

    private String avatar;

    private String role;

    private Integer status;

    private LocalDateTime createTime;
}
