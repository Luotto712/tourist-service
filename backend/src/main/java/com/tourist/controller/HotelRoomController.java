package com.tourist.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourist.annotation.CurrentUser;
import com.tourist.annotation.CurrentUserInfo;
import com.tourist.annotation.RequireRole;
import com.tourist.common.BusinessException;
import com.tourist.common.Result;
import com.tourist.common.ResultCode;
import com.tourist.common.RoleConstants;
import com.tourist.entity.HotelRoom;
import com.tourist.mapper.HotelRoomMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/hotel-rooms")
public class HotelRoomController {

    @Resource
    private HotelRoomMapper hotelRoomMapper;

    /** 游客/酒店管理员查询某酒店某日期的房态 */
    @GetMapping
    @RequireRole({RoleConstants.TOURIST, RoleConstants.HOTEL_ADMIN, RoleConstants.PLATFORM_ADMIN})
    public Result<List<HotelRoom>> list(@RequestParam String hotelType,
                                        @RequestParam Long hotelId,
                                        @RequestParam(required = false) String date) {
        LambdaQueryWrapper<HotelRoom> w = new LambdaQueryWrapper<HotelRoom>()
                .eq(HotelRoom::getHotelType, hotelType)
                .eq(HotelRoom::getHotelId, hotelId);
        if (date != null && !date.isEmpty()) {
            w.eq(HotelRoom::getDate, LocalDate.parse(date));
        }
        w.orderByAsc(HotelRoom::getRoomType);
        return Result.success(hotelRoomMapper.selectList(w));
    }

    /** 酒店管理员录入/更新本酒店房态（按 hotel+hotelType+roomType+date 幂等） */
    @PutMapping
    @RequireRole(RoleConstants.HOTEL_ADMIN)
    public Result<String> upsert(@CurrentUser CurrentUserInfo currentUser, @RequestBody HotelRoom room) {
        if (room.getHotelType() == null || room.getHotelId() == null || room.getRoomType() == null || room.getDate() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "hotelType/hotelId/roomType/date 不能为空");
        }
        LambdaQueryWrapper<HotelRoom> w = new LambdaQueryWrapper<HotelRoom>()
                .eq(HotelRoom::getHotelType, room.getHotelType())
                .eq(HotelRoom::getHotelId, room.getHotelId())
                .eq(HotelRoom::getRoomType, room.getRoomType())
                .eq(HotelRoom::getDate, room.getDate());
        HotelRoom existing = hotelRoomMapper.selectOne(w);
        room.setUpdateUserId(currentUser.getUserId());
        if (existing != null) {
            room.setId(existing.getId());
            hotelRoomMapper.updateById(room);
        } else {
            room.setId(null);
            hotelRoomMapper.insert(room);
        }
        return Result.success("房态已保存");
    }
}
