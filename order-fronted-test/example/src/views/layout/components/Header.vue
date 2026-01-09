<!-- 这个文件实现了应用主界面的顶部导航栏，包含4大功能 -->
<!-- 1.侧边栏折叠控制 -->
<!-- 2. 面包屑导航 -->
<!-- 3. 快捷操作按钮 -->
<!-- 4. 用户信息菜单 -->
<template>
  <div class="header-container">
    <div class="left">
      <el-icon class="collapse-icon" @click="toggleCollapse">
        <Fold v-if="!isCollapse" />
        <Expand v-else />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="currentRoute.meta.title">
          {{ currentRoute.meta.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="right">
      <!-- 数据管理工具栏 -->
      <el-button-group style="margin-right: 20px">
        <el-button :icon="Upload" @click="handleDataImport">数据导入</el-button>
        <el-button :icon="Download" @click="handleDataExport">数据导出</el-button>
      </el-button-group>

      <!-- 用户下拉菜单 -->
      <el-dropdown @command="handleCommand">
        <span class="user-info">
          <el-avatar :size="32" :src="userAvatar" />
          <span class="username">{{ userName }}</span>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
            <el-dropdown-item command="settings">系统设置</el-dropdown-item>
            <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Fold, Expand, Upload, Download } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { logout } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

const isCollapse = computed(() => appStore.isCollapse)
const currentRoute = computed(() => route)
const userName = computed(() => userStore.userInfo?.realName || '管理员')
const userAvatar = computed(() => userStore.userInfo?.avatar || '')

function toggleCollapse() {
  appStore.toggleCollapse()
}

// 数据导入
function handleDataImport() {
  router.push('/data/import')
}

// 数据导出
function handleDataExport() {
  router.push('/data/export')
}

function handleCommand(command: string) {
  switch (command) {
    case 'profile':
      ElMessage.info('个人中心功能开发中')
      break
    case 'settings':
      router.push('/system')
      break
    case 'logout':
      ElMessageBox.confirm(
        '确认退出登录吗？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
        .then(() => {
          return logout()
        })
        .then(() => {
          userStore.clearUserInfo()
          localStorage.removeItem('token')
          ElMessage.success('退出登录成功')
          router.push('/login')
        })
        .catch((error) => {
          if (error === 'cancel') {
            // 用户取消操作，不做处理
          } else {
            ElMessage.error('退出登录失败，请重试')
          }
        })
      break
  }
}
</script>

<style scoped lang="scss">
.header-container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;

  .left {
    display: flex;
    align-items: center;
    gap: 20px;

    .collapse-icon {
      font-size: 20px;
      cursor: pointer;
      transition: color 0.3s;

      &:hover {
        color: var(--el-color-primary);
      }
    }
  }

  .right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;

      .username {
        font-size: 14px;
      }
    }
  }
}
</style>
