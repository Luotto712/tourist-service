import request from '@/utils/request'
import type { ApiResult, PageResult } from '@/types/api'
import type { UserProfileResponse } from '@/types/auth'

/** 用户搜索参数 */
export interface SearchUsersParams {
  keyword?: string // 都可以不传 —— 所以用 ?
  role?: string
  college?: string
  page?: number
  pageSize?: number
}

export function getProfile() {
  return request.get<ApiResult<UserProfileResponse>>('/users/profile')
}

export function updateProfile(data: Partial<UserProfileResponse>) {
  return request.put<ApiResult<string>>('/users/profile', data)
}

export function searchUsers(params: SearchUsersParams) {
  return request.get<ApiResult<PageResult<UserProfileResponse>>>('/users/search', { params })
}
