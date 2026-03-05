package com.charging.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FaultCreateRequest {

    @NotBlank(message = "充电桩编号不能为空")
    private String pileNo;

    @NotBlank(message = "故障描述不能为空")
    private String description;
}
