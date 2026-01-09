/**
 * useDialog - 对话框管理 Hook
 *
 * 功能：
 * 1. 管理对话框显示/隐藏
 * 2. 管理对话框数据
 * 3. 统一的打开/关闭方法
 *
 * 使用示例：
 * const { visible, dialogData, openDialog, closeDialog } = useDialog()
 */

import { ref } from 'vue'

interface UseDialogOptions {
  onClose?: () => void
  onOpen?: (data?: any) => void
}

export function useDialog(options: UseDialogOptions = {}) {
  const { onClose, onOpen } = options

  // 对话框显示状态
  const visible = ref(false)

  // 对话框数据
  const dialogData = ref<any>(null)

  /**
   * 打开对话框
   */
  const openDialog = (data?: any) => {
    dialogData.value = data || null
    visible.value = true
    onOpen?.(data)
  }

  /**
   * 关闭对话框
   */
  const closeDialog = () => {
    visible.value = false
    dialogData.value = null
    onClose?.()
  }

  /**
   * 切换对话框状态
   */
  const toggleDialog = () => {
    if (visible.value) {
      closeDialog()
    } else {
      openDialog()
    }
  }

  return {
    visible,
    dialogData,
    openDialog,
    closeDialog,
    toggleDialog
  }
}

/**
 * useConfirmDialog - 确认对话框 Hook
 *
 * 使用示例：
 * const { confirmDelete, confirm } = useConfirmDialog()
 * confirmDelete(() => deleteItem(id))
 */
export function useConfirmDialog() {
  const visible = ref(false)
  const title = ref('')
  const message = ref('')
  const confirmCallback = ref<(() => void) | null>(null)

  /**
   * 显示确认对话框
   */
  const confirm = (msg: string, callback: () => void, titleText = '确认操作') => {
    title.value = titleText
    message.value = msg
    confirmCallback.value = callback
    visible.value = true
  }

  /**
   * 确认删除
   */
  const confirmDelete = (callback: () => void, itemName = '该数据') => {
    confirm(`确定要删除${itemName}吗？删除后将无法恢复。`, callback, '删除确认')
  }

  /**
   * 确认取消
   */
  const confirmCancel = (callback: () => void, itemName = '该操作') => {
    confirm(`确定要取消${itemName}吗？`, callback, '取消确认')
  }

  /**
   * 处理确认
   */
  const handleConfirm = () => {
    confirmCallback.value?.()
    visible.value = false
  }

  /**
   * 处理取消
   */
  const handleCancel = () => {
    visible.value = false
    confirmCallback.value = null
  }

  return {
    visible,
    title,
    message,
    confirm,
    confirmDelete,
    confirmCancel,
    handleConfirm,
    handleCancel
  }
}
