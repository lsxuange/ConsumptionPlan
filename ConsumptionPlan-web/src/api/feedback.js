import request from '@/utils/request'

/** 提交反馈 */
export function submitFeedback(data) {
  return request.post('/api/feedback', data)
}

/** 获取我的反馈列表 */
export function getMyFeedback() {
  return request.get('/api/feedback/my')
}
