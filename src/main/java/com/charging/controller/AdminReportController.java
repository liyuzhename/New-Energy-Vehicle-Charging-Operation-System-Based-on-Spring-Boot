package com.charging.controller;

import com.charging.common.result.Result;
import com.charging.service.ReportService;
import com.charging.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping("/report/user-growth")
    public Result<List<Map<String, Object>>> userGrowth(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(reportService.userGrowth(startDate, endDate));
    }

    @GetMapping("/report/fault-analysis")
    public Result<List<Map<String, Object>>> faultAnalysis() {
        return Result.success(reportService.faultAnalysis(null));
    }

    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.success(reportService.dashboard());
    }
}
