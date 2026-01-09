/**
 * 签收相关 API
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6
 */

import request from '@/utils/request'

/** 签收查询参数 */
export interface ReceiptQueryParams {
  page?: number
  pageSize?: number
  receiptNo?: string
  shipmentId?: number
  shipmentLineId?: number
  orderId?: number
  status?: string
  startDate?: string
  endDate?: string
  keyword?: string
  hasDifference?: boolean
}

/** 签收信息 */
export interface Receipt {
  id: number
  receiptNo: string
  shipmentId: number
  shipmentNo: string
  shipmentLineId: number
  trackingNo: string
  orderId: number
  orderNo: string
  status: string
  receivedQuantity: number
  expectedQuantity: number
  differenceQuantity: number
  receiptDate: string
  receiverName: string
  hasDifference: boolean
  differenceReason?: string
  photos?: string[]
  remark?: string
  createdAt: string
  updatedAt: string
}

/** 签收确认参数 - Requirements: 6.1 */
export interface ReceiptConfirmParams {
  shipmentLineId: number
  receivedQuantity: number
  receiptDate: string
  receiverName: string
  hasDifference: boolean
  differenceQuantity?: number
  differenceReason?: string
  photos?: string[]
  remark?: string
}

/** 批量签收确认参数 - Requirements: 6.2 */
export interface BatchReceiptConfirmParams {
  items: ReceiptConfirmParams[]
}

/** 差异记录查询参数 */
export interface DifferenceRecordQueryParams {
  page?: number
  pageSize?: number
  shipmentId?: number
  orderId?: number
  startDate?: string
  endDate?: string
  status?: string
}

/**
 * 确认签收
 * POST /api/receipt/confirm
 * Requirements: 6.1
 */
export function confirmReceipt(data: ReceiptConfirmParams) {
  return request.post<Receipt>('/receipt/confirm', data)
}

/**
 * 批量签收确认
 * POST /api/receipt/batch-confirm
 * Requirements: 6.2
 */
export function batchConfirmReceipt(data: BatchReceiptConfirmParams) {
  return request.post<Receipt[]>('/receipt/batch-confirm', data)
}

/**
 * 查询快递单的签收记录
 * GET /api/receipt/list-by-shipment-line/{shipmentLineId}
 * Requirements: 6.3
 */
export function getReceiptsByShipmentLine(shipmentLineId: number) {
  return request.get<Receipt[]>(`/receipt/list-by-shipment-line/${shipmentLineId}`)
}

/**
 * 查询发运单的签收记录
 * GET /api/receipt/list-by-shipment/{shipmentId}
 * Requirements: 6.4
 */
export function getReceiptsByShipment(shipmentId: number) {
  return request.get<Receipt[]>(`/receipt/list-by-shipment/${shipmentId}`)
}

/**
 * 查询差异记录
 * GET /api/receipt/difference-records
 * Requirements: 6.5
 */
export function getDifferenceRecords(params?: DifferenceRecordQueryParams) {
  return request.get<{ records: Receipt[]; total: number; size: number; current: number; pages: number }>('/receipt/difference-records', { params })
}

/**
 * 获取签收详情
 * GET /api/receipt/{id}
 * Requirements: 6.6
 */
export function getReceiptDetail(id: number) {
  return request.get<Receipt>(`/receipt/${id}`)
}

/**
 * 分页查询签收列表
 * GET /api/receipt/list
 */
export function getReceiptList(params: ReceiptQueryParams) {
  return request.get<{ records: Receipt[]; total: number; size: number; current: number; pages: number }>('/receipt/list', { params })
}

/**
 * 处理签收差异
 * POST /api/receipt/{id}/handle-difference
 */
export function handleDifference(id: number, data: {
  solution: string
  remark?: string
}) {
  return request.post(`/receipt/${id}/handle-difference`, data)
}

/**
 * 获取签收统计数据
 * GET /api/receipt/statistics
 */
export function getReceiptStatistics() {
  return request.get<{
    totalCount: number
    pendingCount: number
    receivedCount: number
    differenceCount: number
    processedCount: number
    differenceRate: number
  }>('/receipt/statistics')
}
