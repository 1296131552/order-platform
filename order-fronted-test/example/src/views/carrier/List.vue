<template>
  <div class="carrier-list">
    <!-- 搜索区域 -->
    <SearchBar
      :items="searchItems"
      @search="handleSearch"
      @reset="handleReset"
    />

    <!-- 操作按钮 -->
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="handleCreate">新建承运商</el-button>
      <el-button :icon="Download" @click="handleExport">导出</el-button>
    </div>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <TableForm
        :data="tableData"
        :total="total"
        :loading="loading"
        v-model:page="page"
        v-model:page-size="pageSize"
      >
        <el-table-column prop="code" label="承运商编码" width="140" />
        <el-table-column prop="name" label="承运商名称" min-width="180" />
        <el-table-column prop="contactPerson" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'">
              {{ row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link size="small" @click="handleStatistics(row)">统计</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </TableForm>
    </el-card>

    <!-- 统计对话框 -->
    <el-dialog v-model="statisticsVisible" title="承运商统计" width="800px">
      <el-descriptions v-if="currentStatistics" :column="2" border>
        <el-descriptions-item label="发运单数">
          {{ currentStatistics.totalShipments }}
        </el-descriptions-item>
        <el-descriptions-item label="平均评分">
          <el-rate v-model="currentStatistics.averageRating" disabled show-score />
        </el-descriptions-item>
        <el-descriptions-item label="准时率">
          <el-tag :type="getOnTimeRateType(currentStatistics.onTimeRate)">
            {{ currentStatistics.onTimeRate }}%
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="异常率">
          <el-tag :type="getExceptionRateType(currentStatistics.exceptionRate)">
            {{ currentStatistics.exceptionRate }}%
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download } from '@element-plus/icons-vue'
import SearchBar from '@/components/SearchBar.vue'
import TableForm from '@/components/TableForm.vue'
import { getCarrierList, deleteCarrier, getCarrierStatistics, type CarrierQueryParams } from '@/api/carrier'

const router = useRouter()

// 搜索配置
const searchItems = [
  { prop: 'name', label: '承运商名称', type: 'input' },
  { prop: 'code', label: '承运商编码', type: 'input' },
  { prop: 'status', label: '状态', type: 'select', options: [
    { label: '启用', value: 'active' },
    { label: '停用', value: 'inactive' }
  ]}
]

// 表格数据
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

// 搜索表单
const searchForm = ref<CarrierQueryParams>({})

// 统计对话框
const statisticsVisible = ref(false)
const currentStatistics = ref<any>(null)

// 模拟数据
const mockData = [
  {
    id: 1,
    code: 'CAR001',
    name: '顺丰速运',
    contactPerson: '张经理',
    contactPhone: '13800138001',
    address: '北京市朝阳区',
    status: 'active',
    createTime: '2026-01-05 10:30:00'
  },
  {
    id: 2,
    code: 'CAR002',
    name: '德邦物流',
    contactPerson: '李经理',
    contactPhone: '13800138002',
    address: '上海市浦东新区',
    status: 'active',
    createTime: '2026-01-04 14:20:00'
  },
  {
    id: 3,
    code: 'CAR003',
    name: '安能物流',
    contactPerson: '王经理',
    contactPhone: '13800138003',
    address: '深圳市宝安区',
    status: 'inactive',
    createTime: '2026-01-03 09:15:00'
  }
]

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm.value,
      page: page.value,
      pageSize: pageSize.value
    }
    const res = await getCarrierList(params)
    tableData.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('加载承运商列表失败：', error)
    // API 失败时使用模拟数据
    tableData.value = mockData
    total.value = mockData.length
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = (params: any) => {
  searchForm.value = params
  page.value = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.value = {}
  page.value = 1
  loadData()
}

// 新建
const handleCreate = () => {
  router.push('/carrier/create')
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 查看
const handleView = (row: any) => {
  router.push(`/carrier/${row.id}`)
}

// 编辑
const handleEdit = (row: any) => {
  router.push(`/carrier/edit/${row.id}`)
}

// 统计
const handleStatistics = async (row: any) => {
  try {
    currentStatistics.value = await getCarrierStatistics(row.id)
    statisticsVisible.value = true
  } catch (error) {
    console.error('加载统计数据失败：', error)
  }
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除承运商 "${row.name}" 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCarrier(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败：', error)
    }
  }).catch(() => {})
}

// 准时率类型
const getOnTimeRateType = (rate: number) => {
  if (rate >= 95) return 'success'
  if (rate >= 80) return 'warning'
  return 'danger'
}

// 异常率类型
const getExceptionRateType = (rate: number) => {
  if (rate <= 2) return 'success'
  if (rate <= 5) return 'warning'
  return 'danger'
}

// 初始化
loadData()
</script>

<style scoped lang="scss">
.carrier-list {
  .toolbar {
    margin-bottom: 16px;
  }
  .table-card {
    :deep(.el-pagination) {
      margin-top: 16px;
    }
  }
}
</style>
