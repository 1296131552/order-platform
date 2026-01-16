<script setup lang="ts">
// ==================== 订单列表 ====================
</script>

<template>
  <div class="order-list-container">
    <el-page-header @back="$router.push('/')">
      <template #content>
        <span>订单管理</span>
      </template>
    </el-page-header>

    <el-divider />

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="功能开发中"
    >
      订单管理功能正在开发中，敬请期待...
    </el-alert>
  </div>
</template>

<style scoped>
.order-list-container {
  padding: 20px;
}
</style>
