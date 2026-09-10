package com.tourist.service;

import com.tourist.common.BusinessException;
import com.tourist.dto.request.ComplaintReplyRequest;
import com.tourist.dto.request.ComplaintSubmitRequest;
import com.tourist.entity.ApprovalNode;
import com.tourist.entity.ComplaintReply;
import com.tourist.entity.TouristComplaint;
import com.tourist.entity.User;
import com.tourist.enums.ApprovalAction;
import com.tourist.mapper.ComplaintReplyMapper;
import com.tourist.mapper.TouristComplaintMapper;
import com.tourist.mapper.UserMapper;
import com.tourist.service.impl.ComplaintServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 投诉状态机（单元，Mockito）。验证公共接口 ComplaintService 的行为：
 * 合法迁移、非法状态守卫、非本人越权守卫、以及每次动作对应的通知/审批记录。
 */
@ExtendWith(MockitoExtension.class)
class ComplaintServiceImplTest {

    @Mock private TouristComplaintMapper complaintMapper;
    @Mock private ComplaintReplyMapper replyMapper;
    @Mock private UserMapper userMapper;
    @Mock private ApprovalService approvalService;
    @Mock private NotificationService notificationService;
    @Mock private FileService fileService;

    @InjectMocks private ComplaintServiceImpl service;

    private TouristComplaint complaint(Long id, String status, Long userId, Long handlerId) {
        TouristComplaint c = new TouristComplaint();
        c.setId(id);
        c.setStatus(status);
        c.setUserId(userId);
        c.setHandlerId(handlerId);
        c.setIsPublished(0);
        c.setCurrentNodeId(5L);
        return c;
    }

    private User approver() {
        User u = new User();
        u.setId(2L);
        u.setRole("APPROVER");
        u.setStatus(1);
        return u;
    }

    @Test
    void submit_createsPending_flowAndNotifiesApprovers() {
        when(complaintMapper.insert(any())).thenAnswer(inv -> { ((TouristComplaint) inv.getArgument(0)).setId(7L); return 1; });
        when(userMapper.selectList(any())).thenReturn(List.of(approver()));
        ApprovalNode node = new ApprovalNode();
        node.setId(9L);
        when(approvalService.createFlow(eq("COMPLAINT"), eq(7L), eq("APPROVER"), eq("投诉审批"))).thenReturn(node);

        TouristComplaint result = service.submit(100L, new ComplaintSubmitRequest());

        result.setContent(null); // not asserted on content
        ArgumentCaptor<TouristComplaint> cap = ArgumentCaptor.forClass(TouristComplaint.class);
        verify(complaintMapper, atLeast(1)).updateById(cap.capture());
        TouristComplaint updated = cap.getValue();
        assertEquals("PENDING", updated.getStatus());
        assertEquals(9L, updated.getCurrentNodeId());
        verify(notificationService).sendNotification(eq(2L), eq("新投诉待审批"), anyString(), eq("COMPLAINT_SUBMITTED"), eq(7L));
    }

    @Test
    void approve_movesPendingToApproved_andPublishesAndNotifies() {
        when(complaintMapper.selectById(1L)).thenReturn(complaint(1L, "PENDING", 100L, null));

        service.approve(10L, 1L, "同意");

        ArgumentCaptor<TouristComplaint> cap = ArgumentCaptor.forClass(TouristComplaint.class);
        verify(complaintMapper).updateById(cap.capture());
        assertEquals("APPROVED", cap.getValue().getStatus());
        assertEquals(1, cap.getValue().getIsPublished());
        verify(approvalService).recordAction("COMPLAINT", 1L, 5L, 10L, ApprovalAction.APPROVE, "同意");
        verify(notificationService).sendNotification(eq(100L), eq("投诉已通过"), anyString(), eq("COMPLAINT_APPROVED"), eq(1L));
    }

