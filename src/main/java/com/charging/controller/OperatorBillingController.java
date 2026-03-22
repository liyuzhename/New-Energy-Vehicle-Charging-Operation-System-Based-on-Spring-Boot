package com.charging.controller;

import com.charging.common.result.Result;
import com.charging.dto.BillingRuleCreateRequest;
import com.charging.dto.BillingRuleUpdateRequest;
import com.charging.security.util.SecurityUtils;
import com.charging.service.BillingRuleService;
import com.charging.vo.BillingRuleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operator/billing")
@RequiredArgsConstructor
public class OperatorBillingController {

    private final BillingRuleService billingRuleService;

    @GetMapping
    public Result<List<BillingRuleVO>> list(@RequestParam("stationId") Long stationId) {
        return Result.success(billingRuleService.listByStation(stationId));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody BillingRuleCreateRequest request) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        billingRuleService.create(operatorId, request);
        return Result.success("计费规则创建成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id,
                               @RequestBody BillingRuleUpdateRequest request) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        billingRuleService.update(operatorId, id, request);
        return Result.success("计费规则更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        billingRuleService.delete(operatorId, id);
        return Result.success("计费规则删除成功", null);
    }
}
