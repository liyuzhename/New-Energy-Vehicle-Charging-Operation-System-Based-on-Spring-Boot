package com.charging.controller;

import com.charging.common.result.Result;
import com.charging.service.BillingRuleService;
import com.charging.vo.BillingRuleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingRuleController {

    private final BillingRuleService billingRuleService;

    @GetMapping("/list/{stationId}")
    public Result<List<BillingRuleVO>> list(@PathVariable Long stationId) {
        return Result.success(billingRuleService.listByStation(stationId));
    }
}
