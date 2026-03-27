package com.charging.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.dto.ReservationCreateRequest;
import com.charging.vo.ReservationVO;

public interface ReservationService {

    void create(Long userId, ReservationCreateRequest request);

    Page<ReservationVO> listMy(Long userId, String status, int page, int size);

    void cancel(Long userId, Long reservationId);

    void confirm(Long userId, Long reservationId);

    Long startCharging(Long userId, Long reservationId, Long vehicleId);
}
