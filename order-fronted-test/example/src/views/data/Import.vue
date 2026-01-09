<template>
  <div class="data-import">
    <el-page-header title="返回" @back="goBack">
      <template #content>
        <span class="page-title">数据导入</span>
      </template>
    </el-page-header>

    <el-card class="import-card" style="margin-top: 20px">
      <el-steps :active="currentStep" finish-status="success" align-center>
        <el-step title="选择类型" />
        <el-step title="上传文件" />
        <el-step title="数据预览" />
        <el-step title="导入完成" />
      </el-steps>
    </el-card>

    <!-- 步骤1: 选择导入类型 -->
    <el-card v-if="currentStep === 0" class="step-card" style="margin-top: 20px">
      <template #header>
        <span>选择导入类型</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="8" v-for="type in importTypes" :key="type.value">
          <div
            class="type-item"
            :class="{ active: importType === type.value }"
            @click="handleSelectType(type.value)"
          >
            <el-icon :size="40" :color="type.color">
              <component :is="type.icon" />
            </el-icon>
            <div class="type-name">{{ type.label }}</div>
            <div class="type-desc">{{ type.desc }}</div>
          </div>
        </el-col>
      </el-row>
      <div class="step-actions">
        <el-button type="primary" :disabled="!importType" @click="nextStep">下一步</el-button>
      </div>
    </el-card>

    <!-- 步骤2: 上传文件 -->
    <el-card v-if="currentStep === 1" class="step-card" style="margin-top: 20px">
      <template #header>
        <span>上传文件</span>
      </template>
      <div class="upload-area">
        <el-upload
          ref="uploadRef"
          class="upload-demo"
          drag
          :action="uploadUrl"
          :headers="uploadHeaders"
          :accept="'.xlsx,.xls'"
          :limit="1"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :before-upload="beforeUpload"
          :auto-upload="false"
          :on-change="handleFileChange"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 .xlsx、.xls 格式，文件大小不超过 10MB
            </div>
          </template>
        </el-upload>

        <div class="template-download">
          <el-link type="primary" @click="handleDownloadTemplate">
            <el-icon><Download /></el-icon>
            下载导入模板
          </el-link>
        </div>
      </div>
      <div class="step-actions">
        <el-button @click="prevStep">上一步</el-button>
        <el-button type="primary" :disabled="!uploadedFile" @click="handleUpload" :loading="uploading">
          开始上传
        </el-button>
      </div>
    </el-card>

    <!-- 步骤3: 数据预览 -->
    <el-card v-if="currentStep === 2" class="step-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>数据预览</span>
          <el-tag type="success">共 {{ previewData.length }} 条数据</el-tag>
        </div>
      </template>
      <div class="preview-stats" v-if="previewResult">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-label">数据总数</div>
              <div class="stat-value">{{ previewResult.total }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-label">有效数据</div>
              <div class="stat-value stat-success">{{ previewResult.valid }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-label">错误数据</div>
              <div class="stat-value stat-error">{{ previewResult.invalid }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-label">重复数据</div>
              <div class="stat-value stat-warning">{{ previewResult.duplicate }}</div>
            </div>
          </el-col>
        </el-row>
      </div>
      <el-table :data="previewData" border max-height="400" style="margin-top: 16px">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'valid' ? 'success' : 'danger'" size="small">
              {{ row.status === 'valid' ? '有效' : '错误' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="data" label="数据内容" show-overflow-tooltip />
        <el-table-column prop="error" label="错误信息" show-overflow-tooltip />
      </el-table>
      <div class="step-actions">
        <el-button @click="prevStep">上一步</el-button>
        <el-button type="primary" @click="handleImport" :loading="importing" :disabled="previewResult?.invalid > 0">
          确认导入
        </el-button>
      </div>
    </el-card>

    <!-- 步骤4: 导入完成 -->
    <el-card v-if="currentStep === 3" class="step-card" style="margin-top: 20px">
      <template #header>
        <span>导入完成</span>
      </template>
      <el-result
        :icon="importResult?.success ? 'success' : 'error'"
        :title="importResult?.success ? '导入成功' : '导入失败'"
      >
        <template #sub-title>
          <div v-if="importResult?.success">
            <p>成功导入 {{ importResult.successCount }} 条数据</p>
            <p v-if="importResult.failCount > 0">失败 {{ importResult.failCount }} 条数据</p>
          </div>
          <div v-else>
            <p>{{ importResult?.message }}</p>
          </div>
        </template>
        <template #extra>
          <div class="result-actions">
            <el-button type="primary" @click="goBack">返回</el-button>
            <el-button v-if="importResult?.failCount > 0" @click="handleDownloadError">
              下载错误数据
            </el-button>
            <el-button @click="handleReset">继续导入</el-button>
          </div>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UploadFilled, Download, Document, Van, Box, User } from '@element-plus/icons-vue'

const router = useRouter()

// 当前步骤
const currentStep = ref(0)

// 导入类型
const importType = ref('')

// 上传相关
const uploadRef = ref()
const uploadedFile = ref<File | null>(null)
const uploading = ref(false)
const importing = ref(false)

// 预览数据
const previewData = ref([])
const previewResult = ref<any>(null)

// 导入结果
const importResult = ref<any>(null)

// 导入类型列表
const importTypes = [
  { value: 'order', label: '订单数据', icon: Document, color: '#409eff', desc: '导入历史订单信息' },
  { value: 'shipment', label: '发运数据', icon: Van, color: '#67c23a', desc: '导入发运批次信息' },
  { value: 'receipt', label: '签收数据', icon: Box, color: '#e6a23c', desc: '导入签收记录信息' },
  { value: 'supplier', label: '供应商', icon: User, color: '#f56c6c', desc: '导入供应商基础信息' }
]

// 上传地址
const uploadUrl = computed(() => '/api/data/import')
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

// 返回
const goBack = () => {
  router.back()
}

// 选择导入类型
const handleSelectType = (type: string) => {
  importType.value = type
}

// 下一步
const nextStep = () => {
  currentStep.value++
}

// 上一步
const prevStep = () => {
  currentStep.value--
}

// 下载模板
const handleDownloadTemplate = () => {
  ElMessage.info(`下载${importType.value}导入模板`)
}

// 文件变化
const handleFileChange = (file: any) => {
  uploadedFile.value = file.raw
}

// 上传前校验
const beforeUpload = (file: File) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
                  file.type === 'application/vnd.ms-excel'
  if (!isExcel) {
    ElMessage.error('只能上传 Excel 文件')
    return false
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  return true
}

// 上传文件
const handleUpload = () => {
  if (!uploadRef.value) return
  uploading.value = true
  uploadRef.value.submit()
}

// 上传成功
const handleUploadSuccess = (response: any) => {
  uploading.value = false
  if (response.code === 200) {
    ElMessage.success('上传成功')
    previewData.value = response.data.preview || []
    previewResult.value = response.data.stats
    nextStep()
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传失败
const handleUploadError = () => {
  uploading.value = false
  ElMessage.error('上传失败')
}

// 确认导入
const handleImport = async () => {
  importing.value = true
  try {
    // 模拟导入
    await new Promise(resolve => setTimeout(resolve, 1000))
    importResult.value = {
      success: true,
      successCount: previewResult.value.valid,
      failCount: previewResult.value.invalid
    }
    nextStep()
  } catch (error) {
    importResult.value = {
      success: false,
      message: '导入失败，请重试'
    }
    nextStep()
  } finally {
    importing.value = false
  }
}

// 下载错误数据
const handleDownloadError = () => {
  ElMessage.info('下载错误数据')
}

// 重置
const handleReset = () => {
  currentStep.value = 0
  importType.value = ''
  uploadedFile.value = null
  previewData.value = []
  previewResult.value = null
  importResult.value = null
}
</script>

<style scoped lang="scss">
.data-import {
  padding: 20px;

  .page-title {
    font-size: 16px;
    font-weight: 500;
  }

  .type-item {
    padding: 30px 20px;
    text-align: center;
    border: 2px solid var(--el-border-color);
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: var(--el-color-primary);
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    }

    &.active {
      border-color: var(--el-color-primary);
      background: var(--el-color-primary-light-9);
    }

    .type-name {
      margin-top: 12px;
      font-size: 16px;
      font-weight: 500;
    }

    .type-desc {
      margin-top: 8px;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }

  .step-actions {
    margin-top: 24px;
    text-align: right;
  }

  .upload-area {
    .upload-demo {
      width: 100%;
    }

    .template-download {
      margin-top: 16px;
      text-align: center;
    }
  }

  .preview-stats {
    padding: 16px;
    background: var(--el-fill-color-light);
    border-radius: 4px;

    .stat-item {
      text-align: center;

      .stat-label {
        font-size: 14px;
        color: var(--el-text-color-secondary);
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 24px;
        font-weight: bold;

        &.stat-success {
          color: var(--el-color-success);
        }

        &.stat-error {
          color: var(--el-color-danger);
        }

        &.stat-warning {
          color: var(--el-color-warning);
        }
      }
    }
  }

  .result-actions {
    display: flex;
    justify-content: center;
    gap: 12px;
  }
}
</style>
