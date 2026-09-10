package com.tourist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tourist.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0")
    int countUnread(@Param("userId") Long userId);
    
    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId} AND id = #{id}")
    int markAsRead(@Param("userId") Long userId, @Param("id") Long id);
    
    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId}")
    int markAllAsRead(@Param("userId") Long userId);
}
