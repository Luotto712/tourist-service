package com.tourist.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("approval_flow")
public class ApprovalFlow {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("subject_type")
    private String subjectType;

    @TableField("subject_id")
    private Long subjectId;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField(exist = false)
    private java.util.List<ApprovalNode> nodes;
}
