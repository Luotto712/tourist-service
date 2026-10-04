import axios from 'axios'
import type { AxiosError, AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './auth'
import type { ApiResult } from '@/types/api'

// 我们的响应拦截器已经把 response.data 解包了，
// 所以运行时拿到的是 T，而不是 AxiosResponse<T>。
// 这里用一个自定义接口，把"解包后返回 T"这件事在类型上表达出来。
interface RequestInstance {
  get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T>

  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T>

  // ✏️ 补 put —— 签名和 post 一样
  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T>

  // ✏️ 补 delete —— 注意:它没有 data 参数,和 get 一样
  delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T>
}

const instance = axios.create({
  baseURL: '/api',
  timeout: 15000
})

instance.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

instance.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error: AxiosError<ApiResult>) => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        removeToken()
        window.location.href = '/login'
        return Promise.reject(error)
      }
      const message = data?.message || '请求失败，请稍后重试'
      ElMessage.error(message)
    } else if (error.message && error.message.includes('timeout')) {
      ElMessage.error('请求超时，请检查网络连接')
    } else {
      ElMessage.error('网络异常，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

const request = instance as unknown as RequestInstance
export default request
