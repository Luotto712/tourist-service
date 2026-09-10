package com.tourist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourist.entity.RoomType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RoomTypeMapper extends BaseMapper<RoomType> {

    /** 物理删除单个房型 */
    @Delete("DELETE FROM room_type WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);
}
