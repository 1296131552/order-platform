<template>
  <div class="order-detail-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-page-header @back="handleBack">
        <template #content>
          <span class="page-title">订单详情</span>
        </template>
        <template #extra>
          <el-button-group>
            <el-button 
              v-if="orderInfo?.status === 'DRAFT'" 
              :icon="Edit" 
              @click="handleEdit"
            >编辑</el-button>
            <el-button :icon="Download" @click="handleExport">导出</el-button>
          </el-button-group>
        </template>
      </el-page-header>
    </div>

    <div v-loading="loading">
      <!-- 订单基本信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <span class="card-title">基本信息</span>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="订单编号">{{ orderInfo?.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="客户名称">{{ orderInfo?.customerName }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag v-if="orderInfo?.status" :type="getStatusType(orderInfo.status)">
              {{ getStatusText(orderInfo.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单金额">
            ¥{{ orderInfo?.totalAmount?.toLocaleString() }}
          </el-descriptions-item>
          <el-descriptions-item label="订单行数">{{ orderInfo?.lineCount || 0 }} 行</el-descriptions-item>
          <el-descriptions-item label="交货日期">{{ orderInfo?.deliveryDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="交货地址" :span="2">
            {{ orderInfo?.deliveryAddress || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系人">{{ orderInfo?.contactPerson || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ orderInfo?.contactPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ orderInfo?.createdByName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ orderInfo?.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ orderInfo?.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 订单行明细 -->
      <el-card class="items-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">订单行明细</span>
            <el-button 
              v-if="orderInfo?.status === 'DRAFT'" 
              type="primary" 
              size="small" 
              :icon="Plus" 
              @click="handleAddLine"
            >
              添加订单行
            </el-button>
          </div>
        </template>
        <el-table :data="orderLines" border show-summary :summary-method="getSummaries">
          <el-table-column prop="lineNo" label="行号" width="80" align="center" />
          <el-table-column prop="productCode" label="产品编号" width="140" />
          <el-table-column prop="productName" label="产品名称" min-width="150" />
          <el-table-column prop="supplierName" label="供应商" width="150" />
          <el-table-column prop="quantity" label="数量" width="100" align="right" />
          <el-table-column prop="unitPrice" label="单价" width="120" align="right">
            <template #default="{ row }">
              ¥{{ row.unitPrice?.toLocaleString() }}
            </template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="金额" width="120" align="right">
            <template #default="{ row }">
              ¥{{ row.totalAmount?.toLocaleString() }}
            </template>
          </el-table-column>
          <el-table-column prop="statusCode" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getLineStatusType(row.statusCode)">
                {{ getLineStatusText(row.statusCode) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="120" />
          <el-table-column 
            v-if="orderInfo?.status === 'DRAFT'" 
            label="操作" 
            width="120" 
            fixed="right"
          >
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleEditLine(row)">
                编辑
              </el-button>
              <el-button type="danger" link size="small" @click="handleDeleteLine(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 业务地图 -->
      <el-card class="map-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">业务地图</span>
            <el-tag>共 {{ mapRoutes.length }} 条线路</el-tag>
          </div>
        </template>
        <BusinessMap
          v-if="mapRoutes.length > 0"
          :routes="mapRoutes"
          @route-click="handleRouteClick"
        />
        <el-empty v-else description="暂无发运记录，无法展示地图" :image-size="80" />
      </el-card>

      <!-- 流程时间线 -->
      <el-card class="timeline-card" shadow="never">
        <template #header>
          <span class="card-title">流程时间线</span>
        </template>
        <ProcessTimeline :data="timelineData" />
      </el-card>

      <!-- 发运记录 -->
      <el-card class="shipment-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">发运记录</span>
            <el-button 
              v-if="orderInfo?.status === 'EXECUTING'" 
              type="primary" 
              size="small" 
              :icon="Plus" 
              @click="handleAddShipment"
            >
              新建发运
            </el-button>
          </div>
        </template>
        <el-table :data="shipments" border>
          <el-table-column prop="shipmentNo" label="发运单号" width="160" />
          <el-table-column prop="carrierName" label="承运商" width="150" />
          <el-table-column prop="departureTime" label="发运时间" width="160" />
          <el-table-column prop="estimatedArrivalTime" label="预计到达" width="160" />
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getShipmentStatusType(row.status)">
                {{ getShipmentStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="viewShipment(row.id)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="shipments.length === 0" description="暂无发运记录" :image-size="80" />
      </el-card>

      <!-- 附件列表 -->
      <el-card class="attachment-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">附件列表</span>
            <el-button type="primary" size="small" @click="handleUploadAttachment">
              上传附件
            </el-button>
          </div>
        </template>
        <div v-if="attachments.length > 0" class="attachment-list">
          <div v-for="file in attachments" :key="file.id" class="attachment-item">
            <el-icon><Document /></el-icon>
            <el-link :href="file.url" target="_blank" type="primary">
              {{ file.fileOriginalName }}
            </el-link>
            <span class="file-size">{{ formatFileSize(file.fileSize) }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无附件" :image-size="80" />
      </el-card>
    </div>

    <!-- 添加/编辑订单行对话框 -->
    <el-dialog 
      v-model="lineDialogVisible" 
      :title="editingLine ? '编辑订单行' : '添加订单行'" 
      width="600px"
    >
      <el-form :model="lineForm" :rules="lineRules" ref="lineFormRef" label-width="100px">
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="lineForm.supplierId" placeholder="请选择供应商" style="width: 100%">
            <el-option 
              v-for="item in supplierOptions" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="产品编号" prop="productCode">
          <el-input v-model="lineForm.productCode" placeholder="请输入产品编号" />
        </el-form-item>
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="lineForm.productName" placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="lineForm.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number v-model="lineForm.unitPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="lineForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="lineDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveLine">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Edit, Download, Plus, Document } from '@element-plus/icons-vue'
import BusinessMap from '@/components/BusinessMap.vue'
import ProcessTimeline, { type TimelineNode } from '@/components/ProcessTimeline.vue'
import type { RouteInfo } from '@/components/BusinessMap.vue'
import { 
  getOrderWithLines, 
  type OrderWithLines, 
  type OrderLine,
  type OrderStatus 
} from '@/api/order'
import { 
  addOrderLine, 
  updateOrderLine, 
  deleteOrderLine,
  type AddOrderLineParams,
  type UpdateOrderLineParams
} from '@/api/orderLine'

const router = useRouter()
const route = useRoute()

// 加载状态
const loading = ref(false)

// 订单信息
const orderInfo = ref<OrderWithLines | null>(null)
const orderLines = computed(() => orderInfo.value?.lines || [])

// 发运记录（暂时使用空数组，后续对接发运API）
const shipments = ref<any[]>([])

// 附件列表（暂时使用空数组，后续对接附件API）
const attachments = ref<any[]>([])

// 供应商选项（暂时使用模拟数据，后续对接供应商API）
const supplierOptions = ref([
  { id: 1, name: '供应商A' },
  { id: 2, name: '供应商B' },
  { id: 3, name: '供应商C' }
])

// 订单行对话框
const lineDialogVisible = ref(false)
const editingLine = ref<OrderLine | null>(null)
const lineFormRef = ref<FormInstance>()
const lineForm = ref({
  supplierId: undefined as number | undefined,
  productCode: '',
  productName: '',
  quantity: 1,
  unitPrice: 0,
  remark: ''
})

const lineRules: FormRules = {
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  productCode: [{ required: true, message: '请输入产品编号', trigger: 'blur' }],
  productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  unitPrice: [{ required: true, message: '请输入单价', trigger: 'blur' }]
}

// 业务地图数据
const mapRoutes = computed<RouteInfo[]>(() => {
  if (!shipments.value.length) return []
  return shipments.value.map(s => ({
    shipmentId: s.id,
    shipmentNo: s.shipmentNo,
    status: s.status,
    start: {
      longitude: 116.407526,
      latitude: 39.904989,
      location: '发货地'
    },
    end: {
      longitude: 121.473701,
      latitude: 31.230416,
      location: '收货地'
    },
    driverName: s.driverName,
    estimatedArrivalTime: s.estimatedArrivalTime
  }))
})

// 流程时间线数据
const timelineData = computed<TimelineNode[]>(() => {
  if (!orderInfo.value) return []
  
  const nodes: TimelineNode[] = [
    {
      title: '订单创建',
      time: orderInfo.value.createdAt,
      status: 'completed',
      description: `创建订单 ${orderInfo.value.orderNo}`,
      operator: orderInfo.value.createdByName
    }
  ]
  
  if (orderInfo.value.status !== 'DRAFT') {
    nodes.push({
      title: '订单执行',
      status: orderInfo.value.status === 'EXECUTING' ? 'active' : 'completed',
      description: '订单开始执行'
    })
  }
  
  if (orderInfo.value.status === 'PARTIALLY_RECEIVED') {
    nodes.push({
      title: '部分到货',
      status: 'active',
      description: '部分货物已到达'
    })
  }
  
  if (orderInfo.value.status === 'COMPLETED') {
    nodes.push({
      title: '订单完成',
      status: 'completed',
      description: '订单已完成'
    })
  }
  
  if (orderInfo.value.status === 'CANCELLED') {
    nodes.push({
      title: '订单取消',
      status: 'completed',
      description: '订单已取消'
    })
  }
  
  return nodes
})

// 加载订单数据
async function loadOrderData() {
  const orderId = Number(route.params.id)
  if (!orderId) {
    ElMessage.error('订单ID无效')
    router.back()
    return
  }
  
  loading.value = true
  try {
    const res = await getOrderWithLines(orderId)
    orderInfo.value = res
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

// 计算合计
const getSummaries = (param: any) => {
  const { columns, data } = param
  const sums: string[] = []
  columns.forEach((column: any, index: number) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (column.property === 'quantity') {
      sums[index] = String(data.reduce((sum: number, row: any) => sum + (row.quantity || 0), 0))
    } else if (column.property === 'totalAmount') {
      sums[index] = '¥' + data.reduce((sum: number, row: any) => sum + (row.totalAmount || 0), 0).toLocaleString()
    } else {
      sums[index] = ''
    }
  })
  return sums
}

// 返回
function handleBack() {
  router.back()
}

// 编辑
function handleEdit() {
  if (orderInfo.value) {
    router.push(`/order/edit/${orderInfo.value.id}`)
  }
}

// 导出
function handleExport() {
  ElMessage.info('导出功能开发中')
}

// 添加订单行
function handleAddLine() {
  editingLine.value = null
  lineForm.value = {
    supplierId: undefined,
    productCode: '',
    productName: '',
    quantity: 1,
    unitPrice: 0,
    remark: ''
  }
  lineDialogVisible.value = true
}

// 编辑订单行
function handleEditLine(row: OrderLine) {
  editingLine.value = row
  lineForm.value = {
    supplierId: row.supplierId,
    productCode: row.productCode,
    productName: row.productName,
    quantity: row.quantity,
    unitPrice: row.unitPrice,
    remark: row.remark || ''
  }
  lineDialogVisible.value = true
}

// 保存订单行
async function handleSaveLine() {
  if (!lineFormRef.value) return
  
  try {
    await lineFormRef.value.validate()
    
    if (editingLine.value) {
      // 更新订单行
      const params: UpdateOrderLineParams = {
        id: editingLine.value.id,
        supplierId: lineForm.value.supplierId,
        productCode: lineForm.value.productCode,
        productName: lineForm.value.productName,
        quantity: lineForm.value.quantity,
        unitPrice: lineForm.value.unitPrice,
        remark: lineForm.value.remark
      }
      await updateOrderLine(params)
      ElMessage.success('更新成功')
    } else {
      // 添加订单行
      const params: AddOrderLineParams = {
        orderId: orderInfo.value!.id,
        supplierId: lineForm.value.supplierId!,
        productCode: lineForm.value.productCode,
        productName: lineForm.value.productName,
        quantity: lineForm.value.quantity,
        unitPrice: lineForm.value.unitPrice,
        remark: lineForm.value.remark
      }
      await addOrderLine(params)
      ElMessage.success('添加成功')
    }
    
    lineDialogVisible.value = false
    loadOrderData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('保存订单行失败:', error)
      ElMessage.error('保存失败')
    }
  }
}

// 删除订单行
async function handleDeleteLine(row: OrderLine) {
  try {
    await ElMessageBox.confirm(`确定要删除该订单行吗？`, '提示', {
      type: 'warning'
    })
    await deleteOrderLine(row.id)
    ElMessage.success('删除成功')
    loadOrderData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除订单行失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 新建发运
function handleAddShipment() {
  if (orderInfo.value) {
    router.push(`/shipment/create?orderId=${orderInfo.value.id}`)
  }
}

// 查看发运详情
function viewShipment(id: number) {
  router.push(`/shipment/${id}`)
}

// 上传附件
function handleUploadAttachment() {
  ElMessage.info('上传附件功能开发中')
}

// 地图线路点击
function handleRouteClick(route: RouteInfo) {
  ElMessage.info(`选中线路：${route.shipmentNo}`)
}

// 格式化文件大小
function formatFileSize(size: number): string {
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / 1024 / 1024).toFixed(1) + ' MB'
}

// 订单状态类型
function getStatusType(status: OrderStatus): '' | 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<OrderStatus, '' | 'success' | 'warning' | 'info' | 'danger'> = {
    DRAFT: 'info',
    EXECUTING: 'warning',
    PARTIALLY_RECEIVED: '',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return map[status] || 'info'
}

// 订单状态文本
function getStatusText(status: OrderStatus): string {
  const map: Record<OrderStatus, string> = {
    DRAFT: '草稿',
    EXECUTING: '执行中',
    PARTIALLY_RECEIVED: '部分到货',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || '未知'
}

// 订单行状态类型
function getLineStatusType(status: string): '' | 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<string, '' | 'success' | 'warning' | 'info' | 'danger'> = {
    PENDING: 'info',
    SHIPPED: 'warning',
    RECEIVED: 'success',
    CANCELLED: 'danger'
  }
  return map[status] || 'info'
}

// 订单行状态文本
function getLineStatusText(status: string): string {
  const map: Record<string, string> = {
    PENDING: '待发运',
    SHIPPED: '已发运',
    RECEIVED: '已签收',
    CANCELLED: '已取消'
  }
  return map[status] || '未知'
}

// 发运状态类型
function getShipmentStatusType(status: string): '' | 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<string, '' | 'success' | 'warning' | 'info' | 'danger'> = {
    PENDING: 'info',
    IN_TRANSIT: 'warning',
    DELIVERED: 'success'
  }
  return map[status] || 'info'
}

// 发运状态文本
function getShipmentStatusText(status: string): string {
  const map: Record<string, string> = {
    PENDING: '待提货',
    IN_TRANSIT: '在途',
    DELIVERED: '已到货'
  }
  return map[status] || '未知'
}

// 初始化
onMounted(() => {
  loadOrderData()
})
</script>

<style scoped lang="scss">
.order-detail-container {
  padding: 20px;

  .page-header {
    margin-bottom: 16px;

    .page-title {
      font-size: 16px;
      font-weight: 500;
    }
  }

  .info-card,
  .items-card,
  .map-card,
  .timeline-card,
  .shipment-card,
  .attachment-card {
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

  .attachment-list {
    .attachment-item {
      display: flex;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid var(--el-border-color-lighter);

      &:last-child {
        border-bottom: none;
      }

      .el-icon {
        margin-right: 8px;
        color: var(--el-color-primary);
      }

      .file-size {
        margin-left: 12px;
        color: var(--el-text-color-secondary);
        font-size: 12px;
      }
    }
  }
}
</style>
