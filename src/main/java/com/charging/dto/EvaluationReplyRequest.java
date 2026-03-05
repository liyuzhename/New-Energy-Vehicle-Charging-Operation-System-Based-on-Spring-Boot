package com.charging.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EvaluationReplyRequest {

    @NotBlank(message = "回复内容不能为空")
    private String reply;
}
