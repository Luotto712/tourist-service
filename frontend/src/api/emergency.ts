import request from '@/utils/request'
import type { ApiResult, PageResult } from '@/types/api'
import type { EmergencyInfo, EmergencyForm } from '@/types/emergency'

/** 列表查询参数 */
export interface EmergencyListParams {
  page?: number
  pageSize?: number
}

export function publishEmergency(data: EmergencyForm) {
  return request.post<ApiResult<EmergencyInfo>>('/emergency-info', data)
}

export function getEmergencyAdmin(params: EmergencyListParams) {
  return request.get<ApiResult<PageResult<EmergencyInfo>>>('/emergency-info', { params })
}

export function getEmergencyPublic() {
  return request.get<ApiResult<EmergencyInfo[]>>('/emergency-info/public')
}

export function getEmergencyPending() {
  return request.get<ApiResult<EmergencyInfo[]>>('/emergency-info/pending')
}

export function getEmergency(id: number) {
  return request.get<ApiResult<EmergencyInfo>>(`/emergency-info/${id}`)
}

export function updateEmergency(id: number, data: EmergencyForm) {
  return request.put<ApiResult<string>>(`/emergency-info/${id}`, data)
}

export function deleteEmergency(id: number) {
  return request.delete<ApiResult<string>>(`/emergency-info/${id}`)
}

export function approveEmergency(id: number) {
  return request.put<ApiResult<string>>(`/emergency-info/${id}/approve`)
}

export function rejectEmergency(id: number) {
  return request.put<ApiResult<string>>(`/emergency-info/${id}/reject`)
}
