<template>
  <div class="order-create">
    <el-page-header @back="goBack" title="返回订单列表">
      <template #content>
        <span class="page-title">{{ isEdit ? '编辑订单' : '创建订单' }}</span>
      </template>
    </el-page-header>

    <el-card class="form-card" v-loading="loading">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        label-position="right"
      >
        <el-divider content-position="left">基本信息</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户" prop="customerId">
              <el-select
                v-model="formData.customerId"
                placeholder="请选择客户"
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="customer in customerList"
                  :key="customer.id"
                  :label="customer.name"
                  :value="customer.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="交货日期" prop="deliveryDate">
              <el-date-picker
                v-model="formData.deliveryDate"
                type="date"
                placeholder="请选择交货日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="交货地址" prop="deliveryAddress">
          <el-input
            v-model="formData.deliveryAddress"
            placeholder="请输入交货地址"
            clearable
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input
                v-model="formData.contactPerson"
                placeholder="请输入联系人"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input
                v-model="formData.contactPhone"
                placeholder="请输入联系电话"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>

        <el-divider content-position="left">订单明细</el-divider>

        <div class="table-header">
          <el-button type="primary" size="small" @click="handleAddItem">
            <el-icon><Plus /></el-icon>
            添加订单行
          </el-button>
        </div>

        <el-table :data="formData.lines" border style="width: 100%">
          <el-table-column label="序号" type="index" width="60" />
          <el-table-column label="供应商" min-width="150">
            <template #default="{ row }">
              <el-select
                v-model="row.supplierId"
                placeholder="请选择供应商"
                filterable
              >
                <el-option
                  v-for="supplier in supplierList"
                  :key="supplier.id"
                  :label="supplier.name"
                  :value="supplier.id"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="产品编号" width="140">
            <template #default="{ row }">
              <el-input v-model="row.productCode" placeholder="产品编号" />
            </template>
          </el-table-column>
          <el-table-column label="产品名称" min-width="150">
            <template #default="{ row }">
              <el-input v-model="row.productName" placeholder="产品名称" />
            </template>
          </el-table-column>
          <el-table-column label="数量" width="120">
            <template #default="{ row }">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                :precision="0"
                controls-position="right"
                size="small"
              />
            </template>
          </el-table-column>
          <el-table-column label="单价" width="140">
            <template #default="{ row }">
              <el-input-number
                v-model="row.unitPrice"
                :min="0"
                :precision="2"
                controls-position="right"
                size="small"
              />
            </template>
          </el-table-column>
          <el-table-column label="金额" width="120">
            <template #default="{ row }">
              <span>{{ formatAmount((row.quantity || 0) * (row.unitPrice || 0)) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="备注" width="150">
            <template #default="{ row }">
              <el-input v-model="row.remark" placeholder="备注" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ $index }">
              <el-button
                type="danger"
                size="small"
                link
                @click="handleRemoveItem($index)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="total-amount">
          <span>总金额：</span>
          <span class="amount">{{ formatAmount(totalAmount) }}</span>
        </div>

        <el-form-item class="form-actions">
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '保存修改' : '提交订单' }}
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  createOrder, 
  updateOrder, 
  getOrderWithLines,
  type CreateOrderParams,
  type UpdateOrderParams
} from '@/api/order'
import { 
  batchAddOrderLines,
  type AddOrderLineParams 
} from '@/api/orderLine'

const router = useRouter()
const route = useRoute()

// 是否编辑模式
const isEdit = computed(() => !!route.params.id)
const orderId = computed(() => Number(route.params.id) || 0)

// 表单引用
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 客户列表（后续对接客户API）
const customerList = ref([
  { id: 1, name: '北京科技有限公司' },
  { id: 2, name: '上海贸易公司' },
  { id: 3, name: '深圳电子厂' },
  { id: 4, name: '广州物流有限公司' }
])

// 供应商列表（后续对接供应商API）
const supplierList = ref([
  { id: 1, name: '供应商A' },
  { id: 2, name: '供应商B' },
  { id: 3, name: '供应商C' }
])

// 订单行类型
interface OrderLineForm {
  supplierId: number | undefined
  productCode: string
  productName: string
  quantity: number
  unitPrice: number
  remark: string
}

// 表单数据
const formData = reactive({
  customerId: undefined as number | undefined,
  deliveryDate: '',
  deliveryAddress: '',
  contactPerson: '',
  contactPhone: '',
  remark: '',
  lines: [] as OrderLineForm[]
})

