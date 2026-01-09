/**
 * 系统配置状态管理
 * 负责系统级别的配置管理
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'

interface SystemConfig {
  appName: string
  logoUrl: string
  version: string
  copyright: string
  icp: string
}

export const useSettingsStore = defineStore('settings', () => {
  // 系统配置
  const systemConfig = ref<SystemConfig>({
    appName: '订单可视化平台',
    logoUrl: '/logo.png',
    version: 'v1.0.0',
    copyright: '2026',
    icp: '京ICP备12345678号'
  })

  /**
   * 更新系统配置
   */
  function updateConfig(config: Partial<SystemConfig>) {
    Object.assign(systemConfig.value, config)
  }

  return {
    systemConfig,
    updateConfig
  }
})
