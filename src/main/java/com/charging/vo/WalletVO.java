package com.charging.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletVO {

    private Long id;

    private Long userId;

    private BigDecimal balance;

    private BigDecimal totalRecharge;

    private BigDecimal totalConsume;
}
