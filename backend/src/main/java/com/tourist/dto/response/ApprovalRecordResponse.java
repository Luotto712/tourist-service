package com.tourist.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApprovalRecordResponse {
    private Long id;
    private String subjectType;
    private Long subjectId;
    private Long nodeId;
    private Long approverId;
    private String action;
    private String comment;
    private String nodeName;
    private String approverName;
    private String approverRole;
    private LocalDateTime createTime;
}
