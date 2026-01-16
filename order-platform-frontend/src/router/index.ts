import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

// ==================== 公开路由（无需登录） ====================
const publicRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录', public: true }
  }
]

// ==================== 受保护路由（需要登录） ====================
const protectedRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/orders',
    name: 'OrderList',
    component: () => import('@/views/order/OrderListView.vue'),
    meta: { title: '订单列表' }
  },
  {
    path: '/shipments',
    name: 'ShipmentList',
    component: () => import('@/views/shipment/ShipmentListView.vue'),
    meta: { title: '发运列表' }
  },
  {
    path: '/partners',
    name: 'PartnerList',
    component: () => import('@/views/partner/PartnerListView.vue'),
    meta: { title: '合作方列表' }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/DashboardView.vue'),
    meta: { title: '数据看板' }
  }
]

// ==================== 路由配置 ====================
const routes: RouteRecordRaw[] = [
  ...publicRoutes,
  ...protectedRoutes,
  // 404 页面（必须放在最后）
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { title: '页面不存在' }
  }
]

// ==================== 路由实例 ====================
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// ==================== 路由守卫 ====================
router.beforeEach((to, _from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || '订单管理平台'} - 订单可视化平台`

  // 权限检查
  const userStore = useUserStore()
  const isPublicRoute = to.meta.public === true

  if (!isPublicRoute && !userStore.isLoggedIn) {
    // 未登录访问受保护路由，跳转到登录页
    next('/login')
  } else if (to.path === '/login' && userStore.isLoggedIn) {
    // 已登录访问登录页，跳转到首页
    next('/')
  } else {
    next()
  }
})

export default router
