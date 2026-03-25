package com.charging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GunCreateRequest {

    @NotNull(message = "充电桩ID不能为空")
    private Long pileId;

    @NotBlank(message = "枪类型不能为空")
    private String gunType;

    private BigDecimal power;
}
