package com.charging.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentRecordVO {

    private Long id;

    private Long userId;

    private Long orderId;

    private BigDecimal amount;

    private String type;

    private String remark;

    private LocalDateTime createTime;
}
