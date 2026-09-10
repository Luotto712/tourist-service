package com.tourist.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tourist_complaint")
public class TouristComplaint {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("content")
    private String content;

    @TableField("status")
    private String status;

    @TableField("is_published")
    private Integer isPublished;

    @TableField("handler_id")
    private Long handlerId;

    @TableField("result")
    private String result;

    @TableField("rating")
    private Integer rating;

    @TableField("current_node_id")
    private Long currentNodeId;

    @TableField("assign_time")
    private LocalDateTime assignTime;

    @TableField("process_time")
    private LocalDateTime processTime;

    @TableField("confirm_time")
    private LocalDateTime confirmTime;

    @TableField("close_time")
    private LocalDateTime closeTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("deleted")
    private Integer deleted;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String handlerName;
}
