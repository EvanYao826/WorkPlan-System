import request from './request'

export function getPlanList(params) {
  return request.get('/plan/list', { params })
}

export function getPlanDetail(id) {
  return request.get(`/plan/${id}`)
}

export function createPlan(data) {
  return request.post('/plan', data)
}

export function submitPlan(id, approveLeaderId) {
  return request.put(`/plan/${id}/submit`, { approveLeaderId: String(approveLeaderId) })
}

export function approvePlan(id, comment) {
  return request.put(`/plan/${id}/approve`, { comment })
}

export function rejectPlan(id, comment) {
  return request.put(`/plan/${id}/reject`, { comment })
}

export function withdrawPlan(id) {
  return request.put(`/plan/${id}/withdraw`)
}
