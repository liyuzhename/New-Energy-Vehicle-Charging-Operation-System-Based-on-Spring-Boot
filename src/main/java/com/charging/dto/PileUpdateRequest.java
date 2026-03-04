package com.charging.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PileUpdateRequest {

    private String pileNo;

    private String pileType;

    private BigDecimal power;
}