    @Test
    void reject_movesPendingToRejected() {
        when(complaintMapper.selectById(1L)).thenReturn(complaint(1L, "PENDING", 100L, null));

        service.reject(10L, 1L, "不属实");

        ArgumentCaptor<TouristComplaint> cap = ArgumentCaptor.forClass(TouristComplaint.class);
        verify(complaintMapper).updateById(cap.capture());
        assertEquals("REJECTED", cap.getValue().getStatus());
        assertEquals(0, cap.getValue().getIsPublished());
        verify(approvalService).recordAction("COMPLAINT", 1L, 5L, 10L, ApprovalAction.REJECT, "不属实");
    }

    @Test
    void approve_onWrongStatus_throws() {
        when(complaintMapper.selectById(1L)).thenReturn(complaint(1L, "PROCESSING", 100L, null));

        assertThrows(BusinessException.class, () -> service.approve(10L, 1L, "同意"));
        verify(complaintMapper, never()).updateById(any());
        verify(approvalService, never()).recordAction(anyString(), anyLong(), anyLong(), anyLong(), any(), any());
    }

    @Test
    void assign_movesApprovedToProcessing_andSetsHandler() {
        when(complaintMapper.selectById(1L)).thenReturn(complaint(1L, "APPROVED", 100L, null));

        service.assign(1L, 44L);

        ArgumentCaptor<TouristComplaint> cap = ArgumentCaptor.forClass(TouristComplaint.class);
        verify(complaintMapper).updateById(cap.capture());
        assertEquals("PROCESSING", cap.getValue().getStatus());
        assertEquals(44L, cap.getValue().getHandlerId());
        verify(notificationService).sendNotification(eq(44L), eq("投诉已分派给您"), anyString(), eq("COMPLAINT_ASSIGNED"), eq(1L));
    }

    @Test
    void process_requiresAssignedHandler() {
        when(complaintMapper.selectById(1L)).thenReturn(complaint(1L, "PROCESSING", 100L, 44L));

        service.process(44L, 1L, "已处理");

        ArgumentCaptor<TouristComplaint> cap = ArgumentCaptor.forClass(TouristComplaint.class);
        verify(complaintMapper).updateById(cap.capture());
        assertEquals("RESOLVED", cap.getValue().getStatus());
        assertEquals("已处理", cap.getValue().getResult());
    }

    @Test
    void process_byWrongHandler_throwsForbidden() {
        when(complaintMapper.selectById(1L)).thenReturn(complaint(1L, "PROCESSING", 100L, 44L));

        assertThrows(BusinessException.class, () -> service.process(99L, 1L, "已处理"));
        verify(complaintMapper, never()).updateById(any());
    }

    @Test
    void confirm_ownerOnly() {
        when(complaintMapper.selectById(1L)).thenReturn(complaint(1L, "RESOLVED", 100L, 44L));

        service.confirm(100L, 1L);

        ArgumentCaptor<TouristComplaint> cap = ArgumentCaptor.forClass(TouristComplaint.class);
        verify(complaintMapper).updateById(cap.capture());
        assertEquals("CONFIRMED", cap.getValue().getStatus());
    }

    @Test
    void confirm_nonOwner_throwsForbidden() {
        when(complaintMapper.selectById(1L)).thenReturn(complaint(1L, "RESOLVED", 100L, 44L));

        assertThrows(BusinessException.class, () -> service.confirm(999L, 1L));
        verify(complaintMapper, never()).updateById(any());
    }

    @Test
    void close_movesConfirmedToClosed() {
        when(complaintMapper.selectById(1L)).thenReturn(complaint(1L, "CONFIRMED", 100L, 44L));

        service.close(1L);

        ArgumentCaptor<TouristComplaint> cap = ArgumentCaptor.forClass(TouristComplaint.class);
        verify(complaintMapper).updateById(cap.capture());
        assertEquals("CLOSED", cap.getValue().getStatus());
        verify(notificationService).sendNotification(eq(100L), eq("投诉已结案"), anyString(), eq("COMPLAINT_CLOSED"), eq(1L));
    }

