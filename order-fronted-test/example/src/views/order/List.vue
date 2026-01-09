<template>
  <div class="order-list-container">
    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单编号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="请输入关键词" clearable />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 140px">
            <el-option label="全部" value="" />
            <el-option label="草稿" value="DRAFT" />
            <el-option label="执行中" value="EXECUTING" />
            <el-option label="部分到货" value="PARTIALLY_RECEIVED" />
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
      <el-button :icon="Download" @click="handleExport">导出</el-button>
    </div>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column type="selection" width="55" />
        <el-table-column prop="orderNo" label="订单编号" width="160" fixed />
        <el-table-column prop="customerName" label="客户名称" width="180" />
        <el-table-column prop="lineCount" label="订单行数" width="100" align="center" />
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
        <el-table-column prop="createdByName" label="创建人" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button 
              v-if="row.status === 'DRAFT'" 
              type="primary" 
              link 
              size="small" 
              @click="handleEdit(row)"
            >编辑</el-button>
            <el-button 
              v-if="row.status === 'DRAFT'" 
              type="danger" 
              link 
              size="small" 
              @click="handleDelete(row)"
            >删除</el-button>
            <el-button 
              v-if="row.status === 'DRAFT' || row.status === 'EXECUTING'" 
              type="warning" 
              link 
              size="small" 
              @click="handleCancel(row)"
            >取消</el-button>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Download } from '@element-plus/icons-vue'
import { 
  getOrderList, 
  deleteOrder, 
  cancelOrder, 
  exportOrders,
  type Order, 
  type OrderStatus,
  type OrderQueryParams 
} from '@/api/order'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  keyword: '',
  status: '' as OrderStatus | '',
  dateRange: [] as string[]
})

// 表格数据
const loading = ref(false)
const tableData = ref<Order[]>([])

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 获取查询参数
function getQueryParams(): OrderQueryParams {
  const params: OrderQueryParams = {
    page: pagination.page,
    pageSize: pagination.pageSize
  }
  
  if (searchForm.orderNo) {
    params.orderNo = searchForm.orderNo
  }
  if (searchForm.keyword) {
    params.keyword = searchForm.keyword
  }
  if (searchForm.status) {
    params.status = searchForm.status as OrderStatus
  }
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    params.startDate = searchForm.dateRange[0]
    params.endDate = searchForm.dateRange[1]
  }
  
  return params
}

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const params = getQueryParams()
    const res = await getOrderList(params)
    tableData.value = res.records
    pagination.total = res.total
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  loadData()
}

// 重置
function handleReset() {
  Object.assign(searchForm, {
    orderNo: '',
    keyword: '',
    status: '',
    dateRange: []
  })
  pagination.page = 1
  loadData()
}

// 新建
function handleCreate() {
  router.push('/order/create')
}

// 导出
async function handleExport() {
  try {
    const params = getQueryParams()
    const blob = await exportOrders(params) as unknown as Blob
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `订单列表_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 查看
function handleView(row: Order) {
  router.push(`/order/${row.id}`)
}

// 编辑
function handleEdit(row: Order) {
  router.push(`/order/edit/${row.id}`)
}

// 删除
async function handleDelete(row: Order) {
  try {
    await ElMessageBox.confirm(`确定要删除订单 ${row.orderNo} 吗？`, '提示', {
      type: 'warning'
    })
    await deleteOrder(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 取消订单
async function handleCancel(row: Order) {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入取消原因（可选）'
    })
    await cancelOrder(row.id, reason)
    ElMessage.success('订单已取消')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    }
  }
}

// 分页
function handleSizeChange(size: number) {
  pagination.pageSize = size
  loadData()
}

function handleCurrentChange(page: number) {
  pagination.page = page
  loadData()
}

// 状态类型
function getStatusType(status: OrderStatus): '' | 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<OrderStatus, '' | 'success' | 'warning' | 'info' | 'danger'> = {
    DRAFT: 'info',
    EXECUTING: 'warning',
    PARTIALLY_RECEIVED: '',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return map[status] || 'info'
}

// 状态文本
function getStatusText(status: OrderStatus): string {
  const map: Record<OrderStatus, string> = {
    DRAFT: '草稿',
    EXECUTING: '执行中',
    PARTIALLY_RECEIVED: '部分到货',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || '未知'
}

// 初始化
onMounted(() => {
  loadData()
})
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
