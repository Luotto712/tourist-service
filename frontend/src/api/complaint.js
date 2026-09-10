import request from '@/utils/request'

export function submitComplaint(data) {
  return request.post('/complaints', data)
}

export function replyComplaint(id, data) {
  return request.post(`/complaints/${id}/reply`, data)
}

export function deleteComplaint(id) {
  return request.delete(`/complaints/${id}`)
}

export function getMyComplaints(params) {
  return request.get('/complaints/mine', { params })
}

export function getComplaint(id) {
  return request.get(`/complaints/${id}`)
}

export function approveComplaint(id, data) {
  return request.put(`/complaints/${id}/approve`, data)
}

export function rejectComplaint(id, data) {
  return request.put(`/complaints/${id}/reject`, data)
}

export function assignComplaint(id, data) {
  return request.put(`/complaints/${id}/assign`, data)
}

export function getApprovedComplaints() {
  return request.get('/complaints/approved')
}

export function getConfirmedComplaints() {
  return request.get('/complaints/confirmed')
}

export function getPendingComplaints(params) {
  return request.get('/complaints/pending', { params })
}

export function getForApproval(params) {
  return request.get('/complaints/for-approval', { params })
}

export function processComplaint(id, data) {
  return request.post(`/complaints/${id}/process`, data)
}

export function confirmComplaint(id) {
  return request.put(`/complaints/${id}/confirm`)
}

export function closeComplaint(id) {
  return request.put(`/complaints/${id}/close`)
}

export function rateComplaint(id, data) {
  return request.post(`/complaints/${id}/rate`, data)
}
