package com.charging.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.entity.ChargingStation;
import com.charging.vo.StationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ChargingStationMapper extends BaseMapper<ChargingStation> {

    /**
     * 用户端分页查询上线充电站，含可用桩数
     */
    @Select("<script>" +
            "SELECT s.id, s.operator_id, s.name, s.address, s.city, s.longitude, s.latitude," +
            " s.business_hours, s.parking_fee, s.status, s.create_time," +
            " COUNT(p.id) AS available_pile_count" +
            " FROM charging_station s" +
            " LEFT JOIN charging_pile p ON p.station_id = s.id AND p.status = 'IDLE' AND p.deleted = 0" +
            " WHERE s.deleted = 0 AND s.status = 'ONLINE'" +
            "<if test='city != null and city != \"\"'> AND s.city = #{city}</if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (s.name LIKE CONCAT('%',#{keyword},'%') OR s.address LIKE CONCAT('%',#{keyword},'%'))</if>" +
            " GROUP BY s.id" +
            " ORDER BY s.create_time DESC" +
            "</script>")
    Page<StationVO> listForUser(Page<StationVO> page,
                                @Param("keyword") String keyword,
                                @Param("city") String city);
}
