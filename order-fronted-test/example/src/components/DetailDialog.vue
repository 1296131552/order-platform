<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :fullscreen="fullscreen"
    :destroy-on-close="destroyOnClose"
    @close="handleClose"
  >
    <div class="detail-dialog">
      <el-descriptions :column="column" :border="border">
        <el-descriptions-item
          v-for="item in items"
          :key="item.prop"
          :label="item.label"
          :span="item.span || 1"
        >
          <!-- 插槽 -->
          <template v-if="item.slot">
            <slot :name="item.slot" :row="data" />
          </template>
          <!-- 状态标签 -->
          <template v-else-if="item.statusMap">
            <el-tag :type="getStatusType(data[item.prop], item.statusMap)">
              {{ getStatusLabel(data[item.prop], item.statusMap) }}
            </el-tag>
          </template>
          <!-- 图片 -->
          <template v-else-if="item.type === 'image'">
            <el-image
              v-if="data[item.prop]"
              :src="data[item.prop]"
              :preview-src-list="[data[item.prop]]"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px"
            />
            <span v-else>-</span>
          </template>
          <!-- 图片列表 -->
          <template v-else-if="item.type === 'images'">
            <div v-if="getImageList(data[item.prop]).length > 0" class="image-list">
              <el-image
                v-for="(img, index) in getImageList(data[item.prop])"
                :key="index"
                :src="img"
                :preview-src-list="getImageList(data[item.prop])"
                fit="cover"
                style="width: 60px; height: 60px; border-radius: 4px; margin-right: 8px"
              />
            </div>
            <span v-else>-</span>
          </template>
          <!-- 日期时间 -->
          <template v-else-if="item.type === 'dateTime'">
            {{ formatDateTime(data[item.prop]) }}
          </template>
          <!-- 金额 -->
          <template v-else-if="item.type === 'amount'">
            {{ formatAmount(data[item.prop]) }}
          </template>
          <!-- 链接 -->
          <template v-else-if="item.type === 'link' && item.onClick">
            <el-link type="primary" @click="item.onClick!(data)">
              {{ data[item.prop] || '-' }}
            </el-link>
          </template>
          <!-- 默认文本 -->
          <template v-else>
            {{ data[item.prop] || '-' }}
          </template>
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <template #footer v-if="showFooter">
      <el-button @click="handleClose">{{ cancelText }}</el-button>
      <el-button v-if="showConfirm" type="primary" @click="handleConfirm">
        {{ confirmText }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { formatDateTime, formatAmount } from '@/utils/format'

/** 详情项配置 */
export interface DetailItem {
  prop: string
  label: string
  type?: 'text' | 'image' | 'images' | 'dateTime' | 'amount' | 'link'
  span?: number
  slot?: string
  statusMap?: Record<string, { label: string; type: string }>
  onClick?: (data: any) => void
}

interface Props {
  modelValue: boolean
  title?: string
  data: Record<string, any>
  items: DetailItem[]
  width?: string | number
  column?: number
  border?: boolean
  fullscreen?: boolean
  destroyOnClose?: boolean
  showFooter?: boolean
  showConfirm?: boolean
  confirmText?: string
  cancelText?: string
}

const props = withDefaults(defineProps<Props>(), {
  title: '详情',
  width: '800px',
  column: 2,
  border: true,
  fullscreen: false,
  destroyOnClose: true,
  showFooter: true,
  showConfirm: false,
  confirmText: '确定',
  cancelText: '关闭'
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: [data: any]
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 获取状态类型
const getStatusType = (value: any, statusMap: Record<string, { label: string; type: string }>) => {
  return statusMap[value]?.type || 'info'
}

// 获取状态标签
const getStatusLabel = (value: any, statusMap: Record<string, { label: string; type: string }>) => {
  return statusMap[value]?.label || value
}

// 获取图片列表
const getImageList = (value: any) => {
  if (!value) return []
  if (Array.isArray(value)) return value
  if (typeof value === 'string') {
    try {
      return JSON.parse(value)
    } catch {
      return value.split(',').filter(Boolean)
    }
  }
  return []
}

// 关闭
const handleClose = () => {
  visible.value = false
}

// 确认
const handleConfirm = () => {
  emit('confirm', props.data)
}
</script>

<style scoped lang="scss">
.detail-dialog {
  :deep(.el-descriptions) {
    .el-descriptions__label {
      font-weight: 500;
    }
  }

  .image-list {
    display: flex;
    flex-wrap: wrap;
  }
}
</style>
