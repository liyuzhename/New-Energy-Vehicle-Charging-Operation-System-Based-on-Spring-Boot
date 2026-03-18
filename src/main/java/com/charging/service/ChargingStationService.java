package com.charging.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.dto.StationCreateRequest;
import com.charging.dto.StationUpdateRequest;
import com.charging.vo.StationDetailVO;
import com.charging.vo.StationVO;

public interface ChargingStationService {

    Page<StationVO> listForUser(String keyword, String city, int page, int size);

    StationDetailVO getDetail(Long stationId);

    void create(Long operatorId, StationCreateRequest request);

    void update(Long operatorId, Long stationId, StationUpdateRequest request);

    void delete(Long operatorId, Long stationId);

    Page<StationVO> listForOperatorOrAdmin(Long operatorId, String role, String keyword, int page, int size);
}
