package com.charging.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StationDetailVO {

    private Long id;

    private Long operatorId;

    private String name;

    private String address;

    private String city;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String businessHours;

    private String parkingFee;

    private String status;

    private LocalDateTime createTime;

    private Integer availablePileCount;

    private Integer totalPileCount;
}
