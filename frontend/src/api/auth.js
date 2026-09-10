import request from '@/utils/request'

export function register(data) {
  return request.post('/auth/register', data)
}

export function login(data) {
  return request.post('/auth/login', data)
}

export function getCurrentUser() {
  return request.get('/auth/current-user')
}

export function changePassword(data) {
  return request.put('/auth/change-password', data)
}

export function forgotPassword(data) {
  return request.post('/auth/forgot-password', data)
}

export function resetPassword(data) {
  return request.post('/auth/reset-password', data)
}
