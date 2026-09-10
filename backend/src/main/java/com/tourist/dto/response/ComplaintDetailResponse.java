package com.tourist.dto.response;

import com.tourist.entity.ApprovalRecord;
import com.tourist.entity.ComplaintReply;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ComplaintDetailResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String content;
    private String status;
    private Integer isPublished;
    private Long handlerId;
    private String handlerName;
    private String result;
    private Integer rating;
    private LocalDateTime assignTime;
    private LocalDateTime processTime;
    private LocalDateTime confirmTime;
    private LocalDateTime closeTime;
    private LocalDateTime createTime;

    /** 游客/处理人员的追加回复 */
    private List<ComplaintReply> replies;

    /** 审批时间线（审批人/节点/操作/意见） */
    private List<ApprovalRecord> timeline;

    /** 关联附件（多态 relatedType='COMPLAINT'） */
    private List<FileAttachmentResponse> attachments;
}
