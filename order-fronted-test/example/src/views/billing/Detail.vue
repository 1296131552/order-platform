<template>
  <div class="billing-detail" v-loading="loading">
    <el-page-header @back="goBack" title="返回对账单列表">
      <template #content>
        <span class="page-title">{{ billing?.billingNo }}</span>
        <el-tag :type="getStatusType(billing?.status)" style="margin-left: 12px">
          {{ getStatusLabel(billing?.status) }}
        </el-tag>
      </template>
      <template #extra>
        <el-button
          v-if="billing?.status === 'pending'"
          type="primary"
          @click="handleConfirm"
        >
          确认对账
        </el-button>
        <el-button
          v-if="billing?.status === 'confirmed'"
          type="success"
          @click="handleComplete"
        >
          完成对账
        </el-button>
        <el-button @click="handleExport">导出</el-button>
      </template>
    </el-page-header>

    <!-- 基本信息 -->
    <el-card class="info-card" style="margin-top: 20px">
      <template #header>
        <span>基本信息</span>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="对账单号">
          {{ billing?.billingNo }}
        </el-descriptions-item>
        <el-descriptions-item label="客户名称">
          {{ billing?.customerName }}
        </el-descriptions-item>
        <el-descriptions-item label="账期">
          {{ billing?.billingPeriod }}
        </el-descriptions-item>
        <el-descriptions-item label="订单数量">
          {{ billing?.orderCount }}
        </el-descriptions-item>
        <el-descriptions-item label="发运数量">
          {{ billing?.shipmentCount }}
        </el-descriptions-item>
        <el-descriptions-item label="对账状态">
          <el-tag :type="getStatusType(billing?.status)">
            {{ getStatusLabel(billing?.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发票号" :span="2">
          {{ billing?.invoiceNo || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="总金额">
          <span class="amount">¥{{ billing?.totalAmount?.toLocaleString() }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ billing?.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="1">
          {{ billing?.remark || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 对账明细 -->
    <el-card class="items-card" style="margin-top: 20px">
      <template #header>
        <span>对账明细</span>
      </template>
      <el-table :data="items" border>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="orderNo" label="订单编号" width="160" />
        <el-table-column prop="shipmentNo" label="发运单号" width="160" />
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.amount?.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="receiptStatus" label="签收状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getReceiptStatusType(row.receiptStatus)">
              {{ getReceiptStatusLabel(row.receiptStatus) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
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
import { ref, computed, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBillingDetail, confirmBilling, completeBilling } from '@/api/billing'
import { RECEIPT_STATUS_MAP } from '@/utils/constants'

const route = useRoute()
const router = useRouter()

const billingId = computed(() => Number(route.params.id))

// 加载状态
const loading = ref(false)

// 数据
const billing = ref<any>(null)
const items = ref([])

// 确认对话框
const confirmVisible = ref(false)
const confirmForm = reactive({
  invoiceNo: '',
  remark: ''
})

// 加载对账单详情
const loadBillingDetail = async () => {
  loading.value = true
  try {
    const res = await getBillingDetail(billingId.value)
    billing.value = res.billing
    items.value = res.items
  } catch (error) {
    console.error('加载对账单详情失败：', error)
  } finally {
    loading.value = false
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 确认对账
const handleConfirm = () => {
  confirmForm.invoiceNo = billing.value.invoiceNo || ''
  confirmForm.remark = billing.value.remark || ''
  confirmVisible.value = true
}

// 确认提交
const handleConfirmSubmit = async () => {
  if (!confirmForm.invoiceNo) {
    ElMessage.warning('请输入发票号')
    return
  }

  try {
    await confirmBilling(billingId.value, confirmForm)
    ElMessage.success('确认成功')
    confirmVisible.value = false
    loadBillingDetail()
  } catch (error) {
    console.error('确认失败：', error)
  }
}

// 完成对账
const handleComplete = async () => {
  try {
    await completeBilling(billingId.value)
    ElMessage.success('操作成功')
    loadBillingDetail()
  } catch (error) {
    console.error('操作失败：', error)
  }
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
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

// 签收状态类型
const getReceiptStatusType = (status: string) => {
  return RECEIPT_STATUS_MAP[status]?.type || 'info'
}

// 签收状态标签
const getReceiptStatusLabel = (status: string) => {
  return RECEIPT_STATUS_MAP[status]?.label || status
}

// 初始化
onMounted(() => {
  loadBillingDetail()
})
</script>

<style scoped lang="scss">
.billing-detail {
  padding: 20px;

  .page-title {
    font-size: 16px;
    font-weight: 500;
  }

  .amount {
    font-size: 18px;
    font-weight: bold;
    color: var(--el-color-danger);
  }
}
</style>
