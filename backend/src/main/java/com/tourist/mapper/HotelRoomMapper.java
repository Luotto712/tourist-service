package com.tourist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourist.entity.HotelRoom;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HotelRoomMapper extends BaseMapper<HotelRoom> {

    /** 物理删除某酒店类型的全部房态行 */
    @Delete("DELETE FROM hotel_room WHERE hotel_type = #{hotelType} AND hotel_id = #{hotelId}")
    int hardDeleteByHotel(@org.apache.ibatis.annotations.Param("hotelType") String hotelType,
                          @org.apache.ibatis.annotations.Param("hotelId") Long hotelId);
}
