/**
 * 订单相关 API
 * @description 订单模块的所有API接口
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 2.10
 */

import request from '@/utils/request'

// ==================== 类型定义 ====================

/** 订单状态 */
export type OrderStatus = 'DRAFT' | 'EXECUTING' | 'PARTIALLY_RECEIVED' | 'COMPLETED' | 'CANCELLED'

/** 分页响应数据 */
export interface PageResult<T> {
  /** 数据列表 */
  records: T[]
  /** 总记录数 */
  total: number
  /** 每页条数 */
  size: number
  /** 当前页码 */
  current: number
  /** 总页数 */
  pages: number
}

/** 订单查询参数 */
export interface OrderQueryParams {
  page?: number
  pageSize?: number
  orderNo?: string
  customerId?: number
  status?: OrderStatus
  startDate?: string
  endDate?: string
  keyword?: string
}

/** 订单信息 */
export interface Order {
  id: number
  orderNo: string
  customerId: number
  customerName: string
  status: OrderStatus
  totalAmount: number
  lineCount: number
  createdBy: string
  createdByName: string
  createdAt: string
  updatedAt: string
  deliveryDate?: string
  deliveryAddress?: string
  contactPerson?: string
  contactPhone?: string
  remark?: string
}

/** 订单行信息 */
export interface OrderLine {
  id: number
  orderId: number
  lineNo: number
  supplierId: number
  supplierName: string
  productCode: string
  productName: string
  quantity: number
  unitPrice: number
  totalAmount: number
  statusCode: string
  remark?: string
  createdAt: string
  updatedAt: string
}

/** 订单详情（含订单行） */
export interface OrderWithLines extends Order {
  lines: OrderLine[]
}

/** 创建订单参数 */
export interface CreateOrderParams {
  customerId: number
  deliveryDate?: string
  deliveryAddress?: string
  contactPerson?: string
  contactPhone?: string
  remark?: string
}

/** 更新订单参数 */
export interface UpdateOrderParams {
  customerId?: number
  deliveryDate?: string
  deliveryAddress?: string
  contactPerson?: string
  contactPhone?: string
  remark?: string
}

/** 订单统计数据 */
export interface OrderStatistics {
  totalCount: number
  draftCount: number
  executingCount: number
  partiallyReceivedCount?: number
  completedCount: number
  cancelledCount?: number
  totalAmount?: number
}

// ==================== API 函数 ====================

/**
 * 分页查询订单列表
 * @description GET /api/order/list
 * Requirements: 2.1
 */
export function getOrderList(params: OrderQueryParams) {
  return request.get<PageResult<Order>>('/order/list', { params })
}

/**
 * 获取订单详情
 * @description GET /api/order/{id}
 * Requirements: 2.2
 */
export function getOrderDetail(id: number) {
  return request.get<Order>(`/order/${id}`)
}

/**
 * 获取订单详情（含订单行）
 * @description GET /api/order/{id}/with-lines
 * Requirements: 2.3
 */
export function getOrderWithLines(id: number) {
  return request.get<OrderWithLines>(`/order/${id}/with-lines`)
}

/**
 * 创建订单
 * @description POST /api/order/create
 * Requirements: 2.4
 */
export function createOrder(data: CreateOrderParams) {
  return request.post<number>('/order/create', data)
}

/**
 * 更新订单
 * @description PUT /api/order/{id}
 * Requirements: 2.5
 */
export function updateOrder(id: number, data: UpdateOrderParams) {
  return request.put(`/order/${id}`, data)
}

/**
 * 删除订单
 * @description DELETE /api/order/{id}
 * Requirements: 2.6
 */
export function deleteOrder(id: number) {
  return request.delete(`/order/${id}`)
}

/**
 * 更新订单状态
 * @description PUT /api/order/{id}/status
 * Requirements: 2.7
 */
export function updateOrderStatus(id: number, status: OrderStatus) {
  return request.put(`/order/${id}/status`, { status })
}

/**
 * 取消订单
 * @description POST /api/order/{id}/cancel
 * Requirements: 2.8
 */
export function cancelOrder(id: number, reason?: string) {
  return request.post(`/order/${id}/cancel`, { reason })
}

/**
 * 完成订单
 * @description POST /api/order/{id}/complete
 * Requirements: 2.9
 */
export function completeOrder(id: number) {
  return request.post(`/order/${id}/complete`)
}

/**
 * 获取订单统计数据
 * @description GET /api/order/statistics
 * Requirements: 2.10
 */
export function getOrderStatistics() {
  return request.get<OrderStatistics>('/order/statistics')
}

/**
 * 导出订单
 * @description GET /api/order/export
 */
export function exportOrders(params: OrderQueryParams) {
  return request.get('/order/export', { params, responseType: 'blob' })
}
