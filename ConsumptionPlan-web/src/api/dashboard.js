import request from '@/utils/request'

/** 获取仪表盘数据 */
export function getDashboard() {
  return request.get('/api/dashboard')
}
