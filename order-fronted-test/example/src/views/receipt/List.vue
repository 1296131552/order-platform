<template>
  <div class="receipt-list-container">
    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="快递单号">
          <el-input v-model="searchForm.trackingNo" placeholder="请输入快递单号" clearable />
        </el-form-item>
        <el-form-item label="签收状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="待签收" value="PENDING" />
            <el-option label="已签收" value="RECEIVED" />
            <el-option label="有差异" value="DIFFERENCE" />
            <el-option label="已处理" value="PROCESSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否有差异">
          <el-select v-model="searchForm.hasDifference" placeholder="请选择" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="有差异" :value="true" />
            <el-option label="无差异" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="签收日期">
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

    <!-- Tab切换 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部签收记录" name="all" />
      <el-tab-pane label="差异记录" name="difference" />
    </el-tabs>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="receiptNo" label="签收单号" width="150" fixed />
        <el-table-column prop="trackingNo" label="快递单号" width="150" />
        <el-table-column prop="shipmentNo" label="发运单号" width="150" />
        <el-table-column prop="orderNo" label="订单号" width="150" />
        <el-table-column prop="receiverName" label="签收人" width="100" />
        <el-table-column prop="receiptDate" label="签收日期" width="120" />
        <el-table-column prop="expectedQuantity" label="预期数量" width="100" align="right" />
        <el-table-column prop="receivedQuantity" label="实收数量" width="100" align="right" />
        <el-table-column prop="differenceQuantity" label="差异数量" width="100" align="right">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.differenceQuantity > 0 }">
              {{ row.differenceQuantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="hasDifference" label="是否有差异" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.hasDifference ? 'danger' : 'success'">
              {{ row.hasDifference ? '有差异' : '无差异' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button 
              v-if="row.hasDifference && row.status === 'DIFFERENCE'" 
              type="warning" 
              link 
              size="small" 
              @click="handleDifferenceProcess(row)"
            >
              处理差异
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

    <!-- 签收详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="签收详情" width="600px">
      <el-descriptions :column="2" border v-if="currentReceipt">
        <el-descriptions-item label="签收单号">{{ currentReceipt.receiptNo }}</el-descriptions-item>
        <el-descriptions-item label="快递单号">{{ currentReceipt.trackingNo }}</el-descriptions-item>
        <el-descriptions-item label="发运单号">{{ currentReceipt.shipmentNo }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ currentReceipt.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="签收人">{{ currentReceipt.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="签收日期">{{ currentReceipt.receiptDate }}</el-descriptions-item>
        <el-descriptions-item label="预期数量">{{ currentReceipt.expectedQuantity }}</el-descriptions-item>
        <el-descriptions-item label="实收数量">{{ currentReceipt.receivedQuantity }}</el-descriptions-item>
        <el-descriptions-item label="差异数量">
          <span :class="{ 'text-danger': currentReceipt.differenceQuantity > 0 }">
            {{ currentReceipt.differenceQuantity }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentReceipt.status)">
            {{ getStatusText(currentReceipt.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="差异原因" :span="2" v-if="currentReceipt.differenceReason">
          {{ currentReceipt.differenceReason }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2" v-if="currentReceipt.remark">
          {{ currentReceipt.remark }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 差异处理对话框 -->
    <el-dialog v-model="differenceDialogVisible" title="处理差异" width="500px">
      <el-form ref="differenceFormRef" :model="differenceForm" :rules="differenceRules" label-width="100px">
        <el-form-item label="处理方案" prop="solution">
          <el-input
            v-model="differenceForm.solution"
            type="textarea"
            :rows="4"
            placeholder="请输入差异处理方案"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="differenceForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="differenceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitDifferenceHandle">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { 
  getReceiptList, 
  getDifferenceRecords, 
  getReceiptDetail,
  handleDifference,
  type Receipt,
  type ReceiptQueryParams,
  type DifferenceRecordQueryParams
} from '@/api/receipt'

// Tab切换
const activeTab = ref('all')

// 搜索表单
const searchForm = reactive({
  trackingNo: '',
  status: '',
  hasDifference: '' as '' | boolean,
  dateRange: [] as string[]
})

// 表格数据
const loading = ref(false)
const tableData = ref<Receipt[]>([])

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 详情对话框
const detailDialogVisible = ref(false)
const currentReceipt = ref<Receipt | null>(null)

// 差异处理对话框
const differenceDialogVisible = ref(false)
const differenceFormRef = ref<FormInstance>()
const differenceForm = reactive({
  receiptId: 0,
  solution: '',
  remark: ''
})
const differenceRules: FormRules = {
  solution: [{ required: true, message: '请输入处理方案', trigger: 'blur' }]
}
const submitting = ref(false)

// 加载签收列表
async function loadReceiptList() {
  loading.value = true
  try {
    const params: ReceiptQueryParams = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    
    if (searchForm.trackingNo) {
      params.keyword = searchForm.trackingNo
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    if (searchForm.hasDifference !== '') {
      params.hasDifference = searchForm.hasDifference as boolean
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    const res = await getReceiptList(params)
    tableData.value = res.records
    pagination.total = res.total
  } catch (error) {
    console.error('加载签收列表失败：', error)
    ElMessage.error('加载签收列表失败')
  } finally {
    loading.value = false
  }
}

// 加载差异记录
async function loadDifferenceRecords() {
  loading.value = true
  try {
    const params: DifferenceRecordQueryParams = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    
    const res = await getDifferenceRecords(params)
    tableData.value = res.records
    pagination.total = res.total
  } catch (error) {
    console.error('加载差异记录失败：', error)
    ElMessage.error('加载差异记录失败')
  } finally {
    loading.value = false
  }
}

// Tab切换
function handleTabChange(tab: string) {
  pagination.page = 1
  if (tab === 'all') {
    loadReceiptList()
  } else {
    loadDifferenceRecords()
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  if (activeTab.value === 'all') {
    loadReceiptList()
  } else {
    loadDifferenceRecords()
  }
}

// 重置
function handleReset() {
  Object.assign(searchForm, {
    trackingNo: '',
    status: '',
    hasDifference: '',
    dateRange: []
  })
  pagination.page = 1
  if (activeTab.value === 'all') {
    loadReceiptList()
  } else {
    loadDifferenceRecords()
  }
}

// 查看详情
async function handleView(row: Receipt) {
  try {
    const detail = await getReceiptDetail(row.id)
    currentReceipt.value = detail
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取签收详情失败：', error)
    ElMessage.error('获取签收详情失败')
  }
}

// 处理差异
function handleDifferenceProcess(row: Receipt) {
  differenceForm.receiptId = row.id
  differenceForm.solution = ''
  differenceForm.remark = ''
  differenceDialogVisible.value = true
}

// 提交差异处理
async function submitDifferenceHandle() {
  if (!differenceFormRef.value) return
  
  const valid = await differenceFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitting.value = true
  try {
    await handleDifference(differenceForm.receiptId, {
      solution: differenceForm.solution,
      remark: differenceForm.remark
    })
    ElMessage.success('差异处理成功')
    differenceDialogVisible.value = false
    // 刷新列表
    if (activeTab.value === 'all') {
      loadReceiptList()
    } else {
      loadDifferenceRecords()
    }
  } catch (error) {
    console.error('差异处理失败：', error)
    ElMessage.error('差异处理失败')
  } finally {
    submitting.value = false
  }
}

// 分页
function handleSizeChange(size: number) {
  pagination.pageSize = size
  if (activeTab.value === 'all') {
    loadReceiptList()
  } else {
    loadDifferenceRecords()
  }
}

function handleCurrentChange(page: number) {
  pagination.page = page
  if (activeTab.value === 'all') {
    loadReceiptList()
  } else {
    loadDifferenceRecords()
  }
}

// 状态类型
function getStatusType(status: string) {
  const map: Record<string, string> = {
    PENDING: 'info',
    RECEIVED: 'success',
    DIFFERENCE: 'warning',
    PROCESSED: 'success'
  }
  return map[status] || 'info'
}

// 状态文本
function getStatusText(status: string) {
  const map: Record<string, string> = {
    PENDING: '待签收',
    RECEIVED: '已签收',
    DIFFERENCE: '有差异',
    PROCESSED: '已处理'
  }
  return map[status] || '未知'
}

// 初始化
onMounted(() => {
  loadReceiptList()
})
</script>

<style scoped lang="scss">
.receipt-list-container {
  padding: 20px;

  .search-card {
    margin-bottom: 16px;

    .search-form {
      margin-bottom: -10px;

      .el-form-item {
        margin-bottom: 10px;
      }
    }
  }

  .el-tabs {
    margin-bottom: 16px;
  }

  .table-card {
    .el-pagination {
      margin-top: 16px;
      justify-content: flex-end;
    }
  }

  .text-danger {
    color: #f56c6c;
    font-weight: bold;
  }
}
</style>
