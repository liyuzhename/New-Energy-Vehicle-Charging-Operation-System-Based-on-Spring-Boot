package com.charging.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.result.Result;
import com.charging.dto.ReservationCreateRequest;
import com.charging.security.util.SecurityUtils;
import com.charging.service.ReservationService;
import com.charging.vo.ReservationVO;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public Result<Void> create(@Valid @RequestBody ReservationCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        reservationService.create(userId, request);
        return Result.success("预约成功", null);
    }

    @GetMapping("/my")
    public Result<Page<ReservationVO>> listMy(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(reservationService.listMy(userId, status, page, size));
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable("id") Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        reservationService.cancel(userId, id);
        return Result.success("预约已取消", null);
    }

    @PutMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable("id") Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        reservationService.confirm(userId, id);
        return Result.success("确认到场成功", null);
    }

    @PostMapping("/{id}/start-charging")
    public Result<Long> startCharging(@PathVariable("id") Long id,
                                      @RequestBody(required = false) StartChargingBody body) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long vehicleId = body != null ? body.getVehicleId() : null;
        Long orderId = reservationService.startCharging(userId, id, vehicleId);
        return Result.success("充电已开始", orderId);
    }

    @Data
    static class StartChargingBody {
        private Long vehicleId;
    }
}
