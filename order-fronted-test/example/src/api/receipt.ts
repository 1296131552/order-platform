/**
 * 签收相关 API
 */

import request from '@/utils/request'

/** 签收查询参数 */
export interface ReceiptQueryParams {
  page?: number
  pageSize?: number
  receiptNo?: string
  shipmentId?: number
  orderId?: number
  status?: string
  startDate?: string
  endDate?: string
  keyword?: string
}

/** 签收信息 */
export interface Receipt {
  id: number
  receiptNo: string
  shipmentId: number
  shipmentNo: string
  orderId: number
  orderNo: string
  status: string
  receiptTime?: string
  receiptPerson?: string
  receiptPhotos?: string[]
  hasDifference: boolean
  differenceDescription?: string
  remark?: string
  createTime: string
  updateTime: string
}

/** 签收确认参数 */
export interface ConfirmReceiptParams {
  shipmentId: number
  receiptTime: string
  receiptPerson: string
  receiptPhotos?: string[]
  hasDifference: boolean
  differenceDescription?: string
  remark?: string
  items: {
    shipmentItemId: number
    receivedQuantity: number
    differenceReason?: string
  }[]
}

/**
 * 分页查询签收列表
 */
export function getReceiptList(params: ReceiptQueryParams) {
  return request.get<{ list: Receipt[]; total: number }>('/receipt/list', { params })
}

/**
 * 获取签收详情
 */
export function getReceiptDetail(id: number) {
  return request.get<Receipt>(`/receipt/${id}`)
}

/**
 * 根据发运ID获取待签收信息
 */
export function getReceiptByShipment(shipmentId: number) {
  return request.get<{
    shipment: any
    items: Array<{
      id: number
      productName: string
      quantity: number
      unit: string
    }>
  }>(`/receipt/by-shipment/${shipmentId}`)
}

/**
 * 确认签收
 */
export function confirmReceipt(data: ConfirmReceiptParams) {
  return request.post<number>('/receipt/confirm', data)
}

/**
 * 处理签收差异
 */
export function handleDifference(id: number, data: {
  solution: string
  remark?: string
}) {
  return request.post(`/receipt/${id}/handle-difference`, data)
}

/**
 * 获取签收统计数据
 */
export function getReceiptStatistics() {
  return request.get<{
    totalCount: number
    pendingCount: number
    confirmedCount: number
    differenceCount: number
    differenceRate: number
  }>('/receipt/statistics')
}
