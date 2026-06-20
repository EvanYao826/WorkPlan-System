import request from './request'

export function getResultList(params) {
  return request.get('/result/list', { params })
}

export function getResultDetail(id) {
  return request.get(`/result/${id}`)
}

export function createResult(data) {
  return request.post('/result', data)
}

export function approveResult(id, comment) {
  return request.put(`/result/${id}/approve`, { comment })
}

export function rejectResult(id, comment) {
  return request.put(`/result/${id}/reject`, { comment })
}

export function withdrawResult(id) {
  return request.put(`/result/${id}/withdraw`)
}
