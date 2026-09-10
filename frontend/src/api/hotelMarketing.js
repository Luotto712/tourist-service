import request from '@/utils/request'

export function listMarketing(params) {
  return request.get('/hotel-marketing', { params })
}

export function createMarketing(data) {
  return request.post('/hotel-marketing', data)
}

export function updateMarketing(id, data) {
  return request.put(`/hotel-marketing/${id}`, data)
}

export function deleteMarketing(id) {
  return request.delete(`/hotel-marketing/${id}`)
}
