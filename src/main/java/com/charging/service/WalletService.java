package com.charging.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.vo.PaymentRecordVO;
import com.charging.vo.WalletVO;

import java.math.BigDecimal;

public interface WalletService {

    WalletVO getWallet(Long userId);

    void recharge(Long userId, BigDecimal amount);

    void deduct(Long userId, BigDecimal amount, Long orderId, String remark);

    void refund(Long userId, BigDecimal amount, Long orderId, String remark);

    Page<PaymentRecordVO> getRecords(Long userId, String type, int page, int size);
}
