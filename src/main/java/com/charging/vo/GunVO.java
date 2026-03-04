package com.charging.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GunVO {

    private Long id;

    private Long pileId;

    private String gunNo;

    private String gunType;

    private BigDecimal power;

    private String status;
}
