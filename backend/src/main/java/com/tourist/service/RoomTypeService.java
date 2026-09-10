package com.tourist.service;

import com.tourist.entity.RoomType;

import java.util.List;

/** 房型基准（总房量/基准已预定/单价），供酒店管理员维护、查询方读取。 */
public interface RoomTypeService {

    /** 某酒店全部房型（按房型名排序） */
    List<RoomType> listByHotel(String hotelType, Long hotelId);

    /** 新增/更新房型；存量酒店首次维护时从 hotel_room 一次性迁移全部房型基准 */
    RoomType save(RoomType roomType);

    /** 按 (酒店,房型) 查找 */
    RoomType find(String hotelType, Long hotelId, String roomType);

    /** 彻底删除房型（物理删除，连带删除其预订记录） */
    void delete(Long id);
}
