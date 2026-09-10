package com.tourist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourist.entity.HotelMarketing;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HotelMarketingMapper extends BaseMapper<HotelMarketing> {

    /** 物理删除营销记录 */
    @Delete("DELETE FROM hotel_marketing WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);
}
