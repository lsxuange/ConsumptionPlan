import request from '@/utils/request'

/** 获取通知列表 */
export function getNotifications() {
  return request.get('/api/notifications')
}

/** 全部标记已读 */
export function markAllRead() {
  return request.put('/api/notifications/read-all')
}

/** 获取未读数量 */
export function getUnreadCount() {
  return request.get('/api/notifications/unread-count')
}
