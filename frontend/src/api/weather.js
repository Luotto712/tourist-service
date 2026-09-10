import request from '@/utils/request'

export function getWeather() {
  return request.get('/weather')
}

export function getRoad() {
  return request.get('/road-conditions')
}
