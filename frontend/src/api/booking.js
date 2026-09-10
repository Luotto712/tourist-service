import request from '@/utils/request'

export function getAvailability(params) {
  return request.get('/bookings/availability', { params })
}

export function createBooking(data) {
  return request.post('/bookings', data)
}

export function getMyBookings(params) {
  return request.get('/bookings/mine', { params })
}

export function cancelBooking(id) {
  return request.put(`/bookings/${id}/cancel`)
}

export function getRoomTypeBookings(params) {
  return request.get('/bookings/room-type', { params })
}
