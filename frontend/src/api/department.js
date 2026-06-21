import request from './request'

/**
 * 获取部门树
 */
export function getDepartmentTree() {
  return request.get('/department/tree')
}

/**
 * 新增部门
 */
export function createDepartment(data) {
  return request.post('/department', data)
}

/**
 * 编辑部门
 */
export function updateDepartment(id, data) {
  return request.put(`/department/${id}`, data)
}

/**
 * 删除部门
 */
export function deleteDepartment(id) {
  return request.delete(`/department/${id}`)
}
