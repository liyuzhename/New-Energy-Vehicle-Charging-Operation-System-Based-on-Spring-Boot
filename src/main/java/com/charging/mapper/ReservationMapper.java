package com.charging.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.charging.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {

    /**
     * 查询同一充电枪时段冲突预约（PENDING/CONFIRMED状态）
     */
    @Select("SELECT * FROM reservation WHERE gun_id = #{gunId} AND deleted = 0 " +
            "AND status IN ('PENDING','CONFIRMED') " +
            "AND start_time < #{endTime} AND end_time > #{startTime}")
    List<Reservation> selectConflict(@Param("gunId") Long gunId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 查询某枪当前时刻处于活跃状态（PENDING/CONFIRMED）的预约
     * 用于定时任务激活枪状态
     */
    @Select("SELECT * FROM reservation WHERE gun_id = #{gunId} AND deleted = 0 " +
            "AND status IN ('PENDING','CONFIRMED') " +
            "AND start_time <= #{now} AND end_time > #{now}")
    List<Reservation> selectActiveByGunId(@Param("gunId") Long gunId,
                                          @Param("now") LocalDateTime now);

    /**
     * 查询所有已进入时间段但尚未激活枪状态的PENDING预约
     * （startTime <= now < endTime，状态PENDING）
     */
    @Select("SELECT * FROM reservation WHERE deleted = 0 " +
            "AND status = 'PENDING' " +
            "AND start_time <= #{now} AND end_time > #{now}")
    List<Reservation> selectPendingInProgress(@Param("now") LocalDateTime now);

    /**
     * 查询所有已过期但状态仍为PENDING/CONFIRMED的预约
     */
    @Select("SELECT * FROM reservation WHERE deleted = 0 " +
            "AND status IN ('PENDING','CONFIRMED') " +
            "AND end_time < #{now}")
    List<Reservation> selectOverdue(@Param("now") LocalDateTime now);

    /**
     * 查询某站点在指定时段内已被预约的枪ID列表
     */
    @Select("SELECT DISTINCT r.gun_id FROM reservation r " +
            "INNER JOIN charging_gun g ON r.gun_id = g.id " +
            "INNER JOIN charging_pile p ON g.pile_id = p.id " +
            "WHERE p.station_id = #{stationId} AND r.deleted = 0 " +
            "AND r.status IN ('PENDING','CONFIRMED') " +
            "AND r.start_time < #{endTime} AND r.end_time > #{startTime}")
    List<Long> selectReservedGunIds(@Param("stationId") Long stationId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);
}
