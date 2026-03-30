package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.exception.BusinessException;
import com.charging.dto.ReservationCreateRequest;
import com.charging.entity.ChargingGun;
import com.charging.entity.ChargingPile;
import com.charging.entity.ChargingStation;
import com.charging.entity.Reservation;
import com.charging.entity.Vehicle;
import com.charging.mapper.ChargingGunMapper;
import com.charging.mapper.ChargingPileMapper;
import com.charging.mapper.ChargingStationMapper;
import com.charging.mapper.ReservationMapper;
import com.charging.mapper.VehicleMapper;
import com.charging.service.ChargingOrderService;
import com.charging.service.ReservationService;
import com.charging.vo.OrderVO;
import com.charging.vo.ReservationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationMapper reservationMapper;
    private final ChargingPileMapper pileMapper;
    private final ChargingGunMapper gunMapper;
    private final ChargingStationMapper stationMapper;
    private final VehicleMapper vehicleMapper;
    private final ChargingOrderService chargingOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Long userId, ReservationCreateRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException(400, "结束时间必须晚于开始时间");
        }
        // 不校验开始时间是否早于当前时间，允许用户预约当前时刻及未来时段

        // 校验充电枪存在且属于该充电桩
        ChargingGun gun = gunMapper.selectById(request.getGunId());
        if (gun == null || gun.getDeleted() == 1) {
            throw new BusinessException(404, "充电枪不存在");
        }
        if (!gun.getPileId().equals(request.getPileId())) {
            throw new BusinessException(400, "充电枪不属于该充电桩");
        }
        if ("FAULT".equals(gun.getStatus())) {
            throw new BusinessException(400, "该充电枪故障中，无法预约");
        }

        // 枪级别时段冲突检测
        List<Reservation> conflicts = reservationMapper.selectConflict(
                request.getGunId(), request.getStartTime(), request.getEndTime());
        if (!conflicts.isEmpty()) {
            throw new BusinessException(400, "该充电枪在所选时段已有预约，请选择其他时段或其他充电枪");
        }

        // 用户自身冲突检测：同一用户不能在同一时段预约多把枪
        long userConflict = reservationMapper.selectCount(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getUserId, userId)
                        .in(Reservation::getStatus, "PENDING", "CONFIRMED")
                        .lt(Reservation::getStartTime, request.getEndTime())
                        .gt(Reservation::getEndTime, request.getStartTime())
        );
        if (userConflict > 0) {
            throw new BusinessException(400, "您在该时段已有其他预约");
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setPileId(request.getPileId());
        reservation.setGunId(request.getGunId());
        reservation.setReserveDate(request.getStartTime().toLocalDate());
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setStatus("PENDING");
        reservationMapper.insert(reservation);
        // 创建时不改变枪状态，枪状态由定时任务在进入时间段时切换
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
            if (r.getGunId() != null) {
                ChargingGun g = gunMapper.selectById(r.getGunId());
                if (g != null) vo.setGunNo(g.getGunNo());
            }
            if (r.getUserId() != null) {
                var vehicles = vehicleMapper.selectList(
                        new LambdaQueryWrapper<Vehicle>()
                                .eq(Vehicle::getUserId, r.getUserId())
                                .last("LIMIT 1"));
                if (!vehicles.isEmpty()) vo.setPlateNo(vehicles.get(0).getPlateNo());
            }
            return vo;
        }).toList());
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long userId, Long reservationId) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) throw new BusinessException(404, "预约不存在");
        if (!reservation.getUserId().equals(userId)) throw new BusinessException(403, "无权操作该预约");
        if (!"PENDING".equals(reservation.getStatus()) && !"CONFIRMED".equals(reservation.getStatus())) {
            throw new BusinessException(400, "该预约无法取消");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(reservation.getStartTime())) {
            // 未进入时间段：开始前30分钟内不可取消
            if (now.isAfter(reservation.getStartTime().minusMinutes(30))) {
                throw new BusinessException(400, "预约开始前30分钟内不可取消");
            }
            // 枪状态不变（还没到时间段，枪仍是IDLE）
        } else {
            // 已进入时间段，枪已被设为RESERVED，需恢复IDLE
            if (reservation.getGunId() != null) {
                ChargingGun gun = gunMapper.selectById(reservation.getGunId());
                if (gun != null && "RESERVED".equals(gun.getStatus())) {
                    gun.setStatus("IDLE");
                    gunMapper.updateById(gun);
                }
            }
        }
        reservation.setStatus("CANCELLED");
        reservationMapper.updateById(reservation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long userId, Long reservationId) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) throw new BusinessException(404, "预约不存在");
        if (!reservation.getUserId().equals(userId)) throw new BusinessException(403, "无权操作该预约");
        if (!"PENDING".equals(reservation.getStatus())) {
            throw new BusinessException(400, "当前预约状态无法确认到场");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(reservation.getStartTime())) {
            throw new BusinessException(400, "尚未到达预约时间段，无法确认到场");
        }
        if (!now.isBefore(reservation.getEndTime())) {
            throw new BusinessException(400, "预约时间段已过");
        }
        reservation.setStatus("CONFIRMED");
        reservationMapper.updateById(reservation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startCharging(Long userId, Long reservationId, Long vehicleId) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) throw new BusinessException(404, "预约不存在");
        if (!reservation.getUserId().equals(userId)) throw new BusinessException(403, "无权操作该预约");
        if (!"CONFIRMED".equals(reservation.getStatus())) {
            throw new BusinessException(400, "请先确认到场后再开始充电");
        }
        OrderVO orderVO = chargingOrderService.startByReservation(userId, reservationId, vehicleId);
        // 记录关联订单ID
        reservation.setOrderId(orderVO.getId());
        reservationMapper.updateById(reservation);
        return orderVO.getId();
    }

    /**
     * 每1分钟：将已进入时间段的PENDING预约对应的枪状态切换为RESERVED
     */
    @Scheduled(fixedRate = 60000)
    @Transactional(rollbackFor = Exception.class)
    public void activateReservedGuns() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> pendingInProgress = reservationMapper.selectPendingInProgress(now);
        for (Reservation r : pendingInProgress) {
            if (r.getGunId() == null) continue;
            ChargingGun gun = gunMapper.selectById(r.getGunId());
            if (gun != null && "IDLE".equals(gun.getStatus())) {
                gun.setStatus("RESERVED");
                gunMapper.updateById(gun);
                log.debug("预约[{}] 进入时间段，枪[{}] 状态切换为RESERVED", r.getId(), gun.getGunNo());
            }
        }
    }

    /**
     * 每5分钟：扫描超时预约，标记为EXPIRED，并恢复枪状态
     */
    @Scheduled(fixedRate = 300000)
    @Transactional(rollbackFor = Exception.class)
    public void expireOverdueReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> overdueList = reservationMapper.selectOverdue(now);
        for (Reservation r : overdueList) {
            if (r.getGunId() != null) {
                ChargingGun gun = gunMapper.selectById(r.getGunId());
                if (gun != null && "RESERVED".equals(gun.getStatus())) {
                    gun.setStatus("IDLE");
                    gunMapper.updateById(gun);
                    log.debug("预约[{}] 过期，枪[{}] 状态恢复为IDLE", r.getId(), gun.getGunNo());
                }
            }
            r.setStatus("EXPIRED");
            reservationMapper.updateById(r);
        }
        log.debug("超时预约扫描完成，共处理{}条", overdueList.size());
    }
}
