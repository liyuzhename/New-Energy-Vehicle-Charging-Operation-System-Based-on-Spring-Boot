package com.charging.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.charging.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM `user` WHERE username = #{username} AND deleted = 0 LIMIT 1")
    User selectByUsername(String username);

    @Select("SELECT * FROM `user` WHERE phone = #{phone} AND deleted = 0 LIMIT 1")
    User selectByPhone(String phone);

    @Select("SELECT * FROM `user` WHERE email = #{email} AND deleted = 0 LIMIT 1")
    User selectByEmail(String email);

    @Select("SELECT DATE_FORMAT(create_time,'%Y-%m') AS month, COUNT(*) AS newUsers " +
            "FROM `user` WHERE deleted = 0 AND create_time >= #{startDate} AND create_time < #{endDate} " +
            "GROUP BY DATE_FORMAT(create_time,'%Y-%m') ORDER BY month")
    List<Map<String, Object>> selectUserGrowth(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);
}
