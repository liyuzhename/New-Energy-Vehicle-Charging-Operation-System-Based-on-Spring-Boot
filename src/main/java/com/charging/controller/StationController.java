package com.charging.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.result.Result;
import com.charging.service.ChargingPileService;
import com.charging.service.ChargingStationService;
import com.charging.vo.PileWithGunsVO;
import com.charging.vo.StationDetailVO;
import com.charging.vo.StationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StationController {

    private final ChargingStationService chargingStationService;
    private final ChargingPileService chargingPileService;

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
}
