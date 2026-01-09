<template>
  <div class="receipt-list-container">
    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="签收单号">
          <el-input v-model="searchForm.receiptNo" placeholder="请输入签收单号" clearable />
        </el-form-item>
        <el-form-item label="发运单号">
          <el-input v-model="searchForm.shipmentNo" placeholder="请输入发运单号" clearable />
        </el-form-item>
        <el-form-item label="是否有差异">
          <el-select v-model="searchForm.hasException" placeholder="请选择" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="有差异" value="true" />
            <el-option label="无差异" value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="签收日期">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="handleCreate">新建签收</el-button>
      <el-button :icon="Download" @click="handleExport">导出</el-button>
    </div>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column type="selection" width="55" />
        <el-table-column prop="receiptNo" label="签收单号" width="150" fixed />
        <el-table-column prop="shipmentNo" label="发运单号" width="150" />
        <el-table-column prop="customerName" label="客户名称" width="180" />
        <el-table-column prop="receiver" label="签收人" width="100" />
        <el-table-column prop="receiptDate" label="签收日期" width="120" />
        <el-table-column prop="receiptQuantity" label="签收数量" width="100" align="right" />
        <el-table-column prop="hasException" label="是否有差异" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.hasException ? 'danger' : 'success'">
              {{ row.hasException ? '有差异' : '无差异' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="处理状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.hasException" type="warning" link size="small" @click="handleException(row)">
              异常处理
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
  receiptNo: '',
  shipmentNo: '',
  hasException: '',
  dateRange: []
})

// 表格数据
const loading = ref(false)
const tableData = ref([
  {
    id: 1,
    receiptNo: 'RCP20260110001',
    shipmentNo: 'SHP20260107001',
    customerName: '上海贸易公司',
    receiver: '李四',
    receiptDate: '2026-01-10',
    receiptQuantity: 50,
    hasException: false,
    status: 'completed'
  },
  {
    id: 2,
    receiptNo: 'RCP20260109001',
    shipmentNo: 'SHP20260106001',
    customerName: '深圳电子厂',
    receiver: '王五',
    receiptDate: '2026-01-09',
    receiptQuantity: 23,
    hasException: true,
    status: 'pending'
  }
])

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 2
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
    receiptNo: '',
    shipmentNo: '',
    hasException: '',
    dateRange: []
  })
  ElMessage.info('已重置搜索条件')
}

// 新建签收
function handleCreate() {
  ElMessage.info('新建签收功能开发中')
}

// 导出
function handleExport() {
  ElMessage.info('导出功能开发中')
}

// 查看
function handleView(row: any) {
  ElMessage.info(`查看签收单：${row.receiptNo}`)
}

// 异常处理
function handleException(row: any) {
  ElMessage.info(`处理 ${row.receiptNo} 的异常`)
}

// 分页
function handleSizeChange(size: number) {
  pagination.pageSize = size
}

function handleCurrentChange(page: number) {
  pagination.page = page
}

// 状态类型
function getStatusType(status: string) {
  const map: Record<string, any> = {
    pending: 'warning',
    processing: 'primary',
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
.receipt-list-container {
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
