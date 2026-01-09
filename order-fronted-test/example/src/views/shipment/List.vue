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
            <el-option label="待提货" value="PENDING" />
            <el-option label="在途" value="IN_TRANSIT" />
            <el-option label="已到货" value="DELIVERED" />
          </el-select>
        </el-form-item>
        <el-form-item label="发运日期">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
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
        <el-table-column prop="departureTime" label="发运日期" width="120">
          <template #default="{ row }">
            {{ row.departureTime ? row.departureTime.substring(0, 10) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="primary" link size="small" @click="handleTrack(row)">轨迹</el-button>
            <el-button 
              v-if="row.status === 'PENDING'" 
              type="success" 
              link 
              size="small" 
              @click="handleDispatch(row)"
            >发货</el-button>
            <el-button 
              v-if="row.status === 'IN_TRANSIT'" 
              type="warning" 
              link 
              size="small" 
              @click="handleArrive(row)"
            >到货</el-button>
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

    <!-- 发货对话框 -->
    <el-dialog v-model="dispatchDialogVisible" title="确认发货" width="400px">
      <el-form :model="dispatchForm" label-width="80px">
        <el-form-item label="发货时间" required>
          <el-date-picker
            v-model="dispatchForm.departureTime"
            type="datetime"
            placeholder="选择发货时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dispatchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDispatch" :loading="dispatchLoading">确认</el-button>
      </template>
    </el-dialog>

    <!-- 到货对话框 -->
    <el-dialog v-model="arriveDialogVisible" title="确认到货" width="400px">
      <el-form :model="arriveForm" label-width="80px">
        <el-form-item label="到货时间" required>
          <el-date-picker
            v-model="arriveForm.arrivalTime"
            type="datetime"
            placeholder="选择到货时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="arriveForm.remark" type="textarea" rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="arriveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmArrive" :loading="arriveLoading">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Search, Refresh, Download } from '@element-plus/icons-vue'
import { 
  getShipmentList, 
  dispatchShipment, 
  arriveShipment,
  ShipmentStatus,
  type Shipment,
  type ShipmentQueryParams
} from '@/api/shipment'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  shipmentNo: '',
  orderNo: '',
  carrierName: '',
  status: '',
  dateRange: [] as string[]
})

// 表格数据
const loading = ref(false)
const tableData = ref<Shipment[]>([])

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 发货对话框
const dispatchDialogVisible = ref(false)
const dispatchLoading = ref(false)
const dispatchForm = reactive({
  id: 0,
  departureTime: ''
})

// 到货对话框
const arriveDialogVisible = ref(false)
const arriveLoading = ref(false)
const arriveForm = reactive({
  id: 0,
  arrivalTime: '',
  remark: ''
})

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const params: ShipmentQueryParams = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      shipmentNo: searchForm.shipmentNo || undefined,
      status: searchForm.status || undefined,
      startDate: searchForm.dateRange?.[0] || undefined,
      endDate: searchForm.dateRange?.[1] || undefined,
      keyword: searchForm.orderNo || searchForm.carrierName || undefined
    }
    const res = await getShipmentList(params)
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('加载发运列表失败:', error)
    ElMessage.error('加载发运列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  loadData()
}

// 重置
function handleReset() {
  Object.assign(searchForm, {
    shipmentNo: '',
    orderNo: '',
    carrierName: '',
    status: '',
    dateRange: []
  })
  pagination.page = 1
  loadData()
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
function handleView(row: Shipment) {
  router.push(`/shipment/${row.id}`)
}

// 轨迹
function handleTrack(row: Shipment) {
  ElMessage.info(`查看 ${row.shipmentNo} 的运输轨迹`)
}

// 发货
function handleDispatch(row: Shipment) {
  dispatchForm.id = row.id
  dispatchForm.departureTime = ''
  dispatchDialogVisible.value = true
}

// 确认发货
async function confirmDispatch() {
  if (!dispatchForm.departureTime) {
    ElMessage.warning('请选择发货时间')
    return
  }
  dispatchLoading.value = true
  try {
    await dispatchShipment(dispatchForm.id, { departureTime: dispatchForm.departureTime })
    ElMessage.success('发货成功')
    dispatchDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('发货失败:', error)
    ElMessage.error('发货失败')
  } finally {
    dispatchLoading.value = false
  }
}

// 到货
function handleArrive(row: Shipment) {
  arriveForm.id = row.id
  arriveForm.arrivalTime = ''
  arriveForm.remark = ''
  arriveDialogVisible.value = true
}

// 确认到货
async function confirmArrive() {
  if (!arriveForm.arrivalTime) {
    ElMessage.warning('请选择到货时间')
    return
  }
  arriveLoading.value = true
  try {
    await arriveShipment(arriveForm.id, { 
      arrivalTime: arriveForm.arrivalTime,
      remark: arriveForm.remark || undefined
    })
    ElMessage.success('到货确认成功')
    arriveDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('到货确认失败:', error)
    ElMessage.error('到货确认失败')
  } finally {
    arriveLoading.value = false
  }
}

// 分页
function handleSizeChange(size: number) {
  pagination.pageSize = size
  loadData()
}

function handleCurrentChange(page: number) {
  pagination.page = page
  loadData()
}

// 状态类型
function getStatusType(status: string) {
  const map: Record<string, any> = {
    [ShipmentStatus.PENDING]: 'info',
    [ShipmentStatus.IN_TRANSIT]: 'warning',
    [ShipmentStatus.DELIVERED]: 'success'
  }
  return map[status] || 'info'
}

// 状态文本
function getStatusText(status: string) {
  const map: Record<string, string> = {
    [ShipmentStatus.PENDING]: '待提货',
    [ShipmentStatus.IN_TRANSIT]: '在途',
    [ShipmentStatus.DELIVERED]: '已到货'
  }
  return map[status] || '未知'
}

// 初始化
onMounted(() => {
  loadData()
})
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
