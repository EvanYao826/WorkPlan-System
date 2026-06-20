import request from './request'

export function getPlanList(params) {
  return request.get('/plan/list', { params })
}

export function getPlanDetail(id) {
  return request.get(`/plan/${id}`)
}
