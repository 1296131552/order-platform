<!--
  订单列表页面示例
  功能：搜索、表格展示、分页、状态标签、操作按钮
  说明：这是一个完整的生产级页面示例，可以直接参考使用
-->
<template>
  <div class="order-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">订单管理</h2>
      <div class="page-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新建订单
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="订单号">
          <el-input
            v-model="searchForm.orderNo"
            placeholder="请输入订单号"
            clearable
            style="width: 180px"
          />
        </el-form-item>

        <el-form-item label="客户名称">
          <el-input
            v-model="searchForm.customerName"
            placeholder="请输入客户名称"
            clearable
            style="width: 180px"
          />
        </el-form-item>

        <el-form-item label="订单状态">
          <el-select
            v-model="searchForm.statusCode"
            placeholder="全部"
            clearable
            style="width: 150px"
          >
            <el-option label="全部" value="" />
            <el-option label="草稿" value="DRAFT" />
            <el-option label="执行中" value="EXECUTING" />
            <el-option label="部分到货" value="PARTIAL_RECEIVED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>

        <el-form-item label="创建时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 280px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格区域 -->
    <el-card class="table-card" shadow="never">
      <!-- 批量操作栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <span class="selected-info" v-if="selectedRows.length > 0">
            已选择 {{ selectedRows.length }} 项
          </span>
        </div>
        <div class="toolbar-right">
          <el-button
            type="danger"
            :disabled="selectedRows.length === 0"
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
          <el-button @click="handleExport">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        @selection-change="handleSelectionChange"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column type="selection" width="50" fixed />

        <el-table-column prop="orderNo" label="订单号" width="150" fixed>
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">
              {{ row.orderNo }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column prop="customerName" label="客户名称" width="150" />

        <el-table-column label="订单状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.statusCode" type="order" />
          </template>
        </el-table-column>

        <el-table-column prop="totalAmount" label="订单金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ formatMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="lineCount" label="订单行数" width="100" />

        <el-table-column prop="createdBy" label="创建人" width="100" />

        <el-table-column prop="createdAt" label="创建时间" width="160" />

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              详情
            </el-button>
            <el-button
              link
              type="primary"
              @click="handleEdit(row)"
              v-if="row.statusCode === 'DRAFT'"
            >
              编辑
            </el-button>
            <el-button
              link
              type="primary"
              @click="handleSubmit(row)"
              v-if="row.statusCode === 'DRAFT'"
            >
              提交
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(row)"
              v-if="row.statusCode === 'DRAFT'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 订单详情抽屉 -->
    <el-drawer
      v-model="detailVisible"
      title="订单详情"
      size="60%"
      destroy-on-close
    >
      <div class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">
            {{ currentOrder?.orderNo }}
          </el-descriptions-item>
          <el-descriptions-item label="客户名称">
            {{ currentOrder?.customerName }}
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <StatusTag
              v-if="currentOrder"
              :status="currentOrder.statusCode"
              type="order"
            />
          </el-descriptions-item>
          <el-descriptions-item label="订单金额">
            ¥{{ formatMoney(currentOrder?.totalAmount) }}
          </el-descriptions-item>
          <el-descriptions-item label="创建人">
            {{ currentOrder?.createdBy }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ currentOrder?.createdAt }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 订单行表格 -->
        <div class="order-lines-section">
          <h4>订单明细</h4>
          <el-table :data="currentOrder?.lines" border>
            <el-table-column prop="lineNo" label="行号" width="80" />
            <el-table-column prop="productCode" label="产品编码" />
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="unitPrice" label="单价" width="100">
              <template #default="{ row }">
                ¥{{ formatMoney(row.unitPrice) }}
              </template>
            </el-table-column>
            <el-table-column prop="totalAmount" label="金额" width="100">
              <template #default="{ row }">
                ¥{{ formatMoney(row.totalAmount) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Refresh,
  Search,
  RefreshLeft,
  Download
} from '@element-plus/icons-vue'
import StatusTag from './components/StatusTag.vue'

// ==================== 类型定义 ====================

interface Order {
  id: number
  orderNo: string
  customerName: string
  statusCode: string
  totalAmount: number
  lineCount: number
  createdBy: string
  createdAt: string
  lines?: OrderLine[]
}

interface OrderLine {
  lineNo: number
  productCode: string
  quantity: number
  unitPrice: number
  totalAmount: number
}

interface SearchForm {
  orderNo: string
  customerName: string
  statusCode: string
  dateRange: string[]
}

// ==================== 响应式数据 ====================

const loading = ref(false)
const tableData = ref<Order[]>([])
const selectedRows = ref<Order[]>([])
const detailVisible = ref(false)
const currentOrder = ref<Order | null>(null)

const searchForm = reactive<SearchForm>({
  orderNo: '',
  customerName: '',
  statusCode: '',
  dateRange: []
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// ==================== 模拟数据 ====================

const mockData: Order[] = [
  {
    id: 1,
    orderNo: 'ORD20260105001',
    customerName: '北京某某科技有限公司',
    statusCode: 'EXECUTING',
    totalAmount: 50000,
    lineCount: 3,
    createdBy: '张三',
    createdAt: '2026-01-05 10:30:00',
    lines: [
      { lineNo: 1, productCode: 'P001', quantity: 10, unitPrice: 1000, totalAmount: 10000 },
      { lineNo: 2, productCode: 'P002', quantity: 20, unitPrice: 2000, totalAmount: 40000 }
    ]
  },
  {
    id: 2,
    orderNo: 'ORD20260105002',
    customerName: '上海某某贸易有限公司',
    statusCode: 'COMPLETED',
    totalAmount: 35000,
    lineCount: 2,
    createdBy: '李四',
    createdAt: '2026-01-04 14:20:00'
  },
  {
    id: 3,
    orderNo: 'ORD20260105003',
    customerName: '深圳某某电子有限公司',
    statusCode: 'DRAFT',
    totalAmount: 28000,
    lineCount: 1,
    createdBy: '王五',
    createdAt: '2026-01-03 09:15:00'
  }
]

// ==================== 工具函数 ====================

const formatMoney = (amount: number | undefined): string => {
  if (!amount) return '0.00'
  return amount.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

// ==================== 数据操作 ====================

const fetchData = async () => {
  loading.value = true
  try {
    // 模拟API请求延迟
    await new Promise(resolve => setTimeout(resolve, 500))

    // 模拟分页数据
    const start = (pagination.current - 1) * pagination.size
    const end = start + pagination.size
    tableData.value = mockData.slice(start, end)
    pagination.total = mockData.length
  } finally {
    loading.value = false
  }
}

// ==================== 事件处理 ====================

const handleAdd = () => {
  ElMessage.info('打开新建订单表单')
  // TODO: 跳转到新建订单页面
}

const handleRefresh = () => {
  fetchData()
  ElMessage.success('刷新成功')
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.customerName = ''
  searchForm.statusCode = ''
  searchForm.dateRange = []
  handleSearch()
}

const handleSelectionChange = (selection: Order[]) => {
  selectedRows.value = selection
}

const handleView = (row: Order) => {
  currentOrder.value = row
  detailVisible.value = true
}

const handleEdit = (row: Order) => {
  ElMessage.info(`编辑订单：${row.orderNo}`)
  // TODO: 跳转到编辑页面
}

const handleSubmit = async (row: Order) => {
  try {
    await ElMessageBox.confirm(`确认提交订单 ${row.orderNo}？`, '提示')
    // TODO: 调用提交API
    ElMessage.success('提交成功')
    fetchData()
  } catch {
    // 取消操作
  }
}

const handleDelete = async (row: Order) => {
  try {
    await ElMessageBox.confirm(`确认删除订单 ${row.orderNo}？`, '警告', {
      type: 'warning'
    })
    // TODO: 调用删除API
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // 取消操作
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${selectedRows.value.length} 个订单？`,
      '警告',
      { type: 'warning' }
    )
    // TODO: 调用批量删除API
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // 取消操作
  }
}

const handleExport = () => {
  ElMessage.info('导出功能开发中')
  // TODO: 实现导出功能
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchData()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  fetchData()
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.order-list-container {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }

    .page-actions {
      display: flex;
      gap: 10px;
    }
  }

  .search-card {
    margin-bottom: 16px;

    :deep(.el-card__body) {
      padding: 16px;
    }

    .search-form {
      margin-bottom: 0;

      :deep(.el-form-item) {
        margin-bottom: 0;
        margin-right: 16px;
      }
    }
  }

  .table-card {
    .toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      padding: 0 4px;

      .selected-info {
        color: #409eff;
        font-size: 14px;
      }

      .toolbar-right {
        display: flex;
        gap: 10px;
      }
    }

    .amount {
      font-weight: 600;
      color: #f56c6c;
    }

    .pagination-container {
      display: flex;
      justify-content: flex-end;
      margin-top: 16px;
    }
  }

  .detail-content {
    padding: 20px;

    .order-lines-section {
      margin-top: 24px;

      h4 {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 16px;
      }
    }
  }
}
</style>
