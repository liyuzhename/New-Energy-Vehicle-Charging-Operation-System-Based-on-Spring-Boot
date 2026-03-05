package com.charging.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FaultHandleRequest {

    @NotBlank(message = "处理状态不能为空")
    private String status;

    private String handleNote;
}
