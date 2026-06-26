import request from '@/utils/request'

/** 生成报表 */
export function generateReport(month) {
  return request.post('/api/report/generate', null, { params: { month } })
}

/** 获取某月报表 */
export function getReport(month) {
  return request.get('/api/report', { params: { month } })
}

/** 获取报表列表 */
export function listReports() {
  return request.get('/api/reports')
}
