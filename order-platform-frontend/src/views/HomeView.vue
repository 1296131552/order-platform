<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { Component } from 'vue'
import { get } from '@/utils/request'
import {
  HomeFilled,
  Document,
  Van,
  User,
  DataAnalysis,
  Connection
} from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)

// ==================== 导航项配置（数据驱动） ====================
interface NavItem {
  path: string
  icon: Component
  title: string
  subtitle: string
  color: string
}

const navItems: NavItem[] = [
  {
    path: '/orders',
    icon: Document,
    title: '订单管理',
    subtitle: 'Order Management',
    color: '#409eff'
  },
  {
    path: '/shipments',
    icon: Van,
    title: '发运管理',
    subtitle: 'Shipment Management',
    color: '#67c23a'
  },
  {
    path: '/partners',
    icon: User,
    title: '合作方管理',
    subtitle: 'Partner Management',
    color: '#e6a23c'
  },
  {
    path: '/dashboard',
    icon: DataAnalysis,
    title: '数据看板',
    subtitle: 'Dashboard',
    color: '#f56c6c'
  }
]

// ==================== 导航方法（单一函数） ====================
function navigateTo(path: string) {
  router.push(path)
}

// ==================== 验证后端 API 连接 ====================
async function checkBackendHealth() {
  loading.value = true
  try {
    await get<null>('/health')
    console.log('后端连接成功')
  } catch (error) {
    console.error('后端连接失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  checkBackendHealth()
})
</script>

<template>
  <div class="home-container">
    <!-- 头部 -->
    <header class="header">
      <h1>订单可视化数字化管理平台</h1>
      <p class="subtitle">Order Visualization Digital Management Platform</p>
    </header>

    <!-- 主内容 -->
    <main class="main-content">
      <!-- 欢迎卡片 -->
      <el-card class="welcome-card">
        <template #header>
          <div class="card-header">
            <el-icon :size="24"><HomeFilled /></el-icon>
            <span>欢迎使用</span>
          </div>
        </template>
        <p class="welcome-text">
          这是一个基于 Vue 3 + Spring Boot 的订单可视化管理平台。
        </p>
        <el-alert
          type="success"
          :closable="false"
          show-icon
          title="前端项目初始化完成"
        >
          <template #default>
            <p>✅ Vite + Vue 3 + TypeScript</p>
            <p>✅ Element Plus UI 框架</p>
            <p>✅ Vue Router 路由配置</p>
            <p>✅ Pinia 状态管理</p>
            <p>✅ Axios HTTP 请求封装</p>
          </template>
        </el-alert>
      </el-card>

      <!-- 功能导航（数据驱动渲染） -->
      <div class="nav-grid">
        <el-card
          v-for="item in navItems"
          :key="item.path"
          class="nav-card"
          shadow="hover"
          @click="navigateTo(item.path)"
        >
          <div class="nav-content">
            <el-icon :size="40" :color="item.color">
              <component :is="item.icon" />
            </el-icon>
            <h3>{{ item.title }}</h3>
            <p>{{ item.subtitle }}</p>
          </div>
        </el-card>
      </div>

      <!-- API 测试 -->
      <el-card class="api-card">
        <template #header>
          <div class="card-header">
            <el-icon :size="24"><Connection /></el-icon>
            <span>后端连接测试</span>
          </div>
        </template>
        <div class="api-test">
          <p>测试连接后端健康检查接口：GET /api/health</p>
          <el-button
            type="primary"
            :loading="loading"
            @click="checkBackendHealth"
          >
            {{ loading ? '连接中...' : '测试连接' }}
          </el-button>
          <p class="hint">请确保后端服务已启动（http://localhost:8080/api）</p>
        </div>
      </el-card>
    </main>
  </div>
</template>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.header {
  text-align: center;
  color: white;
  margin-bottom: 40px;
}

.header h1 {
  font-size: 36px;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 16px;
  opacity: 0.9;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.welcome-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: bold;
}

.welcome-text {
  margin-bottom: 20px;
  font-size: 16px;
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.nav-card {
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.2s;
}

.nav-card:hover {
  transform: translateY(-4px);
}

.nav-content {
  text-align: center;
  padding: 20px;
}

.nav-content h3 {
  margin: 15px 0 5px;
  font-size: 20px;
}

.nav-content p {
  color: #909399;
  font-size: 14px;
}

.api-card {
  border-radius: 12px;
}

.api-test {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.hint {
  color: #909399;
  font-size: 14px;
}
</style>
