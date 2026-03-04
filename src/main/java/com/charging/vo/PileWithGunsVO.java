package com.charging.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PileWithGunsVO {

    private Long id;

    private String pileNo;

    private Long stationId;

    private Long operatorId;

    private String pileType;

    private BigDecimal power;

    private String status;

    private LocalDateTime createTime;

    private List<GunVO> guns;
}
