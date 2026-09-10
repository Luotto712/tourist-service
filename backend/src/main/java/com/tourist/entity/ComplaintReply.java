package com.tourist.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("complaint_reply")
public class ComplaintReply {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("complaint_id")
    private Long complaintId;

    @TableField("user_id")
    private Long userId;

    @TableField("content")
    private String content;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private List<com.tourist.dto.response.FileAttachmentResponse> attachments;
}
