package com.tourist.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourist.common.BusinessException;
import com.tourist.common.PageResult;
import com.tourist.common.ResultCode;
import com.tourist.dto.request.BookingRequest;
import com.tourist.dto.response.BookingDetailResponse;
import com.tourist.dto.response.RoomAvailabilityResponse;
import com.tourist.entity.HotelBooking;
import com.tourist.entity.RoomType;
import com.tourist.entity.StarHotel;
import com.tourist.entity.NonstarHotel;
import com.tourist.mapper.HotelBookingMapper;
import com.tourist.mapper.NonstarHotelMapper;
import com.tourist.mapper.StarHotelMapper;
import com.tourist.mapper.UserMapper;
import com.tourist.service.BookingService;
import com.tourist.service.RoomTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class BookingServiceImpl implements BookingService {

    @Resource
    private HotelBookingMapper bookingMapper;

    @Resource
    private RoomTypeService roomTypeService;

    @Resource
    private StarHotelMapper starHotelMapper;

    @Resource
    private NonstarHotelMapper nonstarHotelMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public List<RoomAvailabilityResponse> availability(String hotelType, Long hotelId, LocalDate date) {
        LocalDate d = date != null ? date : LocalDate.now();
        List<RoomType> types = roomTypeService.listByHotel(hotelType, hotelId);
        List<RoomAvailabilityResponse> out = new ArrayList<>();
        for (RoomType rt : types) {
            int base = rt.getBaseBooked() == null ? 0 : rt.getBaseBooked();
            int bookedToday = base + bookingMapper.countCoveringDate(hotelType, hotelId, rt.getRoomType(), d);
            int total = rt.getTotal() == null ? 0 : rt.getTotal();
            RoomAvailabilityResponse r = new RoomAvailabilityResponse();
            r.setRoomTypeId(rt.getId());
            r.setRoomType(rt.getRoomType());
            r.setTotal(total);
            r.setBaseBooked(base);
            r.setBooked(bookedToday);
            r.setRemaining(total - bookedToday);
            r.setPrice(rt.getPrice());
            out.add(r);
        }
        return out;
    }

    @Override
    @Transactional
    public HotelBooking book(Long userId, BookingRequest request) {
        LocalDate in = request.getCheckIn();
        LocalDate outDate = request.getCheckOut();
        if (outDate.isBefore(in)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "离开日期不得早于入住日期");
        }
        RoomType rt = roomTypeService.find(request.getHotelType(), request.getHotelId(), request.getRoomType());
        if (rt == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "房型不存在");
        }
        // 逐日校验余量
        for (LocalDate d = in; !d.isAfter(outDate); d = d.plusDays(1)) {
            int bookedToday = (rt.getBaseBooked() == null ? 0 : rt.getBaseBooked())
                    + bookingMapper.countCoveringDate(request.getHotelType(), request.getHotelId(), rt.getRoomType(), d);
            int remaining = (rt.getTotal() == null ? 0 : rt.getTotal()) - bookedToday;
            if (remaining < 1) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "所选日期余量不足（" + d + "）");
            }
        }
        long nights = ChronoUnit.DAYS.between(in, outDate) + 1;
        BigDecimal price = rt.getPrice() == null ? BigDecimal.ZERO : rt.getPrice();
        HotelBooking booking = new HotelBooking();
        booking.setUserId(userId);
        booking.setHotelType(request.getHotelType());
        booking.setHotelId(request.getHotelId());
        booking.setHotelName(hotelName(request.getHotelType(), request.getHotelId()));
        booking.setRoomType(rt.getRoomType());
        booking.setPrice(price);
        booking.setCheckIn(in);
        booking.setCheckOut(outDate);
        booking.setGuests(request.getGuests());
        booking.setTotalPrice(price.multiply(BigDecimal.valueOf(nights)));
        booking.setStatus("BOOKED");
        bookingMapper.insert(booking);
        return booking;
    }

    @Override
    public PageResult<HotelBooking> myBookings(Long userId, int page, int pageSize) {
        IPage<HotelBooking> iPage = bookingMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<HotelBooking>()
                        .eq(HotelBooking::getUserId, userId)
                        .orderByDesc(HotelBooking::getCreateTime));
        return PageResult.of(iPage);
    }

    @Override
    @Transactional
    public void cancel(Long userId, Long bookingId) {
        HotelBooking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "预订不存在");
        }
        if (!Objects.equals(booking.getUserId(), userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权取消该预订");
        }
        if (!"BOOKED".equals(booking.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该预订不可取消");
        }
        booking.setStatus("CANCELLED");
        bookingMapper.updateById(booking);
    }

    @Override
    public List<BookingDetailResponse> roomTypeBookings(String hotelType, Long hotelId, String roomType, LocalDate date) {
        LambdaQueryWrapper<HotelBooking> w = new LambdaQueryWrapper<HotelBooking>()
                .eq(HotelBooking::getHotelType, hotelType)
                .eq(HotelBooking::getHotelId, hotelId)
                .eq(HotelBooking::getRoomType, roomType)
                .eq(HotelBooking::getStatus, "BOOKED")
                .orderByDesc(HotelBooking::getCreateTime);
        if (date != null) {
            w.le(HotelBooking::getCheckIn, date).ge(HotelBooking::getCheckOut, date);
        }
        List<HotelBooking> bookings = bookingMapper.selectList(w);
        Map<Long, String> names = userMapper.selectList(null).stream()
                .collect(java.util.stream.Collectors.toMap(
                        com.tourist.entity.User::getId, com.tourist.entity.User::getRealName, (a, b) -> a));
        List<BookingDetailResponse> out = new ArrayList<>();
        for (HotelBooking b : bookings) {
            BookingDetailResponse d = new BookingDetailResponse();
            d.setId(b.getId());
            d.setUserName(names.getOrDefault(b.getUserId(), ""));
            d.setRoomType(b.getRoomType());
            d.setCheckIn(b.getCheckIn());
            d.setCheckOut(b.getCheckOut());
            d.setGuests(b.getGuests());
            d.setTotalPrice(b.getTotalPrice());
            d.setStatus(b.getStatus());
            out.add(d);
        }
        return out;
    }

    private String hotelName(String hotelType, Long hotelId) {
        if ("STAR".equals(hotelType)) {
            StarHotel h = starHotelMapper.selectById(hotelId);
            return h == null ? "" : h.getName();
        }
        NonstarHotel h = nonstarHotelMapper.selectById(hotelId);
        return h == null ? "" : h.getName();
    }
}
