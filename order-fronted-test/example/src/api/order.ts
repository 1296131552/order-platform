/**
 * 订单相关 API
 */

import request from '@/utils/request'

/** 订单查询参数 */
export interface OrderQueryParams {
  page?: number
  pageSize?: number
  orderNo?: string
  customerId?: number
  status?: string
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
  status: string
  totalAmount: number
  deliveryDate?: string
  deliveryAddress: string
  contactPerson: string
  contactPhone: string
  remark?: string
  createTime: string
  updateTime: string
}

/** 创建订单参数 */
export interface CreateOrderParams {
  customerId: number
  deliveryDate?: string
  deliveryAddress: string
  contactPerson: string
  contactPhone: string
  remark?: string
  items: {
    productId: number
    quantity: number
    price: number
    remark?: string
  }[]
}

/** 更新订单参数 */
export interface UpdateOrderParams extends Partial<CreateOrderParams> {
  id: number
}

/**
 * 分页查询订单列表
 */
export function getOrderList(params: OrderQueryParams) {
  return request.get<{ list: Order[]; total: number }>('/order/list', { params })
}

/**
 * 获取订单详情
 */
export function getOrderDetail(id: number) {
  return request.get<Order>(`/order/${id}`)
}

/**
 * 创建订单
 */
export function createOrder(data: CreateOrderParams) {
  return request.post<number>('/order/create', data)
}

/**
 * 更新订单
 */
export function updateOrder(data: UpdateOrderParams) {
  return request.put(`/order/${data.id}`, data)
}

/**
 * 删除订单
 */
export function deleteOrder(id: number) {
  return request.delete(`/order/${id}`)
}

/**
 * 取消订单
 */
export function cancelOrder(id: number, reason?: string) {
  return request.post(`/order/${id}/cancel`, { reason })
}

/**
 * 订单状态流转
 */
export function updateOrderStatus(id: number, status: string) {
  return request.put(`/order/${id}/status`, { status })
}

/**
 * 导出订单
 */
export function exportOrders(params: OrderQueryParams) {
  return request.get('/order/export', { params, responseType: 'blob' })
}

/**
 * 获取订单统计数据
 */
export function getOrderStatistics() {
  return request.get<{
    totalCount: number
    draftCount: number
    executingCount: number
    completedCount: number
    totalAmount: number
  }>('/order/statistics')
}
