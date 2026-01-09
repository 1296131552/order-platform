/**
 * 路由守卫
 */

import type { Router } from 'vue-router'
import { ElMessage } from 'element-plus'

/**
 * 设置路由守卫
 */
export function setupRouterGuards(router: Router) {
  // 前置守卫
  router.beforeEach((to, from, next) => {
    // 设置页面标题
    document.title = `${to.meta.title || '订单可视化平台'} - 订单可视化数字化管理平台`

    // 开发环境：跳过登录验证
    if (import.meta.env.DEV) {
      next()
      return
    }

    // 白名单路由（不需要登录）
    const whiteList = ['/login']

    if (whiteList.includes(to.path)) {
      next()
      return
    }

    // 检查token
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.warning('请先登录')
      next('/login')
      return
    }

    next()
  })

  // 后置守卫
  router.afterEach((to, from) => {
    // 可以在这里添加页面访问统计等逻辑
  })
}
