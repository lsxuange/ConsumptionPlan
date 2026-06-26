import request from '@/utils/request'

/** 签到 */
export function checkIn() {
  return request.post('/api/checkin')
}

/** 获取签到状态 */
export function getCheckInStatus() {
  return request.get('/api/checkin/status')
}

/** 获取某月打卡日期列表 */
export function getCheckedDates(month) {
  return request.get('/api/checkin/month', { params: { month } })
}
