package com.tourist.enums;

/**
 * 游客投诉状态机（PRD §3.1 / docs/CONTEXT.md「投诉状态」）。
 * 顺序：PENDING → APPROVED → PROCESSING → RESOLVED → CONFIRMED → CLOSED；REJECTED 为待审批的特例分支。
 * 结案后才开放评分（rating）。
 */
public enum ComplaintStatus {
    PENDING,
    APPROVED,
    REJECTED,
    PROCESSING,
    RESOLVED,
    CONFIRMED,
    CLOSED
}
