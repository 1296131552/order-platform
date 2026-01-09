<template>
  <div class="order-create">
    <el-page-header @back="goBack" title="返回订单列表">
      <template #content>
        <span class="page-title">创建订单</span>
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
            添加商品
          </el-button>
        </div>

        <el-table :data="formData.items" border style="width: 100%">
          <el-table-column label="序号" type="index" width="60" />
          <el-table-column label="商品" min-width="150">
            <template #default="{ row }">
              <el-select
                v-model="row.productId"
                placeholder="请选择商品"
                filterable
                @change="handleProductChange(row)"
              >
                <el-option
                  v-for="product in productList"
                  :key="product.id"
                  :label="product.name"
                  :value="product.id"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="150">
            <template #default="{ row }">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                :precision="0"
                controls-position="right"
                @change="calculateItemAmount(row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="单价" width="150">
            <template #default="{ row }">
              <el-input-number
                v-model="row.price"
                :min="0"
                :precision="2"
                controls-position="right"
                @change="calculateItemAmount(row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="金额" width="150">
            <template #default="{ row }">
              <span>{{ formatAmount(row.quantity * row.price) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="备注" width="180">
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

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            提交订单
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="warning" @click="handleSaveDraft" :loading="submitting">
            保存草稿
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { createOrder } from '@/api/order'
import { formatAmount } from '@/utils/format'

const router = useRouter()

// 表单引用
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 客户列表（模拟数据）
const customerList = ref([
  { id: 1, name: '客户A' },
  { id: 2, name: '客户B' },
  { id: 3, name: '客户C' }
])

// 商品列表（模拟数据）
const productList = ref([
  { id: 1, name: '商品A', price: 100 },
  { id: 2, name: '商品B', price: 200 },
  { id: 3, name: '商品C', price: 300 }
])

// 表单数据
const formData = reactive({
  customerId: undefined as number | undefined,
  deliveryDate: '',
  deliveryAddress: '',
  contactPerson: '',
  contactPhone: '',
  remark: '',
  items: [] as Array<{
    productId: number | undefined
    quantity: number
    price: number
    remark: string
  }>
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
  return formData.items.reduce((sum, item) => {
    return sum + (item.quantity || 0) * (item.price || 0)
  }, 0)
})

// 添加商品行
const handleAddItem = () => {
  formData.items.push({
    productId: undefined,
    quantity: 1,
    price: 0,
    remark: ''
  })
}

// 删除商品行
const handleRemoveItem = (index: number) => {
  formData.items.splice(index, 1)
}

// 商品选择变化
const handleProductChange = (row: any) => {
  const product = productList.value.find(p => p.id === row.productId)
  if (product) {
    row.price = product.price
  }
}

// 计算行金额
const calculateItemAmount = (row: any) => {
  // 金额会自动计算，这里只是为了触发响应式更新
}

// 返回
const goBack = () => {
  router.back()
}

// 提交订单
const handleSubmit = async () => {
  if (!formRef.value) return

  // 验证表单
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    ElMessage.warning('请填写完整信息')
    return
  }

  // 验证明细
  if (formData.items.length === 0) {
    ElMessage.warning('请添加至少一个商品')
    return
  }

  const hasEmptyItem = formData.items.some(item => !item.productId)
  if (hasEmptyItem) {
    ElMessage.warning('请选择所有商品')
    return
  }

  submitting.value = true
  try {
    const data = {
      ...formData,
      items: formData.items.filter(item => item.productId)
    }
    const orderId = await createOrder(data)
    ElMessage.success('订单创建成功')
    router.push(`/order/${orderId}`)
  } catch (error) {
    console.error('创建订单失败：', error)
  } finally {
    submitting.value = false
  }
}

// 保存草稿
const handleSaveDraft = async () => {
  submitting.value = true
  try {
    await createOrder({
      ...formData,
      items: formData.items.filter(item => item.productId)
    })
    ElMessage.success('草稿保存成功')
    goBack()
  } catch (error) {
    console.error('保存草稿失败：', error)
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
  // 可以在这里加载客户和商品列表
  // loadCustomerList()
  // loadProductList()
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

  :deep(.el-form-item__content) {
    .el-select,
    .el-date-picker {
      width: 100%;
    }
  }
}
</style>
