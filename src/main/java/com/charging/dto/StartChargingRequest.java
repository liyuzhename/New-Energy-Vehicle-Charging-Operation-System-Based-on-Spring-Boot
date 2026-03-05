package com.charging.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartChargingRequest {

    @NotBlank(message = "充电桩编号不能为空")
    private String pileNo;

    private Long vehicleId;
}
