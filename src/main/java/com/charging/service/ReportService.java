package com.charging.service;

import com.charging.vo.DashboardVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReportService {

    List<Map<String, Object>> orderTrend(Long operatorId, Long stationId, LocalDate startDate, LocalDate endDate);

    List<Map<String, Object>> pileUsage(Long operatorId, Long stationId, LocalDate startDate, LocalDate endDate);

    List<Map<String, Object>> userGrowth(LocalDate startDate, LocalDate endDate);

    List<Map<String, Object>> faultAnalysis(Long operatorId);

    DashboardVO dashboard();

    byte[] exportOrderExcel(Long operatorId, LocalDate startDate, LocalDate endDate);

    byte[] exportAdminReportExcel(LocalDate startDate, LocalDate endDate);
}
