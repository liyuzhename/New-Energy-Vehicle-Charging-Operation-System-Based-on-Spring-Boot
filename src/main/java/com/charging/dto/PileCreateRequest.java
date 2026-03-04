package com.charging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PileCreateRequest {

    @NotBlank(message = "桩编号不能为空")
    private String pileNo;

    @NotNull(message = "所属充电站不能为空")
    private Long stationId;

    @NotBlank(message = "桩类型不能为空")
    private String pileType;

    private BigDecimal power;
}
