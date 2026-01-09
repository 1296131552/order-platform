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
        <span>货物明细</span>
      </template>

      <el-table :data="receiptItems" border>
        <el-table-column label="序号" type="index" width="60" />
        <el-table-column label="商品名称" prop="productName" min-width="150" />
        <el-table-column label="发运数量" prop="quantity" width="120" />
        <el-table-column label="单位" prop="unit" width="80" />
        <el-table-column label="实收数量" width="150">
          <template #default="{ row }">
            <el-input-number
              v-model="row.receivedQuantity"
              :min="0"
              :max="row.quantity"
              :precision="0"
              controls-position="right"
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
              clearable
            />
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
            <el-form-item label="签收时间" prop="receiptTime">
              <el-date-picker
                v-model="formData.receiptTime"
                type="datetime"
                placeholder="请选择签收时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="签收人" prop="receiptPerson">
              <el-input
                v-model="formData.receiptPerson"
                placeholder="请输入签收人姓名"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="签收照片">
          <Upload
            v-model="formData.receiptPhotos"
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

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确认签收
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
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
import { getShipmentDetail } from '@/api/shipment'
import { getReceiptByShipment, confirmReceipt } from '@/api/receipt'
import { formatDateTime } from '@/utils/format'
import { SHIPMENT_STATUS_MAP } from '@/utils/constants'

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
const shipment = ref<any>(null)

// 签收明细
const receiptItems = ref<Array<{
  id: number
  shipmentItemId: number
  productName: string
  quantity: number
  unit: string
  receivedQuantity: number
  differenceReason: string
}>>([])

// 表单数据
const formData = reactive({
  receiptTime: '',
  receiptPerson: '',
  receiptPhotos: [] as string[],
  remark: ''
})

// 表单验证规则
const formRules: FormRules = {
  receiptTime: [{ required: true, message: '请选择签收时间', trigger: 'change' }],
  receiptPerson: [{ required: true, message: '请输入签收人', trigger: 'blur' }]
}

// 获取状态类型
const getStatusType = (status: string) => {
  return SHIPMENT_STATUS_MAP[status]?.type || 'info'
}

// 获取状态标签
const getStatusLabel = (status: string) => {
  return SHIPMENT_STATUS_MAP[status]?.label || status
}

// 判断是否有差异
const hasDifference = (row: any) => {
  return row.receivedQuantity < row.quantity
}

// 获取差异类型
const getDifferenceType = (row: any) => {
  const diff = row.quantity - row.receivedQuantity
  if (diff === 0) return 'success'
  if (diff === row.quantity) return 'danger'
  return 'warning'
}

// 获取差异文本
const getDifferenceText = (row: any) => {
  const diff = row.quantity - row.receivedQuantity
  if (diff === 0) return '无差异'
  if (diff === row.quantity) return '全部缺失'
  return `短缺 ${diff}`
}

// 数量变化
const handleQuantityChange = (row: any) => {
  // 默认设置为发运数量
  if (row.receivedQuantity === undefined) {
    row.receivedQuantity = row.quantity
  }
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
  } finally {
    loading.value = false
  }
}

// 加载签收明细
const loadReceiptItems = async () => {
  try {
    const data = await getReceiptByShipment(shipmentId.value)
    receiptItems.value = data.items.map((item: any) => ({
      ...item,
      receivedQuantity: item.quantity,
      differenceReason: ''
    }))
  } catch (error) {
    console.error('加载签收明细失败：', error)
  }
}

// 提交签收
const handleSubmit = async () => {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    ElMessage.warning('请填写完整信息')
    return
  }

  // 检查是否有差异但未填写原因
  const hasUnfilledDiff = receiptItems.value.some(
    item => hasDifference(item) && !item.differenceReason
  )
  if (hasUnfilledDiff) {
    ElMessage.warning('请为有差异的商品填写差异原因')
    return
  }

  submitting.value = true
  try {
    await confirmReceipt({
      shipmentId: shipmentId.value,
      ...formData,
      receiptPhotos: typeof formData.receiptPhotos === 'string'
        ? [formData.receiptPhotos]
        : formData.receiptPhotos,
      hasDifference: receiptItems.value.some(item => hasDifference(item)),
      items: receiptItems.value.map(item => ({
        shipmentItemId: item.id,
        receivedQuantity: item.receivedQuantity,
        differenceReason: item.differenceReason
      }))
    })
    ElMessage.success('签收确认成功')
    router.push('/receipt')
  } catch (error) {
    console.error('签收确认失败：', error)
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = () => {
  goBack()
}

// 初始化
onMounted(() => {
  loadShipmentDetail()
  loadReceiptItems()
  // 默认当前时间
  formData.receiptTime = new Date().toISOString().slice(0, 19).replace('T', ' ')
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
