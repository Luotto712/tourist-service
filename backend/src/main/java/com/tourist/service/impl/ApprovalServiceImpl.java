package com.tourist.service.impl;

import com.tourist.entity.ApprovalFlow;
import com.tourist.entity.ApprovalNode;
import com.tourist.entity.ApprovalRecord;
import com.tourist.mapper.ApprovalFlowMapper;
import com.tourist.mapper.ApprovalNodeMapper;
import com.tourist.mapper.ApprovalRecordMapper;
import com.tourist.enums.ApprovalAction;
import com.tourist.service.ApprovalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ApprovalServiceImpl implements ApprovalService {

    @Resource
    private ApprovalFlowMapper approvalFlowMapper;

    @Resource
    private ApprovalNodeMapper approvalNodeMapper;

    @Resource
    private ApprovalRecordMapper approvalRecordMapper;

    @Override
    @Transactional
    public ApprovalNode createFlow(String subjectType, Long subjectId, String approverRole, String nodeName) {
        ApprovalFlow flow = approvalFlowMapper.selectBySubject(subjectType, subjectId);
        if (flow == null) {
            flow = new ApprovalFlow();
            flow.setSubjectType(subjectType);
            flow.setSubjectId(subjectId);
            approvalFlowMapper.insert(flow);
        }
        ApprovalNode node = new ApprovalNode();
        node.setFlowId(flow.getId());
        node.setNodeName(nodeName);
        node.setApproverRole(approverRole);
        node.setSortOrder(1);
        approvalNodeMapper.insert(node);
        return node;
    }

    @Override
    @Transactional
    public void recordAction(String subjectType, Long subjectId, Long nodeId, Long approverId, ApprovalAction action, String comment) {
        ApprovalRecord record = new ApprovalRecord();
        record.setSubjectType(subjectType);
        record.setSubjectId(subjectId);
        record.setNodeId(nodeId);
        record.setApproverId(approverId);
        record.setAction(action.name());
        record.setComment(comment);
        approvalRecordMapper.insert(record);
    }

    @Override
    public List<ApprovalRecord> getTimeline(String subjectType, Long subjectId) {
        return approvalRecordMapper.selectBySubject(subjectType, subjectId);
    }

    @Override
    @Transactional
    public void deleteForSubject(String subjectType, Long subjectId) {
        ApprovalFlow flow = approvalFlowMapper.selectBySubject(subjectType, subjectId);
        if (flow != null) {
            approvalNodeMapper.deleteByFlowId(flow.getId());
        }
        approvalFlowMapper.deleteBySubject(subjectType, subjectId);
        approvalRecordMapper.deleteBySubject(subjectType, subjectId);
    }
}
