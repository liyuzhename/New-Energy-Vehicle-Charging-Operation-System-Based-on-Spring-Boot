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
     * 查询同一充电桩时段冲突预约（PENDING/CONFIRMED状态）
     */
    @Select("SELECT * FROM reservation WHERE pile_id = #{pileId} AND deleted = 0 " +
            "AND status IN ('PENDING','CONFIRMED') " +
            "AND start_time < #{endTime} AND end_time > #{startTime}")
    List<Reservation> selectConflict(@Param("pileId") Long pileId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);
}
