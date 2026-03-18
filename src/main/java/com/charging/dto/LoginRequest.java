package com.charging.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /** 记住密码：true 时 token 有效期延长至 7 天，默认 false（1 天） */
    private boolean rememberMe = false;
}