    @Test
    void rate_closedOwner_setsRating() {
        when(complaintMapper.selectById(1L)).thenReturn(complaint(1L, "CLOSED", 100L, 44L));

        service.rate(100L, 1L, 5);

        ArgumentCaptor<TouristComplaint> cap = ArgumentCaptor.forClass(TouristComplaint.class);
        verify(complaintMapper).updateById(cap.capture());
        assertEquals(5, cap.getValue().getRating());
    }

    @Test
    void rate_nonOwner_throwsForbidden() {
        when(complaintMapper.selectById(1L)).thenReturn(complaint(1L, "CLOSED", 100L, 44L));

        assertThrows(BusinessException.class, () -> service.rate(999L, 1L, 5));
        verify(complaintMapper, never()).updateById(any());
    }

    @Test
    void reply_byOwnerAfterReject_reopensToPending() {
        when(complaintMapper.selectById(1L)).thenReturn(complaint(1L, "REJECTED", 100L, null));
        when(replyMapper.insert(any())).thenAnswer(inv -> { ((com.tourist.entity.ComplaintReply) inv.getArgument(0)).setId(50L); return 1; });

        service.reply(100L, 1L, new ComplaintReplyRequest());

        ArgumentCaptor<TouristComplaint> cap = ArgumentCaptor.forClass(TouristComplaint.class);
        verify(complaintMapper).updateById(cap.capture());
        assertEquals("PENDING", cap.getValue().getStatus());
    }

    @Test
    void reply_byOwnerAfterResolved_reopensToProcessing() {
        when(complaintMapper.selectById(1L)).thenReturn(complaint(1L, "RESOLVED", 100L, 44L));
        when(replyMapper.insert(any())).thenAnswer(inv -> { ((com.tourist.entity.ComplaintReply) inv.getArgument(0)).setId(50L); return 1; });

        service.reply(100L, 1L, new ComplaintReplyRequest());

        ArgumentCaptor<TouristComplaint> cap = ArgumentCaptor.forClass(TouristComplaint.class);
        verify(complaintMapper).updateById(cap.capture());
        assertEquals("PROCESSING", cap.getValue().getStatus());
    }

    @Test
    void reply_byApprover_succeedsWithoutStatusChange() {
        when(complaintMapper.selectById(1L)).thenReturn(complaint(1L, "RESOLVED", 100L, 44L));
        when(replyMapper.insert(any())).thenAnswer(inv -> { ((com.tourist.entity.ComplaintReply) inv.getArgument(0)).setId(50L); return 1; });

        service.reply(2L, 1L, new ComplaintReplyRequest()); // approver (非 owner / 非 handler)

        ArgumentCaptor<ComplaintReply> cap = ArgumentCaptor.forClass(ComplaintReply.class);
        verify(replyMapper).insert(cap.capture());
        assertEquals(2L, cap.getValue().getUserId());
        verify(complaintMapper, never()).updateById(any());
    }


    @Test
    void delete_byOwner_hardDeletesAndCascades() {
        when(complaintMapper.selectById(1L)).thenReturn(complaint(1L, "PENDING", 100L, null));

        service.deleteComplaint(100L, 1L);

        verify(complaintMapper).hardDeleteById(1L);
        verify(replyMapper).delete(any());
        verify(approvalService).deleteForSubject("COMPLAINT", 1L);
    }

    @Test
    void delete_byNonOwner_throwsForbidden() {
        when(complaintMapper.selectById(1L)).thenReturn(complaint(1L, "PENDING", 100L, null));

        assertThrows(BusinessException.class, () -> service.deleteComplaint(999L, 1L));
        verify(complaintMapper, never()).hardDeleteById(any(Long.class));
    }


    @Test
    void process_createsHandlerReply() {
        when(complaintMapper.selectById(1L)).thenReturn(complaint(1L, "PROCESSING", 100L, 44L));
        when(replyMapper.insert(any())).thenAnswer(inv -> { ((com.tourist.entity.ComplaintReply) inv.getArgument(0)).setId(60L); return 1; });

        com.tourist.entity.ComplaintReply r = service.process(44L, 1L, "已处理");

        assertEquals(60L, r.getId());
        assertEquals(44L, r.getUserId());
        assertEquals("已处理", r.getContent());
    }

}
