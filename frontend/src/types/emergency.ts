// src/types/emergency.ts

/**
 * 应急信息状态
 * ⚠️ 这次用【字面量联合】而不是 string —— 和 ④ 的 Role 同一个手法
 */
export type EmergencyStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

/** 应急信息（对照后端 EmergencyInfo.java） */
export interface EmergencyInfo {
  id: number
  title: string
  content: string
  validFrom: string        // 后端 LocalDate → JSON 里是 'YYYY-MM-DD' 字符串
  validTo: string
  status: EmergencyStatus  // ← 精确到三个值
  publisherId: number
  publishTime: string
  createTime: string
  updateTime: string
  publisherName: string
}

/** 新增 / 编辑的请求体（对照 EmergencyInfoRequest.java） */
export interface EmergencyForm {
  title: string
  content: string
  validFrom: string
  validTo: string
}
