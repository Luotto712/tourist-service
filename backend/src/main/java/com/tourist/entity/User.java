package com.tourist.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("real_name")
    private String realName;

    @TableField("role")
    private String role;

    @TableField("college")
    private String college;

    @TableField("student_no")
    private String studentNo;

    @TableField("teacher_no")
    private String teacherNo;

    @TableField("gpa")
    private BigDecimal gpa;

    @TableField("grade_score")
    private BigDecimal gradeScore;

    @TableField("phone")
    private String phone;

    @TableField("email")
    private String email;

    @TableField("department_id")
    private Long departmentId;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
