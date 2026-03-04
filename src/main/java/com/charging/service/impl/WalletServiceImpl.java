package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.exception.BusinessException;
import com.charging.entity.PaymentRecord;
import com.charging.entity.Wallet;
import com.charging.mapper.PaymentRecordMapper;
import com.charging.mapper.WalletMapper;
import com.charging.service.WalletService;
import com.charging.vo.PaymentRecordVO;
import com.charging.vo.WalletVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletMapper walletMapper;
    private final PaymentRecordMapper paymentRecordMapper;

    @Override
    public WalletVO getWallet(Long userId) {
        Wallet wallet = getWalletByUserId(userId);
        WalletVO vo = new WalletVO();
        BeanUtils.copyProperties(wallet, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recharge(Long userId, BigDecimal amount) {
        Wallet wallet = getWalletByUserId(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setTotalRecharge(wallet.getTotalRecharge().add(amount));
        walletMapper.updateById(wallet);

        PaymentRecord record = new PaymentRecord();
        record.setUserId(userId);
        record.setAmount(amount);
        record.setType("RECHARGE");
        record.setRemark("钱包充值 +" + amount + " 元");
        paymentRecordMapper.insert(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deduct(Long userId, BigDecimal amount, Long orderId, String remark) {
        Wallet wallet = getWalletByUserId(userId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(400, "钱包余额不足");
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setTotalConsume(wallet.getTotalConsume().add(amount));
        walletMapper.updateById(wallet);

        PaymentRecord record = new PaymentRecord();
        record.setUserId(userId);
        record.setOrderId(orderId);
        record.setAmount(amount.negate());
        record.setType("CONSUME");
        record.setRemark(remark != null ? remark : "订单消费 -" + amount + " 元");
        paymentRecordMapper.insert(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long userId, BigDecimal amount, Long orderId, String remark) {
        Wallet wallet = getWalletByUserId(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletMapper.updateById(wallet);

        PaymentRecord record = new PaymentRecord();
        record.setUserId(userId);
        record.setOrderId(orderId);
        record.setAmount(amount);
        record.setType("REFUND");
        record.setRemark(remark != null ? remark : "退款到账 +" + amount + " 元");
        paymentRecordMapper.insert(record);
    }

    @Override
    public Page<PaymentRecordVO> getRecords(Long userId, int page, int size) {
        Page<PaymentRecord> pageParam = new Page<>(page, size);
        Page<PaymentRecord> recordPage = paymentRecordMapper.selectPage(pageParam,
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getUserId, userId)
                        .orderByDesc(PaymentRecord::getCreateTime)
        );

        Page<PaymentRecordVO> voPage = new Page<>(recordPage.getCurrent(), recordPage.getSize(), recordPage.getTotal());
        voPage.setRecords(recordPage.getRecords().stream().map(r -> {
            PaymentRecordVO vo = new PaymentRecordVO();
            BeanUtils.copyProperties(r, vo);
            return vo;
        }).toList());
        return voPage;
    }

    private Wallet getWalletByUserId(Long userId) {
        Wallet wallet = walletMapper.selectOne(
                new LambdaQueryWrapper<Wallet>().eq(Wallet::getUserId, userId)
        );
        if (wallet == null) {
            throw new BusinessException(404, "钱包信息不存在");
        }
        return wallet;
    }
}