// 表单验证规则
const formRules: FormRules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  deliveryAddress: [{ required: true, message: '请输入交货地址', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 计算总金额
const totalAmount = computed(() => {
  return formData.lines.reduce((sum, item) => {
    return sum + (item.quantity || 0) * (item.unitPrice || 0)
  }, 0)
})

// 格式化金额
function formatAmount(amount: number): string {
  return '¥' + amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 添加订单行
function handleAddItem() {
  formData.lines.push({
    supplierId: undefined,
    productCode: '',
    productName: '',
    quantity: 1,
    unitPrice: 0,
    remark: ''
  })
}

// 删除订单行
function handleRemoveItem(index: number) {
  formData.lines.splice(index, 1)
}

// 返回
function goBack() {
  router.back()
}

// 加载订单数据（编辑模式）
async function loadOrderData() {
  if (!isEdit.value) return
  
  loading.value = true
  try {
    const res = await getOrderWithLines(orderId.value)
    formData.customerId = res.customerId
    formData.deliveryDate = res.deliveryDate || ''
    formData.deliveryAddress = res.deliveryAddress || ''
    formData.contactPerson = res.contactPerson || ''
    formData.contactPhone = res.contactPhone || ''
    formData.remark = res.remark || ''
    formData.lines = res.lines.map(line => ({
      supplierId: line.supplierId,
      productCode: line.productCode,
      productName: line.productName,
      quantity: line.quantity,
      unitPrice: line.unitPrice,
      remark: line.remark || ''
    }))
  } catch (error) {
    console.error('加载订单数据失败:', error)
    ElMessage.error('加载订单数据失败')
  } finally {
    loading.value = false
  }
}

// 验证订单行
function validateLines(): boolean {
  if (formData.lines.length === 0) {
    ElMessage.warning('请添加至少一个订单行')
    return false
  }
  
  for (let i = 0; i < formData.lines.length; i++) {
    const line = formData.lines[i]
    if (!line.supplierId) {
      ElMessage.warning(`第 ${i + 1} 行请选择供应商`)
      return false
    }
    if (!line.productCode) {
      ElMessage.warning(`第 ${i + 1} 行请输入产品编号`)
      return false
    }
    if (!line.productName) {
      ElMessage.warning(`第 ${i + 1} 行请输入产品名称`)
      return false
    }
    if (line.quantity <= 0) {
      ElMessage.warning(`第 ${i + 1} 行数量必须大于0`)
      return false
    }
    if (line.unitPrice < 0) {
      ElMessage.warning(`第 ${i + 1} 行单价不能为负数`)
      return false
    }
  }
  
  return true
}

// 提交订单
async function handleSubmit() {
  if (!formRef.value) return

  // 验证表单
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    ElMessage.warning('请填写完整信息')
    return
  }

  // 验证订单行
  if (!validateLines()) {
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      // 更新订单
      const updateParams: UpdateOrderParams = {
        customerId: formData.customerId,
        deliveryDate: formData.deliveryDate || undefined,
        deliveryAddress: formData.deliveryAddress || undefined,
        contactPerson: formData.contactPerson || undefined,
        contactPhone: formData.contactPhone || undefined,
        remark: formData.remark || undefined
      }
      await updateOrder(orderId.value, updateParams)
      ElMessage.success('订单更新成功')
      router.push(`/order/${orderId.value}`)
    } else {
      // 创建订单
      const createParams: CreateOrderParams = {
        customerId: formData.customerId!,
        deliveryDate: formData.deliveryDate || undefined,
        deliveryAddress: formData.deliveryAddress || undefined,
        contactPerson: formData.contactPerson || undefined,
        contactPhone: formData.contactPhone || undefined,
        remark: formData.remark || undefined
      }
      const newOrderId = await createOrder(createParams)
      
      // 批量添加订单行
      if (formData.lines.length > 0) {
        const linesParams = {
          orderId: newOrderId,
          lines: formData.lines.map(line => ({
            supplierId: line.supplierId!,
            productCode: line.productCode,
            productName: line.productName,
            quantity: line.quantity,
            unitPrice: line.unitPrice,
            remark: line.remark || undefined
          }))
        }
        await batchAddOrderLines(linesParams)
      }
      
      ElMessage.success('订单创建成功')
      router.push(`/order/${newOrderId}`)
    }
  } catch (error) {
    console.error('保存订单失败:', error)
    ElMessage.error('保存订单失败')
  } finally {
    submitting.value = false
  }
}

// 取消
function handleCancel() {
  goBack()
}

// 初始化
onMounted(() => {
  loadOrderData()
})
</script>

<style scoped lang="scss">
.order-create {
  padding: 20px;

  .page-title {
    font-size: 16px;
    font-weight: 500;
  }

  .form-card {
    margin-top: 20px;
  }

  .table-header {
    margin-bottom: 12px;
  }

  .total-amount {
    margin-top: 16px;
    padding: 16px;
    text-align: right;
    background: var(--el-fill-color-light);
    border-radius: 4px;

    .amount {
      margin-left: 8px;
      font-size: 20px;
      font-weight: bold;
      color: var(--el-color-danger);
    }
  }

  .form-actions {
    margin-top: 24px;
  }

  :deep(.el-form-item__content) {
    .el-select,
    .el-date-picker {
      width: 100%;
    }
  }

  :deep(.el-table) {
    .el-select,
    .el-input,
    .el-input-number {
      width: 100%;
    }
  }
}
</style>
