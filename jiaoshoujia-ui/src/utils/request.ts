import axios, { type AxiosRequestConfig, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getToken, removeToken, removeRefreshToken } from '@/utils/auth'
import router from '@/router'

export interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
}

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '',
  timeout: 30000,
})

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

let isRelogin = false

service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data
    if (res.code === 200) {
      return res as any
    }
    if (res.code === 401) {
      if (!isRelogin) {
        isRelogin = true
        ElMessageBox.confirm('登录状态已过期，请重新登录', '系统提示', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning',
        })
          .then(() => {
            removeToken()
            removeRefreshToken()
            router.push('/login')
          })
          .finally(() => {
            isRelogin = false
          })
      }
      return Promise.reject(new Error(res.msg || '登录已过期'))
    }
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => {
    let message = error.message
    if (error.response) {
      switch (error.response.status) {
        case 400: message = '请求错误'; break
        case 403: message = '拒绝访问'; break
        case 404: message = '请求地址不存在'; break
        case 408: message = '请求超时'; break
        case 500: message = '服务器内部错误'; break
        case 502: message = '网关错误'; break
        case 503: message = '服务不可用'; break
        case 504: message = '网关超时'; break
        default: message = `连接错误${error.response.status}`
      }
    } else if (message === 'Network Error') {
      message = '网络异常，请检查网络连接'
    } else if (message.includes('timeout')) {
      message = '请求超时，请稍后重试'
    }
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export function get<T = any>(url: string, params?: Record<string, any>, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.get(url, { params, ...config })
}

export function post<T = any>(url: string, data?: Record<string, any>, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.post(url, data, config)
}

export function put<T = any>(url: string, data?: Record<string, any>, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.put(url, data, config)
}

export function del<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.delete(url, config)
}

export default service
