<!--
  StatusTag - 状态标签组件
  功能：统一管理各种业务状态的显示样式
  支持：订单、发运、签收、异常等类型
-->
<template>
  <el-tag :type="tagType">{{ label }}</el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  status: string
  type: 'order' | 'shipment' | 'receipt' | 'exception'
}

const props = defineProps<Props>()

// 订单状态映射
const orderStatusMap: Record<string, { label: string; type: any }> = {
  DRAFT: { label: '草稿', type: 'info' },
  CONFIRMED: { label: '已确认', type: '' },
  EXECUTING: { label: '执行中', type: 'warning' },
  PARTIAL_RECEIVED: { label: '部分到货', type: 'warning' },
  COMPLETED: { label: '已完成', type: 'success' },
  CANCELLED: { label: '已取消', type: 'danger' }
}

// 发运计划状态映射
const shipmentStatusMap: Record<string, { label: string; type: any }> = {
  DRAFT: { label: '草稿', type: 'info' },
  APPROVED: { label: '已审核', type: 'success' },
  EXECUTING: { label: '执行中', type: 'warning' },
  COMPLETED: { label: '已完成', type: '' }
}

// 快递单状态映射
const shipmentLineStatusMap: Record<string, { label: string; type: any }> = {
  CREATED: { label: '已创建', type: 'info' },
  PICKED_UP: { label: '已取件', type: '' },
  IN_TRANSIT: { label: '运输中', type: 'warning' },
  DELIVERED: { label: '已送达', type: 'success' },
  RECEIVED: { label: '已签收', type: '' }
}

// 签收状态映射
const receiptStatusMap: Record<string, { label: string; type: any }> = {
  PENDING: { label: '待签收', type: 'warning' },
  RECEIVED: { label: '已签收', type: 'success' },
  DIFFERENCE: { label: '有差异', type: 'danger' }
}

// 异常状态映射
const exceptionStatusMap: Record<string, { label: string; type: any }> = {
  PENDING: { label: '待处理', type: 'danger' },
  PROCESSING: { label: '处理中', type: 'warning' },
  RESOLVED: { label: '已解决', type: 'success' },
  CLOSED: { label: '已关闭', type: 'info' }
}

// 状态映射表
const statusMaps = {
  order: orderStatusMap,
  shipment: shipmentStatusMap,
  receipt: receiptStatusMap,
  exception: exceptionStatusMap,
  // 快递单状态也用shipment的映射（如果需要可以单独区分）
  shipmentLine: shipmentLineStatusMap
}

// 获取状态信息
const tagInfo = computed(() => {
  // 优先使用shipmentLine类型（兼容快递单）
  const statusMap = statusMaps[props.type] || statusMaps.order
  return statusMap[props.status] || { label: props.status, type: 'info' }
})

const label = computed(() => tagInfo.value.label)
const tagType = computed(() => tagInfo.value.type)
</script>
