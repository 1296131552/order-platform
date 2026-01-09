<!-- 登录页面 - 展示项目效果 -->
<template>
  <div class="login-container">
    <!-- 几何装饰元素 -->
    <div class="geometric-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <div class="login-card-wrapper">
      <el-card class="login-card">
        <!-- 卡片头部 -->
        <div class="login-header">
          <h1 class="title">账号登录</h1>
          <p class="subtitle">订单可视化数字化管理平台</p>
        </div>

        <el-form
          :model="loginForm"
          :rules="loginRules"
          ref="loginFormRef"
          @submit.prevent="handleLogin"
          class="login-form"
        >
          <el-form-item prop="account">
            <el-input
              v-model="loginForm.account"
              placeholder="请输入用户名/邮箱/手机号"
              size="large"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item>
            <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleLogin"
              style="width: 100%"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>

          <div class="form-footer">
            <el-divider />
            <p class="demo-tips">
              <el-icon><InfoFilled /></el-icon>
              <span>演示账号：admin / admin</span>
            </p>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 登录页面
 * Requirements: 1.1, 1.2
 */
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, InfoFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { login, getCurrentUser } from '@/api/auth'
import type { LoginResult } from '@/api/auth'

// ==================== 类型定义 ====================

interface LoginForm {
  /** 账号（用户名/邮箱/手机号） - Requirements: 1.1 */
  account: string
  /** 密码 */
  password: string
  /** 记住我 */
  remember: boolean
}

