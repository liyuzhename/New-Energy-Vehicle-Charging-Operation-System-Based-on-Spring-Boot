package com.charging.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.dto.StartChargingRequest;
import com.charging.vo.IncomeVO;
import com.charging.vo.OrderDetailVO;
import com.charging.vo.OrderVO;

import java.time.LocalDate;

public interface ChargingOrderService {

    OrderVO start(Long userId, StartChargingRequest request);

    OrderVO getCharging(Long userId);

    OrderDetailVO stop(Long userId, Long orderId);

    OrderDetailVO getDetail(Long orderId);

    Page<OrderVO> listMy(Long userId, String status, String payStatus, int page, int size);

    Page<OrderVO> listForOperator(Long operatorId, Long stationId, String status, String orderNo, LocalDate startDate, LocalDate endDate, int page, int size);

    Page<OrderVO> listForAdmin(String status, String orderNo, LocalDate startDate, LocalDate endDate, int page, int size);

    void applyRefund(Long userId, Long orderId);

    void approveRefund(Long orderId);

    void rejectRefund(Long orderId);

    void pay(Long userId, Long orderId);

    IncomeVO getIncome(Long operatorId, Long stationId, LocalDate startDate, LocalDate endDate);
}
