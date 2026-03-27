package com.charging.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.result.Result;
import com.charging.dto.FaultCreateRequest;
import com.charging.dto.FaultHandleRequest;
import com.charging.security.util.SecurityUtils;
import com.charging.service.FaultRecordService;
import com.charging.vo.FaultRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class FaultController {

    private final FaultRecordService faultRecordService;

    @PostMapping("/api/fault")
    public Result<Void> create(@Valid @RequestBody FaultCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        faultRecordService.create(userId, request);
        return Result.success("报修提交成功", null);
    }

    @GetMapping("/api/fault/my")
    public Result<List<FaultRecordVO>> listMy() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(faultRecordService.listMy(userId));
    }

    @GetMapping("/api/operator/fault/list")
    public Result<Page<FaultRecordVO>> listForOperator(
            @RequestParam(value = "stationId", required = false) Long stationId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        return Result.success(faultRecordService.listForOperator(operatorId, stationId, status, startDate, endDate, page, size));
    }

    @PutMapping("/api/operator/fault/{id}/handle")
    public Result<Void> handle(@PathVariable("id") Long id,
                               @Valid @RequestBody FaultHandleRequest request) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        faultRecordService.handle(operatorId, id, request);
        return Result.success("故障处理状态已更新", null);
    }

    @GetMapping("/api/admin/fault/list")
    public Result<Page<FaultRecordVO>> listForAdmin(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "stationId", required = false) Long stationId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return Result.success(faultRecordService.listForAdmin(status, stationId, startDate, endDate, page, size));
    }
}
