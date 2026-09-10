package com.tourist.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourist.common.BusinessException;
import com.tourist.common.ResultCode;
import com.tourist.entity.HotelRoom;
import com.tourist.entity.RoomType;
import com.tourist.entity.HotelBooking;
import com.tourist.mapper.HotelBookingMapper;
import com.tourist.mapper.HotelRoomMapper;
import com.tourist.mapper.RoomTypeMapper;
import com.tourist.service.RoomTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RoomTypeServiceImpl implements RoomTypeService {

    @Resource
    private RoomTypeMapper roomTypeMapper;

    @Resource
    private HotelRoomMapper hotelRoomMapper;

    @Resource
    private HotelBookingMapper hotelBookingMapper;

    @Override
    public List<RoomType> listByHotel(String hotelType, Long hotelId) {
        return roomTypeMapper.selectList(new LambdaQueryWrapper<RoomType>()
                .eq(RoomType::getHotelType, hotelType)
                .eq(RoomType::getHotelId, hotelId)
                .orderByAsc(RoomType::getId));
    }

    @Override
    public RoomType find(String hotelType, Long hotelId, String roomType) {
        return roomTypeMapper.selectOne(new LambdaQueryWrapper<RoomType>()
                .eq(RoomType::getHotelType, hotelType)
                .eq(RoomType::getHotelId, hotelId)
                .eq(RoomType::getRoomType, roomType));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        RoomType rt = roomTypeMapper.selectById(id);
        if (rt == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "房型不存在");
        }
        // 彻底删除：连带删除该房型的预订记录
        hotelBookingMapper.delete(new LambdaQueryWrapper<HotelBooking>()
                .eq(HotelBooking::getHotelType, rt.getHotelType())
                .eq(HotelBooking::getHotelId, rt.getHotelId())
                .eq(HotelBooking::getRoomType, rt.getRoomType()));
        roomTypeMapper.hardDeleteById(id);
    }

    @Override
    @Transactional
    public RoomType save(RoomType roomType) {
        RoomType existing = find(roomType.getHotelType(), roomType.getHotelId(), roomType.getRoomType());
        if (existing != null) {
            roomType.setId(existing.getId());
            roomTypeMapper.updateById(roomType);
            return roomType;
        }
        // 首次（该酒店尚无任何基准行）→ 从 hotel_room 迁移存量房型与基准已预定，再清空遗留行
        if (listByHotel(roomType.getHotelType(), roomType.getHotelId()).isEmpty()) {
            List<HotelRoom> legacy = hotelRoomMapper.selectList(new LambdaQueryWrapper<HotelRoom>()
                    .eq(HotelRoom::getHotelType, roomType.getHotelType())
                    .eq(HotelRoom::getHotelId, roomType.getHotelId()));
            for (HotelRoom hr : legacy) {
                RoomType m = new RoomType();
                m.setHotelType(hr.getHotelType());
                m.setHotelId(hr.getHotelId());
                m.setRoomType(hr.getRoomType());
                m.setTotal(hr.getTotal());
                m.setBaseBooked(hr.getBooked());
                m.setPrice(hr.getPrice());
                roomTypeMapper.insert(m);
            }
            if (!legacy.isEmpty()) {
                hotelRoomMapper.hardDeleteByHotel(roomType.getHotelType(), roomType.getHotelId());
            }
            // 迁移后若已存在同名房型，直接更新
            RoomType migrated = find(roomType.getHotelType(), roomType.getHotelId(), roomType.getRoomType());
            if (migrated != null) {
                roomType.setId(migrated.getId());
                roomTypeMapper.updateById(roomType);
                return roomType;
            }
        }
        roomTypeMapper.insert(roomType);
        return roomType;
    }
}
