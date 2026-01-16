import axios, { AxiosError, type InternalAxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types/api'

// ==================== Axios 实例配置 ====================
const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// ==================== 请求拦截器 ====================
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 从 localStorage 读取 token
    // 注：在请求拦截器中直接从 localStorage 读取是安全的
    // 因为这是唯一的数据持久化源，userStore 也会从这里初始化
    const token = localStorage.getItem('access_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// ==================== 响应拦截器 ====================
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { code, message } = response.data

    // 成功响应：code = 200
    if (code === 200) {
      return response
    }

    // 业务错误
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message || '请求失败'))
  },
  (error: AxiosError<ApiResponse>) => {
    // 错误处理：三种互斥情况
    if (error.response) {
      // 服务器返回了响应（4xx, 5xx）
      const { status, data } = error.response

      switch (status) {
        case 401:
          ElMessage.error('未授权，请重新登录')
          localStorage.removeItem('access_token')
          // 避免在登录页重复跳转
          if (window.location.pathname !== '/login') {
            window.location.href = '/login'
          }
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          ElMessage.error('请求资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || `请求失败 (${status})`)
      }
    } else if (error.request) {
      // 请求发出了但没收到响应（网络断开、服务器挂了）
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      // 请求配置出错
      ElMessage.error(error.message || '请求配置错误')
    }

    return Promise.reject(error)
  }
)

// ==================== 便捷方法 ====================

/**
 * GET 请求
 */
export function get<T = any>(url: string, config?: InternalAxiosRequestConfig): Promise<ApiResponse<T>> {
  return request.get<ApiResponse<T>>(url, config).then((res) => res.data)
}

/**
 * POST 请求
 */
export function post<T = any>(
  url: string,
  data?: any,
  config?: InternalAxiosRequestConfig
): Promise<ApiResponse<T>> {
  return request.post<ApiResponse<T>>(url, data, config).then((res) => res.data)
}

/**
 * PUT 请求
 */
export function put<T = any>(
  url: string,
  data?: any,
  config?: InternalAxiosRequestConfig
): Promise<ApiResponse<T>> {
  return request.put<ApiResponse<T>>(url, data, config).then((res) => res.data)
}

/**
 * DELETE 请求
 */
export function del<T = any>(url: string, config?: InternalAxiosRequestConfig): Promise<ApiResponse<T>> {
  return request.delete<ApiResponse<T>>(url, config).then((res) => res.data)
}

export default request
