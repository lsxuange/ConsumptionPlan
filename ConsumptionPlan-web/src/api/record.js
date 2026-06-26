import request from '@/utils/request'

/** 添加消费记录 */
export function addRecord(data) {
  return request.post('/api/records', data)
}

/** 更新消费记录 */
export function updateRecord(id, data) {
  return request.put(`/api/records/${id}`, data)
}

/** 删除消费记录 */
export function deleteRecord(id) {
  return request.delete(`/api/records/${id}`)
}

/** 获取消费记录列表（支持查询参数） */
export function getRecords(params) {
  return request.get('/api/records', { params })
}
