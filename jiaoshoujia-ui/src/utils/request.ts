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
    if (response.config.responseType === 'blob') {
      return response as any
    }
    const res = response.data
    if (res.code === 200) {
      if (res.data && typeof res.data === 'object' && !Array.isArray(res.data)) {
        return { ...res, ...res.data } as any
      }
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
        case 401:
          removeToken()
          removeRefreshToken()
          router.push('/login')
          message = '登录已过期，请重新登录'
          break
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

// 列表接口会额外返回 rows/total 等分页字段，用户接口返回 roles 等，
// 故在标准结构上叠加索引签名，既保留 data 的类型推断，又允许访问这些附加字段。
export type ApiResult<T = any> = ApiResponse<T> & Record<string, any>

export function get<T = any>(url: string, params?: Record<string, any>, config?: AxiosRequestConfig): Promise<ApiResult<T>> {
  return service.get(url, { params, ...config })
}

export function post<T = any>(url: string, data?: Record<string, any>, config?: AxiosRequestConfig): Promise<ApiResult<T>> {
  return service.post(url, data, config)
}

export function put<T = any>(url: string, data?: Record<string, any>, config?: AxiosRequestConfig): Promise<ApiResult<T>> {
  return service.put(url, data, config)
}

export function del<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResult<T>> {
  return service.delete(url, config)
}

/**
 * 下载文件（导出 Excel 等）。params 以查询参数形式提交，后端以 @ModelAttribute 绑定。
 */
export async function download(url: string, params?: Record<string, any>, fileName?: string): Promise<void> {
  const response: AxiosResponse<Blob> = await service.post(url, null, {
    params,
    responseType: 'blob',
  })
  const blob = new Blob([response.data], {
    type: (response.headers['content-type'] as string) || 'application/octet-stream',
  })
  let name = fileName || ''
  if (!name) {
    const disposition = response.headers['content-disposition'] as string | undefined
    const match = disposition && /filename=([^;]+)/i.exec(disposition)
    name = match ? decodeURIComponent(match[1].replace(/%20/g, ' ').trim()) : `export_${Date.now()}.xlsx`
  }
  const link = document.createElement('a')
  link.href = window.URL.createObjectURL(blob)
  link.download = name
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(link.href)
}

export default service
