import request from '@/utils/request'

/** 获取分类列表，type 为整数：0=支出 1=收入 */
export function getCategories(type) {
  return request.get('/api/categories', { params: { type } })
}
