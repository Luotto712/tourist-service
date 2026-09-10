import request from '@/utils/request'

export function getProfile() {
  return request.get('/users/profile')
}

export function updateProfile(data) {
  return request.put('/users/profile', data)
}

export function searchUsers(params) {
  return request.get('/users/search', { params })
}
