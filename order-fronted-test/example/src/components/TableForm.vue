<template>
  <div class="table-form">
    <el-table
      v-loading="loading"
      :data="data"
      :border="border"
      :stripe="stripe"
      :height="height"
      @selection-change="handleSelectionChange"
    >
      <el-table-column
        v-if="showSelection"
        type="selection"
        width="55"
      />
      <el-table-column
        v-if="showIndex"
        type="index"
        label="序号"
        width="60"
      />
      <slot />
    </el-table>

    <div v-if="showPagination" class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

interface Props {
  data: any[]
  total?: number
  loading?: boolean
  border?: boolean
  stripe?: boolean
  height?: string | number
  showSelection?: boolean
  showIndex?: boolean
  showPagination?: boolean
  page?: number
  pageSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  total: 0,
  loading: false,
  border: true,
  stripe: true,
  showSelection: false,
  showIndex: true,
  showPagination: true,
  page: 1,
  pageSize: 10
})

const emit = defineEmits<{
  'update:page': [page: number]
  'update:pageSize': [size: number]
  'selection-change': [selection: any[]]
}>()

const currentPage = ref(props.page)
const pageSize = ref(props.pageSize)

// 监听外部 page 变化
watch(() => props.page, (newVal) => {
  currentPage.value = newVal
})

watch(() => props.pageSize, (newVal) => {
  pageSize.value = newVal
})

// 页码变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  emit('update:page', page)
}

// 每页条数变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  emit('update:pageSize', size)
  emit('update:page', 1)
}

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  emit('selection-change', selection)
}
</script>

<style scoped lang="scss">
.table-form {
  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
