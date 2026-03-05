package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.charging.entity.ChargingOrder;
import com.charging.entity.ChargingPile;
import com.charging.entity.User;
import com.charging.mapper.ChargingOrderMapper;
import com.charging.mapper.ChargingPileMapper;
import com.charging.mapper.FaultRecordMapper;
import com.charging.mapper.UserMapper;
import com.charging.service.ReportService;
import com.charging.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ChargingOrderMapper orderMapper;
    private final ChargingPileMapper pileMapper;
    private final FaultRecordMapper faultRecordMapper;
    private final UserMapper userMapper;

    @Override
    public List<Map<String, Object>> orderTrend(Long operatorId, LocalDate startDate, LocalDate endDate) {
        return orderMapper.selectOrderTrend(operatorId, startDate, endDate.plusDays(1));
    }

    @Override
    public List<Map<String, Object>> pileUsage(Long operatorId, LocalDate startDate, LocalDate endDate) {
        return orderMapper.selectPileUsage(operatorId, startDate, endDate.plusDays(1));
    }

    @Override
    public List<Map<String, Object>> userGrowth(LocalDate startDate, LocalDate endDate) {
        // 通过原生SQL查询按月用户增长（直接用UserMapper扩展）
        return userMapper.selectUserGrowth(startDate, endDate.plusDays(1));
    }

    @Override
    public List<Map<String, Object>> faultAnalysis(Long operatorId) {
        if (operatorId == null) {
            return faultRecordMapper.selectFaultAnalysis();
        }
        return faultRecordMapper.selectFaultAnalysisByOperator(operatorId);
    }

    @Override
    public DashboardVO dashboard() {
        DashboardVO vo = new DashboardVO();
        vo.setTotalUsers(userMapper.selectCount(new LambdaQueryWrapper<User>().ne(User::getRole, "ADMIN")));
        LocalDate today = LocalDate.now();
        vo.setTodayOrders(orderMapper.selectCount(
                new LambdaQueryWrapper<ChargingOrder>()
                        .ge(ChargingOrder::getCreateTime, today.atStartOfDay())
                        .lt(ChargingOrder::getCreateTime, today.plusDays(1).atStartOfDay())));
        List<Map<String, Object>> todayIncome = orderMapper.selectDailyIncome(null, today, today.plusDays(1));
        BigDecimal income = BigDecimal.ZERO;
        if (!todayIncome.isEmpty()) {
            Object t = todayIncome.get(0).get("totalFee");
            if (t != null) income = new BigDecimal(t.toString()).setScale(2, RoundingMode.HALF_UP);
        }
        vo.setTodayIncome(income);
        vo.setOnlinePiles(pileMapper.selectCount(
                new LambdaQueryWrapper<ChargingPile>().in(ChargingPile::getStatus, "IDLE", "OCCUPIED")));
        return vo;
    }

    @Override
    public byte[] exportOrderExcel(Long operatorId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<ChargingOrder> wrapper = new LambdaQueryWrapper<ChargingOrder>()
                .ge(ChargingOrder::getCreateTime, startDate.atStartOfDay())
                .lt(ChargingOrder::getCreateTime, endDate.plusDays(1).atStartOfDay())
                .orderByDesc(ChargingOrder::getCreateTime);
        if (operatorId != null) wrapper.eq(ChargingOrder::getOperatorId, operatorId);
        List<ChargingOrder> orders = orderMapper.selectList(wrapper);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("充电订单报表");
            String[] headers = {"订单号", "开始时间", "结束时间", "电量(kWh)", "电费(元)", "服务费(元)", "总费用(元)", "状态"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) headerRow.createCell(i).setCellValue(headers[i]);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < orders.size(); i++) {
                ChargingOrder o = orders.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(o.getOrderNo());
                row.createCell(1).setCellValue(o.getStartTime() != null ? o.getStartTime().format(fmt) : "");
                row.createCell(2).setCellValue(o.getEndTime() != null ? o.getEndTime().format(fmt) : "");
                row.createCell(3).setCellValue(o.getChargeKwh() != null ? o.getChargeKwh().doubleValue() : 0);
                row.createCell(4).setCellValue(o.getChargeFee() != null ? o.getChargeFee().doubleValue() : 0);
                row.createCell(5).setCellValue(o.getServiceFee() != null ? o.getServiceFee().doubleValue() : 0);
                row.createCell(6).setCellValue(o.getTotalFee() != null ? o.getTotalFee().doubleValue() : 0);
                row.createCell(7).setCellValue(o.getStatus());
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Excel导出失败", e);
        }
    }
}
