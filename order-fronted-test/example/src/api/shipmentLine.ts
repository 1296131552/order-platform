/**
 * 快递单相关 API
 * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8, 5.9, 5.10
 */

import request from '@/utils/request'

/** 分页响应类型 */
export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/** 快递单状态枚举 */
export enum ShipmentLineStatus {
  PENDING = 'PENDING',       // 待发货
  IN_TRANSIT = 'IN_TRANSIT', // 在途
  DELIVERED = 'DELIVERED',   // 已送达
  SIGNED = 'SIGNED'          // 已签收
}

/** 快递单查询参数 */
export interface ShipmentLineQueryParams {
  page?: number
  pageSize?: number
  shipmentId?: number
  trackingNo?: string
  carrierId?: number
  status?: string
  keyword?: string
}

/** 快递单信息 */
export interface ShipmentLine {
  id: number
  shipmentId: number
  lineNo: number
  trackingNo: string
  carrierId: number
  carrierName: string
  quantity: number
  weight?: number
  volume?: number
  status: string
  remark?: string
  createdAt: string
  updatedAt: string
}

/** 添加快递单参数 */
export interface AddShipmentLineParams {
  shipmentId: number
  trackingNo: string
  carrierId: number
  quantity: number
  weight?: number
  volume?: number
  remark?: string
}

/** 更新快递单参数 */
export interface UpdateShipmentLineParams {
  id: number
  trackingNo?: string
  carrierId?: number
  quantity?: number
  weight?: number
  volume?: number
  remark?: string
}

/** 物流事件 */
export interface TrackingEvent {
  time: string
  location: string
  description: string
}

/** 物流信息 */
export interface TrackingInfo {
  trackingNo: string
  carrier: string
  status: string
  events: TrackingEvent[]
}

/**
 * 查询发运单的快递单列表
 * Requirements: 5.1
 */
export function getShipmentLinesByShipmentId(shipmentId: number) {
  return request.get<ShipmentLine[]>(`/shipment-line/list/${shipmentId}`)
}

/**
 * 分页查询快递单
 * Requirements: 5.2
 */
export function getShipmentLineList(params: ShipmentLineQueryParams) {
  return request.get<PageResponse<ShipmentLine>>('/shipment-line/list', { params })
}

/**
 * 获取快递单详情
 * Requirements: 5.3
 */
export function getShipmentLineDetail(id: number) {
  return request.get<ShipmentLine>(`/shipment-line/${id}`)
}

/**
 * 添加快递单
 * Requirements: 5.4
 */
export function addShipmentLine(data: AddShipmentLineParams) {
  return request.post<number>('/shipment-line/add', data)
}

/**
 * 批量添加快递单
 * Requirements: 5.5
 */
export function batchAddShipmentLines(data: AddShipmentLineParams[]) {
  return request.post<number[]>('/shipment-line/batch-add', data)
}

/**
 * 更新快递单
 * Requirements: 5.6
 */
export function updateShipmentLine(data: UpdateShipmentLineParams) {
  return request.put('/shipment-line/update', data)
}

/**
 * 删除快递单
 * Requirements: 5.7
 */
export function deleteShipmentLine(id: number) {
  return request.delete(`/shipment-line/${id}`)
}

/**
 * 更新快递单状态
 * Requirements: 5.8
 */
export function updateShipmentLineStatus(id: number, status: string) {
  return request.put(`/shipment-line/${id}/status`, { status })
}

/**
 * 查询物流信息
 * Requirements: 5.9
 */
export function getTrackingInfo(trackingNo: string) {
  return request.get<TrackingInfo>(`/shipment-line/tracking/${trackingNo}`)
}

/**
 * 获取下一个行号
 * Requirements: 5.10
 */
export function getNextLineNo(shipmentId: number) {
  return request.get<number>(`/shipment-line/next-line-no/${shipmentId}`)
}
