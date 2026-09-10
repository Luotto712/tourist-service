package com.tourist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourist.entity.TouristComplaint;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TouristComplaintMapper extends BaseMapper<TouristComplaint> {

    /** 物理删除（不走逻辑删除），确保记录彻底消失 */
    @Delete("DELETE FROM tourist_complaint WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);
}
