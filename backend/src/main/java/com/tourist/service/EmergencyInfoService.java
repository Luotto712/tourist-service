package com.tourist.service;

import com.tourist.common.PageResult;
import com.tourist.dto.request.EmergencyInfoRequest;
import com.tourist.entity.EmergencyInfo;

import java.util.List;

public interface EmergencyInfoService {

    /** 发布应急信息（草稿，PENDING） */
    EmergencyInfo publish(Long publisherId, EmergencyInfoRequest request);

    /** 管理端分页查询（全状态） */
    PageResult<EmergencyInfo> adminList(int page, int pageSize);

    /** 游客可见列表：APPROVED 且在有效期内 */
    List<EmergencyInfo> publicList();

    /** 待审批列表（审批人员）：PENDING */
    List<EmergencyInfo> pendingList();

    /** 详情 */
    EmergencyInfo detail(Long id);

    /** 修改（回退为 PENDING 重新审批） */
    void update(Long id, EmergencyInfoRequest request);

    /** 删除（逻辑删除） */
    void delete(Long id);

    /** 审批通过：PENDING -> APPROVED，记录 publish_time */
    void approve(Long id);

    /** 审批驳回：PENDING -> REJECTED */
    void reject(Long id);
}
