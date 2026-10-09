import request from '@/utils/request'
import type { ApiResult } from '@/types/api'

/**
 * 统计接口的返回类型
 * 后端是 Map<String, Long>（不是 DTO）—— 键是动态的统计项名，值是数量
 * 所以只能用 Record<string, number>，写不出更精确的
 */
export type StatsMap = Record<string, number>

export function getOverview() {
  return request.get<ApiResult<StatsMap>>('/statistics/overview')
}

export function getComplaintStatus() {
  return request.get<ApiResult<StatsMap>>('/statistics/complaint-status')
}

export function getEmergencyStatus() {
  return request.get<ApiResult<StatsMap>>('/statistics/emergency-status')
}
