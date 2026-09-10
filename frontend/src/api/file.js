import request from '@/utils/request'

export function uploadFile(formData) {
  return request.post('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getDownloadUrl(id) {
  return request.get(`/files/${id}/download`)
}
