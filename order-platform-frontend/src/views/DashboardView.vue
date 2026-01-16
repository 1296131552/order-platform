<script setup lang="ts">
// ==================== 数据看板 ====================
</script>

<template>
  <div class="dashboard-container">
    <el-page-header @back="$router.push('/')">
      <template #content>
        <span>数据看板</span>
      </template>
    </el-page-header>

    <el-divider />

    <el-row :gutter="20" class="kpi-row">
      <el-col :span="6">
        <el-card class="kpi-card">
          <el-statistic title="订单总数" :value="0" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="kpi-card">
          <el-statistic title="在途订单" :value="0" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="kpi-card">
          <el-statistic title="准时率" :value="0" suffix="%" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="kpi-card">
          <el-statistic title="异常件数" :value="0" />
        </el-card>
      </el-col>
    </el-row>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="功能开发中"
      style="margin-top: 20px"
    >
      数据看板功能正在开发中，敬请期待...
    </el-alert>
  </div>
</template>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.kpi-row {
  margin-top: 20px;
}

.kpi-card {
  text-align: center;
}
</style>
