package com.tourist.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("approval_record")
public class ApprovalRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("subject_type")
    private String subjectType;

    @TableField("subject_id")
    private Long subjectId;

    @TableField("node_id")
    private Long nodeId;

    @TableField("approver_id")
    private Long approverId;

    @TableField("action")
    private String action;

    @TableField("comment")
    private String comment;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String nodeName;

    @TableField(exist = false)
    private String approverName;

    @TableField(exist = false)
    private String approverRole;
}
