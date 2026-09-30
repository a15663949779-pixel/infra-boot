import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { getToken, removeToken } from '../utils/auth'
import { useUserStore } from '../stores/user'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

let isTokenExpired = false

service.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
    isTokenExpired = false
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body instanceof Blob) {
      return body
    }
    if (body.code !== 200) {
      if (body.code === 401) {
        if (!isTokenExpired) {
          isTokenExpired = true
          ElMessage.error(body.message || '登录已过期')
          const userStore = useUserStore()
          userStore.reset()
          router.replace('/login')
        }
        return Promise.reject(new Error(body.message || '请求失败'))
      }
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body.data
  },
  (error) => {
    if (error.response?.status === 401) {
      if (!isTokenExpired) {
        isTokenExpired = true
        ElMessage.error(error.response?.data?.message || '登录已过期')
        const userStore = useUserStore()
        userStore.reset()
        router.replace('/login')
      }
    } else {
      ElMessage.error(error.response?.data?.message || error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default service
