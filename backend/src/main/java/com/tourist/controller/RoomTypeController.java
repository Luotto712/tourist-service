package com.tourist.controller;

import com.tourist.annotation.CurrentUser;
import com.tourist.annotation.CurrentUserInfo;
import com.tourist.annotation.RequireRole;
import com.tourist.common.Result;
import com.tourist.common.RoleConstants;
import com.tourist.dto.response.RoomAvailabilityResponse;
import com.tourist.entity.RoomType;
import com.tourist.service.BookingService;
import com.tourist.service.RoomTypeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

/** 房型基准：酒店管理员维护，其余登录角色可读。 */
@RestController
@RequestMapping("/api/room-types")
public class RoomTypeController {

    @Resource
    private RoomTypeService roomTypeService;

    @Resource
    private BookingService bookingService;

    @GetMapping
    @RequireRole({RoleConstants.TOURIST, RoleConstants.PLATFORM_ADMIN, RoleConstants.HOTEL_ADMIN,
            RoleConstants.APPROVER, RoleConstants.COMPLAINT_HANDLER})
    public Result<List<RoomAvailabilityResponse>> list(@RequestParam String hotelType,
                                                       @RequestParam Long hotelId,
                                                       @RequestParam(required = false) String date) {
        LocalDate d = (date == null || date.isEmpty()) ? null : LocalDate.parse(date);
        return Result.success(bookingService.availability(hotelType, hotelId, d));
    }

    @PostMapping
    @RequireRole(RoleConstants.HOTEL_ADMIN)
    public Result<RoomType> save(@CurrentUser CurrentUserInfo currentUser, @RequestBody RoomType roomType) {
        roomType.setUpdateUserId(currentUser.getUserId());
        return Result.success(roomTypeService.save(roomType));
    }

    @DeleteMapping("/{id}")
    @RequireRole(RoleConstants.HOTEL_ADMIN)
    public Result<String> delete(@PathVariable Long id) {
        roomTypeService.delete(id);
        return Result.success("删除成功");
    }
}
