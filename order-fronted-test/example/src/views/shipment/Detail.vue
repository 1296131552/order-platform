<template>
  <div class="shipment-detail-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-page-header @back="handleBack">
        <template #content>
          <span class="page-title">发运详情</span>
        </template>
        <template #extra>
          <el-button-group>
            <el-button :icon="Edit" @click="handleEdit">编辑</el-button>
            <el-button :icon="Download" @click="handleExport">导出</el-button>
          </el-button-group>
        </template>
      </el-page-header>
    </div>

    <!-- 发运基本信息 -->
    <el-card class="info-card" shadow="never" v-loading="loading">
      <template #header>
        <span class="card-title">基本信息</span>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="发运单号">{{ shipmentInfo.shipmentNo }}</el-descriptions-item>
        <el-descriptions-item label="订单编号">{{ shipmentInfo.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ shipmentInfo.customerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="承运商">{{ shipmentInfo.carrierName }}</el-descriptions-item>
        <el-descriptions-item label="车辆号牌">{{ shipmentInfo.vehicleNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="司机电话">{{ shipmentInfo.driverPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发货地址">{{ shipmentInfo.shipmentAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收货地址">{{ shipmentInfo.receiverAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发运状态">
          <el-tag :type="getStatusType(shipmentInfo.status)">{{ getStatusText(shipmentInfo.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发运日期">{{ shipmentInfo.departureTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="预计到达">{{ shipmentInfo.estimatedArrivalTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="实际到达">{{ shipmentInfo.actualArrivalTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 快递单列表 -->
    <el-card class="shipment-line-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">快递单列表</span>
          <el-button type="primary" size="small" :icon="Plus" @click="handleAddShipmentLine">
            添加快递单
          </el-button>
        </div>
      </template>
      <el-table :data="shipmentLines" v-loading="linesLoading" border>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="lineNo" label="行号" width="80" />
        <el-table-column prop="trackingNo" label="快递单号" width="180" />
        <el-table-column prop="carrierName" label="承运商" width="150" />
        <el-table-column prop="quantity" label="数量" width="100" align="right" />
        <el-table-column prop="weight" label="重量(kg)" width="100" align="right">
          <template #default="{ row }">{{ row.weight ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLineStatusType(row.status)">{{ getLineStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewTracking(row)">物流</el-button>
            <el-button type="primary" link size="small" @click="handleEditLine(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDeleteLine(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 运输轨迹 -->
    <el-card class="timeline-card" shadow="never">
      <template #header>
        <span class="card-title">运输轨迹</span>
      </template>
      <el-timeline v-if="trackingEvents.length > 0">
        <el-timeline-item
          v-for="(item, index) in trackingEvents"
          :key="index"
          :timestamp="item.time"
          :type="index === 0 ? 'primary' : 'info'"
          placement="top"
        >
          <el-card>
            <h4>{{ item.status }}</h4>
            <p>{{ item.description }}</p>
            <p v-if="item.location" class="location">📍 {{ item.location }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无运输轨迹" />
    </el-card>
  </div>
</template>


<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Edit, Download, Plus } from '@element-plus/icons-vue'
import { 
  getShipmentDetail, 
  getShipmentTrack,
  ShipmentStatus,
  type Shipment 
} from '@/api/shipment'
import {
  getShipmentLinesByShipmentId,
  addShipmentLine,
  updateShipmentLine,
  deleteShipmentLine,
  getTrackingInfo,
  type ShipmentLine,
  type TrackingInfo,
  ShipmentLineStatus
} from '@/api/shipmentLine'

const router = useRouter()
const route = useRoute()

// 发运信息
const loading = ref(false)
const shipmentInfo = ref<Partial<Shipment>>({})

// 快递单列表
const linesLoading = ref(false)
const shipmentLines = ref<ShipmentLine[]>([])

// 运输轨迹
const trackingEvents = ref<Array<{
  time: string
  location: string
  status: string
  description?: string
}>>([])

// 获取发运ID
const shipmentId = Number(route.params.id)

// 加载发运详情
async function loadShipmentDetail() {
  loading.value = true
  try {
    const res = await getShipmentDetail(shipmentId)
    shipmentInfo.value = res
  } catch (error) {
    console.error('加载发运详情失败:', error)
    ElMessage.error('加载发运详情失败')
  } finally {
    loading.value = false
  }
}

// 加载快递单列表
async function loadShipmentLines() {
  linesLoading.value = true
  try {
    const res = await getShipmentLinesByShipmentId(shipmentId)
    shipmentLines.value = res || []
  } catch (error) {
    console.error('加载快递单列表失败:', error)
    ElMessage.error('加载快递单列表失败')
  } finally {
    linesLoading.value = false
  }
}

// 加载运输轨迹
async function loadTrackingEvents() {
  try {
    const res = await getShipmentTrack(shipmentId)
    trackingEvents.value = res || []
  } catch (error) {
    console.error('加载运输轨迹失败:', error)
  }
}

// 返回
function handleBack() {
  router.back()
}

// 编辑
function handleEdit() {
  ElMessage.info('编辑功能开发中')
}

// 导出
function handleExport() {
  ElMessage.info('导出功能开发中')
}

// 添加快递单
function handleAddShipmentLine() {
  ElMessage.info('添加快递单功能开发中')
}

// 编辑快递单
function handleEditLine(row: ShipmentLine) {
  ElMessage.info(`编辑快递单 ${row.trackingNo}`)
}

// 删除快递单
async function handleDeleteLine(row: ShipmentLine) {
  try {
    await ElMessageBox.confirm('确定要删除该快递单吗？', '提示', { type: 'warning' })
    await deleteShipmentLine(row.id)
    ElMessage.success('删除成功')
    loadShipmentLines()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除快递单失败:', error)
      ElMessage.error('删除快递单失败')
    }
  }
}

// 查看物流信息
async function handleViewTracking(row: ShipmentLine) {
  try {
    const res = await getTrackingInfo(row.trackingNo)
    ElMessage.info(`物流状态: ${res.status}`)
  } catch (error) {
    console.error('查询物流信息失败:', error)
    ElMessage.error('查询物流信息失败')
  }
}

// 状态类型
function getStatusType(status: string | undefined) {
  const map: Record<string, any> = {
    [ShipmentStatus.PENDING]: 'info',
    [ShipmentStatus.IN_TRANSIT]: 'warning',
    [ShipmentStatus.DELIVERED]: 'success'
  }
  return map[status || ''] || 'info'
}

// 状态文本
function getStatusText(status: string | undefined) {
  const map: Record<string, string> = {
    [ShipmentStatus.PENDING]: '待提货',
    [ShipmentStatus.IN_TRANSIT]: '在途',
    [ShipmentStatus.DELIVERED]: '已到货'
  }
  return map[status || ''] || '未知'
}

// 快递单状态类型
function getLineStatusType(status: string) {
  const map: Record<string, any> = {
    [ShipmentLineStatus.PENDING]: 'info',
    [ShipmentLineStatus.IN_TRANSIT]: 'warning',
    [ShipmentLineStatus.DELIVERED]: 'success',
    [ShipmentLineStatus.SIGNED]: 'success'
  }
  return map[status] || 'info'
}

// 快递单状态文本
function getLineStatusText(status: string) {
  const map: Record<string, string> = {
    [ShipmentLineStatus.PENDING]: '待发货',
    [ShipmentLineStatus.IN_TRANSIT]: '在途',
    [ShipmentLineStatus.DELIVERED]: '已送达',
    [ShipmentLineStatus.SIGNED]: '已签收'
  }
  return map[status] || '未知'
}

// 初始化
onMounted(() => {
  loadShipmentDetail()
  loadShipmentLines()
  loadTrackingEvents()
})
</script>

<style scoped lang="scss">
.shipment-detail-container {
  .page-header {
    margin-bottom: 16px;
    .page-title {
      font-size: 16px;
      font-weight: 500;
    }
  }

  .info-card,
  .shipment-line-card,
  .timeline-card {
    margin-bottom: 16px;
    .card-title {
      font-size: 14px;
      font-weight: 500;
    }
    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
  }

  .timeline-card {
    .location {
      margin-top: 8px;
      color: #909399;
    }
    h4 {
      margin: 0 0 8px 0;
      font-size: 14px;
    }
    p {
      margin: 4px 0;
      font-size: 13px;
      color: #606266;
    }
  }
}
</style>
