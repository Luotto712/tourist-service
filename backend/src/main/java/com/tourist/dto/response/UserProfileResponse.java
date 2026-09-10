package com.tourist.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserProfileResponse {
    private Long id;
    private String username;
    private String realName;
    private String role;
    private String college;
    private String studentNo;
    private String teacherNo;
    private BigDecimal gpa;
    private BigDecimal gradeScore;
    private String phone;
    private String email;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
