package com.tourist.common;

/**
 * 集中式角色常量，避免角色字符串散落硬编码。
 * 角色声明见 PRD §1.3 与 docs/CONTEXT.md。
 */
public final class RoleConstants {

    /** 游客：查询信息、提交/跟踪/确认/评分投诉 */
    public static final String TOURIST = "TOURIST";

    /** 平台管理员：查询类数据维护、应急/营销发布、投诉分派与结案、数据看板 */
    public static final String PLATFORM_ADMIN = "PLATFORM_ADMIN";

    /** 审批人员：仅负责「是否批准/发布」——投诉审批、应急信息审批 */
    public static final String APPROVER = "APPROVER";

    /** 投诉处理人员：处理分派的投诉并提交结果、附件 */
    public static final String COMPLAINT_HANDLER = "COMPLAINT_HANDLER";

    /** 酒店管理员：录入本酒店房间实时预订信息 */
    public static final String HOTEL_ADMIN = "HOTEL_ADMIN";

    private RoleConstants() {
    }
}
