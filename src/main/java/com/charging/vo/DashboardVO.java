package com.charging.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardVO {

    private Long totalUsers;

    private Long todayOrders;

    private BigDecimal todayIncome;

    private Long onlinePiles;
}
