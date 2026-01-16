<script setup lang="ts">
// ==================== 合作方列表 ====================
</script>

<template>
  <div class="partner-list-container">
    <el-page-header @back="$router.push('/')">
      <template #content>
        <span>合作方管理</span>
      </template>
    </el-page-header>

    <el-divider />

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="功能开发中"
    >
      合作方管理功能正在开发中，敬请期待...
    </el-alert>
  </div>
</template>

<style scoped>
.partner-list-container {
  padding: 20px;
}
</style>
