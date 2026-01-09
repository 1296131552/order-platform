<template>
  <div class="billing-create">
    <el-page-header @back="goBack" title="返回对账单列表">
      <template #content>
        <span class="page-title">创建对账单</span>
      </template>
    </el-page-header>

    <el-card class="form-card" style="margin-top: 20px">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="客户" prop="customerId">
          <el-select
            v-model="formData.customerId"
            placeholder="请选择客户"
            filterable
            style="width: 100%"
          >
            <el-option label="客户A" value="1" />
            <el-option label="客户B" value="2" />
          </el-select>
        </el-form-item>

        <el-form-item label="账期" prop="billingPeriod">
          <el-date-picker
            v-model="formData.billingPeriod"
            type="month"
            placeholder="请选择账期"
            value-format="YYYY-MM"
            style="width: 100%"
          />
        </el-form-item>

        <el-divider content-position="left">选择订单</el-divider>

        <el-table
          :data="selectedOrders"
          border
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="orderNo" label="订单编号" width="160" />
          <el-table-column prop="amount" label="订单金额" width="120" />
          <el-table-column prop="shipmentCount" label="发运数" width="80" />
          <el-table-column prop="receiptStatus" label="签收状态" width="100" />
        </el-table>

        <el-form-item style="margin-top: 16px">
          <el-button type="primary" @click="handleSelectOrders">选择订单</el-button>
        </el-form-item>

        <el-form-item label="合计金额">
          <span class="amount">¥{{ totalAmount.toLocaleString() }}</span>
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
            创建对账单
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 表单引用
const formRef = ref()

// 提交状态
const submitting = ref(false)

// 表单数据
const formData = reactive({
  customerId: '',
  billingPeriod: '',
  orderIds: [],
  remark: ''
})

// 表单验证规则
const formRules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  billingPeriod: [{ required: true, message: '请选择账期', trigger: 'change' }]
}

// 选中的订单
const selectedOrders = ref([])

// 计算总金额
const totalAmount = computed(() => {
  return selectedOrders.value.reduce((sum, order) => sum + (order.amount || 0), 0)
})

// 返回
const goBack = () => {
  router.back()
}

// 选择订单
const handleSelectOrders = () => {
  ElMessage.info('选择订单功能开发中')
}

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  formData.orderIds = selection.map(item => item.id)
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    ElMessage.warning('请填写完整信息')
    return
  }

  if (formData.orderIds.length === 0) {
    ElMessage.warning('请选择至少一个订单')
    return
  }

  submitting.value = true
  try {
    // 模拟创建
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('创建成功')
    goBack()
  } catch (error) {
    console.error('创建失败：', error)
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = () => {
  goBack()
}
</script>

<style scoped lang="scss">
.billing-create {
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
