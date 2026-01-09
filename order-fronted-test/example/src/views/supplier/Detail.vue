<template>
  <div class="supplier-detail" v-loading="loading">
    <!-- 页面头部 -->
    <el-page-header @back="goBack" title="返回供应商列表">
      <template #content>
        <span class="page-title">{{ supplier?.name }}</span>
        <el-tag :type="supplier?.status === 'ACTIVE' ? 'success' : 'info'" style="margin-left: 12px">
          {{ supplier?.status === 'ACTIVE' ? '启用' : '停用' }}
        </el-tag>
      </template>
      <template #extra>
        <el-button type="primary" @click="handleEdit">编辑</el-button>
        <el-button 
          v-if="supplier?.status === 'INACTIVE'" 
          type="success" 
          @click="handleActivate"
        >激活</el-button>
        <el-button 
          v-if="supplier?.status === 'ACTIVE'" 
          type="warning" 
          @click="handleDeactivate"
        >停用</el-button>
      </template>
    </el-page-header>

    <!-- 基本信息 -->
    <el-card class="info-card" style="margin-top: 20px">
      <template #header>
        <span>基本信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="供应商编码">
          {{ supplier?.supplierNo }}
        </el-descriptions-item>
        <el-descriptions-item label="供应商名称">
          {{ supplier?.name }}
        </el-descriptions-item>
        <el-descriptions-item label="联系人">
          {{ supplier?.contactPerson }}
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">
          {{ supplier?.contactPhone }}
        </el-descriptions-item>
        <el-descriptions-item label="联系邮箱">
          {{ supplier?.contactEmail || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="supplier?.status === 'ACTIVE' ? 'success' : 'info'">
            {{ supplier?.status === 'ACTIVE' ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">
          {{ supplier?.address }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ supplier?.description || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 履约统计 -->
    <el-card class="stats-card" style="margin-top: 20px">
      <template #header>
        <span>履约统计</span>
      </template>
      <el-row :gutter="20" v-if="statistics">
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-label">合作订单数</div>
            <div class="stat-value">{{ statistics.totalOrders }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-label">订单总金额</div>
            <div class="stat-value">¥{{ statistics.totalAmount?.toLocaleString() }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-label">准时率</div>
            <div class="stat-value" :class="getOnTimeRateClass(statistics.onTimeRate)">
              {{ statistics.onTimeRate }}%
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-label">异常率</div>
            <div class="stat-value" :class="getExceptionRateClass(statistics.exceptionRate)">
              {{ statistics.exceptionRate }}%
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 资质文件 -->
    <el-card class="files-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>资质文件</span>
          <el-button type="primary" size="small" @click="handleUpload">上传资质</el-button>
        </div>
      </template>
      <div v-if="supplier?.qualifications?.length" class="file-list">
        <div v-for="(url, index) in supplier.qualifications" :key="index" class="file-item">
          <el-link :href="url" target="_blank" type="primary">
            资质文件_{{ index + 1 }}
          </el-link>
          <el-button link type="danger" size="small" @click="handleDeleteFile(index)">删除</el-button>
        </div>
      </div>
      <el-empty v-else description="暂无资质文件" :image-size="80" />
    </el-card>

    <!-- 相关订单 -->
    <el-card class="orders-card" style="margin-top: 20px">
      <template #header>
        <span>相关订单</span>
      </template>
      <el-table :data="orders" border>
        <el-table-column prop="orderNo" label="订单编号" width="160" />
        <el-table-column prop="amount" label="订单金额" width="120">
          <template #default="{ row }">
            ¥{{ row.amount?.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSupplierDetail, getSupplierStatistics, getSupplierOrders, activateSupplier, deactivateSupplier } from '@/api/supplier'
import { ORDER_STATUS_MAP } from '@/utils/constants'

const route = useRoute()
const router = useRouter()

const supplierId = computed(() => Number(route.params.id))

// 加载状态
const loading = ref(false)

// 数据
const supplier = ref<any>(null)
const statistics = ref<any>(null)
const orders = ref([])

// 加载供应商详情
const loadSupplierDetail = async () => {
  loading.value = true
  try {
    supplier.value = await getSupplierDetail(supplierId.value)
  } catch (error) {
    console.error('加载供应商详情失败：', error)
  } finally {
    loading.value = false
  }
}

// 加载统计信息
const loadStatistics = async () => {
  try {
    statistics.value = await getSupplierStatistics(supplierId.value)
  } catch (error) {
    console.error('加载统计信息失败：', error)
  }
}

// 加载相关订单
const loadOrders = async () => {
  try {
    const res = await getSupplierOrders(supplierId.value, { page: 1, pageSize: 10 })
    orders.value = res.list
  } catch (error) {
    console.error('加载订单失败：', error)
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 编辑
const handleEdit = () => {
  router.push(`/supplier/edit/${supplierId.value}`)
}

// 激活供应商
const handleActivate = async () => {
  try {
    await activateSupplier(supplierId.value)
    ElMessage.success('激活成功')
    loadSupplierDetail()
  } catch (error) {
    console.error('激活失败：', error)
  }
}

// 停用供应商
const handleDeactivate = () => {
  ElMessageBox.confirm(`确定要停用供应商 "${supplier.value?.name}" 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deactivateSupplier(supplierId.value)
      ElMessage.success('停用成功')
      loadSupplierDetail()
    } catch (error) {
      console.error('停用失败：', error)
    }
  }).catch(() => {})
}

// 上传资质
const handleUpload = () => {
  ElMessage.info('上传功能开发中')
}

// 删除文件
const handleDeleteFile = (index: number) => {
  ElMessage.info('删除功能开发中')
}

// 准时率样式
const getOnTimeRateClass = (rate: number) => {
  if (rate >= 95) return 'stat-success'
  if (rate >= 80) return 'stat-warning'
  return 'stat-danger'
}

// 异常率样式
const getExceptionRateClass = (rate: number) => {
  if (rate <= 2) return 'stat-success'
  if (rate <= 5) return 'stat-warning'
  return 'stat-danger'
}

// 状态类型
const getStatusType = (status: string) => {
  return ORDER_STATUS_MAP[status]?.type || 'info'
}

// 状态标签
const getStatusLabel = (status: string) => {
  return ORDER_STATUS_MAP[status]?.label || status
}

// 初始化
onMounted(() => {
  loadSupplierDetail()
  loadStatistics()
  loadOrders()
})
</script>

<style scoped lang="scss">
.supplier-detail {
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

  .stat-item {
    text-align: center;
    padding: 20px;
    background: var(--el-fill-color-light);
    border-radius: 4px;

    .stat-label {
      font-size: 14px;
      color: var(--el-text-color-secondary);
      margin-bottom: 8px;
    }

    .stat-value {
      font-size: 24px;
      font-weight: bold;
      color: var(--el-text-color-primary);

      &.stat-success {
        color: var(--el-color-success);
      }

      &.stat-warning {
        color: var(--el-color-warning);
      }

      &.stat-danger {
        color: var(--el-color-danger);
      }
    }
  }

  .file-list {
    .file-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      border-bottom: 1px solid var(--el-border-color-lighter);

      &:last-child {
        border-bottom: none;
      }
    }
  }
}
</style>
