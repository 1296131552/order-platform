<template>
  <div class="exception-list-container">
    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="异常编号">
          <el-input v-model="searchForm.exceptionNo" placeholder="请输入异常编号" clearable />
        </el-form-item>
        <el-form-item label="异常类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="数量差异" value="quantity" />
            <el-option label="质量异常" value="quality" />
            <el-option label="运输延误" value="delay" />
            <el-option label="货物损坏" value="damage" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <div class="toolbar">
      <el-button type="danger" :icon="Plus" @click="handleCreate">上报异常</el-button>
      <el-button :icon="Download" @click="handleExport">导出</el-button>
    </div>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column type="selection" width="55" />
        <el-table-column prop="exceptionNo" label="异常编号" width="150" fixed />
        <el-table-column prop="orderNo" label="关联订单" width="150" />
        <el-table-column prop="type" label="异常类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="异常描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="reporter" label="上报人" width="100" />
        <el-table-column prop="reportTime" label="上报时间" width="160" />
        <el-table-column prop="status" label="处理状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handler" label="处理人" width="100" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 'pending'" type="warning" link size="small" @click="handleProcess(row)">
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search, Refresh, Download } from '@element-plus/icons-vue'

// 搜索表单
const searchForm = reactive({
  exceptionNo: '',
  type: '',
  status: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref([
  {
    id: 1,
    exceptionNo: 'EXC20260110001',
    orderNo: 'ORD20260104001',
    type: 'quantity',
    description: '签收数量与发运数量不符，少2件',
    reporter: '王五',
    reportTime: '2026-01-10 14:30:00',
    status: 'pending',
    handler: ''
  },
  {
    id: 2,
    exceptionNo: 'EXC20260109001',
    orderNo: 'ORD20260103001',
    type: 'damage',
    description: '货物在运输过程中发生损坏，包装破损',
    reporter: '李四',
    reportTime: '2026-01-09 10:15:00',
    status: 'processing',
    handler: '张三'
  },
  {
    id: 3,
    exceptionNo: 'EXC20260108001',
    orderNo: 'ORD20260102001',
    type: 'delay',
    description: '因天气原因，货物运输延误2天',
    reporter: '赵六',
    reportTime: '2026-01-08 16:20:00',
    status: 'completed',
    handler: '张三'
  }
])

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 3
})

// 搜索
function handleSearch() {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('查询成功')
  }, 500)
}

// 重置
function handleReset() {
  Object.assign(searchForm, {
    exceptionNo: '',
    type: '',
    status: ''
  })
  ElMessage.info('已重置搜索条件')
}

// 上报异常
function handleCreate() {
  ElMessage.info('上报异常功能开发中')
}

// 导出
function handleExport() {
  ElMessage.info('导出功能开发中')
}

// 查看
function handleView(row: any) {
  ElMessage.info(`查看异常：${row.exceptionNo}`)
}

// 处理
function handleProcess(row: any) {
  ElMessage.info(`处理异常：${row.exceptionNo}`)
}

// 分页
function handleSizeChange(size: number) {
  pagination.pageSize = size
}

function handleCurrentChange(page: number) {
  pagination.page = page
}

// 类型文本
function getTypeText(type: string) {
  const map: Record<string, string> = {
    quantity: '数量差异',
    quality: '质量异常',
    delay: '运输延误',
    damage: '货物损坏'
  }
  return map[type] || '其他'
}

// 状态类型
function getStatusType(status: string) {
  const map: Record<string, any> = {
    pending: 'danger',
    processing: 'warning',
    completed: 'success'
  }
  return map[status] || 'info'
}

// 状态文本
function getStatusText(status: string) {
  const map: Record<string, string> = {
    pending: '待处理',
    processing: '处理中',
    completed: '已完成'
  }
  return map[status] || '未知'
}
</script>

<style scoped lang="scss">
.exception-list-container {
  .search-card {
    margin-bottom: 16px;

    .search-form {
      margin-bottom: -10px;

      .el-form-item {
        margin-bottom: 10px;
      }
    }
  }

  .toolbar {
    margin-bottom: 16px;
  }

  .table-card {
    .el-pagination {
      margin-top: 16px;
      justify-content: flex-end;
    }
  }
}
</style>
