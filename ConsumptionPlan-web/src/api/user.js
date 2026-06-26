import request from '@/utils/request'

/** 获取用户信息 */
export function getUserInfo() {
  return request.get('/api/user/info')
}

/** 更新用户信息 */
export function updateUserInfo(data) {
  return request.put('/api/user/info', data)
}

/** 修改密码 */
export const changePassword = (data) => request.put('/api/user/password', data)
