package com.charging.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.result.Result;
import com.charging.dto.StartChargingRequest;
import com.charging.security.util.SecurityUtils;
import com.charging.service.ChargingOrderService;
import com.charging.vo.OrderDetailVO;
import com.charging.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final ChargingOrderService orderService;

    @PostMapping("/start")
    public Result<OrderVO> start(@Valid @RequestBody StartChargingRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(orderService.start(userId, request));
    }

    @GetMapping("/charging")
    public Result<OrderVO> getCharging() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(orderService.getCharging(userId));
    }

    @PutMapping("/{orderId}/stop")
    public Result<OrderDetailVO> stop(@PathVariable Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(orderService.stop(userId, orderId));
    }

    @GetMapping("/{orderId}")
    public Result<OrderDetailVO> detail(@PathVariable Long orderId) {
        return Result.success(orderService.getDetail(orderId));
    }

    @GetMapping("/my")
    public Result<Page<OrderVO>> listMy(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(orderService.listMy(userId, status, page, size));
    }

    @PostMapping("/{orderId}/refund")
    public Result<Void> applyRefund(@PathVariable Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        orderService.applyRefund(userId, orderId);
        return Result.success("退款申请已提交", null);
    }

    @PostMapping("/{orderId}/pay")
    public Result<Void> pay(@PathVariable Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        orderService.pay(userId, orderId);
        return Result.success("支付成功", null);
    }
}
