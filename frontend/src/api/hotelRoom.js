import request from '@/utils/request'

export function listRooms(params) {
  return request.get('/hotel-rooms', { params })
}

export function getUserRooms(params) {
  return request.get('/hotel-rooms', { params })
}

export function upsertRoom(data) {
  return request.put('/hotel-rooms', data)
}
