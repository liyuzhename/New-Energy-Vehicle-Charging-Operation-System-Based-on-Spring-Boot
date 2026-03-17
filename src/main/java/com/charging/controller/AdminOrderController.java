package com.charging.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.result.Result;
import com.charging.service.ChargingOrderService;
import com.charging.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final ChargingOrderService orderService;

    @GetMapping("/list")
    public Result<Page<OrderVO>> list(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return Result.success(orderService.listForAdmin(status, orderNo, startDate, endDate, page, size));
    }

    @PutMapping("/{id}/refund/approve")
    public Result<Void> approveRefund(@PathVariable("id") Long id) {
        orderService.approveRefund(id);
        return Result.success("退款已批准", null);
    }

    @PutMapping("/{id}/refund/reject")
    public Result<Void> rejectRefund(@PathVariable("id") Long id) {
        orderService.rejectRefund(id);
        return Result.success("退款已拒绝", null);
    }
}
