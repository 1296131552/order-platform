<template>
  <div class="shipment-list-container">
    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="发运单号">
          <el-input v-model="searchForm.shipmentNo" placeholder="请输入发运单号" clearable />
        </el-form-item>
        <el-form-item label="订单编号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable />
        </el-form-item>
        <el-form-item label="承运商">
          <el-input v-model="searchForm.carrierName" placeholder="请输入承运商" clearable />
        </el-form-item>
        <el-form-item label="发运状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="待提货" value="pending" />
            <el-option label="在途" value="transit" />
            <el-option label="已到货" value="arrived" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="handleCreate">创建发运</el-button>
      <el-button :icon="Download" @click="handleExport">导出</el-button>
    </div>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column type="selection" width="55" />
        <el-table-column prop="shipmentNo" label="发运单号" width="150" fixed />
        <el-table-column prop="orderNo" label="订单编号" width="150" />
        <el-table-column prop="customerName" label="客户名称" width="180" />
        <el-table-column prop="carrierName" label="承运商" width="150" />
        <el-table-column prop="vehicleNo" label="车辆号牌" width="120" />
        <el-table-column prop="driverPhone" label="司机电话" width="130" />
        <el-table-column prop="shipmentDate" label="发运日期" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="primary" link size="small" @click="handleTrack(row)">轨迹</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Search, Refresh, Download } from '@element-plus/icons-vue'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  shipmentNo: '',
  orderNo: '',
  carrierName: '',
  status: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref([
  {
    id: 1,
    shipmentNo: 'SHP20260108001',
    orderNo: 'ORD20260105001',
    customerName: '北京科技有限公司',
    carrierName: '顺丰速运',
    vehicleNo: '京A12345',
    driverPhone: '138****1234',
    shipmentDate: '2026-01-08',
    status: 'transit'
  },
  {
    id: 2,
    shipmentNo: 'SHP20260107001',
    orderNo: 'ORD20260104002',
    customerName: '上海贸易公司',
    carrierName: '德邦物流',
    vehicleNo: '沪B67890',
    driverPhone: '139****5678',
    shipmentDate: '2026-01-07',
    status: 'arrived'
  },
  {
    id: 3,
    shipmentNo: 'SHP20260106001',
    orderNo: 'ORD20260104001',
    customerName: '深圳电子厂',
    carrierName: '安能物流',
    vehicleNo: '粤C24680',
    driverPhone: '137****9012',
    shipmentDate: '2026-01-06',
    status: 'arrived'
  }
])

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 3
})

// 搜索
function handleSearch() {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('查询成功')
  }, 500)
}

// 重置
function handleReset() {
  Object.assign(searchForm, {
    shipmentNo: '',
    orderNo: '',
    carrierName: '',
    status: ''
  })
  ElMessage.info('已重置搜索条件')
}

// 创建发运
function handleCreate() {
  ElMessage.info('创建发运功能开发中')
}

// 导出
function handleExport() {
  ElMessage.info('导出功能开发中')
}

// 查看
function handleView(row: any) {
  router.push(`/shipment/${row.id}`)
}

// 轨迹
function handleTrack(row: any) {
  ElMessage.info(`查看 ${row.shipmentNo} 的运输轨迹`)
}

// 分页
function handleSizeChange(size: number) {
  pagination.pageSize = size
}

function handleCurrentChange(page: number) {
  pagination.page = page
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
.shipment-list-container {
  .search-card {
    margin-bottom: 16px;

    .search-form {
      margin-bottom: -10px;

      .el-form-item {
        margin-bottom: 10px;
      }
    }
  }

  .toolbar {
    margin-bottom: 16px;
  }

  .table-card {
    .el-pagination {
      margin-top: 16px;
      justify-content: flex-end;
    }
  }
}
</style>
