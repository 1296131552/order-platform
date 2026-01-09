<template>
  <div class="data-export">
    <el-page-header title="返回" @back="goBack">
      <template #content>
        <span class="page-title">数据导出</span>
      </template>
    </el-page-header>

    <el-card class="export-card" style="margin-top: 20px">
      <el-form ref="formRef" :model="formData" label-width="120px">
        <!-- 导出类型 -->
        <el-form-item label="导出类型">
          <el-radio-group v-model="formData.exportType">
            <el-radio value="order">订单数据</el-radio>
            <el-radio value="shipment">发运数据</el-radio>
            <el-radio value="receipt">签收数据</el-radio>
            <el-radio value="supplier">供应商数据</el-radio>
            <el-radio value="carrier">承运商数据</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 时间范围 -->
        <el-form-item label="时间范围">
          <el-radio-group v-model="dateRangeType">
            <el-radio value="all">全部</el-radio>
            <el-radio value="custom">自定义</el-radio>
            <el-radio value="today">今天</el-radio>
            <el-radio value="week">本周</el-radio>
            <el-radio value="month">本月</el-radio>
            <el-radio value="year">今年</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="dateRangeType === 'custom'" label="选择日期">
          <el-date-picker
            v-model="formData.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <!-- 筛选条件 -->
        <el-divider content-position="left">筛选条件（可选）</el-divider>

        <el-row :gutter="20">
          <el-col :span="12" v-if="formData.exportType === 'order'">
            <el-form-item label="客户">
              <el-select v-model="formData.customerId" placeholder="请选择客户" clearable>
                <el-option label="客户A" value="1" />
                <el-option label="客户B" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="['order', 'shipment'].includes(formData.exportType)">
            <el-form-item label="状态">
              <el-select v-model="formData.status" placeholder="请选择状态" clearable>
                <el-option label="全部" value="" />
                <el-option label="执行中" value="executing" />
                <el-option label="已完成" value="completed" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 导出字段 -->
        <el-divider content-position="left">导出字段配置</el-divider>

        <el-form-item label="选择字段">
          <el-checkbox-group v-model="selectedFields">
            <el-checkbox
              v-for="field in availableFields"
              :key="field.value"
              :label="field.value"
            >
              {{ field.label }}
            </el-checkbox>
          </el-checkbox-group>
          <div style="margin-top: 8px">
            <el-link type="primary" @click="selectAllFields">全选</el-link>
            <el-link type="primary" @click="clearAllFields" style="margin-left: 12px">清空</el-link>
            <el-link type="primary" @click="selectDefaultFields" style="margin-left: 12px">默认字段</el-link>
          </div>
        </el-form-item>

        <!-- 导出格式 -->
        <el-form-item label="导出格式">
          <el-radio-group v-model="formData.format">
            <el-radio value="xlsx">Excel (.xlsx)</el-radio>
            <el-radio value="xls">Excel (.xls)</el-radio>
            <el-radio value="csv">CSV (.csv)</el-radio>
            <el-radio value="pdf">PDF (.pdf)</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleExport" :loading="exporting">
            <el-icon><Download /></el-icon>
            开始导出
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 导出历史 -->
    <el-card class="history-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>导出历史</span>
          <el-button type="primary" size="small" @click="loadHistory">刷新</el-button>
        </div>
      </template>
      <el-table :data="historyList" border>
        <el-table-column prop="exportType" label="导出类型" width="120">
          <template #default="{ row }">
            {{ getExportTypeLabel(row.exportType) }}
          </template>
        </el-table-column>
        <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="recordCount" label="记录数" width="100" />
        <el-table-column prop="format" label="格式" width="80" />
        <el-table-column prop="createTime" label="导出时间" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'warning'" size="small">
              {{ row.status === 'success' ? '成功' : '处理中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'success'"
              type="primary"
              link
              size="small"
              @click="handleDownload(row)"
            >
              下载
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download } from '@element-plus/icons-vue'

const router = useRouter()

// 表单数据
const formData = ref({
  exportType: 'order',
  dateRange: [],
  customerId: '',
  status: '',
  format: 'xlsx'
})

// 时间范围类型
const dateRangeType = ref('all')

// 选中的字段
const selectedFields = ref([])

// 导出状态
const exporting = ref(false)

// 导出历史
const historyList = ref([
  {
    id: 1,
    exportType: 'order',
    fileName: '订单数据_20260106.xlsx',
    recordCount: 150,
    format: 'xlsx',
    status: 'success',
    createTime: '2026-01-06 10:30:00'
  },
  {
    id: 2,
    exportType: 'shipment',
    fileName: '发运数据_20260105.xlsx',
    recordCount: 89,
    format: 'xlsx',
    status: 'success',
    createTime: '2026-01-05 15:20:00'
  }
])

// 可用字段
const availableFields = computed(() => {
  const fieldsMap: Record<string, any[]> = {
    order: [
      { value: 'orderNo', label: '订单编号' },
      { value: 'customerName', label: '客户名称' },
      { value: 'status', label: '订单状态' },
      { value: 'totalAmount', label: '订单金额' },
      { value: 'deliveryDate', label: '交货日期' },
      { value: 'deliveryAddress', label: '交货地址' },
      { value: 'contactPerson', label: '联系人' },
      { value: 'contactPhone', label: '联系电话' },
      { value: 'createTime', label: '创建时间' },
      { value: 'updateTime', label: '更新时间' }
    ],
    shipment: [
      { value: 'shipmentNo', label: '发运单号' },
      { value: 'orderNo', label: '订单编号' },
      { value: 'carrierName', label: '承运商' },
      { value: 'vehicleNo', label: '车牌号' },
      { value: 'route', label: '路线' },
      { value: 'status', label: '发运状态' },
      { value: 'createTime', label: '创建时间' }
    ],
    receipt: [
      { value: 'receiptNo', label: '签收单号' },
      { value: 'shipmentNo', label: '发运单号' },
      { value: 'receiptTime', label: '签收时间' },
      { value: 'receiptPerson', label: '签收人' },
      { value: 'hasDifference', label: '是否有差异' },
      { value: 'status', label: '签收状态' }
    ],
    supplier: [
      { value: 'code', label: '供应商编码' },
      { value: 'name', label: '供应商名称' },
      { value: 'contactPerson', label: '联系人' },
      { value: 'contactPhone', label: '联系电话' },
      { value: 'address', label: '地址' },
      { value: 'status', label: '状态' }
    ],
    carrier: [
      { value: 'code', label: '承运商编码' },
      { value: 'name', label: '承运商名称' },
      { value: 'contactPerson', label: '联系人' },
      { value: 'contactPhone', label: '联系电话' },
      { value: 'address', label: '地址' },
      { value: 'status', label: '状态' }
    ]
  }
  return fieldsMap[formData.value.exportType] || []
})

// 返回
const goBack = () => {
  router.back()
}

// 获取导出类型标签
const getExportTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    order: '订单数据',
    shipment: '发运数据',
    receipt: '签收数据',
    supplier: '供应商数据',
    carrier: '承运商数据'
  }
  return map[type] || type
}

