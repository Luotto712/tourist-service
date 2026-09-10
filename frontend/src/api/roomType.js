import request from '@/utils/request'

export function listRoomTypes(params) {
  return request.get('/room-types', { params })
}

export function saveRoomType(data) {
  return request.post('/room-types', data)
}

export function deleteRoomType(id) {
  return request.delete(`/room-types/${id}`)
}
