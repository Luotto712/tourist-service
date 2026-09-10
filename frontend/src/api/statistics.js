import request from '@/utils/request'

export function getOverview() {
  return request.get('/statistics/overview')
}

export function getComplaintStatus() {
  return request.get('/statistics/complaint-status')
}

export function getEmergencyStatus() {
  return request.get('/statistics/emergency-status')
}
