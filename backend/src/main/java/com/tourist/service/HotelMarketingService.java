package com.tourist.service;

import com.tourist.entity.HotelMarketing;

import java.util.List;

public interface HotelMarketingService {

    List<HotelMarketing> list(String hotelType, Long hotelId);

    /** 录入营销并通知所有游客（标题=酒店名，内容=营销内容） */
    HotelMarketing create(Long userId, HotelMarketing marketing);

    /** 编辑=重新发布：更新内容并再次通知游客 */
    HotelMarketing update(Long id, HotelMarketing marketing);

    /** 彻底删除 */
    void delete(Long id);
}
