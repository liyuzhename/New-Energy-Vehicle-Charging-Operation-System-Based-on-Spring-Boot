package com.charging.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.result.Result;
import com.charging.dto.GunCreateRequest;
import com.charging.dto.PileCreateRequest;
import com.charging.dto.PileStatusRequest;
import com.charging.dto.PileUpdateRequest;
import com.charging.security.util.SecurityUtils;
import com.charging.service.ChargingPileService;
import com.charging.vo.GunVO;
import com.charging.vo.PileVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OperatorPileController {

    private final ChargingPileService chargingPileService;

    @GetMapping("/api/operator/pile/list")
    public Result<Page<PileVO>> list(
            @RequestParam(value = "stationId", required = false) Long stationId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        return Result.success(chargingPileService.listForOperator(operatorId, stationId, keyword, status, page, size));
    }

    @PostMapping("/api/operator/pile")
    public Result<Void> create(@Valid @RequestBody PileCreateRequest request) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        chargingPileService.create(operatorId, request);
        return Result.success("充电桩创建成功", null);
    }

    @PutMapping("/api/operator/pile/{id}")
    public Result<Void> update(@PathVariable("id") Long id,
                               @RequestBody PileUpdateRequest request) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        chargingPileService.update(operatorId, id, request);
        return Result.success("充电桩更新成功", null);
    }

    @DeleteMapping("/api/operator/pile/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        chargingPileService.delete(operatorId, id);
        return Result.success("充电桩删除成功", null);
    }

    @PutMapping("/api/operator/pile/{id}/status")
    public Result<Void> updateStatus(@PathVariable("id") Long id,
                                     @Valid @RequestBody PileStatusRequest request) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        chargingPileService.updateStatus(operatorId, id, request);
        return Result.success("状态更新成功", null);
    }

    @GetMapping("/api/operator/gun/list/{pileId}")
    public Result<List<GunVO>> listGuns(@PathVariable("pileId") Long pileId) {
        return Result.success(chargingPileService.listGuns(pileId));
    }

    @PostMapping("/api/operator/gun")
    public Result<Void> addGun(@Valid @RequestBody GunCreateRequest request) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        chargingPileService.addGun(operatorId, request);
        return Result.success("充电枪添加成功", null);
    }

    @DeleteMapping("/api/operator/gun/{id}")
    public Result<Void> deleteGun(@PathVariable("id") Long id) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        chargingPileService.deleteGun(operatorId, id);
        return Result.success("充电枪删除成功", null);
    }
}
