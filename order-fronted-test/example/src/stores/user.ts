/**
 * 用户状态管理
 * 负责用户信息的存储和管理
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

interface UserInfo {
  id: number
  username: string
  realName: string
  email?: string
  phone?: string
  avatar?: string
  roles: string[]
}

export const useUserStore = defineStore('user', () => {
  // 用户信息
  const userInfo = ref<UserInfo | null>(null)

  /**
   * 设置用户信息
   */
  function setUserInfo(info: UserInfo) {
    userInfo.value = info
  }

  /**
   * 清除用户信息
   */
  function clearUserInfo() {
    userInfo.value = null
  }

  /**
   * 是否已登录
   */
  const isLoggedIn = computed(() => !!userInfo.value)

  return {
    userInfo,
    setUserInfo,
    clearUserInfo,
    isLoggedIn
  }
})

/**
 * 导出类型供其他地方使用
 */
export interface User {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  avatar: string
}
