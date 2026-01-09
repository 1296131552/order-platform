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
    <el-card class="info-card" shadow="never">
      <template #header>
        <span class="card-title">基本信息</span>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="发运单号">{{ shipmentInfo.shipmentNo }}</el-descriptions-item>
        <el-descriptions-item label="订单编号">{{ shipmentInfo.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ shipmentInfo.customerName }}</el-descriptions-item>
        <el-descriptions-item label="承运商">{{ shipmentInfo.carrierName }}</el-descriptions-item>
        <el-descriptions-item label="车辆号牌">{{ shipmentInfo.vehicleNo }}</el-descriptions-item>
        <el-descriptions-item label="司机电话">{{ shipmentInfo.driverPhone }}</el-descriptions-item>
        <el-descriptions-item label="发货地址">{{ shipmentInfo.shipmentAddress }}</el-descriptions-item>
        <el-descriptions-item label="收货地址">{{ shipmentInfo.receiverAddress }}</el-descriptions-item>
        <el-descriptions-item label="发运状态">
          <el-tag :type="getStatusType(shipmentInfo.status)">{{ getStatusText(shipmentInfo.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发运日期">{{ shipmentInfo.shipmentDate }}</el-descriptions-item>
        <el-descriptions-item label="预计到达">{{ shipmentInfo.estimatedArrival }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ shipmentInfo.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 发运产品 -->
    <el-card class="product-card" shadow="never">
      <template #header>
        <span class="card-title">发运产品</span>
      </template>
      <el-table :data="shipmentInfo.products" border>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="productNo" label="产品编号" width="140" />
        <el-table-column prop="productName" label="产品名称" />
        <el-table-column prop="quantity" label="数量" width="100" align="right" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="weight" label="重量(kg)" width="100" align="right" />
        <el-table-column prop="volume" label="体积(m³)" width="100" align="right" />
      </el-table>
    </el-card>

    <!-- 运输轨迹 -->
    <el-card class="timeline-card" shadow="never">
      <template #header>
        <span class="card-title">运输轨迹</span>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="(item, index) in shipmentInfo.timeline"
          :key="index"
          :timestamp="item.time"
          :type="index === 0 ? 'primary' : 'info'"
          placement="top"
        >
          <el-card>
            <h4>{{ item.title }}</h4>
            <p>{{ item.description }}</p>
            <p v-if="item.location" class="location">📍 {{ item.location }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <!-- 签收信息 -->
    <el-card class="receipt-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">签收信息</span>
          <el-button v-if="!shipmentInfo.receipt" type="primary" size="small" :icon="Plus" @click="handleAddReceipt">
            新建签收
          </el-button>
        </div>
      </template>
      <div v-if="shipmentInfo.receipt">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="签收单号">{{ shipmentInfo.receipt.receiptNo }}</el-descriptions-item>
          <el-descriptions-item label="签收日期">{{ shipmentInfo.receipt.receiptDate }}</el-descriptions-item>
          <el-descriptions-item label="签收人">{{ shipmentInfo.receipt.receiver }}</el-descriptions-item>
          <el-descriptions-item label="签收数量">{{ shipmentInfo.receipt.quantity }}</el-descriptions-item>
          <el-descriptions-item label="是否有差异">
            <el-tag :type="shipmentInfo.receipt.hasException ? 'danger' : 'success'">
              {{ shipmentInfo.receipt.hasException ? '有差异' : '无差异' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="备注">{{ shipmentInfo.receipt.remark }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <el-empty v-else description="暂无签收记录" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Edit, Download, Plus } from '@element-plus/icons-vue'

const router = useRouter()

// 发运信息（模拟数据）
const shipmentInfo = ref({
  id: 1,
  shipmentNo: 'SHP20260108001',
  orderNo: 'ORD20260105001',
  customerName: '北京科技有限公司',
  carrierName: '顺丰速运',
  vehicleNo: '京A12345',
  driverPhone: '138****1234',
  shipmentAddress: '北京市朝阳区xxx路xxx号',
  receiverAddress: '上海市浦东新区xxx路xxx号',
  status: 'transit',
  shipmentDate: '2026-01-08 10:30',
  estimatedArrival: '2026-01-10 18:00',
  createTime: '2026-01-08 09:15:00',
  products: [
    { productNo: 'P001', productName: '精密机械零件A', quantity: 100, unit: '件', weight: 500, volume: 2.5 },
    { productNo: 'P002', productName: '精密机械零件B', quantity: 50, unit: '件', weight: 300, volume: 1.5 },
    { productNo: 'P003', productName: '精密机械零件C', quantity: 25, unit: '件', weight: 400, volume: 2.0 }
  ],
  timeline: [
    {
      time: '2026-01-08 10:30',
      title: '已发运',
      description: '货物已从发货地发出，司机正在运输中',
      location: '北京市朝阳区'
    },
    {
      time: '2026-01-08 14:20',
      title: '运输中',
      description: '货物正在高速公路运输中，预计按时到达',
      location: '天津市服务区'
    },
    {
      time: '2026-01-09 08:15',
      title: '运输中',
      description: '货物已到达江苏省，继续运输',
      location: '江苏省南京市'
    }
  ],
  receipt: null
})

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

// 新建签收
function handleAddReceipt() {
  ElMessage.info('新建签收功能开发中')
}

// 状态类型
function getStatusType(status: string) {
  const map: Record<string, any> = {
    pending: 'info',
    transit: 'warning',
    arrived: 'success'
  }
  return map[status] || 'info'
}

// 状态文本
function getStatusText(status: string) {
  const map: Record<string, string> = {
    pending: '待提货',
    transit: '在途',
    arrived: '已到货'
  }
  return map[status] || '未知'
}
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
  .product-card,
  .timeline-card,
  .receipt-card {
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
