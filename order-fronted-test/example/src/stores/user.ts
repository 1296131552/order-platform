/**
 * 用户状态管理
 * 负责用户信息的存储和管理
 * Requirements: 1.2
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// ==================== 类型定义 ====================

/** 用户信息 */
export interface UserInfo {
  id: number
  username: string
  realName: string
  email?: string
  phone?: string
  avatar?: string
  status?: string
}

/** 数据权限范围 */
export interface DataScope {
  type: string
  scopeIds?: number[]
}

/** 完整的登录状态 */
export interface AuthState {
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
  dataScope?: DataScope
}

// ==================== 本地存储键名 ====================

const STORAGE_KEYS = {
  TOKEN: 'token',
  TOKEN_TYPE: 'tokenType',
  EXPIRES_IN: 'expiresIn',
  USER_INFO: 'userInfo',
  ROLES: 'roles',
  PERMISSIONS: 'permissions',
  DATA_SCOPE: 'dataScope'
}

// ==================== Store 定义 ====================

export const useUserStore = defineStore('user', () => {
  // ==================== 状态 ====================
  
  /** 访问令牌 */
  const token = ref<string>(localStorage.getItem(STORAGE_KEYS.TOKEN) || '')
  
  /** 令牌类型 */
  const tokenType = ref<string>(localStorage.getItem(STORAGE_KEYS.TOKEN_TYPE) || 'Bearer')
  
  /** 过期时间（秒） */
  const expiresIn = ref<number>(Number(localStorage.getItem(STORAGE_KEYS.EXPIRES_IN)) || 0)
  
  /** 用户信息 */
  const userInfo = ref<UserInfo | null>(
    localStorage.getItem(STORAGE_KEYS.USER_INFO) 
      ? JSON.parse(localStorage.getItem(STORAGE_KEYS.USER_INFO)!) 
      : null
  )
  
  /** 角色列表 */
  const roles = ref<string[]>(
    localStorage.getItem(STORAGE_KEYS.ROLES) 
      ? JSON.parse(localStorage.getItem(STORAGE_KEYS.ROLES)!) 
      : []
  )
  
  /** 权限列表 */
  const permissions = ref<string[]>(
    localStorage.getItem(STORAGE_KEYS.PERMISSIONS) 
      ? JSON.parse(localStorage.getItem(STORAGE_KEYS.PERMISSIONS)!) 
      : []
  )
  
  /** 数据权限范围 */
  const dataScope = ref<DataScope | null>(
    localStorage.getItem(STORAGE_KEYS.DATA_SCOPE) 
      ? JSON.parse(localStorage.getItem(STORAGE_KEYS.DATA_SCOPE)!) 
      : null
  )

  // ==================== 计算属性 ====================

  /** 是否已登录 */
  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)

  /** 获取完整的Authorization头 */
  const authHeader = computed(() => token.value ? `${tokenType.value} ${token.value}` : '')

  /** 检查是否有指定权限 */
  const hasPermission = (permission: string) => permissions.value.includes(permission)

  /** 检查是否有指定角色 */
  const hasRole = (role: string) => roles.value.includes(role)

  // ==================== 方法 ====================

  /**
   * 设置完整的登录信息
   * Requirements: 1.2 - 存储token、tokenType、expiresIn和完整的userInfo信息
   */
  function setLoginInfo(authState: AuthState) {
    // 设置状态
    token.value = authState.token
    tokenType.value = authState.tokenType || 'Bearer'
    expiresIn.value = authState.expiresIn
    userInfo.value = authState.userInfo
    roles.value = authState.roles || []
    permissions.value = authState.permissions || []
    dataScope.value = authState.dataScope || null

    // 持久化到本地存储
    localStorage.setItem(STORAGE_KEYS.TOKEN, authState.token)
    localStorage.setItem(STORAGE_KEYS.TOKEN_TYPE, authState.tokenType || 'Bearer')
    localStorage.setItem(STORAGE_KEYS.EXPIRES_IN, String(authState.expiresIn))
    localStorage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(authState.userInfo))
    localStorage.setItem(STORAGE_KEYS.ROLES, JSON.stringify(authState.roles || []))
    localStorage.setItem(STORAGE_KEYS.PERMISSIONS, JSON.stringify(authState.permissions || []))
    if (authState.dataScope) {
      localStorage.setItem(STORAGE_KEYS.DATA_SCOPE, JSON.stringify(authState.dataScope))
    }
  }

  /**
   * 更新Token（用于刷新Token）
   */
  function updateToken(newToken: string, newTokenType?: string, newExpiresIn?: number) {
    token.value = newToken
    localStorage.setItem(STORAGE_KEYS.TOKEN, newToken)
    
    if (newTokenType) {
      tokenType.value = newTokenType
      localStorage.setItem(STORAGE_KEYS.TOKEN_TYPE, newTokenType)
    }
    
    if (newExpiresIn) {
      expiresIn.value = newExpiresIn
      localStorage.setItem(STORAGE_KEYS.EXPIRES_IN, String(newExpiresIn))
    }
  }

  /**
   * 设置用户信息（兼容旧接口）
   */
  function setUserInfo(info: UserInfo & { roles?: string[] }) {
    userInfo.value = info
    localStorage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(info))
    
    if (info.roles) {
      roles.value = info.roles
      localStorage.setItem(STORAGE_KEYS.ROLES, JSON.stringify(info.roles))
    }
  }

  /**
   * 清除所有用户信息
   * Requirements: 1.3 - 登出后清除本地存储的认证信息
   */
  function clearUserInfo() {
    // 清除状态
    token.value = ''
    tokenType.value = 'Bearer'
    expiresIn.value = 0
    userInfo.value = null
    roles.value = []
    permissions.value = []
    dataScope.value = null

    // 清除本地存储
    localStorage.removeItem(STORAGE_KEYS.TOKEN)
    localStorage.removeItem(STORAGE_KEYS.TOKEN_TYPE)
    localStorage.removeItem(STORAGE_KEYS.EXPIRES_IN)
    localStorage.removeItem(STORAGE_KEYS.USER_INFO)
    localStorage.removeItem(STORAGE_KEYS.ROLES)
    localStorage.removeItem(STORAGE_KEYS.PERMISSIONS)
    localStorage.removeItem(STORAGE_KEYS.DATA_SCOPE)
  }

  return {
    // 状态
    token,
    tokenType,
    expiresIn,
    userInfo,
    roles,
    permissions,
    dataScope,
    // 计算属性
    isLoggedIn,
    authHeader,
    // 方法
    setLoginInfo,
    updateToken,
    setUserInfo,
    clearUserInfo,
    hasPermission,
    hasRole
  }
})

/**
 * 导出类型供其他地方使用
 */
export type User = UserInfo
