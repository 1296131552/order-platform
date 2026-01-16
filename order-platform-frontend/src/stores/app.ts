import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 应用全局状态
 */
export const useAppStore = defineStore('app', () => {
  // ==================== 状态 ====================
  const sidebarCollapsed = ref<boolean>(false)

  // ==================== 方法 ====================
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  return {
    sidebarCollapsed,
    toggleSidebar
  }
})
