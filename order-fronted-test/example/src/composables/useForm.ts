/**
 * useForm - 表单管理 Hook
 *
 * 功能：
 * 1. 管理表单数据
 * 2. 表单验证
 * 3. 表单重置
 *
 * 使用示例：
 * const { formData, formRef, validate, resetForm, setFormData } = useForm(defaultData)
 */

import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

interface UseFormOptions<T> {
  defaultData?: T
  rules?: FormRules
  onSuccess?: (data: T) => void
  onError?: (error: any) => void
}

export function useForm<T extends Record<string, any> = any>(
  options: UseFormOptions<T> = {}
) {
  const { defaultData, rules, onSuccess, onError } = options

  // 表单引用
  const formRef = ref<FormInstance>()

  // 表单数据
  const formData = reactive<T>({ ...(defaultData || {}) } as T)

  // 表单验证规则
  const formRules = ref<FormRules>(rules || {})

  /**
   * 验证表单
   */
  const validate = async (): Promise<boolean> => {
    if (!formRef.value) return false

    try {
      await formRef.value.validate()
      return true
    } catch {
      return false
    }
  }

  /**
   * 验证并提交
   */
  const validateAndSubmit = async (submitFn: (data: T) => Promise<void> | void) => {
    const valid = await validate()
    if (valid) {
      try {
        await submitFn({ ...formData })
        onSuccess?.(formData as T)
      } catch (error) {
        onError?.(error)
        throw error
      }
    }
  }

  /**
   * 重置表单
   */
  const resetForm = () => {
    formRef.value?.resetFields()
    if (defaultData) {
      Object.assign(formData, defaultData)
    }
  }

  /**
   * 清空表单验证
   */
  const clearValidate = () => {
    formRef.value?.clearValidate()
  }

  /**
   * 设置表单数据
   */
  const setFormData = (data: Partial<T>) => {
    Object.assign(formData, data)
  }

  /**
   * 获取表单数据
   */
  const getFormData = (): T => {
    return { ...formData }
  }

  return {
    formRef,
    formData,
    formRules,
    validate,
    validateAndSubmit,
    resetForm,
    clearValidate,
    setFormData,
    getFormData
  }
}

/**
 * useSearchForm - 搜索表单 Hook
 *
 * 使用示例：
 * const { searchForm, handleSearch, handleReset } = useSearchForm(onSearch)
 */
export function useSearchForm<T extends Record<string, any> = any>(
  onSearch: (params: T) => void,
  defaultData?: T
) {
  const searchForm = reactive<T>({ ...(defaultData || {}) } as T)

  /**
   * 执行搜索
   */
  const handleSearch = () => {
    onSearch({ ...searchForm })
  }

  /**
   * 重置搜索
   */
  const handleReset = () => {
    Object.keys(searchForm).forEach(key => {
      searchForm[key as keyof T] = (defaultData?.[key] ?? '') as any
    })
    onSearch({ ...searchForm })
  }

  return {
    searchForm,
    handleSearch,
    handleReset,
    setSearchForm: (data: Partial<T>) => Object.assign(searchForm, data)
  }
}
