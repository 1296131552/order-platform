/**
 * 认证相关 API
 * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7
 */

import request from '@/utils/request'

// ==================== 类型定义 ====================

/** 登录参数 */
export interface LoginParams {
  /** 账号（用户名/邮箱/手机号） */
  account: string
  /** 密码 */
  password: string
}

/** 用户信息 */
export interface UserInfo {
  id: number
  username: string
  realName: string
  email?: string
  phone?: string
  avatar?: string
  status: string
}

/** 数据权限范围 */
export interface DataScope {
  type: string
  scopeIds?: number[]
}

/** 登录响应 - 完整格式 */
export interface LoginResult {
  /** 访问令牌 */
  token: string
  /** 令牌类型 */
  tokenType: string
  /** 过期时间（秒） */
  expiresIn: number
  /** 用户信息 */
  userInfo: UserInfo
  /** 角色列表 */
  roles: string[]
  /** 权限列表 */
  permissions: string[]
  /** 数据权限范围 */
  dataScope: DataScope
}

/** 修改密码参数 */
export interface ChangePasswordParams {
  /** 旧密码 */
  oldPassword: string
  /** 新密码 */
  newPassword: string
  /** 确认密码 */
  confirmPassword: string
}

/** 刷新Token响应 */
export interface RefreshTokenResult {
  token: string
  tokenType: string
  expiresIn: number
}

// ==================== API 函数 ====================

/**
 * 用户登录
 * POST /api/auth/login
 * Requirements: 1.1, 1.2
 */
export function login(data: LoginParams) {
  return request.post<LoginResult>('/auth/login', data)
}

/**
 * 用户登出
 * POST /api/auth/logout
 * Requirements: 1.3
 */
export function logout() {
  return request.post('/auth/logout')
}

/**
 * 获取当前用户信息
 * GET /api/auth/current
 * Requirements: 1.4
 */
export function getCurrentUser() {
  return request.get<LoginResult>('/auth/current')
}

/**
 * 刷新Token
 * POST /api/auth/refresh
 * Requirements: 1.5
 */
export function refreshToken() {
  return request.post<RefreshTokenResult>('/auth/refresh')
}

/**
 * 修改密码
 * PUT /api/auth/change-password
 * Requirements: 1.6
 */
export function changePassword(data: ChangePasswordParams) {
  return request.put('/auth/change-password', data)
}

/**
 * 重置用户密码（管理员操作）
 * PUT /api/auth/reset-password/{id}
 * Requirements: 1.7
 */
export function resetPassword(userId: number) {
  return request.put(`/auth/reset-password/${userId}`)
}

// ==================== 兼容旧接口（保持向后兼容） ====================

/**
 * 获取用户信息（兼容旧接口）
 * @deprecated 请使用 getCurrentUser
 */
export function getUserInfo() {
  return getCurrentUser().then(res => res.userInfo)
}
