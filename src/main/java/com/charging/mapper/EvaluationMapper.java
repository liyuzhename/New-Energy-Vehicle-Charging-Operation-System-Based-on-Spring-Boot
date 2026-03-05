package com.charging.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.charging.entity.Evaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EvaluationMapper extends BaseMapper<Evaluation> {

    @Select("SELECT AVG(rating) FROM evaluation WHERE station_id = #{stationId} AND is_hidden = 0 AND deleted = 0")
    Double selectAvgRating(@Param("stationId") Long stationId);
}
