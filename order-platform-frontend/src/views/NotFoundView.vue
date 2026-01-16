<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>

<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <el-icon :size="120" color="#909399">
        <Warning />
      </el-icon>
      <h1>404</h1>
      <p>页面不存在</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<style scoped>
.not-found-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.not-found-content {
  text-align: center;
  padding: 40px;
}

.not-found-content h1 {
  font-size: 72px;
  color: #909399;
  margin: 20px 0;
}

.not-found-content p {
  font-size: 18px;
  color: #606266;
  margin-bottom: 30px;
}
</style>
