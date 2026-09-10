import request from '@/utils/request'

export function publishEmergency(data) {
  return request.post('/emergency-info', data)
}

export function getEmergencyAdmin(params) {
  return request.get('/emergency-info', { params })
}

export function getEmergencyPublic() {
  return request.get('/emergency-info/public')
}

export function getEmergencyPending() {
  return request.get('/emergency-info/pending')
}

export function getEmergency(id) {
  return request.get(`/emergency-info/${id}`)
}

export function updateEmergency(id, data) {
  return request.put(`/emergency-info/${id}`, data)
}

export function deleteEmergency(id) {
  return request.delete(`/emergency-info/${id}`)
}

export function approveEmergency(id) {
  return request.put(`/emergency-info/${id}/approve`)
}

export function rejectEmergency(id) {
  return request.put(`/emergency-info/${id}/reject`)
}
