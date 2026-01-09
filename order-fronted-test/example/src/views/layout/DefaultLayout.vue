<template>
  <el-container class="layout-container">
    <el-aside :width="sidebarWidth" class="sidebar">
      <Sidebar />
    </el-aside>
    <el-container class="main-container">
      <el-header class="header">
        <LayoutHeader />
      </el-header>
      <el-main class="main-content">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'
import Sidebar from './components/Sidebar.vue'
import LayoutHeader from './components/Header.vue'

const appStore = useAppStore()
const sidebarWidth = computed(() => appStore.sidebarWidth + 'px')
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;

  .sidebar {
    background: var(--el-bg-color);
    border-right: 1px solid var(--el-border-color);
    transition: width 0.3s;
    overflow: hidden;
  }

  .main-container {
    display: flex;
    flex-direction: column;

    .header {
      height: 60px;
      border-bottom: 1px solid var(--el-border-color);
      padding: 0;
    }

    .main-content {
      background: var(--el-bg-color-page);
      padding: 20px;
      overflow-y: auto;
    }
  }
}
</style>
