import request from '@/utils/request'

// 通用的目录资源 CRUD。resource 形如 'attractions' / 'hotels/star'。
export function listCatalog(resource, params) {
  return request.get(`/${resource}`, { params })
}

export function getCatalog(resource, id) {
  return request.get(`/${resource}/${id}`)
}

export function createCatalog(resource, data) {
  return request.post(`/${resource}`, data)
}

export function updateCatalog(resource, id, data) {
  return request.put(`/${resource}/${id}`, data)
}

export function deleteCatalog(resource, id) {
  return request.delete(`/${resource}/${id}`)
}
