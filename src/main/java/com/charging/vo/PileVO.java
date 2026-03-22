package com.charging.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PileVO {

    private Long id;

    private String pileNo;

    private Long stationId;

    private String stationName;

    private Long operatorId;

    private String pileType;

    private BigDecimal power;

    private String status;

    private LocalDateTime createTime;
}
