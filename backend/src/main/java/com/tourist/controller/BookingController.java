package com.tourist.controller;

import com.tourist.annotation.CurrentUser;
import com.tourist.annotation.CurrentUserInfo;
import com.tourist.annotation.RequireRole;
import com.tourist.common.PageResult;
import com.tourist.common.Result;
import com.tourist.common.RoleConstants;
import com.tourist.dto.request.BookingRequest;
import com.tourist.dto.response.BookingDetailResponse;
import com.tourist.dto.response.RoomAvailabilityResponse;
import com.tourist.entity.HotelBooking;
import com.tourist.service.BookingService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Resource
    private BookingService bookingService;

    /** 某酒店某日期的房型可用性（游客/其他角色只读） */
    @GetMapping("/availability")
    @RequireRole({RoleConstants.TOURIST, RoleConstants.PLATFORM_ADMIN, RoleConstants.HOTEL_ADMIN,
            RoleConstants.APPROVER, RoleConstants.COMPLAINT_HANDLER})
    public Result<List<RoomAvailabilityResponse>> availability(@RequestParam String hotelType,
                                                               @RequestParam Long hotelId,
                                                               @RequestParam(required = false) String date) {
        LocalDate d = (date == null || date.isEmpty()) ? null : LocalDate.parse(date);
        return Result.success(bookingService.availability(hotelType, hotelId, d));
    }

    /** 游客下单 */
    @PostMapping
    @RequireRole(RoleConstants.TOURIST)
    public Result<HotelBooking> book(@CurrentUser CurrentUserInfo currentUser,
                                     @Valid @RequestBody BookingRequest request) {
        return Result.success(bookingService.book(currentUser.getUserId(), request));
    }

    /** 某房型的预订明细（酒店管理员/平台管理员） */
    @GetMapping("/room-type")
    @RequireRole({RoleConstants.HOTEL_ADMIN, RoleConstants.PLATFORM_ADMIN})
    public Result<List<BookingDetailResponse>> roomTypeBookings(@RequestParam String hotelType,
                                                                 @RequestParam Long hotelId,
                                                                 @RequestParam String roomType,
                                                                 @RequestParam(required = false) String date) {
        LocalDate d = (date == null || date.isEmpty()) ? null : LocalDate.parse(date);
        return Result.success(bookingService.roomTypeBookings(hotelType, hotelId, roomType, d));
    }

    /** 我的预订 */
    @GetMapping("/mine")
    @RequireRole(RoleConstants.TOURIST)
    public Result<PageResult<HotelBooking>> mine(@CurrentUser CurrentUserInfo currentUser,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(bookingService.myBookings(currentUser.getUserId(), page, pageSize));
    }

    /** 取消我的预订 */
    @PutMapping("/{id}/cancel")
    @RequireRole(RoleConstants.TOURIST)
    public Result<String> cancel(@CurrentUser CurrentUserInfo currentUser, @PathVariable Long id) {
        bookingService.cancel(currentUser.getUserId(), id);
        return Result.success("已取消");
    }
}
