package com.charging.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.result.Result;
import com.charging.dto.RechargeRequest;
import com.charging.security.util.SecurityUtils;
import com.charging.service.WalletService;
import com.charging.vo.PaymentRecordVO;
import com.charging.vo.WalletVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public Result<WalletVO> getWallet() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(walletService.getWallet(userId));
    }

    @PostMapping("/recharge")
    public Result<Void> recharge(@Valid @RequestBody RechargeRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        walletService.recharge(userId, request.getAmount());
        return Result.success("充值成功", null);
    }

    @GetMapping("/records")
    public Result<Page<PaymentRecordVO>> getRecords(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(walletService.getRecords(userId, type, page, size));
    }
}
