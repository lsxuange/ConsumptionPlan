import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/userStore'

let isRedirecting = false

const request = axios.create({
  baseURL: '/',
  timeout: 10000
})

// 请求拦截器：携带 token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理返回结果
request.interceptors.response.use(
  (response) => {
    const { code, message, data } = response.data
    if (code === 200) {
      return data
    }
    // 业务错误
    if (code === 401) {
      if (!isRedirecting) {
        isRedirecting = true
        const userStore = useUserStore()
        userStore.clearUser()
        ElMessage.error('登录已过期，请重新登录')
        router.push('/login').finally(() => {
          isRedirecting = false
        })
      }
      return Promise.reject(new Error(message || '登录已过期'))
    }
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message || '请求失败'))
  },
  (error) => {
    // HTTP 401 处理（与业务 401 共用防重复标志位）
    if (error.response?.status === 401) {
      if (!isRedirecting) {
        isRedirecting = true
        const userStore = useUserStore()
        userStore.clearUser()
        ElMessage.error('登录已过期，请重新登录')
        router.push('/login').finally(() => {
          isRedirecting = false
        })
      }
      return Promise.reject(error)
    }
    // 其他 HTTP 错误
    const message = error.response?.data?.message || error.message || '网络错误'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
