package com.charging.controller;

import com.charging.common.result.Result;
import com.charging.security.util.SecurityUtils;
import com.charging.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/order-trend")
    public Result<List<Map<String, Object>>> orderTrend(
            @RequestParam(value = "stationId", required = false) Long stationId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (endDate == null) endDate = LocalDate.now();
        if (startDate == null) startDate = endDate.minusDays(29);
        Long operatorId = null;
        String role = SecurityUtils.getCurrentUserRole();
        if ("OPERATOR".equals(role)) operatorId = SecurityUtils.getCurrentUserId();
        return Result.success(reportService.orderTrend(operatorId, stationId, startDate, endDate));
    }

    @GetMapping("/income")
    public Result<List<Map<String, Object>>> income(
            @RequestParam(value = "stationId", required = false) Long stationId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (endDate == null) endDate = LocalDate.now();
        if (startDate == null) startDate = endDate.minusDays(29);
        Long operatorId = null;
        String role = SecurityUtils.getCurrentUserRole();
        if ("OPERATOR".equals(role)) operatorId = SecurityUtils.getCurrentUserId();
        return Result.success(reportService.orderTrend(operatorId, stationId, startDate, endDate));
    }

    @GetMapping("/pile-usage")
    public Result<List<Map<String, Object>>> pileUsage(
            @RequestParam(value = "stationId", required = false) Long stationId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (endDate == null) endDate = LocalDate.now();
        if (startDate == null) startDate = endDate.minusDays(29);
        Long operatorId = null;
        String role = SecurityUtils.getCurrentUserRole();
        if ("OPERATOR".equals(role)) operatorId = SecurityUtils.getCurrentUserId();
        return Result.success(reportService.pileUsage(operatorId, stationId, startDate, endDate));
    }

    @GetMapping("/export")
    public void export(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {
        if (endDate == null) endDate = LocalDate.now();
        if (startDate == null) startDate = endDate.minusDays(29);
        Long operatorId = null;
        String role = SecurityUtils.getCurrentUserRole();
        if ("OPERATOR".equals(role)) operatorId = SecurityUtils.getCurrentUserId();
        byte[] data = reportService.exportOrderExcel(operatorId, startDate, endDate);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=orders.xlsx");
        response.getOutputStream().write(data);
    }
}
