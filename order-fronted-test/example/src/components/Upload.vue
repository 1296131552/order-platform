<template>
  <div class="upload-container">
    <el-upload
      ref="uploadRef"
      v-model:file-list="fileList"
      :action="uploadUrl"
      :headers="uploadHeaders"
      :on-preview="handlePreview"
      :on-remove="handleRemove"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-progress="handleProgress"
      :before-upload="beforeUpload"
      :on-exceed="handleExceed"
      :limit="limit"
      :accept="accept"
      :multiple="multiple"
      :drag="drag"
      :disabled="disabled"
      list-type="picture-card"
    >
      <el-icon v-if="!disabled"><Plus /></el-icon>

      <template #tip>
        <div class="el-upload__tip">
          {{ tip }}
        </div>
      </template>
    </el-upload>

    <!-- 图片预览 -->
    <el-dialog v-model="previewVisible" title="预览" width="800px">
      <img :src="previewUrl" style="width: 100%" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { UploadInstance, UploadUserFile, UploadProps, UploadRawFile } from 'element-plus'

interface Props {
  modelValue?: string | string[]
  accept?: string
  limit?: number
  maxSize?: number // MB
  multiple?: boolean
  drag?: boolean
  disabled?: boolean
  tip?: string
}

const props = withDefaults(defineProps<Props>(), {
  accept: 'image/*',
  limit: 9,
  maxSize: 10,
  multiple: false,
  drag: false,
  disabled: false,
  tip: '支持 JPG、PNG 格式，文件大小不超过 10MB'
})

const emit = defineEmits<{
  'update:modelValue': [value: string | string[]]
  success: [response: any, file: UploadUserFile]
  remove: [file: UploadUserFile]
}>()

const uploadRef = ref<UploadInstance>()
const fileList = ref<UploadUserFile[]>([])
const previewVisible = ref(false)
const previewUrl = ref('')

// 上传地址
const uploadUrl = computed(() => '/api/attachment/upload')

// 上传头
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

// 初始化文件列表
if (props.modelValue) {
  const urls = Array.isArray(props.modelValue) ? props.modelValue : [props.modelValue]
  fileList.value = urls.map((url, index) => ({
    name: `file-${index}`,
    url,
    status: 'success'
  }))
}

// 预览
const handlePreview: UploadProps['onPreview'] = (uploadFile) => {
  previewUrl.value = uploadFile.url || ''
  previewVisible.value = true
}

// 移除
const handleRemove: UploadProps['onRemove'] = (uploadFile) => {
  emit('remove', uploadFile)
  updateModelValue()
}

// 上传成功
const handleSuccess: UploadProps['onSuccess'] = (response, uploadFile) => {
  if (response.code === 200) {
    ElMessage.success('上传成功')
    emit('success', response.data, uploadFile)
    updateModelValue()
  } else {
    ElMessage.error(response.message || '上传失败')
    // 移除失败的文件
    const index = fileList.value.indexOf(uploadFile)
    if (index > -1) {
      fileList.value.splice(index, 1)
    }
  }
}

// 上传失败
const handleError: UploadProps['onError'] = () => {
  ElMessage.error('上传失败')
}

// 上传进度
const handleProgress: UploadProps['onProgress'] = () => {
  // 可以在这里处理进度
}

// 上传前校验
const beforeUpload: UploadProps['beforeUpload'] = (rawFile) => {
  // 检查文件大小
  if (rawFile.size > props.maxSize * 1024 * 1024) {
    ElMessage.error(`文件大小不能超过 ${props.maxSize}MB`)
    return false
  }

  // 检查文件类型
  if (props.accept && props.accept !== '*') {
    const acceptTypes = props.accept.split(',').map(t => t.trim())
    const fileType = rawFile.type
    const fileName = rawFile.name
    const isValid = acceptTypes.some(type => {
      if (type.startsWith('.')) {
        return fileName.endsWith(type)
      }
      return fileType.includes(type.replace('*', ''))
    })

    if (!isValid) {
      ElMessage.error(`文件类型不支持，仅支持 ${props.accept}`)
      return false
    }
  }

  return true
}

// 超出限制
const handleExceed: UploadProps['onExceed'] = () => {
  ElMessage.warning(`最多只能上传 ${props.limit} 个文件`)
}

// 更新 v-model 值
const updateModelValue = () => {
  const urls = fileList.value
    .filter(file => file.status === 'success' && file.url)
    .map(file => file.url!)

  emit('update:modelValue', props.multiple ? urls : (urls[0] || ''))
}

// 手动上传
const submit = () => {
  uploadRef.value?.submit()
}

// 清空文件列表
const clearFiles = () => {
  uploadRef.value?.clearFiles()
  fileList.value = []
  emit('update:modelValue', props.multiple ? [] : '')
}

defineExpose({
  submit,
  clearFiles
})
</script>

<style scoped lang="scss">
.upload-container {
  :deep(.el-upload) {
    .el-upload__tip {
      margin-top: 8px;
      color: var(--el-text-color-secondary);
      font-size: 12px;
    }
  }
}
</style>
