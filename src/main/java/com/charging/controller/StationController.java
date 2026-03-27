package com.charging.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.result.Result;
import com.charging.entity.ChargingGun;
import com.charging.entity.ChargingPile;
import com.charging.mapper.ChargingGunMapper;
import com.charging.mapper.ChargingPileMapper;
import com.charging.mapper.ReservationMapper;
import com.charging.service.ChargingPileService;
import com.charging.service.ChargingStationService;
import com.charging.vo.GunVO;
import com.charging.vo.PileWithGunsVO;
import com.charging.vo.StationDetailVO;
import com.charging.vo.StationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class StationController {

    private final ChargingStationService chargingStationService;
    private final ChargingPileService chargingPileService;
    private final ChargingPileMapper chargingPileMapper;
    private final ChargingGunMapper chargingGunMapper;
    private final ReservationMapper reservationMapper;

    @GetMapping("/api/station/list")
    public Result<Page<StationVO>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return Result.success(chargingStationService.listForUser(keyword, city, page, size));
    }

    @GetMapping("/api/station/{id}")
    public Result<StationDetailVO> detail(@PathVariable("id") Long id) {
        return Result.success(chargingStationService.getDetail(id));
    }

    @GetMapping("/api/station/{stationId}/piles")
    public Result<List<PileWithGunsVO>> piles(@PathVariable("stationId") Long stationId) {
        return Result.success(chargingPileService.listByStation(stationId));
    }

    /**
     * 查询指定站点在某时段内可预约的充电枪列表
     * 过滤掉：① 该时段已有PENDING/CONFIRMED预约的枪；② FAULT状态的枪
     */
    @GetMapping("/api/station/{stationId}/guns-available")
    public Result<List<GunVO>> gunsAvailable(
            @PathVariable("stationId") Long stationId,
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        // 查出该站所有桩
        List<ChargingPile> piles = chargingPileMapper.selectList(
                new LambdaQueryWrapper<ChargingPile>().eq(ChargingPile::getStationId, stationId));
        if (piles.isEmpty()) return Result.success(List.of());

        List<Long> pileIds = piles.stream().map(ChargingPile::getId).toList();

        // 查出该站所有枪（非FAULT）
        List<ChargingGun> allGuns = chargingGunMapper.selectList(
                new LambdaQueryWrapper<ChargingGun>()
                        .in(ChargingGun::getPileId, pileIds)
                        .ne(ChargingGun::getStatus, "FAULT"));

        // 查出该时段已被预约的枪ID集合
        Set<Long> reservedGunIds = new java.util.HashSet<>(
                reservationMapper.selectReservedGunIds(stationId, startTime, endTime));

        // 过滤掉已预约的枪
        List<GunVO> result = allGuns.stream()
                .filter(g -> !reservedGunIds.contains(g.getId()))
                .map(g -> {
                    GunVO vo = new GunVO();
                    BeanUtils.copyProperties(g, vo);
                    return vo;
                })
                .toList();

        return Result.success(result);
    }
}
