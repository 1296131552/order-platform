<template>
  <div class="attachment-list-container">
    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="业务ID">
          <el-input v-model="searchForm.bizId" placeholder="请输入业务ID" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <div class="toolbar">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :show-file-list="false"
        :on-change="handleFileChange"
        multiple
      >
        <el-button type="primary" :icon="Upload">上传附件</el-button>
      </el-upload>
      <el-button :icon="Download" :disabled="!selectedRows.length" @click="handleBatchDownload">
        批量下载
      </el-button>
    </div>

    <!-- 文件列表 -->
    <el-card class="file-card" shadow="never">
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="文件名" min-width="250">
          <template #default="{ row }">
            <div class="file-name">
              <el-icon v-if="isImage(row.fileType)" :size="20"><Picture /></el-icon>
              <el-icon v-else :size="20"><Document /></el-icon>
              <span>{{ row.fileOriginalName || row.fileName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="fileType" label="文件类型" width="120" />
        <el-table-column label="文件大小" width="120">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="uploaderName" label="上传人" width="100" />
        <el-table-column prop="createdAt" label="上传时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleDownload(row)">
              下载
            </el-button>
            <el-button type="primary" link size="small" @click="handlePreview(row)">
              预览
            </el-button>
            <el-popconfirm title="确定删除该附件吗？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 预览对话框 -->
    <el-dialog v-model="previewVisible" title="文件预览" width="800px">
      <div class="preview-content">
        <img v-if="previewType === 'image'" :src="previewUrl" alt="预览图片" />
        <div v-else class="no-preview">
          <el-icon :size="64"><Document /></el-icon>
          <p>该文件类型不支持预览，请下载后查看</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, Download, Search, Refresh, Picture, Document } from '@element-plus/icons-vue'
import type { UploadFile } from 'element-plus'
import {
  getAttachmentListByBizId,
  uploadAttachment,
  batchUploadAttachment,
  downloadAttachment,
  deleteAttachment,
  type Attachment
} from '@/api/attachment'

// 搜索表单
const searchForm = reactive({
  bizId: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref<Attachment[]>([])
const selectedRows = ref<Attachment[]>([])

// 预览
const previewVisible = ref(false)
const previewUrl = ref('')
const previewType = ref<'image' | 'other'>('other')

// 搜索
async function handleSearch() {
  if (!searchForm.bizId) {
    ElMessage.warning('请输入业务ID')
    return
  }
  loading.value = true
  try {
    const res = await getAttachmentListByBizId(searchForm.bizId)
    tableData.value = res || []
    ElMessage.success('查询成功')
  } catch (error) {
    console.error('查询附件列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置
function handleReset() {
  searchForm.bizId = ''
  tableData.value = []
  ElMessage.info('已重置')
}

// 文件选择变化
async function handleFileChange(uploadFile: UploadFile) {
  if (!uploadFile.raw) return
  
  loading.value = true
  try {
    await uploadAttachment(uploadFile.raw)
    ElMessage.success('上传成功')
    // 如果有业务ID，刷新列表
    if (searchForm.bizId) {
      await handleSearch()
    }
  } catch (error) {
    console.error('上传失败:', error)
  } finally {
    loading.value = false
  }
}

// 批量下载
async function handleBatchDownload() {
  for (const row of selectedRows.value) {
    await handleDownload(row)
  }
}

// 下载
async function handleDownload(row: Attachment) {
  try {
    const res = await downloadAttachment(row.id)
    // 创建下载链接
    const blob = new Blob([res as unknown as BlobPart])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = row.fileOriginalName || row.fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success(`开始下载：${row.fileOriginalName || row.fileName}`)
  } catch (error) {
    console.error('下载失败:', error)
  }
}

// 预览
function handlePreview(row: Attachment) {
  if (isImage(row.fileType)) {
    previewType.value = 'image'
    previewUrl.value = row.url
  } else {
    previewType.value = 'other'
    previewUrl.value = ''
  }
  previewVisible.value = true
}

// 删除
async function handleDelete(row: Attachment) {
  try {
    await deleteAttachment(row.id)
    ElMessage.success('删除成功')
    // 刷新列表
    if (searchForm.bizId) {
      await handleSearch()
    }
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 选择变化
function handleSelectionChange(rows: Attachment[]) {
  selectedRows.value = rows
}

// 判断是否为图片
function isImage(fileType: string): boolean {
  if (!fileType) return false
  return fileType.startsWith('image/') || ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(fileType.toLowerCase())
}

// 格式化文件大小
function formatFileSize(size: number): string {
  if (!size) return '-'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  if (size < 1024 * 1024 * 1024) return (size / (1024 * 1024)).toFixed(2) + ' MB'
  return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
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
    display: flex;
    gap: 12px;
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
  }

  .preview-content {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 300px;

    img {
      max-width: 100%;
      max-height: 500px;
    }

    .no-preview {
      text-align: center;
      color: #909399;

      p {
        margin-top: 16px;
      }
    }
  }
}
</style>
