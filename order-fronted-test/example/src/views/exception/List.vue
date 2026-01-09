<template>
  <div class="exception-list-container">
    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="异常编号">
          <el-input v-model="searchForm.exceptionNo" placeholder="请输入异常编号" clearable />
        </el-form-item>
        <el-form-item label="异常类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="数量差异" value="QUANTITY" />
            <el-option label="质量异常" value="QUALITY" />
            <el-option label="运输延误" value="DELAY" />
            <el-option label="货物损坏" value="DAMAGE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="异常级别">
          <el-select v-model="searchForm.level" placeholder="请选择级别" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="CRITICAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已解决" value="RESOLVED" />
            <el-option label="已关闭" value="CLOSED" />
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
      <el-button type="danger" :icon="Plus" @click="handleCreate">上报异常</el-button>
      <el-button :icon="Download" @click="handleExport">导出</el-button>
    </div>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column type="selection" width="55" />
        <el-table-column prop="exceptionNo" label="异常编号" width="150" fixed />
        <el-table-column prop="title" label="异常标题" min-width="150" show-overflow-tooltip />
        <el-table-column prop="type" label="异常类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="异常级别" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)">{{ getLevelText(row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="异常描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="reporterName" label="上报人" width="100" />
        <el-table-column prop="reportTime" label="上报时间" width="160" />
        <el-table-column prop="status" label="处理状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handlerName" label="处理人" width="100" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button 
              v-if="row.status === 'PENDING'" 
              type="warning" 
              link 
              size="small" 
              @click="handleAssign(row)"
            >
              分配
            </el-button>
            <el-button 
              v-if="row.status === 'PROCESSING'" 
              type="success" 
              link 
              size="small" 
              @click="handleProcess(row)"
            >
              处理
            </el-button>
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

    <!-- 上报异常对话框 -->
    <el-dialog v-model="createDialogVisible" title="上报异常" width="600px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="异常标题" prop="title">
          <el-input v-model="createForm.title" placeholder="请输入异常标题" />
        </el-form-item>
        <el-form-item label="异常类型" prop="type">
          <el-select v-model="createForm.type" placeholder="请选择异常类型" style="width: 100%">
            <el-option label="数量差异" value="QUANTITY" />
            <el-option label="质量异常" value="QUALITY" />
            <el-option label="运输延误" value="DELAY" />
            <el-option label="货物损坏" value="DAMAGE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="异常级别" prop="level">
          <el-select v-model="createForm.level" placeholder="请选择异常级别" style="width: 100%">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="CRITICAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联类型" prop="relatedType">
          <el-select v-model="createForm.relatedType" placeholder="请选择关联类型" style="width: 100%">
            <el-option label="订单" value="ORDER" />
            <el-option label="发运单" value="SHIPMENT" />
            <el-option label="快递单" value="SHIPMENT_LINE" />
            <el-option label="签收单" value="RECEIPT" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联ID" prop="relatedId">
          <el-input-number v-model="createForm.relatedId" :min="1" placeholder="请输入关联ID" style="width: 100%" />
        </el-form-item>
        <el-form-item label="异常描述" prop="description">
          <el-input 
            v-model="createForm.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请详细描述异常情况" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitCreate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配处理人对话框 -->
    <el-dialog v-model="assignDialogVisible" title="分配处理人" width="400px">
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="100px">
        <el-form-item label="处理人" prop="handlerId">
          <el-select v-model="assignForm.handlerId" placeholder="请选择处理人" style="width: 100%">
            <el-option 
              v-for="user in userList" 
              :key="user.id" 
              :label="user.realName" 
              :value="user.id" 
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitAssign">确定</el-button>
      </template>
    </el-dialog>

    <!-- 处理异常对话框 -->
    <el-dialog v-model="handleDialogVisible" title="处理异常" width="600px">
      <el-form ref="handleFormRef" :model="handleForm" :rules="handleRules" label-width="100px">
        <el-form-item label="处理方案" prop="solution">
          <el-input 
            v-model="handleForm.solution" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入处理方案" 
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input 
            v-model="handleForm.remark" 
            type="textarea" 
            :rows="2" 
            placeholder="请输入备注（可选）" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitHandle">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="异常详情" width="700px">
      <el-descriptions :column="2" border v-if="currentException">
        <el-descriptions-item label="异常编号">{{ currentException.exceptionNo }}</el-descriptions-item>
        <el-descriptions-item label="异常标题">{{ currentException.title }}</el-descriptions-item>
        <el-descriptions-item label="异常类型">
          <el-tag>{{ getTypeText(currentException.type) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="异常级别">
          <el-tag :type="getLevelType(currentException.level)">{{ getLevelText(currentException.level) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="getStatusType(currentException.status)">{{ getStatusText(currentException.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="关联类型">{{ getRelatedTypeText(currentException.relatedType) }}</el-descriptions-item>
        <el-descriptions-item label="上报人">{{ currentException.reporterName }}</el-descriptions-item>
        <el-descriptions-item label="上报时间">{{ currentException.reportTime }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ currentException.handlerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ currentException.handleTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="异常描述" :span="2">{{ currentException.description }}</el-descriptions-item>
        <el-descriptions-item label="处理方案" :span="2" v-if="currentException.solution">
          {{ currentException.solution }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search, Refresh, Download } from '@element-plus/icons-vue'
import { 
  getExceptionList, 
  getExceptionDetail, 
  createException, 
  assignException, 
  handleException,
  type Exception,
  type ExceptionQueryParams,
  type CreateExceptionParams,
  type HandleExceptionParams
} from '@/api/exception'
import { getUserList } from '@/api/user'

// 搜索表单
const searchForm = reactive<ExceptionQueryParams>({
  exceptionNo: '',
  type: '',
  level: '',
  status: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref<Exception[]>([])

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 用户列表（用于分配处理人）
const userList = ref<{ id: number; realName: string }[]>([])

// 上报异常对话框
const createDialogVisible = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = reactive<CreateExceptionParams>({
  type: '',
  level: '',
  title: '',
  description: '',
  relatedType: '',
  relatedId: 0
})
const createRules: FormRules = {
  title: [{ required: true, message: '请输入异常标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择异常类型', trigger: 'change' }],
  level: [{ required: true, message: '请选择异常级别', trigger: 'change' }],
  relatedType: [{ required: true, message: '请选择关联类型', trigger: 'change' }],
  relatedId: [{ required: true, message: '请输入关联ID', trigger: 'blur' }],
  description: [{ required: true, message: '请输入异常描述', trigger: 'blur' }]
}

// 分配处理人对话框
const assignDialogVisible = ref(false)
const assignFormRef = ref<FormInstance>()
const assignForm = reactive({
  id: 0,
  handlerId: undefined as number | undefined
})
const assignRules: FormRules = {
  handlerId: [{ required: true, message: '请选择处理人', trigger: 'change' }]
}

// 处理异常对话框
const handleDialogVisible = ref(false)
const handleFormRef = ref<FormInstance>()
const handleForm = reactive<HandleExceptionParams & { id: number }>({
  id: 0,
  solution: '',
  remark: ''
})
const handleRules: FormRules = {
  solution: [{ required: true, message: '请输入处理方案', trigger: 'blur' }]
}

// 查看详情对话框
const detailDialogVisible = ref(false)
const currentException = ref<Exception | null>(null)

// 提交加载状态
const submitLoading = ref(false)

// 加载异常列表
async function loadData() {
  loading.value = true
  try {
    const params: ExceptionQueryParams = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    // 移除空值参数
    Object.keys(params).forEach(key => {
      if (params[key as keyof ExceptionQueryParams] === '' || params[key as keyof ExceptionQueryParams] === undefined) {
        delete params[key as keyof ExceptionQueryParams]
      }
    })
    
    const res = await getExceptionList(params)
    if (res && res.records) {
      tableData.value = res.records
      pagination.total = res.total
    } else if (Array.isArray(res)) {
      tableData.value = res
      pagination.total = res.length
    }
  } catch (error) {
    console.error('加载异常列表失败:', error)
    ElMessage.error('加载异常列表失败')
  } finally {
    loading.value = false
  }
}

// 加载用户列表
async function loadUserList() {
  try {
    const res = await getUserList({ page: 1, pageSize: 100 })
    if (res && res.records) {
      userList.value = res.records.map((u: any) => ({ id: u.id, realName: u.realName }))
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
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
    exceptionNo: '',
    type: '',
    level: '',
    status: ''
  })
  pagination.page = 1
  loadData()
}

// 上报异常
function handleCreate() {
  Object.assign(createForm, {
    type: '',
    level: '',
    title: '',
    description: '',
    relatedType: '',
    relatedId: 0
  })
  createDialogVisible.value = true
}

// 提交上报异常
async function submitCreate() {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await createException(createForm)
        ElMessage.success('异常上报成功')
        createDialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('上报异常失败:', error)
        ElMessage.error('上报异常失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 导出
function handleExport() {
  ElMessage.info('导出功能开发中')
}

// 查看详情
async function handleView(row: Exception) {
  try {
    const res = await getExceptionDetail(row.id)
    currentException.value = res || row
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取异常详情失败:', error)
    currentException.value = row
    detailDialogVisible.value = true
  }
}

// 分配处理人
function handleAssign(row: Exception) {
  assignForm.id = row.id
  assignForm.handlerId = undefined
  assignDialogVisible.value = true
}

// 提交分配处理人
async function submitAssign() {
  if (!assignFormRef.value) return
  await assignFormRef.value.validate(async (valid) => {
    if (valid && assignForm.handlerId) {
      submitLoading.value = true
      try {
        await assignException(assignForm.id, assignForm.handlerId)
        ElMessage.success('分配处理人成功')
        assignDialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('分配处理人失败:', error)
        ElMessage.error('分配处理人失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 处理异常
function handleProcess(row: Exception) {
  handleForm.id = row.id
  handleForm.solution = ''
  handleForm.remark = ''
  handleDialogVisible.value = true
}

// 提交处理异常
async function submitHandle() {
  if (!handleFormRef.value) return
  await handleFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await handleException(handleForm.id, {
          solution: handleForm.solution,
          remark: handleForm.remark
        })
        ElMessage.success('异常处理成功')
        handleDialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('处理异常失败:', error)
        ElMessage.error('处理异常失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
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

// 类型文本
function getTypeText(type: string) {
  const map: Record<string, string> = {
    QUANTITY: '数量差异',
    QUALITY: '质量异常',
    DELAY: '运输延误',
    DAMAGE: '货物损坏',
    OTHER: '其他'
  }
  return map[type] || type || '其他'
}

// 级别类型
function getLevelType(level: string) {
  const map: Record<string, any> = {
    LOW: 'info',
    MEDIUM: 'warning',
    HIGH: 'danger',
    CRITICAL: 'danger'
  }
  return map[level] || 'info'
}

// 级别文本
function getLevelText(level: string) {
  const map: Record<string, string> = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高',
    CRITICAL: '紧急'
  }
  return map[level] || level || '未知'
}

// 状态类型
function getStatusType(status: string) {
  const map: Record<string, any> = {
    PENDING: 'danger',
    PROCESSING: 'warning',
    RESOLVED: 'success',
    CLOSED: 'info'
  }
  return map[status] || 'info'
}

// 状态文本
function getStatusText(status: string) {
  const map: Record<string, string> = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    RESOLVED: '已解决',
    CLOSED: '已关闭'
  }
  return map[status] || status || '未知'
}

// 关联类型文本
function getRelatedTypeText(relatedType: string) {
  const map: Record<string, string> = {
    ORDER: '订单',
    SHIPMENT: '发运单',
    SHIPMENT_LINE: '快递单',
    RECEIPT: '签收单'
  }
  return map[relatedType] || relatedType || '未知'
}

// 初始化
onMounted(() => {
  loadData()
  loadUserList()
})
</script>

<style scoped lang="scss">
.exception-list-container {
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