interface LoginRules {
  account: [{ required: true, message: '请输入用户名/邮箱/手机号', trigger: 'blur' }]
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// ==================== 响应式数据 ====================

const loginForm = reactive<LoginForm>({
  account: '',
  password: '',
  remember: true
})

const loginRules: LoginRules = {
  account: [{ required: true, message: '请输入用户名/邮箱/手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const loading = ref(false)
const router = useRouter()
const loginFormRef = ref()
const userStore = useUserStore()

// ==================== 方法 ====================

/**
 * 处理登录响应，存储完整的登录信息
 * Requirements: 1.2 - 存储token、tokenType、expiresIn和完整的userInfo信息
 */
const handleLoginResponse = (result: LoginResult) => {
  // 使用新的 setLoginInfo 方法存储完整的登录信息
  userStore.setLoginInfo({
    token: result.token,
    tokenType: result.tokenType || 'Bearer',
    expiresIn: result.expiresIn,
    userInfo: result.userInfo,
    roles: result.roles || [],
    permissions: result.permissions || [],
    dataScope: result.dataScope
  })
}

/**
 * 处理登录
 * Requirements: 1.1 - 使用account字段传递账号
 */
const handleLogin = async () => {
  const valid = await loginFormRef.value?.validate()
  if (!valid) return

  loading.value = true

  try {
    // 调用登录接口，使用account字段
    const result = await login({
      account: loginForm.account,
      password: loginForm.password
    })

    // 处理完整的登录响应
    handleLoginResponse(result)

    ElMessage.success('登录成功')

    // 跳转到首页
    router.push('/dashboard')
  } catch (error: any) {
    ElMessage.error(error?.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

// ==================== 生命周期 ====================

onMounted(async () => {
  // 如果已登录，验证token有效性并跳转
  if (userStore.token) {
    try {
      // 验证token是否有效，获取当前用户信息
      const result = await getCurrentUser()
      
      // 更新用户信息
      handleLoginResponse(result)
      
      router.push('/dashboard')
    } catch (error) {
      // token无效，清除并停留在登录页
      userStore.clearUserInfo()
    }
  }
})
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

.login-card-wrapper {
  width: 100%;
  max-width: 420px;
  position: relative;
  z-index: 1;
  animation: card-fade-in 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.login-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  padding: 40px;
  transition: box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  }

  :deep(.el-card__body) {
    padding: 0;
  }

  :deep(.el-input__wrapper) {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    &:hover {
      box-shadow: 0 0 0 1px #3b82f6 inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 1px #3b82f6 inset,
                  0 0 0 3px rgba(59, 130, 246, 0.1);
    }
  }

  :deep(.el-button--primary) {
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

    &:hover:not(:disabled) {
      background-color: #2563eb;
      box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
      transform: translateY(-2px);
    }

    &:active:not(:disabled) {
      transform: translateY(0);
    }
  }

  .el-form {
    .el-form-item {
      margin-bottom: 20px;
    }

    .form-footer {
      margin-top: 20px;

      .demo-tips {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 6px;
        padding: 12px;
        background-color: #f0f9ff;
        border: 1px solid #bae6fd;
        border-radius: 6px;
        color: #0369a1;
        font-size: 12px;
        margin-top: 16px;
        transition: all 0.3s ease;

        &:hover {
          background-color: #e0f2fe;
          border-color: #7dd3fc;
        }

        .el-icon {
          font-size: 16px;
          color: #0284c7;
        }
      }
    }
  }
}

// 卡片头部样式
.login-header {
  text-align: center;
  margin-bottom: 32px;

  .title {
    font-size: 24px;
    font-weight: 600;
    color: #1f2937;
    margin: 0 0 8px 0;
  }

  .subtitle {
    font-size: 14px;
    color: #6b7280;
    margin: 0;
  }
}

// 登录表单样式
.login-form {
  // 只给核心元素添加动画
  :deep(.el-form-item:nth-child(1)),
  :deep(.el-form-item:nth-child(2)),
  :deep(.el-form-item:nth-child(4)) {
    // 只动画：账号输入框、密码输入框、登录按钮
    animation: fade-in-up 0.4s ease-out backwards;
  }

  :deep(.el-form-item:nth-child(1)) {
    animation-delay: 0.1s; // 账号输入框
  }

  :deep(.el-form-item:nth-child(2)) {
    animation-delay: 0.2s; // 密码输入框
  }

  :deep(.el-form-item:nth-child(4)) {
    animation-delay: 0.3s; // 登录按钮
  }

  // 记住我 和 演示账号提示 不添加动画，保持简洁
}

// 卡片入场动画
@keyframes card-fade-in {
  0% {
    opacity: 0;
    transform: translateY(20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

// 表单元素淡入上浮动画
@keyframes fade-in-up {
  0% {
    opacity: 0;
    transform: translateY(15px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

// 几何装饰元素样式（参考Apple/Stripe风格）
.geometric-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;

  .circle {
    position: absolute;
    border-radius: 50%;
  }

  .circle-1 {       // 右上角大圆
    width: 500px;
    height: 500px;
    background: radial-gradient(circle, rgba(59,130,246,0.30) 0%,rgba(59,130,246,0.15) 70%,transparent 100%);
    top: -200px;
    right: -150px;
    animation: float 20s ease-in-out infinite;
  }

  .circle-2 {       // 右下角中圆
    width: 300px;
    height: 300px;
    background: radial-gradient(circle,rgba(37,99,235,0.25) 0%, rgba(37,99,235,0.12) 70%,transparent 100%);
    bottom: -100px;
    right: 10%;
    animation: float 25s ease-in-out infinite reverse;
  }

  .circle-3 {       // 左下角小圆
    width: 200px;
    height: 200px;
    background: radial-gradient(circle,rgba(59,130,246,0.25) 0%,transparent 70%);
    bottom: 10%;
    left: -50px;
    animation: float 15s ease-in-out infinite;
  }
}

// 几何装饰浮动动画
@keyframes float {
  0% {
    /* 初始位置：无偏移，透明度1（基础状态） */
    transform: translate(0px, 0px);
    opacity: 1;
  }
  25% {
    /* 第一阶段：轻微向右上移动，透明度微降（营造轻盈感） */
    transform: translate(10px, -15px);
    opacity: 0.95;
  }
  50% {
    /* 中间阶段：最大偏移量，呼吸感峰值 */
    transform: translate(20px, -20px);
    opacity: 0.9;
  }
  75% {
    /* 回落阶段：向左下轻微移动，透明度回升 */
    transform: translate(10px, -10px);
    opacity: 0.95;
  }
  100% {
    /* 回到初始位置，完成一个呼吸周期 */
    transform: translate(0px, 0px);
    opacity: 1;
  }
}

// 响应式适配
@media (max-width: 768px) {
  .login-card-wrapper {
    max-width: 100%;
  }

  .login-card {
    padding: 24px;
  }

  .login-header .title {
    font-size: 20px;
  }
}
</style>
