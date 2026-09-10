package com.tourist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourist.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM sys_user WHERE username = #{username} AND status = 1")
    User findByUsername(@Param("username") String username);

    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User findByUsernameIncludeDisabled(@Param("username") String username);

    @Select("SELECT * FROM sys_user WHERE email = #{email} AND status = 1")
    User findByEmail(@Param("email") String email);

    @Select("SELECT * FROM sys_user WHERE role = #{role} AND college = #{college} AND status = 1")
    List<User> findByRoleAndCollege(@Param("role") String role, @Param("college") String college);
}
