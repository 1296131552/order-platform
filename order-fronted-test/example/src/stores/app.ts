/**
 * 应用状态管理
 * 负责应用级别的全局状态
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAppStore = defineStore('app', () => {
  /**
   * 是否折叠侧边栏
   */
  const isCollapse = ref(false)

  /**
   * 是否深色主题
   */
  const isDark = ref(false)

  /**
   * 加载状态
   */
  const loading = ref(false)

  /**
   * 侧边栏宽度
   */
  const sidebarWidth = computed(() => {
    return isCollapse.value ? 64 : 200
  })

  /**
   * 切换侧边栏折叠
   */
  function toggleCollapse() {
    isCollapse.value = !isCollapse.value
  }

  /**
   * 切换主题
   */
  function toggleTheme() {
    isDark.value = !isDark.value
  }

  /**
   * 设置加载状态
   */
  function setLoading(loading: boolean) {
    loading.value = loading
  }

  /**
   * 是否为移动端
   */
  const isMobile = computed(() => {
    return window.innerWidth < 768
  })

  return {
    isCollapse,
    isDark,
    loading,
    sidebarWidth,
    toggleCollapse,
    toggleTheme,
    setLoading,
    isMobile
  }
})
