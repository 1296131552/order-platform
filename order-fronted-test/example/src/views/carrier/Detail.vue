<template>
  <div class="carrier-detail" v-loading="loading">
    <el-page-header @back="goBack" title="返回承运商列表">
      <template #content>
        <span class="page-title">{{ carrier?.name }}</span>
        <el-tag :type="carrier?.status === 'active' ? 'success' : 'info'" style="margin-left: 12px">
          {{ carrier?.status === 'active' ? '启用' : '停用' }}
        </el-tag>
      </template>
      <template #extra>
        <el-button type="primary" @click="handleEdit">编辑</el-button>
      </template>
    </el-page-header>

    <!-- 基本信息 -->
    <el-card class="info-card" style="margin-top: 20px">
      <template #header>
        <span>基本信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="承运商编码">
          {{ carrier?.code }}
        </el-descriptions-item>
        <el-descriptions-item label="承运商名称">
          {{ carrier?.name }}
        </el-descriptions-item>
        <el-descriptions-item label="联系人">
          {{ carrier?.contactPerson }}
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">
          {{ carrier?.contactPhone }}
        </el-descriptions-item>
        <el-descriptions-item label="联系邮箱">
          {{ carrier?.contactEmail || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="carrier?.status === 'active' ? 'success' : 'info'">
            {{ carrier?.status === 'active' ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">
          {{ carrier?.address }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ carrier?.description || '-' }}
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
            <div class="stat-label">发运单数</div>
            <div class="stat-value">{{ statistics.totalShipments }}</div>
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
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-label">平均评分</div>
            <div class="stat-value">{{ statistics.averageRating }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 车辆信息 -->
    <el-card class="vehicles-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>车辆信息</span>
          <el-button type="primary" size="small" @click="handleAddVehicle">添加车辆</el-button>
        </div>
      </template>
      <el-table :data="vehicles" border>
        <el-table-column prop="vehicleNo" label="车牌号" width="140" />
        <el-table-column prop="vehicleType" label="车辆类型" width="120" />
        <el-table-column prop="loadCapacity" label="载重(吨)" width="100" />
        <el-table-column prop="driverName" label="司机" width="100" />
        <el-table-column prop="driverPhone" label="司机电话" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'available' ? 'success' : 'warning'">
              {{ row.status === 'available' ? '可用' : '使用中' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 相关发运 -->
    <el-card class="shipments-card" style="margin-top: 20px">
      <template #header>
        <span>相关发运</span>
      </template>
      <el-table :data="shipments" border>
        <el-table-column prop="shipmentNo" label="发运单号" width="160" />
        <el-table-column prop="route" label="路线" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getShipmentStatusType(row.status)">
              {{ getShipmentStatusLabel(row.status) }}
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
import { ElMessage } from 'element-plus'
import { getCarrierDetail, getCarrierStatistics, getCarrierShipments, getCarrierVehicles } from '@/api/carrier'
import { SHIPMENT_STATUS_MAP } from '@/utils/constants'

const route = useRoute()
const router = useRouter()

const carrierId = computed(() => Number(route.params.id))

// 加载状态
const loading = ref(false)

// 数据
const carrier = ref<any>(null)
const statistics = ref<any>(null)
const vehicles = ref([])
const shipments = ref([])

// 加载承运商详情
const loadCarrierDetail = async () => {
  loading.value = true
  try {
    carrier.value = await getCarrierDetail(carrierId.value)
  } catch (error) {
    console.error('加载承运商详情失败：', error)
  } finally {
    loading.value = false
  }
}

// 加载统计信息
const loadStatistics = async () => {
  try {
    statistics.value = await getCarrierStatistics(carrierId.value)
  } catch (error) {
    console.error('加载统计信息失败：', error)
  }
}

// 加载车辆信息
const loadVehicles = async () => {
  try {
    vehicles.value = await getCarrierVehicles(carrierId.value)
  } catch (error) {
    console.error('加载车辆信息失败：', error)
  }
}

// 加载相关发运
const loadShipments = async () => {
  try {
    const res = await getCarrierShipments(carrierId.value, { page: 1, pageSize: 10 })
    shipments.value = res.list
  } catch (error) {
    console.error('加载发运失败：', error)
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 编辑
const handleEdit = () => {
  router.push(`/carrier/edit/${carrierId.value}`)
}

// 添加车辆
const handleAddVehicle = () => {
  ElMessage.info('添加车辆功能开发中')
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

// 发运状态类型
const getShipmentStatusType = (status: string) => {
  return SHIPMENT_STATUS_MAP[status]?.type || 'info'
}

// 发运状态标签
const getShipmentStatusLabel = (status: string) => {
  return SHIPMENT_STATUS_MAP[status]?.label || status
}

// 初始化
onMounted(() => {
  loadCarrierDetail()
  loadStatistics()
  loadVehicles()
  loadShipments()
})
</script>

<style scoped lang="scss">
.carrier-detail {
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
}
</style>
