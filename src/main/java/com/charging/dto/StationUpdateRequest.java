package com.charging.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StationUpdateRequest {

    private String name;

    private String address;

    private String city;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String businessHours;

    private String parkingFee;

    private String status;
}
