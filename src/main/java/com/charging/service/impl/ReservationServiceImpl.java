package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.exception.BusinessException;
import com.charging.dto.ReservationCreateRequest;
import com.charging.entity.ChargingPile;
import com.charging.entity.ChargingStation;
import com.charging.entity.Reservation;
import com.charging.entity.Vehicle;
import com.charging.mapper.ChargingPileMapper;
import com.charging.mapper.ChargingStationMapper;
import com.charging.mapper.ReservationMapper;
import com.charging.mapper.VehicleMapper;
import com.charging.service.ReservationService;
import com.charging.vo.ReservationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationMapper reservationMapper;
    private final ChargingPileMapper pileMapper;
    private final ChargingStationMapper stationMapper;
    private final VehicleMapper vehicleMapper;

    @Override
    public void create(Long userId, ReservationCreateRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException(400, "结束时间必须晚于开始时间");
        }
        List<?> conflicts = reservationMapper.selectConflict(
                request.getPileId(), request.getStartTime(), request.getEndTime());
        if (!conflicts.isEmpty()) {
            throw new BusinessException(400, "该时段已有其他预约，请选择其他时段");
        }
        long userConflict = reservationMapper.selectCount(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getUserId, userId)
                        .eq(Reservation::getPileId, request.getPileId())
                        .in(Reservation::getStatus, "PENDING", "CONFIRMED")
                        .lt(Reservation::getStartTime, request.getEndTime())
                        .gt(Reservation::getEndTime, request.getStartTime())
        );
        if (userConflict > 0) {
            throw new BusinessException(400, "您在该时段已有预约");
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setPileId(request.getPileId());
        reservation.setReserveDate(request.getStartTime().toLocalDate());
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setStatus("PENDING");
        reservationMapper.insert(reservation);
    }

    @Override
    public Page<ReservationVO> listMy(Long userId, String status, int page, int size) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getUserId, userId)
                .orderByDesc(Reservation::getCreateTime);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Reservation::getStatus, status);
        }
        Page<Reservation> pageResult = reservationMapper.selectPage(new Page<>(page, size), wrapper);
        Page<ReservationVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        voPage.setRecords(pageResult.getRecords().stream().map(r -> {
            ReservationVO vo = new ReservationVO();
            BeanUtils.copyProperties(r, vo);
            // 关联充电桩和充电站
            if (r.getPileId() != null) {
                ChargingPile pile = pileMapper.selectById(r.getPileId());
                if (pile != null) {
                    vo.setPileNo(pile.getPileNo());
                    if (pile.getStationId() != null) {
                        ChargingStation station = stationMapper.selectById(pile.getStationId());
                        if (station != null) vo.setStationName(station.getName());
                    }
                }
            }
            // 关联用户第一辆车的车牌号
            if (r.getUserId() != null) {
                var vehicles = vehicleMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Vehicle>()
                        .eq(Vehicle::getUserId, r.getUserId())
                        .last("LIMIT 1"));
                if (!vehicles.isEmpty()) vo.setPlateNo(vehicles.get(0).getPlateNo());
            }
            return vo;
        }).toList());
        return voPage;
    }

    @Override
    public void cancel(Long userId, Long reservationId) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) throw new BusinessException(404, "预约不存在");
        if (!reservation.getUserId().equals(userId)) throw new BusinessException(403, "无权操作该预约");
        if (!"PENDING".equals(reservation.getStatus()) && !"CONFIRMED".equals(reservation.getStatus())) {
            throw new BusinessException(400, "该预约无法取消");
        }
        if (LocalDateTime.now().isAfter(reservation.getStartTime().minusMinutes(30))) {
            throw new BusinessException(400, "预约开始前30分钟内不可取消");
        }
        reservation.setStatus("CANCELLED");
        reservationMapper.updateById(reservation);
    }

    /**
     * 每5分钟扫描超时未到场预约，标记为EXPIRED
     */
    @Scheduled(fixedRate = 300000)
    public void expireOverdueReservations() {
        reservationMapper.update(null,
                new LambdaUpdateWrapper<Reservation>()
                        .in(Reservation::getStatus, "PENDING", "CONFIRMED")
                        .lt(Reservation::getEndTime, LocalDateTime.now())
                        .set(Reservation::getStatus, "EXPIRED")
        );
        log.debug("超时预约扫描完成");
    }
}
