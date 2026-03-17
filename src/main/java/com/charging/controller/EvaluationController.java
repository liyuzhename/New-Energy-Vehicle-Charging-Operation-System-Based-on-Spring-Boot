package com.charging.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.result.Result;
import com.charging.dto.EvaluationCreateRequest;
import com.charging.dto.EvaluationReplyRequest;
import com.charging.security.util.SecurityUtils;
import com.charging.service.EvaluationService;
import com.charging.vo.EvaluationVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping("/api/evaluation")
    public Result<Void> create(@Valid @RequestBody EvaluationCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        evaluationService.create(userId, request);
        return Result.success("评价发表成功", null);
    }

    @GetMapping("/api/evaluation/station/{stationId}")
    public Result<Page<EvaluationVO>> listByStation(
            @PathVariable("stationId") Long stationId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return Result.success(evaluationService.listByStation(stationId, page, size));
    }

    @PutMapping("/api/operator/evaluation/{id}/reply")
    public Result<Void> reply(@PathVariable("id") Long id,
                              @Valid @RequestBody EvaluationReplyRequest request) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        evaluationService.reply(operatorId, id, request);
        return Result.success("回复成功", null);
    }

    @GetMapping("/api/admin/evaluation/list")
    public Result<Page<EvaluationVO>> listAll(
            @RequestParam(value = "rating", required = false) Integer rating,
            @RequestParam(value = "isHidden", required = false) Integer isHidden,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return Result.success(evaluationService.listAll(rating, isHidden, page, size));
    }

    @PutMapping("/api/admin/evaluation/{id}/hide")
    public Result<Void> hide(@PathVariable("id") Long id) {
        evaluationService.hide(id);
        return Result.success("评价已屏蔽", null);
    }
}
