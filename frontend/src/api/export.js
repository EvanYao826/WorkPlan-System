import request from './request'

/**
 * 导出计划列表
 */
export function exportPlans() {
  return request.get('/export/plans', { responseType: 'blob' })
}

/**
 * 导出成果列表
 */
export function exportResults() {
  return request.get('/export/results', { responseType: 'blob' })
}
