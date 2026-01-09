<template>
  <div class="billing-list">
    <!-- 搜索区域 -->
    <SearchBar
      :items="searchItems"
      @search="handleSearch"
      @reset="handleReset"
    />

    <!-- 操作按钮 -->
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="handleCreate">创建对账单</el-button>
      <el-button :icon="Download" @click="handleExport">导出</el-button>
    </div>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <TableForm
        :data="tableData"
        :total="total"
        :loading="loading"
        v-model:page="page"
        v-model:page-size="pageSize"
      >
        <el-table-column prop="billingNo" label="对账单号" width="160" />
        <el-table-column prop="customerName" label="客户名称" min-width="150" />
        <el-table-column prop="billingPeriod" label="账期" width="120" />
        <el-table-column prop="orderCount" label="订单数" width="80" align="center" />
        <el-table-column prop="shipmentCount" label="发运数" width="80" align="center" />
        <el-table-column prop="totalAmount" label="总金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.totalAmount?.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="invoiceNo" label="发票号" width="140" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="primary" link size="small" @click="handleConfirm(row)" v-if="row.status === 'pending'">
              确认
            </el-button>
            <el-button type="success" link size="small" @click="handleComplete(row)" v-if="row.status === 'confirmed'">
              完成
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)" v-if="['pending', 'confirmed'].includes(row.status)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </TableForm>
    </el-card>

    <!-- 确认对话框 -->
    <el-dialog v-model="confirmVisible" title="确认对账单" width="600px">
      <el-form :model="confirmForm" label-width="100px">
        <el-form-item label="发票号" required>
          <el-input v-model="confirmForm.invoiceNo" placeholder="请输入发票号" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="confirmForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download } from '@element-plus/icons-vue'
import SearchBar from '@/components/SearchBar.vue'
import TableForm from '@/components/TableForm.vue'
import { getBillingList, deleteBilling, confirmBilling, completeBilling, type BillingQueryParams } from '@/api/billing'

const router = useRouter()

// 搜索配置
const searchItems = [
  { prop: 'billingNo', label: '对账单号', type: 'input' },
  { prop: 'customerName', label: '客户名称', type: 'input' },
  { prop: 'status', label: '状态', type: 'select', options: [
    { label: '待确认', value: 'pending' },
    { label: '已确认', value: 'confirmed' },
    { label: '已完成', value: 'completed' },
    { label: '已取消', value: 'cancelled' }
  ]}
]

// 表格数据
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

// 搜索表单
const searchForm = ref<BillingQueryParams>({})

// 确认对话框
const confirmVisible = ref(false)
const currentBilling = ref<any>(null)
const confirmForm = reactive({
  invoiceNo: '',
  remark: ''
})

// 模拟数据
const mockData = [
  {
    id: 1,
    billingNo: 'BIL202601001',
    customerName: '北京科技有限公司',
    status: 'pending',
    totalAmount: 125000,
    orderCount: 5,
    shipmentCount: 3,
    billingPeriod: '2026-01',
    createTime: '2026-01-05 10:30:00'
  },
  {
    id: 2,
    billingNo: 'BIL202601002',
    customerName: '上海贸易公司',
    status: 'confirmed',
    totalAmount: 89500,
    orderCount: 3,
    shipmentCount: 2,
    billingPeriod: '2026-01',
    invoiceNo: 'INV20260105001',
    createTime: '2026-01-04 14:20:00'
  },
  {
    id: 3,
    billingNo: 'BIL202512003',
    customerName: '深圳电子厂',
    status: 'completed',
    totalAmount: 210000,
    orderCount: 8,
    shipmentCount: 5,
    billingPeriod: '2025-12',
    invoiceNo: 'INV20251231002',
    createTime: '2025-12-31 16:00:00'
  }
]

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm.value,
      page: page.value,
      pageSize: pageSize.value
    }
    const res = await getBillingList(params)
    tableData.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('加载对账单列表失败：', error)
    // API 失败时使用模拟数据
    tableData.value = mockData
    total.value = mockData.length
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = (params: any) => {
  searchForm.value = params
  page.value = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.value = {}
  page.value = 1
  loadData()
}

// 新建
const handleCreate = () => {
  router.push('/billing/create')
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 查看
const handleView = (row: any) => {
  router.push(`/billing/${row.id}`)
}

// 确认
const handleConfirm = (row: any) => {
  currentBilling.value = row
  confirmForm.invoiceNo = row.invoiceNo || ''
  confirmForm.remark = row.remark || ''
  confirmVisible.value = true
}

// 确认提交
const handleConfirmSubmit = async () => {
  if (!confirmForm.invoiceNo) {
    ElMessage.warning('请输入发票号')
    return
  }

  try {
    await confirmBilling(currentBilling.value.id, confirmForm)
    ElMessage.success('确认成功')
    confirmVisible.value = false
    loadData()
  } catch (error) {
    console.error('确认失败：', error)
  }
}

// 完成
const handleComplete = (row: any) => {
  ElMessageBox.confirm(`确定要完成对账单 "${row.billingNo}" 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await completeBilling(row.id)
      ElMessage.success('操作成功')
      loadData()
    } catch (error) {
      console.error('操作失败：', error)
    }
  }).catch(() => {})
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除对账单 "${row.billingNo}" 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteBilling(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败：', error)
    }
  }).catch(() => {})
}

// 状态类型
const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    pending: 'warning',
    confirmed: 'primary',
    completed: 'success',
    cancelled: 'info'
  }
  return map[status] || 'info'
}

// 状态标签
const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    pending: '待确认',
    confirmed: '已确认',
    completed: '已完成',
    cancelled: '已取消'
  }
  return map[status] || status
}

// 初始化
loadData()
</script>

<style scoped lang="scss">
.billing-list {
  .toolbar {
    margin-bottom: 16px;
  }
  .table-card {
    :deep(.el-pagination) {
      margin-top: 16px;
    }
  }
}
</style>
