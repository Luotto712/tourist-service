package com.tourist.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourist.common.BusinessException;
import com.tourist.common.PageResult;
import com.tourist.common.ResultCode;
import com.tourist.common.RoleConstants;
import com.tourist.dto.request.ComplaintReplyRequest;
import com.tourist.dto.request.ComplaintSubmitRequest;
import com.tourist.dto.response.ComplaintDetailResponse;
import com.tourist.entity.ApprovalNode;
import com.tourist.entity.ComplaintReply;
import com.tourist.entity.TouristComplaint;
import com.tourist.entity.User;
import com.tourist.enums.ApprovalAction;
import com.tourist.enums.ComplaintStatus;
import com.tourist.enums.NotificationType;
import com.tourist.enums.SubjectType;
import com.tourist.mapper.ComplaintReplyMapper;
import com.tourist.mapper.TouristComplaintMapper;
import com.tourist.mapper.UserMapper;
import com.tourist.service.ApprovalService;
import com.tourist.service.ComplaintService;
import com.tourist.service.FileService;
import com.tourist.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    private static final String SUBJECT_TYPE = SubjectType.COMPLAINT.name();

    @Resource
    private TouristComplaintMapper complaintMapper;

    @Resource
    private ComplaintReplyMapper replyMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ApprovalService approvalService;

    @Resource
    private NotificationService notificationService;

    @Resource
    private FileService fileService;

    @Override
    @Transactional
    public TouristComplaint submit(Long userId, ComplaintSubmitRequest request) {
        TouristComplaint complaint = new TouristComplaint();
        complaint.setUserId(userId);
        complaint.setContent(request.getContent());
        complaint.setStatus(ComplaintStatus.PENDING.name());
        complaint.setIsPublished(0);
        complaintMapper.insert(complaint);

        ApprovalNode node = approvalService.createFlow(SUBJECT_TYPE, complaint.getId(), RoleConstants.APPROVER, "投诉审批");
        complaint.setCurrentNodeId(node.getId());
        complaintMapper.updateById(complaint);

        // 通知所有审批人员
        List<User> approvers = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, RoleConstants.APPROVER).eq(User::getStatus, 1));
        for (User approver : approvers) {
            notificationService.sendNotification(approver.getId(), "新投诉待审批",
                    "游客提交了投诉，请尽快审批。", NotificationType.COMPLAINT_SUBMITTED.name(), complaint.getId());
        }
        return complaint;
    }

    @Override
    @Transactional
    public ComplaintReply reply(Long userId, Long complaintId, ComplaintReplyRequest request) {
        TouristComplaint complaint = requireComplaint(complaintId);
        ComplaintReply reply = new ComplaintReply();
        reply.setComplaintId(complaintId);
        reply.setUserId(userId);
        reply.setContent(request.getContent());
        replyMapper.insert(reply);
        // 游客追加回复触发状态回退：驳回→待审批；处理完成→处理中（对应人员需再次审批/处理）
        if (Objects.equals(complaint.getUserId(), userId)) {
            if (ComplaintStatus.REJECTED.name().equals(complaint.getStatus())) {
                complaint.setStatus(ComplaintStatus.PENDING.name());
                complaintMapper.updateById(complaint);
            } else if (ComplaintStatus.RESOLVED.name().equals(complaint.getStatus())) {
                complaint.setStatus(ComplaintStatus.PROCESSING.name());
                complaintMapper.updateById(complaint);
            }
        }
        return reply;
    }

    @Override
    @Transactional
    public void deleteComplaint(Long userId, Long complaintId) {
        TouristComplaint complaint = requireComplaint(complaintId);
        if (!Objects.equals(complaint.getUserId(), userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除该投诉");
        }
        // 彻底删除：回复、审批流/节点、审批记录、投诉本体（顺序先子后父）
        replyMapper.delete(new LambdaQueryWrapper<ComplaintReply>().eq(ComplaintReply::getComplaintId, complaintId));
        approvalService.deleteForSubject(SUBJECT_TYPE, complaintId);
        complaintMapper.hardDeleteById(complaintId);
    }

    @Override
    public PageResult<TouristComplaint> myComplaints(Long userId, int page, int pageSize) {
        LambdaQueryWrapper<TouristComplaint> wrapper = new LambdaQueryWrapper<TouristComplaint>()
                .eq(TouristComplaint::getUserId, userId)
                .orderByAsc(TouristComplaint::getCreateTime);
        IPage<TouristComplaint> iPage = complaintMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return PageResult.of(iPage.getTotal(), enrich(iPage.getRecords()));
    }

    @Override
    public ComplaintDetailResponse detail(Long id) {
        TouristComplaint complaint = requireComplaint(id);
        return toDetail(complaint);
    }

    @Override
    @Transactional
    public void approve(Long approverId, Long complaintId, String comment) {
        TouristComplaint complaint = requireComplaint(complaintId);
        ensureStatus(complaint, ComplaintStatus.PENDING);
        complaint.setStatus(ComplaintStatus.APPROVED.name());
        complaint.setIsPublished(1);
        complaintMapper.updateById(complaint);
        approvalService.recordAction(SUBJECT_TYPE, complaintId, complaint.getCurrentNodeId(), approverId,
                ApprovalAction.APPROVE, comment);
        notificationService.sendNotification(complaint.getUserId(), "投诉已通过",
                "您提交的投诉已审批通过。", NotificationType.COMPLAINT_APPROVED.name(), complaintId);
    }

    @Override
    @Transactional
    public void reject(Long approverId, Long complaintId, String comment) {
        TouristComplaint complaint = requireComplaint(complaintId);
        ensureStatus(complaint, ComplaintStatus.PENDING);
        complaint.setStatus(ComplaintStatus.REJECTED.name());
        complaint.setIsPublished(0);
        complaintMapper.updateById(complaint);
        approvalService.recordAction(SUBJECT_TYPE, complaintId, complaint.getCurrentNodeId(), approverId,
                ApprovalAction.REJECT, comment);
        notificationService.sendNotification(complaint.getUserId(), "投诉未通过",
                comment == null || comment.isEmpty() ? "您提交的投诉未通过审批。" : "未通过原因：" + comment,
                NotificationType.COMPLAINT_REJECTED.name(), complaintId);
    }

    @Override
    @Transactional
    public void assign(Long complaintId, Long handlerId) {
        TouristComplaint complaint = requireComplaint(complaintId);
        ensureStatus(complaint, ComplaintStatus.APPROVED);
        complaint.setHandlerId(handlerId);
        complaint.setStatus(ComplaintStatus.PROCESSING.name());
        complaint.setAssignTime(LocalDateTime.now());
        complaintMapper.updateById(complaint);
        notificationService.sendNotification(handlerId, "投诉已分派给您",
                "您有新的投诉待处理，请及时处理。", NotificationType.COMPLAINT_ASSIGNED.name(), complaintId);
    }

    @Override
    public PageResult<TouristComplaint> pendingByHandler(Long handlerId, int page, int pageSize) {
        LambdaQueryWrapper<TouristComplaint> wrapper = new LambdaQueryWrapper<TouristComplaint>()
                .eq(TouristComplaint::getHandlerId, handlerId)
                .eq(TouristComplaint::getStatus, ComplaintStatus.PROCESSING.name())
                .orderByAsc(TouristComplaint::getAssignTime);
        IPage<TouristComplaint> iPage = complaintMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return PageResult.of(iPage.getTotal(), enrich(iPage.getRecords()));
    }

    @Override
    @Transactional
    public ComplaintReply process(Long handlerId, Long complaintId, String result) {
        TouristComplaint complaint = requireComplaint(complaintId);
        ensureStatus(complaint, ComplaintStatus.PROCESSING);
        if (!Objects.equals(complaint.getHandlerId(), handlerId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权处理该投诉");
        }
        complaint.setStatus(ComplaintStatus.RESOLVED.name());
        complaint.setResult(result);
        complaint.setProcessTime(LocalDateTime.now());
        complaintMapper.updateById(complaint);
        // 生成处理人回复（承载处理意见与附件，呈现在「回复记录」处理人位置）
        ComplaintReply reply = new ComplaintReply();
        reply.setComplaintId(complaintId);
        reply.setUserId(handlerId);
        reply.setContent(result);
        replyMapper.insert(reply);
        notificationService.sendNotification(complaint.getUserId(), "投诉已处理",
                "您的投诉已处理完毕，请确认处理意见。", NotificationType.COMPLAINT_RESOLVED.name(), complaintId);
        return reply;
    }

    @Override
    @Transactional
    public void confirm(Long touristId, Long complaintId) {
        TouristComplaint complaint = requireComplaint(complaintId);
        ensureStatus(complaint, ComplaintStatus.RESOLVED);
        if (!Objects.equals(complaint.getUserId(), touristId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权确认该投诉");
        }
        complaint.setStatus(ComplaintStatus.CONFIRMED.name());
        complaint.setConfirmTime(LocalDateTime.now());
        complaintMapper.updateById(complaint);
    }

    @Override
    @Transactional
    public void close(Long complaintId) {
        TouristComplaint complaint = requireComplaint(complaintId);
        ensureStatus(complaint, ComplaintStatus.CONFIRMED);
        complaint.setStatus(ComplaintStatus.CLOSED.name());
        complaint.setCloseTime(LocalDateTime.now());
        complaintMapper.updateById(complaint);
        notificationService.sendNotification(complaint.getUserId(), "投诉已结案",
                "您的投诉已结案，感谢您的配合。", NotificationType.COMPLAINT_CLOSED.name(), complaintId);
    }

    @Override
    @Transactional
    public void rate(Long touristId, Long complaintId, Integer rating) {
        TouristComplaint complaint = requireComplaint(complaintId);
        ensureStatus(complaint, ComplaintStatus.CLOSED);
        if (!Objects.equals(complaint.getUserId(), touristId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权评价该投诉");
        }
        complaint.setRating(rating);
        complaintMapper.updateById(complaint);
    }

    @Override
    public PageResult<TouristComplaint> pendingForApproval(int page, int pageSize) {
        LambdaQueryWrapper<TouristComplaint> wrapper = new LambdaQueryWrapper<TouristComplaint>()
                .eq(TouristComplaint::getStatus, ComplaintStatus.PENDING.name())
                .orderByAsc(TouristComplaint::getCreateTime);
        IPage<TouristComplaint> iPage = complaintMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return PageResult.of(iPage.getTotal(), enrich(iPage.getRecords()));
    }

    @Override
    public List<TouristComplaint> approvedList() {
        List<TouristComplaint> list = complaintMapper.selectList(new LambdaQueryWrapper<TouristComplaint>()
                .eq(TouristComplaint::getStatus, ComplaintStatus.APPROVED.name())
                .orderByAsc(TouristComplaint::getCreateTime));
        return enrich(list);
    }

    @Override
    public List<TouristComplaint> confirmedList() {
        List<TouristComplaint> list = complaintMapper.selectList(new LambdaQueryWrapper<TouristComplaint>()
                .eq(TouristComplaint::getStatus, ComplaintStatus.CONFIRMED.name())
                .orderByAsc(TouristComplaint::getConfirmTime));
        return enrich(list);
    }

    // ---------------- helpers ----------------

    private TouristComplaint requireComplaint(Long id) {
        TouristComplaint complaint = complaintMapper.selectById(id);
        if (complaint == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "投诉不存在");
        }
        return complaint;
    }

    private void ensureStatus(TouristComplaint complaint, ComplaintStatus expected) {
        if (!expected.name().equals(complaint.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "投诉状态不允许该操作");
        }
    }

    private ComplaintDetailResponse toDetail(TouristComplaint complaint) {
        Map<Long, String> names = nameMap();

        ComplaintDetailResponse dto = new ComplaintDetailResponse();
        dto.setId(complaint.getId());
        dto.setUserId(complaint.getUserId());
        dto.setUserName(names.getOrDefault(complaint.getUserId(), ""));
        dto.setContent(complaint.getContent());
        dto.setStatus(complaint.getStatus());
        dto.setIsPublished(complaint.getIsPublished());
        dto.setHandlerId(complaint.getHandlerId());
        dto.setHandlerName(complaint.getHandlerId() == null ? null : names.getOrDefault(complaint.getHandlerId(), ""));
        dto.setResult(complaint.getResult());
        dto.setRating(complaint.getRating());
        dto.setAssignTime(complaint.getAssignTime());
        dto.setProcessTime(complaint.getProcessTime());
        dto.setConfirmTime(complaint.getConfirmTime());
        dto.setCloseTime(complaint.getCloseTime());
        dto.setCreateTime(complaint.getCreateTime());

        List<ComplaintReply> replies = replyMapper.selectList(new LambdaQueryWrapper<ComplaintReply>()
                .eq(ComplaintReply::getComplaintId, complaint.getId())
                .orderByAsc(ComplaintReply::getCreateTime));
        replies.forEach(r -> {
            r.setUserName(names.getOrDefault(r.getUserId(), ""));
            r.setAttachments(fileService.getFilesByRelated("COMPLAINT_REPLY", r.getId()));
        });
        dto.setReplies(replies);

        dto.setTimeline(approvalService.getTimeline(SUBJECT_TYPE, complaint.getId()));
        dto.setAttachments(fileService.getFilesByRelated(SUBJECT_TYPE, complaint.getId()));
        return dto;
    }

    private List<TouristComplaint> enrich(List<TouristComplaint> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, String> names = nameMap();
        list.forEach(c -> {
            c.setUserName(names.getOrDefault(c.getUserId(), ""));
            if (c.getHandlerId() != null) {
                c.setHandlerName(names.getOrDefault(c.getHandlerId(), ""));
            }
        });
        return list;
    }

    private Map<Long, String> nameMap() {
        List<User> users = userMapper.selectList(null);
        return users.stream().collect(Collectors.toMap(User::getId, User::getRealName, (a, b) -> a));
    }
}
