package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.exception.BusinessException;
import com.charging.dto.StationCreateRequest;
import com.charging.dto.StationUpdateRequest;
import com.charging.entity.ChargingPile;
import com.charging.entity.ChargingStation;
import com.charging.mapper.ChargingPileMapper;
import com.charging.mapper.ChargingStationMapper;
import com.charging.service.ChargingStationService;
import com.charging.vo.StationDetailVO;
import com.charging.vo.StationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChargingStationServiceImpl implements ChargingStationService {

    private final ChargingStationMapper chargingStationMapper;
    private final ChargingPileMapper chargingPileMapper;

    @Override
    public Page<StationVO> listForUser(String keyword, String city, int page, int size) {
        Page<StationVO> pageParam = new Page<>(page, size);
        return chargingStationMapper.listForUser(pageParam, keyword, city);
    }

    @Override
    public StationDetailVO getDetail(Long stationId) {
        ChargingStation station = chargingStationMapper.selectById(stationId);
        if (station == null || Integer.valueOf(1).equals(station.getDeleted())) {
            throw new BusinessException(404, "充电站不存在");
        }

        StationDetailVO vo = new StationDetailVO();
        BeanUtils.copyProperties(station, vo);

        long totalPileCount = chargingPileMapper.selectCount(
                new LambdaQueryWrapper<ChargingPile>().eq(ChargingPile::getStationId, stationId)
        );
        long availablePileCount = chargingPileMapper.selectCount(
                new LambdaQueryWrapper<ChargingPile>()
                        .eq(ChargingPile::getStationId, stationId)
                        .eq(ChargingPile::getStatus, "IDLE")
        );
        vo.setTotalPileCount((int) totalPileCount);
        vo.setAvailablePileCount((int) availablePileCount);
        return vo;
    }

    @Override
    public void create(Long operatorId, StationCreateRequest request) {
        ChargingStation station = new ChargingStation();
        BeanUtils.copyProperties(request, station);
        station.setOperatorId(operatorId);
        // 若前端传入了合法状态则使用，否则默认 ONLINE
        String initStatus = request.getStatus();
        if ("ONLINE".equals(initStatus) || "OFFLINE".equals(initStatus)) {
            station.setStatus(initStatus);
        } else {
            station.setStatus("ONLINE");
        }
        chargingStationMapper.insert(station);
    }

    @Override
    public void update(Long operatorId, Long stationId, StationUpdateRequest request) {
        ChargingStation station = getOwnedStation(operatorId, stationId);
        if (request.getName() != null) station.setName(request.getName());
        if (request.getAddress() != null) station.setAddress(request.getAddress());
        if (request.getCity() != null) station.setCity(request.getCity());
        if (request.getLongitude() != null) station.setLongitude(request.getLongitude());
        if (request.getLatitude() != null) station.setLatitude(request.getLatitude());
        if (request.getBusinessHours() != null) station.setBusinessHours(request.getBusinessHours());
        if (request.getParkingFee() != null) station.setParkingFee(request.getParkingFee());
        if (request.getContactPhone() != null) station.setContactPhone(request.getContactPhone());
        if (request.getDescription() != null) station.setDescription(request.getDescription());
        if (request.getStatus() != null) station.setStatus(request.getStatus());
        chargingStationMapper.updateById(station);
    }

    @Override
    public void delete(Long operatorId, Long stationId) {
        getOwnedStation(operatorId, stationId);
        chargingStationMapper.deleteById(stationId);
    }

    @Override
    public Page<StationVO> listForOperatorOrAdmin(Long operatorId, String role, String keyword, String status, int page, int size) {
        Page<ChargingStation> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ChargingStation> wrapper = new LambdaQueryWrapper<ChargingStation>()
                .orderByDesc(ChargingStation::getCreateTime);
        if (!"ADMIN".equals(role)) {
            wrapper.eq(ChargingStation::getOperatorId, operatorId);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(ChargingStation::getName, keyword)
                    .or().like(ChargingStation::getAddress, keyword)
                    .or().like(ChargingStation::getCity, keyword));
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(ChargingStation::getStatus, status);
        }
        Page<ChargingStation> stationPage = chargingStationMapper.selectPage(pageParam, wrapper);

        Page<StationVO> voPage = new Page<>(stationPage.getCurrent(), stationPage.getSize(), stationPage.getTotal());
        voPage.setRecords(stationPage.getRecords().stream().map(s -> {
            StationVO vo = new StationVO();
            BeanUtils.copyProperties(s, vo);
            long available = chargingPileMapper.selectCount(
                    new LambdaQueryWrapper<ChargingPile>()
                            .eq(ChargingPile::getStationId, s.getId())
                            .eq(ChargingPile::getStatus, "IDLE")
            );
            vo.setAvailablePileCount((int) available);
            return vo;
        }).toList());
        return voPage;
    }

    private ChargingStation getOwnedStation(Long operatorId, Long stationId) {
        ChargingStation station = chargingStationMapper.selectById(stationId);
        if (station == null) {
            throw new BusinessException(404, "充电站不存在");
        }
        if (!station.getOperatorId().equals(operatorId)) {
            throw new BusinessException(403, "无权操作该充电站");
        }
        return station;
    }
}
