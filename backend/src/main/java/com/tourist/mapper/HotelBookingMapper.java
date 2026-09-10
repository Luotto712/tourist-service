package com.tourist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourist.entity.HotelBooking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface HotelBookingMapper extends BaseMapper<HotelBooking> {

    /** 覆盖某日期的有效预订数（check_in <= date <= check_out 且未取消） */
    @Select("SELECT COUNT(*) FROM hotel_booking WHERE hotel_type = #{hotelType} AND hotel_id = #{hotelId} "
            + "AND room_type = #{roomType} AND status = 'BOOKED' "
            + "AND check_in <= #{date} AND check_out >= #{date}")
    int countCoveringDate(@Param("hotelType") String hotelType, @Param("hotelId") Long hotelId,
                          @Param("roomType") String roomType, @Param("date") java.time.LocalDate date);
}
