package com.charging.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.dto.GunCreateRequest;
import com.charging.dto.PileCreateRequest;
import com.charging.dto.PileStatusRequest;
import com.charging.dto.PileUpdateRequest;
import com.charging.vo.GunVO;
import com.charging.vo.PileVO;
import com.charging.vo.PileWithGunsVO;

import java.util.List;

public interface ChargingPileService {

    List<PileWithGunsVO> listByStation(Long stationId);

    Page<PileVO> listForOperator(Long operatorId, Long stationId, String keyword, String status, int page, int size);

    void create(Long operatorId, PileCreateRequest request);

    void update(Long operatorId, Long pileId, PileUpdateRequest request);

    void delete(Long operatorId, Long pileId);

    void updateStatus(Long operatorId, Long pileId, PileStatusRequest request);

    void addGun(Long operatorId, GunCreateRequest request);

    void deleteGun(Long operatorId, Long gunId);

    List<GunVO> listGuns(Long pileId);
}
