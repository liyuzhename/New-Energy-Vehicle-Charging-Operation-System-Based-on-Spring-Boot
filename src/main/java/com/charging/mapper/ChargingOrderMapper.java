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
     * 按运营商聚合收益（按日），支持按充电站过滤
     */
    @Select("<script>" +
            "SELECT DATE(start_time) AS day, COUNT(*) AS orderCount, SUM(charge_kwh) AS chargeKwh, " +
            "SUM(charge_fee) AS chargeFee, SUM(service_fee) AS serviceFee, SUM(total_fee) AS totalFee " +
            "FROM charging_order WHERE pay_status = 'PAID' AND deleted = 0 " +
            "<if test='operatorId != null'>AND operator_id = #{operatorId} </if>" +
            "<if test='stationId != null'>AND station_id = #{stationId} </if>" +
            "AND start_time >= #{startDate} AND start_time &lt; #{endDate} " +
            "GROUP BY DATE(start_time) ORDER BY day" +
            "</script>")
    List<Map<String, Object>> selectDailyIncome(@Param("operatorId") Long operatorId,
                                                 @Param("stationId") Long stationId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    /**
     * 按充电站聚合收益，支持按单个充电站过滤
     */
    @Select("<script>" +
            "SELECT o.station_id AS stationId, s.name AS stationName, COUNT(*) AS orderCount, " +
            "SUM(o.charge_kwh) AS chargeKwh, SUM(o.charge_fee) AS chargeFee, " +
            "SUM(o.service_fee) AS serviceFee, SUM(o.total_fee) AS totalFee " +
            "FROM charging_order o LEFT JOIN charging_station s ON o.station_id = s.id " +
            "WHERE o.pay_status = 'PAID' AND o.deleted = 0 " +
            "<if test='operatorId != null'>AND o.operator_id = #{operatorId} </if>" +
            "<if test='stationId != null'>AND o.station_id = #{stationId} </if>" +
            "<if test='startDate != null'>AND o.start_time >= #{startDate} </if>" +
            "<if test='endDate != null'>AND o.start_time &lt; #{endDate} </if>" +
            "GROUP BY o.station_id, s.name ORDER BY totalFee DESC" +
            "</script>")
    List<Map<String, Object>> selectIncomeByStation(@Param("operatorId") Long operatorId,
                                                     @Param("stationId") Long stationId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /**
     * 按充电站+日期双维度聚合收益（收益明细列表，含日期字段）
     */
    @Select("<script>" +
            "SELECT DATE(o.start_time) AS day, o.station_id AS stationId, s.name AS stationName, " +
            "COUNT(*) AS orderCount, SUM(o.charge_kwh) AS chargeKwh, " +
            "SUM(o.charge_fee) AS chargeFee, SUM(o.service_fee) AS serviceFee, SUM(o.total_fee) AS totalFee " +
            "FROM charging_order o LEFT JOIN charging_station s ON o.station_id = s.id " +
            "WHERE o.pay_status = 'PAID' AND o.deleted = 0 " +
            "<if test='operatorId != null'>AND o.operator_id = #{operatorId} </if>" +
            "<if test='stationId != null'>AND o.station_id = #{stationId} </if>" +
            "<if test='startDate != null'>AND o.start_time >= #{startDate} </if>" +
            "<if test='endDate != null'>AND o.start_time &lt; #{endDate} </if>" +
            "GROUP BY DATE(o.start_time), o.station_id, s.name ORDER BY day DESC, totalFee DESC" +
            "</script>")
    List<Map<String, Object>> selectIncomeByStationAndDay(@Param("operatorId") Long operatorId,
                                                           @Param("stationId") Long stationId,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);

    /**
     * 按时间粒度统计订单数（管理员全平台或运营商名下），支持按充电站过滤
     */
    @Select("<script>" +
            "SELECT DATE(start_time) AS period, COUNT(*) AS orderCount " +
            "FROM charging_order WHERE deleted = 0 AND status = 'FINISHED' " +
            "<if test='operatorId != null'>AND operator_id = #{operatorId} </if>" +
            "<if test='stationId != null'>AND station_id = #{stationId} </if>" +
            "AND start_time >= #{startDate} AND start_time &lt; #{endDate} " +
            "GROUP BY DATE(start_time) ORDER BY period" +
            "</script>")
    List<Map<String, Object>> selectOrderTrend(@Param("operatorId") Long operatorId,
                                                @Param("stationId") Long stationId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    /**
     * 统计各充电桩被占用的订单数（用于利用率分析），支持按充电站过滤，返回 pileNo
     */
    @Select("<script>" +
            "SELECT o.pile_id AS pileId, p.pile_no AS pileNo, COUNT(*) AS orderCount, " +
            "SUM(TIMESTAMPDIFF(MINUTE, o.start_time, IFNULL(o.end_time, NOW()))) AS occupiedMinutes " +
            "FROM charging_order o LEFT JOIN charging_pile p ON o.pile_id = p.id " +
            "WHERE o.deleted = 0 " +
            "<if test='operatorId != null'>AND o.operator_id = #{operatorId} </if>" +
            "<if test='stationId != null'>AND o.station_id = #{stationId} </if>" +
            "AND o.start_time >= #{startDate} AND o.start_time &lt; #{endDate} " +
            "GROUP BY o.pile_id, p.pile_no" +
            "</script>")
    List<Map<String, Object>> selectPileUsage(@Param("operatorId") Long operatorId,
                                               @Param("stationId") Long stationId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
}