// 全选字段
const selectAllFields = () => {
  selectedFields.value = availableFields.value.map(f => f.value)
}

// 清空字段
const clearAllFields = () => {
  selectedFields.value = []
}

// 默认字段
const selectDefaultFields = () => {
  selectedFields.value = availableFields.value.slice(0, 6).map(f => f.value)
}

// 导出
const handleExport = () => {
  if (selectedFields.value.length === 0) {
    ElMessage.warning('请至少选择一个导出字段')
    return
  }

  exporting.value = true
  setTimeout(() => {
    exporting.value = false
    ElMessage.success('导出成功')
    loadHistory()
  }, 2000)
}

// 重置
const handleReset = () => {
  formData.value = {
    exportType: 'order',
    dateRange: [],
    customerId: '',
    status: '',
    format: 'xlsx'
  }
  dateRangeType.value = 'all'
  selectedFields.value = []
}

// 加载历史
const loadHistory = () => {
  ElMessage.info('刷新导出历史')
}

// 下载
const handleDownload = (row: any) => {
  ElMessage.success(`下载 ${row.fileName}`)
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除 "${row.fileName}" 吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// 监听导出类型变化，重置字段选择
watch(() => formData.value.exportType, () => {
  selectedFields.value = []
  selectDefaultFields()
})

// 初始化默认字段
selectDefaultFields()
</script>

<style scoped lang="scss">
.data-export {
  padding: 20px;

  .page-title {
    font-size: 16px;
    font-weight: 500;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  :deep(.el-checkbox-group) {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
  }
}
</style>
