import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { getToken, removeToken } from '../utils/auth'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

service.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
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
      ElMessage.error(body.message || '请求失败')
      if (body.code === 401) {
        removeToken()
        router.replace('/login')
      }
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body.data
  },
  (error) => {
    ElMessage.error(error.response?.data?.message || error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default service
