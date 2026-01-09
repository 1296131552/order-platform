/**
 * useTable - 表格数据管理Hook
 *
 * 功能：
 * 1. 自动管理加载状态
 * 2. 自动处理分页
 * 3. 统一的搜索和重置
 * 4. 支持刷新策略
 *
 * 使用示例：
 * const { loading, tableData, total, searchForm, handleSearch, handleReset } = useTable(apiFunction)
 */

import { ref, reactive } from 'vue'

interface Pagination {
  current: number
  size: number
  total: number
}

interface UseTableOptions {
  immediate?: boolean // 是否立即加载数据
}

export function useTable<T>(
  apiFn: (params: any) => Promise<{ data: { records: T[]; total: number } }>,
  options: UseTableOptions = {}
) {
  const { immediate = true } = options

  // 加载状态
  const loading = ref(false)

  // 表格数据
  const tableData = ref<T[]>([])

  // 分页信息
  const pagination = reactive<Pagination>({
    current: 1,
    size: 10,
    total: 0
  })

  // 搜索表单
  const searchForm = ref<Record<string, any>>({})

  /**
   * 获取数据
   */
  const fetchData = async () => {
    loading.value = true
    try {
      const params = {
        ...searchForm.value,
        current: pagination.current,
        size: pagination.size
      }

      const res = await apiFn(params)

      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    } catch (error) {
      console.error('获取数据失败：', error)
      tableData.value = []
      pagination.total = 0
    } finally {
      loading.value = false
    }
  }

  /**
   * 搜索（重置到第一页）
   */
  const handleSearch = () => {
    pagination.current = 1
    fetchData()
  }

  /**
   * 重置搜索表单
   */
  const handleReset = () => {
    // 清空搜索表单
    Object.keys(searchForm.value).forEach(key => {
      searchForm.value[key] = undefined
    })

    // 重置分页
    pagination.current = 1

    // 重新获取数据
    fetchData()
  }

  /**
   * 分页大小变化
   */
  const handleSizeChange = (size: number) => {
    pagination.size = size
    pagination.current = 1
    fetchData()
  }

  /**
   * 当前页变化
   */
  const handleCurrentChange = (current: number) => {
    pagination.current = current
    fetchData()
  }

  /**
   * 新增后刷新（回到第一页）
   */
  const refreshCreate = () => {
    pagination.current = 1
    fetchData()
  }

  /**
   * 更新后刷新（保持当前页）
   */
  const refreshUpdate = () => {
    fetchData()
  }

  /**
   * 删除后刷新（智能处理页码）
   */
  const refreshRemove = () => {
    // 如果当前页只有一条数据且不是第一页，回到上一页
    if (tableData.value.length === 1 && pagination.current > 1) {
      pagination.current--
    }
    fetchData()
  }

  /**
   * 手动刷新（清空搜索）
   */
  const refreshData = () => {
    fetchData()
  }

  /**
   * 清空数据
   */
  const clearData = () => {
    tableData.value = []
    pagination.total = 0
  }

  // 立即加载数据
  if (immediate) {
    fetchData()
  }

  return {
    // 数据
    loading,
    tableData,
    pagination,
    searchForm,

    // 方法
    fetchData,
    handleSearch,
    handleReset,
    handleSizeChange,
    handleCurrentChange,
    refreshCreate,
    refreshUpdate,
    refreshRemove,
    refreshData,
    clearData
  }
}
