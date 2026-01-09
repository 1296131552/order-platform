<template>
  <div class="search-bar">
    <el-form :model="formData" :inline="true" class="search-form">
      <el-form-item
        v-for="item in items"
        :key="item.prop"
        :label="item.label"
      >
        <!-- 输入框 -->
        <el-input
          v-if="item.type === 'input' || !item.type"
          v-model="formData[item.prop]"
          :placeholder="`请输入${item.label}`"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <!-- 选择框 -->
        <el-select
          v-else-if="item.type === 'select'"
          v-model="formData[item.prop]"
          :placeholder="`请选择${item.label}`"
          clearable
          @change="handleSearch"
        >
          <el-option
            v-for="opt in item.options"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
        <!-- 日期范围 -->
        <el-date-picker
          v-else-if="item.type === 'dateRange'"
          v-model="formData[item.prop]"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="handleSearch"
        />
        <!-- 日期选择 -->
        <el-date-picker
          v-else-if="item.type === 'date'"
          v-model="formData[item.prop]"
          type="date"
          :placeholder="`请选择${item.label}`"
          value-format="YYYY-MM-DD"
          @change="handleSearch"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

/** 搜索项配置 */
export interface SearchItem {
  prop: string
  label: string
  type?: 'input' | 'select' | 'date' | 'dateRange'
  options?: Array<{ label: string; value: any }>
  defaultValue?: any
}

interface Props {
  items: SearchItem[]
}

const props = defineProps<Props>()

const emit = defineEmits<{
  search: [params: Record<string, any>]
  reset: []
}>()

// 表单数据
const formData = ref<Record<string, any>>({})

// 初始化表单数据
props.items.forEach(item => {
  if (item.defaultValue !== undefined) {
    formData.value[item.prop] = item.defaultValue
  }
})

// 监听表单数据变化
watch(formData, (newVal) => {
  // 处理日期范围
  const params = { ...newVal }
  props.items.forEach(item => {
    if (item.type === 'dateRange' && Array.isArray(params[item.prop])) {
      params[`${item.prop}Start`] = params[item.prop][0]
      params[`${item.prop}End`] = params[item.prop][1]
      delete params[item.prop]
    }
  })
}, { deep: true })

// 查询
const handleSearch = () => {
  const params = { ...formData.value }
  // 处理日期范围
  props.items.forEach(item => {
    if (item.type === 'dateRange' && Array.isArray(params[item.prop])) {
      params[`${item.prop}Start`] = params[item.prop][0]
      params[`${item.prop}End`] = params[item.prop][1]
      delete params[item.prop]
    }
  })
  emit('search', params)
}

// 重置
const handleReset = () => {
  props.items.forEach(item => {
    formData.value[item.prop] = item.defaultValue !== undefined ? item.defaultValue : ''
  })
  emit('reset')
}
</script>

<style scoped lang="scss">
.search-bar {
  padding: 16px;
  background: var(--el-bg-color);
  border-radius: 4px;
  margin-bottom: 16px;

  .search-form {
    :deep(.el-form-item) {
      margin-bottom: 0;
      margin-right: 16px;
    }
  }
}
</style>
