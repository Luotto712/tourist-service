package com.tourist.service;

import com.tourist.common.PageResult;
import com.tourist.dto.request.BookingRequest;
import com.tourist.dto.response.RoomAvailabilityResponse;
import com.tourist.dto.response.BookingDetailResponse;
import com.tourist.entity.HotelBooking;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    /** 某酒店某日期的全部房型可用性（已预定=基准+覆盖该日期的预订数） */
    List<RoomAvailabilityResponse> availability(String hotelType, Long hotelId, LocalDate date);

    /** 游客下单；校验入住天数、余量，落库 BOOKED，返回预订记录 */
    HotelBooking book(Long userId, BookingRequest request);

    /** 我（游客）的预订，按创建时间倒序 */
    PageResult<HotelBooking> myBookings(Long userId, int page, int pageSize);

    /** 游客取消本人预订（仅 BOOKED 可取消） */
    void cancel(Long userId, Long bookingId);

    /** 某房型的预订明细（可指定日期，只看覆盖该日期的有效预订），含预定人姓名 */
    java.util.List<BookingDetailResponse> roomTypeBookings(String hotelType, Long hotelId, String roomType, java.time.LocalDate date);
}
