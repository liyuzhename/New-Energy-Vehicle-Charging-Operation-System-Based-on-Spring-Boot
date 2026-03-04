package com.charging.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wallet")
public class Wallet {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private BigDecimal balance;

    private BigDecimal totalRecharge;

    private BigDecimal totalConsume;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
