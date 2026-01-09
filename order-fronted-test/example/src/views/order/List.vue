<template>
  <div class="order-list-container">
    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单编号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户名称" clearable />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="草稿" value="draft" />
            <el-option label="执行中" value="processing" />
            <el-option label="已发运" value="shipped" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
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
      <el-button type="primary" :icon="Plus" @click="handleCreate">新建订单</el-button>
      <el-button :icon="Upload" @click="handleImport">导入</el-button>
      <el-button :icon="Download" @click="handleExport">导出</el-button>
    </div>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column type="selection" width="55" />
        <el-table-column prop="orderNo" label="订单编号" width="160" fixed />
        <el-table-column prop="customerName" label="客户名称" width="180" />
        <el-table-column prop="productCount" label="产品数量" width="100" align="center" />
        <el-table-column prop="totalAmount" label="订单金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.totalAmount?.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deliveryDate" label="交货日期" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Upload, Download } from '@element-plus/icons-vue'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  customerName: '',
  status: '',
  dateRange: []
})

// 表格数据
const loading = ref(false)
const tableData = ref([
  {
    id: 1,
    orderNo: 'ORD20260105001',
    customerName: '北京科技有限公司',
    productCount: 5,
    totalAmount: 125000,
    status: 'processing',
    deliveryDate: '2026-02-15',
    createTime: '2026-01-05 10:30:00'
  },
  {
    id: 2,
    orderNo: 'ORD20260104002',
    customerName: '上海贸易公司',
    productCount: 3,
    totalAmount: 89500,
    status: 'shipped',
    deliveryDate: '2026-02-10',
    createTime: '2026-01-04 14:20:00'
  },
  {
    id: 3,
    orderNo: 'ORD20260104001',
    customerName: '深圳电子厂',
    productCount: 8,
    totalAmount: 210000,
    status: 'completed',
    deliveryDate: '2026-01-20',
    createTime: '2026-01-04 09:15:00'
  },
  {
    id: 4,
    orderNo: 'ORD20260103001',
    customerName: '广州物流有限公司',
    productCount: 2,
    totalAmount: 45000,
    status: 'draft',
    deliveryDate: '2026-02-28',
    createTime: '2026-01-03 16:45:00'
  }
])

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 4
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
    orderNo: '',
    customerName: '',
    status: '',
    dateRange: []
  })
  ElMessage.info('已重置搜索条件')
}

// 新建
function handleCreate() {
  ElMessage.info('新建订单功能开发中')
}

// 导入
function handleImport() {
  ElMessage.info('导入功能开发中')
}

// 导出
function handleExport() {
  ElMessage.info('导出功能开发中')
}

// 查看
function handleView(row: any) {
  router.push(`/order/${row.id}`)
}

// 编辑
function handleEdit(row: any) {
  ElMessage.info(`编辑订单：${row.orderNo}`)
}

// 删除
function handleDelete(row: any) {
  ElMessageBox.confirm(`确定要删除订单 ${row.orderNo} 吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
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
    draft: 'info',
    processing: 'warning',
    shipped: 'primary',
    completed: 'success'
  }
  return map[status] || 'info'
}

// 状态文本
function getStatusText(status: string) {
  const map: Record<string, string> = {
    draft: '草稿',
    processing: '执行中',
    shipped: '已发运',
    completed: '已完成'
  }
  return map[status] || '未知'
}
</script>

<style scoped lang="scss">
.order-list-container {
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
