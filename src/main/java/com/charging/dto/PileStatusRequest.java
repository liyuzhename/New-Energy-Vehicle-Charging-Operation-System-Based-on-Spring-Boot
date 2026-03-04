package com.charging.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PileStatusRequest {

    @NotBlank(message = "状态不能为空")
    private String status;
}
