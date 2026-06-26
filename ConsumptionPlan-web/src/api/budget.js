import request from '@/utils/request'

/** 保存/更新预算 */
export function saveBudget(data) {
  return request.post('/api/budget', data)
}

/** 获取某月预算 */
export function getBudget(month) {
  return request.get('/api/budget', { params: { month } })
}

/** 获取预算历史 */
export const getBudgetHistory = () => request.get('/api/budget/history')
