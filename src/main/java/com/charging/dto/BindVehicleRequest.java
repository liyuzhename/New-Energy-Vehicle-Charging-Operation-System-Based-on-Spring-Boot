package com.charging.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BindVehicleRequest {

    @NotBlank(message = "车牌号不能为空")
    private String plateNo;

    private String brand;

    private String model;

    private BigDecimal batteryCap;
}
