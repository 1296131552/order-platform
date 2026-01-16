<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { post } from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

// ==================== 开发环境检测 ====================
const isDev = computed(() => import.meta.env.DEV)

// ==================== 表单引用 ====================
const formRef = ref<FormInstance>()

// ==================== 表单数据 ====================
const loginForm = ref({
  username: '',
  password: ''
})

const loading = ref(false)

// ==================== 表单验证规则 ====================
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 个字符', trigger: 'blur' }
  ]
}

// ==================== 登录方法 ====================
async function handleLogin() {
  if (!formRef.value) return

  try {
    // 验证表单
    await formRef.value.validate()
  } catch {
    // 验证失败，直接返回
    return
  }

  loading.value = true
  try {
    // 调用后端登录接口
    const res = await post<{ token: string; userInfo: { id: number; username: string; realName: string } }>('/auth/login', {
      username: loginForm.value.username,
      password: loginForm.value.password
    })

    // 设置 token 到 store
    userStore.setToken(res.data.token)

    // 设置用户信息
    userStore.setUserInfo(res.data.userInfo)

    // 跳转到首页
    router.push('/')
  } catch (error) {
    // 错误已在 request.ts 中统一处理
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

// ==================== 临时跳过登录（仅开发环境） ====================
function skipLogin() {
  // 设置一个假 token 用于测试
  userStore.setToken('dev_token_' + Date.now())
  userStore.setUserInfo({
    id: 1,
    username: 'dev',
    realName: '开发用户'
  })
  router.push('/')
}
</script>

<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="login-header">
          <el-icon :size="32" color="#409eff"><UserFilled /></el-icon>
          <h2>用户登录</h2>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            style="width: 100%"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>

        <!-- 开发环境：跳过登录按钮 -->
        <el-form-item v-if="isDev">
          <el-button
            type="info"
            plain
            style="width: 100%"
            @click="skipLogin"
          >
            跳过登录（开发调试）
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  border-radius: 12px;
}

.login-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}

.login-header h2 {
  margin: 0;
}
</style>
