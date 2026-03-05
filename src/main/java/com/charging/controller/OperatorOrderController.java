package com.charging.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.result.Result;
import com.charging.security.util.SecurityUtils;
import com.charging.service.ChargingOrderService;
import com.charging.vo.IncomeVO;
import com.charging.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/operator")
@RequiredArgsConstructor
public class OperatorOrderController {

    private final ChargingOrderService orderService;

    @GetMapping("/order/list")
    public Result<Page<OrderVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        return Result.success(orderService.listForOperator(operatorId, page, size));
    }

    @GetMapping("/income")
    public Result<IncomeVO> income(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        return Result.success(orderService.getIncome(operatorId, startDate, endDate));
    }
}
