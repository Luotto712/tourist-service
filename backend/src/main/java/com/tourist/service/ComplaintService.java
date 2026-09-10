package com.tourist.service;

import com.tourist.common.PageResult;
import com.tourist.dto.request.ComplaintReplyRequest;
import com.tourist.dto.request.ComplaintSubmitRequest;
import com.tourist.dto.response.ComplaintDetailResponse;
import com.tourist.entity.ComplaintReply;
import com.tourist.entity.TouristComplaint;

import java.util.List;

public interface ComplaintService {

    /** 游客提交投诉（建单 + 建审批流，PENDING） */
    TouristComplaint submit(Long userId, ComplaintSubmitRequest request);

    /** 追加回复；游客回复触发状态回退（驳回→待审批；处理完成→处理中）。返回新回复（含 id，用于上传回复附件） */
    ComplaintReply reply(Long userId, Long complaintId, ComplaintReplyRequest request);

    /** 游客删除本人投诉（逻辑删除） */
    void deleteComplaint(Long userId, Long complaintId);

    /** 我的投诉（分页） */
    PageResult<TouristComplaint> myComplaints(Long userId, int page, int pageSize);

    /** 投诉详情（含时间线、回复、附件） */
    ComplaintDetailResponse detail(Long id);

    /** 审批通过：PENDING -> APPROVED，is_published=1 */
    void approve(Long approverId, Long complaintId, String comment);

    /** 审批拒绝：PENDING -> REJECTED，is_published=0 */
    void reject(Long approverId, Long complaintId, String comment);

    /** 分派处理人：APPROVED -> PROCESSING */
    void assign(Long complaintId, Long handlerId);

    /** 待我处理（处理人）：PROCESSING 且 handler=me */
    PageResult<TouristComplaint> pendingByHandler(Long handlerId, int page, int pageSize);

    /** 待审批列表（审批人员）：PENDING */
    PageResult<TouristComplaint> pendingForApproval(int page, int pageSize);

    /** 处理提交结果：PROCESSING -> RESOLVED；并生成处理人回复（返回以支持上传处理附件） */
    ComplaintReply process(Long handlerId, Long complaintId, String result);

    /** 游客确认处理意见：RESOLVED -> CONFIRMED */
    void confirm(Long touristId, Long complaintId);

    /** 结案：CONFIRMED -> CLOSED */
    void close(Long complaintId);

    /** 结案后评分：CLOSED -> 写入 rating */
    void rate(Long touristId, Long complaintId, Integer rating);

    /** 已通过待分派列表（平台管理员分派页） */
    List<TouristComplaint> approvedList();

    /** 待结案列表（平台管理员结案页） */
    List<TouristComplaint> confirmedList();
}
