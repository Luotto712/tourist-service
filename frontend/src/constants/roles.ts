// 集中式角色常量与映射，替换各处硬编码的角色字符串
// 角色声明见 docs/CONTEXT.md 与 docs/PRD.md §1.3
export const ROLE = {
  TOURIST: 'TOURIST',
  PLATFORM_ADMIN: 'PLATFORM_ADMIN',
  APPROVER: 'APPROVER',
  COMPLAINT_HANDLER: 'COMPLAINT_HANDLER',
  HOTEL_ADMIN: 'HOTEL_ADMIN'
} as const
// ✏️ 导出 Role 类型，取 ROLE 所有【值】的联合
//    提示：typeof ROLE 拿到对象类型
//          keyof typeof ROLE 拿到键的联合（'TOURIST' | 'PLATFORM_ADMIN' | ...）
//          因为键名和值一样，所以直接用 keyof typeof ROLE 就行
export type Role = keyof typeof ROLE
// 角色 -> 中文名
export const roleLabel = {
  TOURIST: '游客',
  PLATFORM_ADMIN: '平台管理员',
  APPROVER: '审批人员',
  COMPLAINT_HANDLER: '投诉处理人员',
  HOTEL_ADMIN: '酒店管理员'
}

// 角色 -> el-tag 类型
export const roleType = {
  TOURIST: 'success',
  PLATFORM_ADMIN: 'danger',
  APPROVER: 'warning',
  COMPLAINT_HANDLER: 'info',
  HOTEL_ADMIN: 'primary'
}

// 下拉/筛选选项
export const roleOptions = Object.entries(roleLabel).map(([value, label]) => ({ value, label }))
