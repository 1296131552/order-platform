/**
 * 对账相关 API
 */

import request from '@/utils/request'

/** 对账单查询参数 */
export interface BillingQueryParams {
  page?: number
  pageSize?: number
  billingNo?: string
  customerId?: number
  status?: string
  startDate?: string
  endDate?: string
  keyword?: string
}

/** 对账单信息 */
export interface Billing {
  id: number
  billingNo: string
  customerId: number
  customerName: string
  status: string
  totalAmount: number
  orderCount: number
  shipmentCount: number
  billingPeriod: string
  invoiceNo?: string
  invoiceUrl?: string
  remark?: string
  createTime: string
  updateTime: string
}

/** 对账单明细 */
export interface BillingItem {
  id: number
  billingId: number
  orderId: number
  orderNo: string
  shipmentId: number
  shipmentNo: string
  amount: number
  receiptStatus: string
}

/** 创建对账单参数 */
export interface CreateBillingParams {
  customerId: number
  billingPeriod: string
  orderIds: number[]
  remark?: string
}

/**
 * 分页查询对账单列表
 */
export function getBillingList(params: BillingQueryParams) {
  return request.get<{ list: Billing[]; total: number }>('/billing/list', { params })
}

/**
 * 获取对账单详情
 */
export function getBillingDetail(id: number) {
  return request.get<{
    billing: Billing
    items: BillingItem[]
  }>(`/billing/${id}`)
}

/**
 * 创建对账单
 */
export function createBilling(data: CreateBillingParams) {
  return request.post<number>('/billing/create', data)
}

/**
 * 更新对账单
 */
export function updateBilling(id: number, data: {
  invoiceNo?: string
  invoiceUrl?: string
  remark?: string
}) {
  return request.put(`/billing/${id}`, data)
}

/**
 * 删除对账单
 */
export function deleteBilling(id: number) {
  return request.delete(`/billing/${id}`)
}

/**
 * 确认对账单
 */
export function confirmBilling(id: number, data: {
  invoiceNo: string
  invoiceUrl?: string
  remark?: string
}) {
  return request.post(`/billing/${id}/confirm`, data)
}

/**
 * 完成对账单
 */
export function completeBilling(id: number) {
  return request.post(`/billing/${id}/complete`)
}

/**
 * 取消对账单
 */
export function cancelBilling(id: number, reason: string) {
  return request.post(`/billing/${id}/cancel`, { reason })
}

/**
 * 导出对账单
 */
export function exportBilling(id: number) {
  return request.get(`/billing/${id}/export`, { responseType: 'blob' })
}

/**
 * 获取可对账的订单列表
 */
export function getBillableOrders(params: {
  customerId: number
  billingPeriod: string
}) {
  return request.get<Array<{
    orderId: number
    orderNo: string
    amount: number
    shipmentCount: number
    receiptStatus: string
  }>>('/billing/billable-orders', { params })
}

/**
 * 获取对账统计数据
 */
export function getBillingStatistics() {
  return request.get<{
    totalCount: number
    pendingCount: number
    confirmedCount: number
    completedCount: number
    totalAmount: number
  }>('/billing/statistics')
}
