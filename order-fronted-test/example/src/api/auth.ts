/**
 * 认证相关 API
 */

import request from '@/utils/request'

/** 登录接口 */
export interface LoginParams {
  account: string  // 修改为 account，与后端保持一致
  password: string
}

/** 登录响应 */
export interface LoginResult {
  token: string
  userInfo: {
    id: number
    username: string
    realName: string
    email?: string
    phone?: string
    avatar?: string
    roles: string[]
  }
}

/**
 * 用户登录
 */
export function login(data: LoginParams) {
  return request.post<LoginResult>('/auth/login', data)
}

/**
 * 用户登出
 */
export function logout() {
  return request.post('/auth/logout')
}

/**
 * 获取当前用户信息
 */
export function getUserInfo() {
  return request.get<LoginResult['userInfo']>('/auth/user-info')
}

/**
 * 刷新token
 * 根据API文档，刷新token不需要传参数，从header中获取当前token
 */
export function refreshToken() {
  return request.post<string>('/auth/refresh-token')
}

/**
 * 修改密码
 */
export interface ChangePasswordParams {
  oldPassword: string
  newPassword: string
}

export function changePassword(data: ChangePasswordParams) {
  return request.put('/auth/change-password', data)
}
