<template>
  <div class="sidebar-container">
    <div class="logo">
      <span v-if="!isCollapse">订单可视化平台</span>
      <span v-else>订单</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      :unique-opened="true"
      router
      class="sidebar-menu"
    >
      <el-menu-item index="/dashboard">
        <el-icon><DataBoard /></el-icon>
        <template #title>数据看板</template>
      </el-menu-item>
      <el-menu-item index="/order">
        <el-icon><Document /></el-icon>
        <template #title>订单管理</template>
      </el-menu-item>
      <el-menu-item index="/supplier">
        <el-icon><User /></el-icon>
        <template #title>供应商管理</template>
      </el-menu-item>
      <el-menu-item index="/carrier">
        <el-icon><Van /></el-icon>
        <template #title>承运商管理</template>
      </el-menu-item>
      <el-menu-item index="/shipment">
        <el-icon><Van /></el-icon>
        <template #title>发运管理</template>
      </el-menu-item>
      <el-menu-item index="/receipt">
        <el-icon><Box /></el-icon>
        <template #title>签收管理</template>
      </el-menu-item>
      <el-menu-item index="/exception">
        <el-icon><Warning /></el-icon>
        <template #title>异常管理</template>
      </el-menu-item>
      <el-menu-item index="/attachment">
        <el-icon><FolderOpened /></el-icon>
        <template #title>附件管理</template>
      </el-menu-item>
      <el-menu-item index="/billing">
        <el-icon><Tickets /></el-icon>
        <template #title>对账管理</template>
      </el-menu-item>
      <el-menu-item index="/system">
        <el-icon><Setting /></el-icon>
        <template #title>系统管理</template>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { DataBoard, Document, User, Van, Box, Warning, FolderOpened, Setting, Tickets } from '@element-plus/icons-vue'

const route = useRoute()
const appStore = useAppStore()

const isCollapse = computed(() => appStore.isCollapse)
const activeMenu = computed(() => route.path)
</script>

<style scoped lang="scss">
.sidebar-container {
  height: 100%;
  display: flex;
  flex-direction: column;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    font-weight: bold;
    border-bottom: 1px solid var(--el-border-color);
  }

  .sidebar-menu {
    flex: 1;
    border-right: none;
    overflow-y: auto;
  }
}
</style>
