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
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        return Result.success(orderService.listForOperator(operatorId, status, orderNo, startDate, endDate, page, size));
    }

    @GetMapping("/income")
    public Result<IncomeVO> income(
            @RequestParam(value = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        return Result.success(orderService.getIncome(operatorId, startDate, endDate));
    }
}
