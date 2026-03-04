package com.charging.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VehicleVO {

    private Long id;

    private Long userId;

    private String plateNo;

    private String brand;

    private String model;

    private BigDecimal batteryCap;

    private LocalDateTime createTime;
}
