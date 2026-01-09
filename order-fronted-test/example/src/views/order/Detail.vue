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
            <el-button :icon="Edit" @click="handleEdit">编辑</el-button>
            <el-button :icon="Share" @click="handleShare">分享</el-button>
            <el-button :icon="Download" @click="handleExport">导出</el-button>
          </el-button-group>
        </template>
      </el-page-header>
    </div>

    <!-- 订单基本信息 -->
    <el-card class="info-card" shadow="never">
      <template #header>
        <span class="card-title">基本信息</span>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="订单编号">{{ orderInfo.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ orderInfo.customerName }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(orderInfo.status)">{{ getStatusText(orderInfo.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ orderInfo.totalAmount?.toLocaleString() }}</el-descriptions-item>
        <el-descriptions-item label="产品数量">{{ orderInfo.products?.length || 0 }} 件</el-descriptions-item>
        <el-descriptions-item label="交货日期">{{ orderInfo.deliveryDate }}</el-descriptions-item>
        <el-descriptions-item label="交货地址" :span="2">{{ orderInfo.deliveryAddress }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ orderInfo.contactPerson }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ orderInfo.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="销售员">{{ orderInfo.salesman || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ orderInfo.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ orderInfo.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 订单行明细 -->
    <el-card class="items-card" shadow="never">
      <template #header>
        <span class="card-title">订单行明细</span>
      </template>
      <el-table :data="orderInfo.products" border show-summary :summary-method="getSummaries">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="productNo" label="产品编号" width="140" />
        <el-table-column prop="productName" label="产品名称" min-width="150" />
        <el-table-column prop="specification" label="规格型号" width="120" />
        <el-table-column prop="quantity" label="数量" width="100" align="right" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="unitPrice" label="单价" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.unitPrice?.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.amount?.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" />
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
          <el-button type="primary" size="small" :icon="Plus" @click="handleAddShipment">
            新建发运
          </el-button>
        </div>
      </template>
      <el-table :data="orderInfo.shipments" border>
        <el-table-column prop="shipmentNo" label="发运单号" width="160" />
        <el-table-column prop="carrierName" label="承运商" width="150" />
        <el-table-column prop="vehicleNo" label="车辆号牌" width="120" />
        <el-table-column prop="driverName" label="司机" width="100" />
        <el-table-column prop="shipmentDate" label="发运日期" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getShipmentStatusType(row.status)">{{ getShipmentStatusText(row.status) }}</el-tag>
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
      <div v-if="orderInfo.attachments?.length" class="attachment-list">
        <div v-for="(file, index) in orderInfo.attachments" :key="index" class="attachment-item">
          <el-icon><Document /></el-icon>
          <el-link :href="file.url" target="_blank" type="primary">{{ file.name }}</el-link>
          <el-tag size="small" style="margin-left: 8px">{{ file.category }}</el-tag>
        </div>
      </div>
      <el-empty v-else description="暂无附件" :image-size="80" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Edit, Share, Download, Plus, Document } from '@element-plus/icons-vue'
import BusinessMap from '@/components/BusinessMap.vue'
import ProcessTimeline, { type TimelineNode } from '@/components/ProcessTimeline.vue'
import type { RouteInfo } from '@/components/BusinessMap.vue'

const router = useRouter()
const route = useRoute()

// 订单信息（模拟数据）
const orderInfo = ref({
  id: 1,
  orderNo: 'ORD20260105001',
  customerName: '北京科技有限公司',
  totalAmount: 125000,
  deliveryDate: '2026-02-15',
  deliveryAddress: '北京市海淀区中关村软件园',
  contactPerson: '李经理',
  contactPhone: '13800138000',
  salesman: '张三',
  createTime: '2026-01-05 10:30:00',
  updateTime: '2026-01-05 15:20:00',
  remark: '加急订单',
  products: [
    {
      productNo: 'P001',
      productName: '精密机械零件A',
      specification: 'M20×100',
      quantity: 100,
      unit: '件',
      unitPrice: 500,
      amount: 50000,
      remark: ''
    },
    {
      productNo: 'P002',
      productName: '精密机械零件B',
      specification: 'M30×150',
      quantity: 50,
      unit: '件',
      unitPrice: 800,
      amount: 40000,
      remark: '加急'
    },
    {
      productNo: 'P003',
      productName: '精密机械零件C',
      specification: 'M40×200',
      quantity: 25,
      unit: '件',
      unitPrice: 1400,
      amount: 35000,
      remark: ''
    }
  ],
  shipments: [
    {
      id: 1,
      shipmentNo: 'SHP20260108001',
      carrierName: '顺丰速运',
      vehicleNo: '京A12345',
      driverName: '王师傅',
      shipmentDate: '2026-01-08',
      status: 'in_transit'
    },
    {
      id: 2,
      shipmentNo: 'SHP20260110002',
      carrierName: '德邦物流',
      vehicleNo: '京B67890',
      driverName: '李师傅',
      shipmentDate: '2026-01-10',
      status: 'pending'
    }
  ],
  attachments: [
    {
      name: '合同_ORD20260105001.pdf',
      url: '#',
      category: '合同'
    },
    {
      name: '报价单.pdf',
      url: '#',
      category: '报价'
    }
  ]
})

// 业务地图数据
const mapRoutes = computed<RouteInfo[]>(() => {
  if (!orderInfo.value.shipments) return []

  return orderInfo.value.shipments.map(s => ({
    shipmentId: s.id,
    shipmentNo: s.shipmentNo,
    status: s.status,
    start: {
      longitude: 116.407526,
      latitude: 39.904989,
      location: '北京市海淀区'
    },
    end: {
      longitude: 121.473701,
      latitude: 31.230416,
      location: '上海市浦东新区'
    },
    driverName: s.driverName,
    estimatedArrivalTime: s.status === 'in_transit' ? '2026-01-12 18:00' : undefined
  }))
})

// 流程时间线数据
const timelineData = computed<TimelineNode[]>(() => {
  return [
    {
      title: '来单登记',
      time: '2026-01-05 10:30',
      status: 'completed',
      description: '客户下达订单需求，产品数量3种，交货日期2026-02-15',
      operator: '张三（客户经理）',
      attachments: [
        { name: '需求单.pdf', url: '#' }
      ]
    },
    {
      title: '供应商确认',
      time: '2026-01-05 14:20',
      status: 'completed',
      description: '确认供应商为北京精密机械厂，完成资质审核',
      operator: '李四（采购专员）',
      attachments: [
        { name: '供应商资质.pdf', url: '#' },
        { name: '报价单.pdf', url: '#' }
      ]
    },
    {
      title: '物流发运',
      time: '2026-01-08 09:00',
      status: 'active',
      description: '第1批发运已发出，预计1月12日到达',
      operator: '王五（运营专员）',
      attachments: [
        { name: '发货单.pdf', url: '#' },
        { name: '物流凭证.jpg', url: '#' }
      ]
    },
    {
      title: '到货签收',
      status: 'pending',
      description: '等待货物到达并完成签收'
    },
    {
      title: '结算归档',
      status: 'pending',
      description: '完成对账结算后归档'
    }
  ]
})

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
      sums[index] = data.reduce((sum: number, row: any) => sum + row.quantity, 0)
    } else if (column.property === 'amount') {
      sums[index] = '¥' + data.reduce((sum: number, row: any) => sum + row.amount, 0).toLocaleString()
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
  router.push(`/order/edit/${orderInfo.value.id}`)
}

// 分享
function handleShare() {
  ElMessage.info('分享功能开发中')
}

// 导出
function handleExport() {
  ElMessage.info('导出功能开发中')
}

// 新建发运
function handleAddShipment() {
  router.push(`/shipment/create?orderId=${orderInfo.value.id}`)
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

// 状态类型
function getStatusType(status: string) {
  const map: Record<string, any> = {
    draft: 'info',
    executing: 'warning',
    partially_delivered: 'primary',
    completed: 'success',
    cancelled: 'danger'
  }
  return map[status] || 'info'
}

// 状态文本
function getStatusText(status: string) {
  const map: Record<string, string> = {
    draft: '草稿',
    executing: '执行中',
    partially_delivered: '部分到货',
    completed: '已完成',
    cancelled: '已取消'
  }
  return map[status] || '未知'
}

// 发运状态类型
function getShipmentStatusType(status: string) {
  const map: Record<string, any> = {
    pending: 'info',
    in_transit: 'warning',
    delivered: 'success'
  }
  return map[status] || 'info'
}

// 发运状态文本
function getShipmentStatusText(status: string) {
  const map: Record<string, string> = {
    pending: '待提货',
    in_transit: '在途',
    delivered: '已到货'
  }
  return map[status] || '未知'
}
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
    }
  }
}
</style>
