package com.tourist.service;

import com.tourist.entity.ApprovalNode;
import com.tourist.entity.ApprovalRecord;
import com.tourist.enums.ApprovalAction;

import java.util.List;

/**
 * 领域无关的审批引擎：以 (subjectType, subjectId) 承载任意主体的单节点审批。
 * 目前用于游客投诉（subject_type='COMPLAINT'），未来可复用于其它需审批的模块。
 */
public interface ApprovalService {

    /**
     * 为主题创建（或复用）审批流，并返回首个审批节点。
     *
     * @param subjectType 主体类型，如 COMPLAINT
     * @param subjectId   主体ID
     * @param approverRole 审批人角色，如 APPROVER
     * @param nodeName     节点名称，如「投诉审批」
     * @return 首个审批节点
     */
    ApprovalNode createFlow(String subjectType, Long subjectId, String approverRole, String nodeName);

    /**
     * 记录一次审批动作（通过/拒绝）。
     */
    void recordAction(String subjectType, Long subjectId, Long nodeId, Long approverId, ApprovalAction action, String comment);

    /**
     * 查询某主体的审批时间线（按时间升序，含节点名/审批人名/审批人角色）。
     */
    List<ApprovalRecord> getTimeline(String subjectType, Long subjectId);

    /** 删除某主体的全部审批流/节点与审批记录（用于投诉彻底删除） */
    void deleteForSubject(String subjectType, Long subjectId);
}
