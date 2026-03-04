package com.charging.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StationCreateRequest {

    @NotBlank(message = "充电站名称不能为空")
    private String name;

    @NotBlank(message = "详细地址不能为空")
    private String address;

    private String city;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String businessHours;

    private String parkingFee;
}
