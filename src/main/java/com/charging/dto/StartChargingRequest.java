package com.charging.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartChargingRequest {

    @NotBlank(message = "充电桩编号不能为空")
    private String pileNo;

    /** 充电枪 ID，前端选择枪口后传入 */
    private Long gunId;

    private Long vehicleId;
}
