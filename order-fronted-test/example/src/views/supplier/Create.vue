<template>
  <div class="supplier-create">
    <el-page-header @back="goBack" title="返回供应商列表">
      <template #content>
        <span class="page-title">{{ isEdit ? '编辑供应商' : '新建供应商' }}</span>
      </template>
    </el-page-header>

    <el-card class="form-card" style="margin-top: 20px" v-loading="loading">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商编码" prop="supplierNo">
              <el-input
                v-model="formData.supplierNo"
                placeholder="请输入供应商编码"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商名称" prop="name">
              <el-input
                v-model="formData.name"
                placeholder="请输入供应商名称"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

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

        <el-form-item label="联系邮箱">
          <el-input
            v-model="formData.contactEmail"
            placeholder="请输入联系邮箱"
            clearable
          />
        </el-form-item>

        <el-form-item label="地址" prop="address">
          <el-input
            v-model="formData.address"
            placeholder="请输入地址"
            clearable
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '保存' : '创建' }}
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
import { createSupplier, updateSupplier, getSupplierDetail, type CreateSupplierParams } from '@/api/supplier'

const route = useRoute()
const router = useRouter()

// 是否为编辑模式
const isEdit = computed(() => !!route.params.id)

// 表单引用
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 表单数据
const formData = reactive<CreateSupplierParams>({
  supplierNo: '',
  name: '',
  contactPerson: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
  description: ''
})

// 表单验证规则
const formRules: FormRules = {
  supplierNo: [{ required: true, message: '请输入供应商编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }]
}

// 返回
const goBack = () => {
  router.back()
}

// 加载供应商详情（编辑模式）
const loadSupplierDetail = async () => {
  if (!isEdit.value) return

  loading.value = true
  try {
    const data = await getSupplierDetail(Number(route.params.id))
    Object.assign(formData, {
      supplierNo: data.supplierNo,
      name: data.name,
      contactPerson: data.contactPerson,
      contactPhone: data.contactPhone,
      contactEmail: data.contactEmail || '',
      address: data.address,
      description: data.description || ''
    })
  } catch (error) {
    console.error('加载供应商详情失败：', error)
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    ElMessage.warning('请填写完整信息')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateSupplier(Number(route.params.id), formData)
      ElMessage.success('更新成功')
    } else {
      await createSupplier(formData)
      ElMessage.success('创建成功')
    }
    goBack()
  } catch (error) {
    console.error('提交失败：', error)
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
  loadSupplierDetail()
})
</script>

<style scoped lang="scss">
.supplier-create {
  padding: 20px;

  .page-title {
    font-size: 16px;
    font-weight: 500;
  }
}
</style>
