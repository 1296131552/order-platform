/**
 * API 统一响应格式
 * 与后端 Result<T> 保持一致
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

/**
 * 分页请求参数
 */
export interface PageParam {
  pageNum: number
  pageSize: number
}

/**
 * 分页响应数据
 */
export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

// ==================== 状态常量对象（用于值访问） ====================
/**
 * 订单状态常量对象
 * 使用: OrderStatusValues.DRAFT
 */
export const OrderStatusValues = {
  DRAFT: 'draft',
  EXECUTING: 'executing',
  PARTIALLY_RECEIVED: 'partially_received',
  COMPLETED: 'completed',
  ARCHIVED: 'archived'
} as const

/**
 * 合作方类型常量对象
 */
export const PartnerTypeValues = {
  SUPPLIER: 'supplier',
  CARRIER: 'carrier',
  CUSTOMER: 'customer'
} as const

/**
 * 发运状态常量对象
 */
export const ShipmentStatusValues = {
  PENDING: 'pending',
  IN_TRANSIT: 'in_transit',
  DELIVERED: 'delivered',
  RECEIVED: 'received'
} as const

/**
 * 快递单状态常量对象
 */
export const ShipmentLineStatusValues = {
  CREATED: 'created',
  PICKED_UP: 'picked_up',
  IN_TRANSIT: 'in_transit',
  DELIVERED: 'delivered',
  RECEIVED: 'received',
  EXCEPTION: 'exception'
} as const

// ==================== 类型定义（用于类型注解） ====================
/**
 * 订单状态类型
 */
export type OrderStatus = (typeof OrderStatusValues)[keyof typeof OrderStatusValues]

/**
 * 合作方类型
 */
export type PartnerType = (typeof PartnerTypeValues)[keyof typeof PartnerTypeValues]

/**
 * 发运状态类型
 */
export type ShipmentStatus = (typeof ShipmentStatusValues)[keyof typeof ShipmentStatusValues]

/**
 * 快递单状态类型
 */
export type ShipmentLineStatus = (typeof ShipmentLineStatusValues)[keyof typeof ShipmentLineStatusValues]
