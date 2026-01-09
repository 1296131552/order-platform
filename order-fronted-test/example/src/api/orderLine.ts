/**
 * 订单行相关 API
 * @description 订单行模块的所有API接口
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9
 */

import request from '@/utils/request'

// ==================== 类型定义 ====================

/** 订单行状态 */
export type OrderLineStatus = 'PENDING' | 'SHIPPED' | 'RECEIVED' | 'CANCELLED'

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

/** 添加订单行参数 */
export interface AddOrderLineParams {
  orderId: number
  supplierId: number
  productCode: string
  productName: string
  quantity: number
  unitPrice: number
  remark?: string
}

/** 批量添加订单行参数 */
export interface BatchAddOrderLineParams {
  orderId: number
  lines: Omit<AddOrderLineParams, 'orderId'>[]
}

/** 更新订单行参数 */
export interface UpdateOrderLineParams {
  id: number
  supplierId?: number
  productCode?: string
  productName?: string
  quantity?: number
  unitPrice?: number
  remark?: string
}

// ==================== API 函数 ====================

/**
 * 查询订单的所有订单行
 * @description GET /api/order-line/list/{orderId}
 * Requirements: 3.1
 */
export function getOrderLineList(orderId: number) {
  return request.get<OrderLine[]>(`/order-line/list/${orderId}`)
}

/**
 * 获取订单行详情
 * @description GET /api/order-line/{id}
 * Requirements: 3.2
 */
export function getOrderLineDetail(id: number) {
  return request.get<OrderLine>(`/order-line/${id}`)
}

/**
 * 添加订单行
 * @description POST /api/order-line/add
 * Requirements: 3.3
 */
export function addOrderLine(data: AddOrderLineParams) {
  return request.post<number>('/order-line/add', data)
}

/**
 * 批量添加订单行
 * @description POST /api/order-line/batch-add
 * Requirements: 3.4
 */
export function batchAddOrderLines(data: BatchAddOrderLineParams) {
  return request.post<number[]>('/order-line/batch-add', data)
}

/**
 * 更新订单行
 * @description PUT /api/order-line/update
 * Requirements: 3.5
 */
export function updateOrderLine(data: UpdateOrderLineParams) {
  return request.put('/order-line/update', data)
}

/**
 * 删除订单行
 * @description DELETE /api/order-line/{id}
 * Requirements: 3.6
 */
export function deleteOrderLine(id: number) {
  return request.delete(`/order-line/${id}`)
}

/**
 * 更新订单行状态
 * @description PUT /api/order-line/{id}/status
 * Requirements: 3.7
 */
export function updateOrderLineStatus(id: number, status: OrderLineStatus) {
  return request.put(`/order-line/${id}/status`, { status })
}

/**
 * 计算订单总金额
 * @description GET /api/order-line/total-amount/{orderId}
 * Requirements: 3.8
 */
export function getOrderTotalAmount(orderId: number) {
  return request.get<number>(`/order-line/total-amount/${orderId}`)
}

/**
 * 获取下一个行号
 * @description GET /api/order-line/next-line-no/{orderId}
 * Requirements: 3.9
 */
export function getNextLineNo(orderId: number) {
  return request.get<number>(`/order-line/next-line-no/${orderId}`)
}
