package com.tourist.service;

import com.tourist.entity.ApprovalFlow;
import com.tourist.entity.ApprovalNode;
import com.tourist.entity.ApprovalRecord;
import com.tourist.enums.ApprovalAction;
import com.tourist.mapper.ApprovalFlowMapper;
import com.tourist.mapper.ApprovalNodeMapper;
import com.tourist.mapper.ApprovalRecordMapper;
import com.tourist.service.impl.ApprovalServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** 审批引擎（单元，Mockito）：createFlow / recordAction / getTimeline。 */
@ExtendWith(MockitoExtension.class)
class ApprovalServiceImplTest {

    @Mock private ApprovalFlowMapper approvalFlowMapper;
    @Mock private ApprovalNodeMapper approvalNodeMapper;
    @Mock private ApprovalRecordMapper approvalRecordMapper;

    @InjectMocks private ApprovalServiceImpl service;

    @Test
    void createFlow_createsFlowAndNode() {
        when(approvalFlowMapper.selectBySubject("COMPLAINT", 1L)).thenReturn(null);
        when(approvalFlowMapper.insert(any())).thenAnswer(inv -> { ((ApprovalFlow) inv.getArgument(0)).setId(3L); return 1; });

        ApprovalNode node = service.createFlow("COMPLAINT", 1L, "APPROVER", "投诉审批");

        assertEquals(3L, node.getFlowId());
        assertEquals("APPROVER", node.getApproverRole());
        assertEquals(1, node.getSortOrder());
    }

    @Test
    void createFlow_reusesExistingFlow() {
        ApprovalFlow existing = new ApprovalFlow();
        existing.setId(3L);
        when(approvalFlowMapper.selectBySubject("COMPLAINT", 1L)).thenReturn(existing);

        ApprovalNode node = service.createFlow("COMPLAINT", 1L, "APPROVER", "投诉审批");

        assertEquals(3L, node.getFlowId());
        verify(approvalFlowMapper, never()).insert(any());
    }

    @Test
    void recordAction_storesEnumName() {
        service.recordAction("COMPLAINT", 1L, 5L, 10L, ApprovalAction.APPROVE, "同意");

        ArgumentCaptor<ApprovalRecord> cap = ArgumentCaptor.forClass(ApprovalRecord.class);
        verify(approvalRecordMapper).insert(cap.capture());
        ApprovalRecord rec = cap.getValue();
        assertEquals("COMPLAINT", rec.getSubjectType());
        assertEquals(1L, rec.getSubjectId());
        assertEquals("APPROVE", rec.getAction());
        assertEquals("同意", rec.getComment());
    }

    @Test
    void getTimeline_returnsBySubject() {
        List<ApprovalRecord> records = List.of(new ApprovalRecord(), new ApprovalRecord());
        when(approvalRecordMapper.selectBySubject("COMPLAINT", 1L)).thenReturn(records);

        List<ApprovalRecord> result = service.getTimeline("COMPLAINT", 1L);
        assertEquals(records, result);
    }
}
