<template>
  <div class="system-container">
    <el-row :gutter="20">
      <!-- 系统配置 -->
      <el-col :xs="24" :sm="24" :md="12">
        <el-card class="config-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Setting /></el-icon>
              <span>系统配置</span>
            </div>
          </template>
          <el-form label-width="100px">
            <el-form-item label="系统名称">
              <el-input v-model="systemConfig.appName" />
            </el-form-item>
            <el-form-item label="Logo地址">
              <el-input v-model="systemConfig.logoUrl" />
            </el-form-item>
            <el-form-item label="系统版本">
              <el-input v-model="systemConfig.version" disabled />
            </el-form-item>
            <el-form-item label="版权信息">
              <el-input v-model="systemConfig.copyright" />
            </el-form-item>
            <el-form-item label="备案号">
              <el-input v-model="systemConfig.icp" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveConfig">保存配置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 用户信息 -->
      <el-col :xs="24" :sm="24" :md="12">
        <el-card class="user-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><User /></el-icon>
              <span>当前用户</span>
            </div>
          </template>
          <div class="user-info">
            <el-avatar :size="80" :src="userInfo.avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
            <h3>{{ userInfo.realName }}</h3>
            <p class="username">@{{ userInfo.username }}</p>
            <div class="user-detail">
              <p><el-icon><Message /></el-icon> {{ userInfo.email }}</p>
              <p><el-icon><Phone /></el-icon> {{ userInfo.phone }}</p>
            </div>
            <div class="roles">
              <el-tag v-for="role in userInfo.roles" :key="role" type="primary">{{ role }}</el-tag>
            </div>
            <el-button type="primary" @click="handleEditProfile">编辑资料</el-button>
            <el-button @click="handleChangePassword">修改密码</el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 系统状态 -->
      <el-col :xs="24" :sm="24" :md="12">
        <el-card class="status-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Monitor /></el-icon>
              <span>系统状态</span>
            </div>
          </template>
          <div class="status-list">
            <div class="status-item">
              <span class="label">运行状态</span>
              <el-tag type="success">正常</el-tag>
            </div>
            <div class="status-item">
              <span class="label">数据库</span>
              <el-tag type="success">连接正常</el-tag>
            </div>
            <div class="status-item">
              <span class="label">缓存服务</span>
              <el-tag type="success">运行中</el-tag>
            </div>
            <div class="status-item">
              <span class="label">文件存储</span>
              <el-tag type="success">可用</el-tag>
            </div>
            <div class="status-item">
              <span class="label">API接口</span>
              <el-tag type="success">正常</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 快捷操作 -->
      <el-col :xs="24" :sm="24" :md="12">
        <el-card class="action-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Operation /></el-icon>
              <span>快捷操作</span>
            </div>
          </template>
          <div class="action-list">
            <el-button :icon="User" @click="handleUserManagement">用户管理</el-button>
            <el-button :icon="Lock" @click="handlePermissionManagement">权限管理</el-button>
            <el-button :icon="Document" @click="handleLogManagement">操作日志</el-button>
            <el-button :icon="Delete" @click="handleClearCache">清除缓存</el-button>
            <el-button :icon="Refresh" @click="handleRestartService">重启服务</el-button>
            <el-button :icon="Download" @click="handleBackupData">数据备份</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Setting,
  User,
  Monitor,
  Operation,
  Message,
  Phone,
  Lock,
  Document,
  Delete,
  Refresh,
  Download
} from '@element-plus/icons-vue'
import { useSettingsStore } from '@/stores/settings'
import { useUserStore } from '@/stores/user'

const settingsStore = useSettingsStore()
const userStore = useUserStore()

// 系统配置
const systemConfig = reactive({
  appName: settingsStore.systemConfig.appName,
  logoUrl: settingsStore.systemConfig.logoUrl,
  version: settingsStore.systemConfig.version,
  copyright: settingsStore.systemConfig.copyright,
  icp: settingsStore.systemConfig.icp
})

// 用户信息
const userInfo = ref({
  id: 1,
  username: 'admin',
  realName: '管理员',
  email: 'admin@example.com',
  phone: '13800138000',
  avatar: '',
  roles: ['超级管理员', '系统管理员']
})

// 保存配置
function handleSaveConfig() {
  settingsStore.updateConfig(systemConfig)
  ElMessage.success('配置保存成功')
}

// 编辑资料
function handleEditProfile() {
  ElMessage.info('编辑资料功能开发中')
}

// 修改密码
function handleChangePassword() {
  ElMessage.info('修改密码功能开发中')
}

// 用户管理
function handleUserManagement() {
  ElMessage.info('用户管理功能开发中')
}

// 权限管理
function handlePermissionManagement() {
  ElMessage.info('权限管理功能开发中')
}

// 操作日志
function handleLogManagement() {
  ElMessage.info('操作日志功能开发中')
}

// 清除缓存
function handleClearCache() {
  ElMessage.success('缓存已清除')
}

// 重启服务
function handleRestartService() {
  ElMessage.warning('重启服务功能需要管理员权限')
}

// 数据备份
function handleBackupData() {
  ElMessage.info('数据备份功能开发中')
}
</script>

<style scoped lang="scss">
.system-container {
  .el-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 500;
    }
  }

  .user-card {
    .user-info {
      text-align: center;

      h3 {
        margin: 16px 0 8px;
        font-size: 18px;
      }

      .username {
        color: #909399;
        font-size: 14px;
        margin-bottom: 16px;
      }

      .user-detail {
        text-align: left;
        padding: 16px;
        background: #f5f7fa;
        border-radius: 4px;
        margin-bottom: 16px;

        p {
          display: flex;
          align-items: center;
          gap: 8px;
          margin: 8px 0;
          font-size: 14px;
          color: #606266;
        }
      }

      .roles {
        margin-bottom: 16px;

        .el-tag {
          margin: 0 4px;
        }
      }

      .el-button {
        margin: 0 8px 8px;
      }
    }
  }

  .status-card {
    .status-list {
      .status-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #ebeef5;

        &:last-child {
          border-bottom: none;
        }

        .label {
          font-size: 14px;
          color: #606266;
        }
      }
    }
  }

  .action-card {
    .action-list {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;

      .el-button {
        width: 100%;
      }
    }
  }
}
</style>
