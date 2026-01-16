import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 用户信息接口
 */
export interface UserInfo {
  id: number
  username: string
  realName: string
  email?: string
  role?: string
}

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', () => {
  // ==================== 状态 ====================
  const token = ref<string>(localStorage.getItem('access_token') || '')
  const userInfo = ref<UserInfo | null>(null)

  // ==================== 计算属性 ====================
  // isLoggedIn 是 token 的投影，不是独立状态
  const isLoggedIn = computed(() => !!token.value)

  // ==================== 方法 ====================
  /**
   * 设置 Token
   */
  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('access_token', newToken)
  }

  /**
   * 设置用户信息
   */
  function setUserInfo(info: UserInfo) {
    userInfo.value = info
  }

  /**
   * 登出
   */
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('access_token')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    setToken,
    setUserInfo,
    logout
  }
})
