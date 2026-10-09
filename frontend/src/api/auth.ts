// 把 api/auth.js 改名成 api/auth.ts
import request from '@/utils/request'
import type { ApiResult } from '@/types/api'
import type {
  LoginParams,
  LoginResponse,
  UserProfileResponse,
  RegisterParams,
  ChangePasswordParams,
  ForgotPasswordParams
} from '@/types/auth'

export function register(data: RegisterParams) {
  return request.post<ApiResult<string>>('/auth/register', data)
}

export function login(data: LoginParams) {
  return request.post<ApiResult<LoginResponse>>('/auth/login', data)
}

export function getCurrentUser() {
  return request.get<ApiResult<UserProfileResponse>>('/auth/current-user')
}

export function changePassword(data: ChangePasswordParams) {
  return request.put<ApiResult<string>>('/auth/change-password', data)
}

export function forgotPassword(data: ForgotPasswordParams) {
  return request.post<ApiResult<string>>('/auth/forgot-password', data)
}

export function resetPassword(data: Record<string, string>) {
  return request.post<ApiResult<string>>('/auth/reset-password', data)
}
