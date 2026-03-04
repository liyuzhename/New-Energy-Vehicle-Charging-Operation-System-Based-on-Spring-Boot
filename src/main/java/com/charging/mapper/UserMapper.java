package com.charging.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.charging.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM `user` WHERE username = #{username} AND deleted = 0 LIMIT 1")
    User selectByUsername(String username);

    @Select("SELECT * FROM `user` WHERE phone = #{phone} AND deleted = 0 LIMIT 1")
    User selectByPhone(String phone);
}
