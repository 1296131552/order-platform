/**
 * 常量定义
 */

/**
 * 订单状态枚举
 */
export const ORDER_STATUS = {
  DRAFT: 'draft',
  EXECUTING: 'executing',
  PARTIALLY_DELIVERED: 'partially_delivered',
  COMPLETED: 'completed',
  CANCELLED: 'cancelled'
} as const

/**
 * 订单状态映射
 */
export const ORDER_STATUS_MAP = {
  draft: { label: '草稿', type: 'info' },
  executing: { label: '执行中', type: 'warning' },
  partially_delivered: { label: '部分到货', type: 'primary' },
  completed: { label: '已完成', type: 'success' },
  cancelled: { label: '已取消', type: 'danger' }
} as const

/**
 * 发运状态枚举
 */
export const SHIPMENT_STATUS = {
  PENDING: 'pending',
  IN_TRANSIT: 'in_transit',
  DELIVERED: 'delivered'
} as const

/**
 * 发运状态映射
 */
export const SHIPMENT_STATUS_MAP = {
  pending: { label: '待提货', type: 'info' },
  in_transit: { label: '在途', type: 'warning' },
  delivered: { label: '已到货', type: 'success' }
} as const

/**
 * 签收状态枚举
 */
export const RECEIPT_STATUS = {
  PENDING: 'pending',
  CONFIRMED: 'confirmed',
  DIFFERENCE: 'difference',
  PROCESSED: 'processed'
} as const

/**
 * 签收状态映射
 */
export const RECEIPT_STATUS_MAP = {
  pending: { label: '待签收', type: 'info' },
  confirmed: { label: '已签收', type: 'success' },
  difference: { label: '有差异', type: 'warning' },
  processed: { label: '已处理', type: 'success' }
} as const
