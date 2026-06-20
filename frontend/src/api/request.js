import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from '../utils/auth'
import router from '../router'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器：自动带 token
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理错误
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 业务层错误
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      // token 过期
      if (res.code === 401) {
        removeToken()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        removeToken()
        router.push('/login')
      }
      ElMessage.error(error.response.data?.message || `请求错误 (${status})`)
    } else {
      ElMessage.error('网络异常，请检查连接')
    }
    return Promise.reject(error)
  }
)

export default service
