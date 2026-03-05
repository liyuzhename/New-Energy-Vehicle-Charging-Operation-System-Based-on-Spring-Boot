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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long operatorId = null;
        String role = SecurityUtils.getCurrentUserRole();
        if ("OPERATOR".equals(role)) operatorId = SecurityUtils.getCurrentUserId();
        return Result.success(reportService.orderTrend(operatorId, startDate, endDate));
    }

    @GetMapping("/income")
    public Result<List<Map<String, Object>>> income(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long operatorId = null;
        String role = SecurityUtils.getCurrentUserRole();
        if ("OPERATOR".equals(role)) operatorId = SecurityUtils.getCurrentUserId();
        return Result.success(reportService.orderTrend(operatorId, startDate, endDate));
    }

    @GetMapping("/pile-usage")
    public Result<List<Map<String, Object>>> pileUsage(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long operatorId = null;
        String role = SecurityUtils.getCurrentUserRole();
        if ("OPERATOR".equals(role)) operatorId = SecurityUtils.getCurrentUserId();
        return Result.success(reportService.pileUsage(operatorId, startDate, endDate));
    }

    @GetMapping("/export")
    public void export(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {
        Long operatorId = null;
        String role = SecurityUtils.getCurrentUserRole();
        if ("OPERATOR".equals(role)) operatorId = SecurityUtils.getCurrentUserId();
        byte[] data = reportService.exportOrderExcel(operatorId, startDate, endDate);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=orders.xlsx");
        response.getOutputStream().write(data);
    }
}
