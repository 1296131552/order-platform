<template>
  <div class="receipt-confirm">
    <el-page-header @back="goBack" title="返回签收列表">
      <template #content>
        <span class="page-title">签收确认</span>
      </template>
    </el-page-header>

    <el-card class="info-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>发运信息</span>
          <el-tag :type="getStatusType(shipment?.status)">
            {{ getStatusLabel(shipment?.status) }}
          </el-tag>
        </div>
      </template>

      <el-descriptions :column="2" border v-if="shipment">
        <el-descriptions-item label="发运单号">
          {{ shipment.shipmentNo }}
        </el-descriptions-item>
        <el-descriptions-item label="订单号">
          <el-link type="primary" @click="goToOrder(shipment.orderId)">
            {{ shipment.orderNo }}
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item label="承运商">
          {{ shipment.carrierName }}
        </el-descriptions-item>
        <el-descriptions-item label="车牌号">
          {{ shipment.vehicleNo || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="司机">
          {{ shipment.driverName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="司机电话">
          {{ shipment.driverPhone || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="预计到达时间">
          {{ formatDateTime(shipment.estimatedArrivalTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="路线">
          {{ shipment.route || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="items-card">
      <template #header>
        <div class="card-header">
          <span>快递单明细</span>
          <el-button type="primary" size="small" @click="handleBatchConfirm" :disabled="!hasSelectedItems">
            批量签收
          </el-button>
        </div>
      </template>

      <el-table :data="shipmentLines" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column label="序号" type="index" width="60" />
        <el-table-column label="快递单号" prop="trackingNo" min-width="150" />
        <el-table-column label="承运商" prop="carrierName" width="120" />
        <el-table-column label="发运数量" prop="quantity" width="100" align="right" />
        <el-table-column label="实收数量" width="150">
          <template #default="{ row }">
            <el-input-number
              v-model="row.receivedQuantity"
              :min="0"
              :max="row.quantity * 2"
              :precision="0"
              controls-position="right"
              size="small"
              @change="handleQuantityChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="差异" width="100">
          <template #default="{ row }">
            <el-tag :type="getDifferenceType(row)">
              {{ getDifferenceText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="差异原因" min-width="180">
          <template #default="{ row }">
            <el-input
              v-model="row.differenceReason"
              placeholder="如有差异请填写原因"
              :disabled="!hasDifference(row)"
              size="small"
              clearable
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link 
              size="small" 
              @click="handleSingleConfirm(row)"
              :disabled="row.status === 'SIGNED'"
            >
              {{ row.status === 'SIGNED' ? '已签收' : '签收' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="form-card">
      <template #header>
        <span>签收信息</span>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="签收时间" prop="receiptDate">
              <el-date-picker
                v-model="formData.receiptDate"
                type="datetime"
                placeholder="请选择签收时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="签收人" prop="receiverName">
              <el-input
                v-model="formData.receiverName"
                placeholder="请输入签收人姓名"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="签收照片">
          <Upload
            v-model="formData.photos"
            :limit="6"
            tip="最多上传6张照片，支持 JPG、PNG 格式"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import Upload from '@/components/Upload.vue'
import { getShipmentDetail, type Shipment } from '@/api/shipment'
import { getShipmentLinesByShipmentId, type ShipmentLine } from '@/api/shipmentLine'
import { confirmReceipt, batchConfirmReceipt, type ReceiptConfirmParams } from '@/api/receipt'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()

// 发运ID
const shipmentId = computed(() => Number(route.params.id))

// 表单引用
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 发运信息
const shipment = ref<Shipment | null>(null)

// 快递单列表（带签收信息）
interface ShipmentLineWithReceipt extends ShipmentLine {
  receivedQuantity: number
  differenceReason: string
}
const shipmentLines = ref<ShipmentLineWithReceipt[]>([])

// 选中的快递单
const selectedLines = ref<ShipmentLineWithReceipt[]>([])
const hasSelectedItems = computed(() => selectedLines.value.length > 0)

// 表单数据
const formData = reactive({
  receiptDate: '',
  receiverName: '',
  photos: [] as string[],
  remark: ''
})

// 表单验证规则
const formRules: FormRules = {
  receiptDate: [{ required: true, message: '请选择签收时间', trigger: 'change' }],
  receiverName: [{ required: true, message: '请输入签收人', trigger: 'blur' }]
}

// 获取状态类型
const getStatusType = (status?: string) => {
  const map: Record<string, string> = {
    PENDING: 'info',
    IN_TRANSIT: 'warning',
    DELIVERED: 'success'
  }
  return map[status || ''] || 'info'
}

// 获取状态标签
const getStatusLabel = (status?: string) => {
  const map: Record<string, string> = {
    PENDING: '待提货',
    IN_TRANSIT: '在途',
    DELIVERED: '已到货'
  }
  return map[status || ''] || status || ''
}

// 判断是否有差异
const hasDifference = (row: ShipmentLineWithReceipt) => {
  return row.receivedQuantity !== row.quantity
}

// 获取差异类型
const getDifferenceType = (row: ShipmentLineWithReceipt) => {
  const diff = row.quantity - row.receivedQuantity
  if (diff === 0) return 'success'
  if (diff === row.quantity) return 'danger'
  return 'warning'
}

// 获取差异文本
const getDifferenceText = (row: ShipmentLineWithReceipt) => {
  const diff = row.quantity - row.receivedQuantity
  if (diff === 0) return '无差异'
  if (diff === row.quantity) return '全部缺失'
  if (diff > 0) return `短缺 ${diff}`
  return `多收 ${Math.abs(diff)}`
}

// 数量变化
const handleQuantityChange = (row: ShipmentLineWithReceipt) => {
  // 如果有差异，清空差异原因让用户重新填写
  if (!hasDifference(row)) {
    row.differenceReason = ''
  }
}

// 选择变化
const handleSelectionChange = (selection: ShipmentLineWithReceipt[]) => {
  selectedLines.value = selection
}

// 返回
const goBack = () => {
  router.back()
}

// 跳转到订单
const goToOrder = (orderId: number) => {
  router.push(`/order/${orderId}`)
}

// 加载发运详情
const loadShipmentDetail = async () => {
  loading.value = true
  try {
    shipment.value = await getShipmentDetail(shipmentId.value)
  } catch (error) {
    console.error('加载发运详情失败：', error)
    ElMessage.error('加载发运详情失败')
  } finally {
    loading.value = false
  }
}

// 加载快递单列表
const loadShipmentLines = async () => {
  try {
    const data = await getShipmentLinesByShipmentId(shipmentId.value)
    shipmentLines.value = data.map((item: ShipmentLine) => ({
      ...item,
      receivedQuantity: item.quantity,
      differenceReason: ''
    }))
  } catch (error) {
    console.error('加载快递单列表失败：', error)
    ElMessage.error('加载快递单列表失败')
  }
}

// 验证表单
const validateForm = async (): Promise<boolean> => {
  if (!formRef.value) return false
  return await formRef.value.validate().catch(() => false)
}

// 构建签收参数
const buildConfirmParams = (line: ShipmentLineWithReceipt): ReceiptConfirmParams => {
  const hasDiff = hasDifference(line)
  return {
    shipmentLineId: line.id,
    receivedQuantity: line.receivedQuantity,
    receiptDate: formData.receiptDate,
    receiverName: formData.receiverName,
    hasDifference: hasDiff,
    differenceQuantity: hasDiff ? Math.abs(line.quantity - line.receivedQuantity) : undefined,
    differenceReason: hasDiff ? line.differenceReason : undefined,
    photos: formData.photos.length > 0 ? formData.photos : undefined,
    remark: formData.remark || undefined
  }
}

// 单个签收
const handleSingleConfirm = async (row: ShipmentLineWithReceipt) => {
  const valid = await validateForm()
  if (!valid) {
    ElMessage.warning('请填写完整签收信息')
    return
  }

  // 检查差异原因
  if (hasDifference(row) && !row.differenceReason) {
    ElMessage.warning('请填写差异原因')
    return
  }

  submitting.value = true
  try {
    const params = buildConfirmParams(row)
    await confirmReceipt(params)
    ElMessage.success('签收成功')
    // 更新状态
    row.status = 'SIGNED'
  } catch (error) {
    console.error('签收失败：', error)
    ElMessage.error('签收失败')
  } finally {
    submitting.value = false
  }
}

// 批量签收
const handleBatchConfirm = async () => {
  const valid = await validateForm()
  if (!valid) {
    ElMessage.warning('请填写完整签收信息')
    return
  }

  // 检查差异原因
  const hasUnfilledDiff = selectedLines.value.some(
    item => hasDifference(item) && !item.differenceReason
  )
  if (hasUnfilledDiff) {
    ElMessage.warning('请为有差异的快递单填写差异原因')
    return
  }

  // 过滤已签收的
  const toConfirm = selectedLines.value.filter(item => item.status !== 'SIGNED')
  if (toConfirm.length === 0) {
    ElMessage.warning('所选快递单均已签收')
    return
  }

  submitting.value = true
  try {
    const items = toConfirm.map(line => buildConfirmParams(line))
    await batchConfirmReceipt({ items })
    ElMessage.success(`成功签收 ${toConfirm.length} 个快递单`)
    // 更新状态
    toConfirm.forEach(line => {
      line.status = 'SIGNED'
    })
    selectedLines.value = []
  } catch (error) {
    console.error('批量签收失败：', error)
    ElMessage.error('批量签收失败')
  } finally {
    submitting.value = false
  }
}

// 初始化
onMounted(() => {
  loadShipmentDetail()
  loadShipmentLines()
  // 默认当前时间
  formData.receiptDate = new Date().toISOString().slice(0, 19).replace('T', ' ')
})
</script>

<style scoped lang="scss">
.receipt-confirm {
  padding: 20px;

  .page-title {
    font-size: 16px;
    font-weight: 500;
  }

  .info-card,
  .items-card,
  .form-card {
    margin-top: 20px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  :deep(.el-form-item__content) {
    .el-date-picker {
      width: 100%;
    }
  }
}
</style>
