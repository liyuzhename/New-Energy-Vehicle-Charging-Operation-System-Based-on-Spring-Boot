package com.charging.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.result.Result;
import com.charging.dto.StationCreateRequest;
import com.charging.dto.StationUpdateRequest;
import com.charging.security.util.SecurityUtils;
import com.charging.service.ChargingStationService;
import com.charging.vo.StationVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operator/station")
@RequiredArgsConstructor
public class OperatorStationController {

    private final ChargingStationService chargingStationService;

    @PostMapping
    public Result<Void> create(@Valid @RequestBody StationCreateRequest request) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        chargingStationService.create(operatorId, request);
        return Result.success("充电站创建成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id,
                               @RequestBody StationUpdateRequest request) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        chargingStationService.update(operatorId, id, request);
        return Result.success("充电站更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        chargingStationService.delete(operatorId, id);
        return Result.success("充电站删除成功", null);
    }

    @GetMapping("/list")
    public Result<Page<StationVO>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        String role = SecurityUtils.getCurrentUserRole();
        return Result.success(chargingStationService.listForOperatorOrAdmin(operatorId, role, keyword, status, page, size));
    }
}
