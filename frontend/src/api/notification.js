import request from './request'

export function getNotificationList(params) {
  return request.get('/notification/list', { params })
}

export function getUnreadCount() {
  return request.get('/notification/unread-count')
}

export function markAsRead(id) {
  return request.put(`/notification/${id}/read`)
}

export function markAllAsRead() {
  return request.put('/notification/read-all')
}

export function clearReadNotifications() {
  return request.delete('/notification/clear-read')
}
