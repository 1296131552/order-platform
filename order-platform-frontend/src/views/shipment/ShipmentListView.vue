<script setup lang="ts">
// ==================== 发运列表 ====================
</script>

<template>
  <div class="shipment-list-container">
    <el-page-header @back="$router.push('/')">
      <template #content>
        <span>发运管理</span>
      </template>
    </el-page-header>

    <el-divider />

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="功能开发中"
    >
      发运管理功能正在开发中，敬请期待...
    </el-alert>
  </div>
</template>

<style scoped>
.shipment-list-container {
  padding: 20px;
}
</style>
