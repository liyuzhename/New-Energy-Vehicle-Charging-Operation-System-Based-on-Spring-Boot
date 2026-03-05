package com.charging.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.result.Result;
import com.charging.service.ChargingOrderService;
import com.charging.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final ChargingOrderService orderService;

    @GetMapping("/list")
    public Result<Page<OrderVO>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(orderService.listForAdmin(status, page, size));
    }

    @PutMapping("/{id}/refund/approve")
    public Result<Void> approveRefund(@PathVariable Long id) {
        orderService.approveRefund(id);
        return Result.success("退款已批准", null);
    }

    @PutMapping("/{id}/refund/reject")
    public Result<Void> rejectRefund(@PathVariable Long id) {
        orderService.rejectRefund(id);
        return Result.success("退款已拒绝", null);
    }
}
