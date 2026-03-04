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
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(chargingStationService.listForUser(keyword, city, page, size));
    }

    @GetMapping("/api/station/{id}")
    public Result<StationDetailVO> detail(@PathVariable Long id) {
        return Result.success(chargingStationService.getDetail(id));
    }

    @GetMapping("/api/station/{stationId}/piles")
    public Result<List<PileWithGunsVO>> piles(@PathVariable Long stationId) {
        return Result.success(chargingPileService.listByStation(stationId));
    }
}
