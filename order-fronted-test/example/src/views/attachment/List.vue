<template>
  <div class="attachment-list-container">
    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="文件名称">
          <el-input v-model="searchForm.fileName" placeholder="请输入文件名称" clearable />
        </el-form-item>
        <el-form-item label="关联单据">
          <el-input v-model="searchForm.refNo" placeholder="请输入单据编号" clearable />
        </el-form-item>
        <el-form-item label="文件类型">
          <el-select v-model="searchForm.fileType" placeholder="请选择类型" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="图片" value="image" />
            <el-option label="文档" value="document" />
            <el-option label="表格" value="spreadsheet" />
            <el-option label="其他" value="other" />
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
      <el-button type="primary" :icon="Upload" @click="handleUpload">上传附件</el-button>
      <el-button :icon="FolderAdd" @click="handleNewFolder">新建文件夹</el-button>
      <el-button :icon="Download" @click="handleBatchDownload">批量下载</el-button>
    </div>

    <!-- 文件列表 -->
    <el-card class="file-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column type="selection" width="55" />
        <el-table-column label="文件名" min-width="250">
          <template #default="{ row }">
            <div class="file-name">
              <el-icon v-if="row.type === 'folder'" :size="20"><Folder /></el-icon>
              <el-icon v-else-if="row.fileType === 'image'" :size="20"><Picture /></el-icon>
              <el-icon v-else-if="row.fileType === 'document'" :size="20"><Document /></el-icon>
              <el-icon v-else-if="row.fileType === 'spreadsheet'" :size="20"><Tickets /></el-icon>
              <el-icon v-else :size="20"><Document /></el-icon>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="refType" label="关联类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.refType" size="small">{{ getRefTypeText(row.refType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="refNo" label="关联单据" width="150" />
        <el-table-column prop="fileSize" label="文件大小" width="100" />
        <el-table-column prop="uploader" label="上传人" width="100" />
        <el-table-column prop="uploadTime" label="上传时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.type !== 'folder'" type="primary" link size="small" @click="handleDownload(row)">
              下载
            </el-button>
            <el-button type="primary" link size="small" @click="handlePreview(row)">预览</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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
import { ElMessage } from 'element-plus'
import {
  Upload,
  FolderAdd,
  Download,
  Search,
  Refresh,
  Folder,
  Picture,
  Document,
  Tickets
} from '@element-plus/icons-vue'

// 搜索表单
const searchForm = reactive({
  fileName: '',
  refNo: '',
  fileType: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref([
  {
    id: 1,
    name: '订单相关文档',
    type: 'folder',
    fileType: '',
    refType: '',
    refNo: '',
    fileSize: '-',
    uploader: 'admin',
    uploadTime: '2026-01-05 10:00:00'
  },
  {
    id: 2,
    name: 'ORD20260105001_合同.pdf',
    type: 'file',
    fileType: 'document',
    refType: 'order',
    refNo: 'ORD20260105001',
    fileSize: '2.5 MB',
    uploader: '张三',
    uploadTime: '2026-01-05 14:30:00'
  },
  {
    id: 3,
    name: 'ORD20260104002_产品图片.jpg',
    type: 'file',
    fileType: 'image',
    refType: 'order',
    refNo: 'ORD20260104002',
    fileSize: '1.2 MB',
    uploader: '李四',
    uploadTime: '2026-01-04 16:20:00'
  },
  {
    id: 4,
    name: 'SHP20260108001_运单.xlsx',
    type: 'file',
    fileType: 'spreadsheet',
    refType: 'shipment',
    refNo: 'SHP20260108001',
    fileSize: '156 KB',
    uploader: '王五',
    uploadTime: '2026-01-08 09:15:00'
  }
])

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 4
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
    fileName: '',
    refNo: '',
    fileType: ''
  })
  ElMessage.info('已重置搜索条件')
}

// 上传附件
function handleUpload() {
  ElMessage.info('上传附件功能开发中')
}

// 新建文件夹
function handleNewFolder() {
  ElMessage.info('新建文件夹功能开发中')
}

// 批量下载
function handleBatchDownload() {
  ElMessage.info('批量下载功能开发中')
}

// 下载
function handleDownload(row: any) {
  ElMessage.success(`开始下载：${row.name}`)
}

// 预览
function handlePreview(row: any) {
  ElMessage.info(`预览文件：${row.name}`)
}

// 删除
function handleDelete(row: any) {
  ElMessage.success(`已删除：${row.name}`)
}

// 分页
function handleSizeChange(size: number) {
  pagination.pageSize = size
}

function handleCurrentChange(page: number) {
  pagination.page = page
}

// 关联类型文本
function getRefTypeText(type: string) {
  const map: Record<string, string> = {
    order: '订单',
    shipment: '发运',
    receipt: '签收',
    exception: '异常'
  }
  return map[type] || '其他'
}
</script>

<style scoped lang="scss">
.attachment-list-container {
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

  .file-card {
    .file-name {
      display: flex;
      align-items: center;
      gap: 8px;

      span {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .el-pagination {
      margin-top: 16px;
      justify-content: flex-end;
    }
  }
}
</style>
