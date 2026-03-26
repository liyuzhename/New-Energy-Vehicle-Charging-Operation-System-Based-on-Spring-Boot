package com.charging.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.charging.entity.ChargingOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface ChargingOrderMapper extends BaseMapper<ChargingOrder> {

    /**
     * 按运营商聚合收益（按日），operatorId 为 null 时统计全平台
     */
    @Select("<script>" +
            "SELECT DATE(start_time) AS day, COUNT(*) AS orderCount, SUM(charge_kwh) AS chargeKwh, " +
            "SUM(charge_fee) AS chargeFee, SUM(service_fee) AS serviceFee, SUM(total_fee) AS totalFee " +
            "FROM charging_order WHERE pay_status = 'PAID' AND deleted = 0 " +
            "<if test='operatorId != null'>AND operator_id = #{operatorId} </if>" +
            "AND start_time >= #{startDate} AND start_time &lt; #{endDate} " +
            "GROUP BY DATE(start_time) ORDER BY day" +
            "</script>")
    List<Map<String, Object>> selectDailyIncome(@Param("operatorId") Long operatorId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    /**
     * 按充电站聚合收益，operatorId 为 null 时统计全平台
     */
    @Select("<script>" +
            "SELECT o.station_id AS stationId, s.name AS stationName, COUNT(*) AS orderCount, " +
            "SUM(o.charge_kwh) AS chargeKwh, SUM(o.charge_fee) AS chargeFee, " +
            "SUM(o.service_fee) AS serviceFee, SUM(o.total_fee) AS totalFee " +
            "FROM charging_order o LEFT JOIN charging_station s ON o.station_id = s.id " +
            "WHERE o.pay_status = 'PAID' AND o.deleted = 0 " +
            "<if test='operatorId != null'>AND o.operator_id = #{operatorId} </if>" +
            "<if test='startDate != null'>AND o.start_time >= #{startDate} </if>" +
            "<if test='endDate != null'>AND o.start_time &lt; #{endDate} </if>" +
            "GROUP BY o.station_id, s.name ORDER BY totalFee DESC" +
            "</script>")
    List<Map<String, Object>> selectIncomeByStation(@Param("operatorId") Long operatorId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /**
     * 按时间粒度统计订单数（管理员全平台或运营商名下）
     */
    @Select("<script>" +
            "SELECT DATE(start_time) AS period, COUNT(*) AS orderCount " +
            "FROM charging_order WHERE deleted = 0 AND status = 'FINISHED' " +
            "<if test='operatorId != null'>AND operator_id = #{operatorId} </if>" +
            "AND start_time >= #{startDate} AND start_time &lt; #{endDate} " +
            "GROUP BY DATE(start_time) ORDER BY period" +
            "</script>")
    List<Map<String, Object>> selectOrderTrend(@Param("operatorId") Long operatorId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    /**
     * 统计各充电桩被占用的订单数（用于利用率分析）
     */
    @Select("<script>" +
            "SELECT pile_id AS pileId, COUNT(*) AS orderCount, SUM(TIMESTAMPDIFF(MINUTE, start_time, IFNULL(end_time, NOW()))) AS occupiedMinutes " +
            "FROM charging_order WHERE deleted = 0 " +
            "<if test='operatorId != null'>AND operator_id = #{operatorId} </if>" +
            "AND start_time >= #{startDate} AND start_time &lt; #{endDate} " +
            "GROUP BY pile_id" +
            "</script>")
    List<Map<String, Object>> selectPileUsage(@Param("operatorId") Long operatorId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
}
