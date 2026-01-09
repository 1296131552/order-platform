/**
 * 发运相关 API
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8
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

/** 发运状态枚举 */
export enum ShipmentStatus {
  PENDING = 'PENDING',       // 待提货
  IN_TRANSIT = 'IN_TRANSIT', // 在途
  DELIVERED = 'DELIVERED'    // 已到货
}

/** 发运查询参数 */
export interface ShipmentQueryParams {
  page?: number
  pageSize?: number
  shipmentNo?: string
  orderId?: number
  carrierId?: number
  status?: string
  startDate?: string
  endDate?: string
  keyword?: string
}

/** 发运信息 */
export interface Shipment {
  id: number
  shipmentNo: string
  orderId: number
  orderNo: string
  carrierId: number
  carrierName: string
  customerId?: number
  customerName?: string
  status: string
  vehicleNo?: string
  driverName?: string
  driverPhone?: string
  departureTime?: string
  estimatedArrivalTime?: string
  actualArrivalTime?: string
  shipmentAddress?: string
  receiverAddress?: string
  route?: string
  remark?: string
  createdAt: string
  updatedAt: string
}

/** 创建发运参数 */
export interface CreateShipmentParams {
  orderId: number
  carrierId: number
  vehicleNo?: string
  driverName?: string
  driverPhone?: string
  estimatedArrivalTime?: string
  shipmentAddress?: string
  receiverAddress?: string
  route?: string
  remark?: string
}

/** 更新发运参数 */
export interface UpdateShipmentParams {
  carrierId?: number
  vehicleNo?: string
  driverName?: string
  driverPhone?: string
  estimatedArrivalTime?: string
  shipmentAddress?: string
  receiverAddress?: string
  route?: string
  remark?: string
}

/** 发运统计数据 */
export interface ShipmentStatistics {
  totalCount: number
  pendingCount: number
  inTransitCount: number
  deliveredCount: number
}

/**
 * 分页查询发运列表
 * Requirements: 4.1
 */
export function getShipmentList(params: ShipmentQueryParams) {
  return request.get<PageResponse<Shipment>>('/shipment/list', { params })
}

/**
 * 获取发运详情
 * Requirements: 4.2
 */
export function getShipmentDetail(id: number) {
  return request.get<Shipment>(`/shipment/${id}`)
}

/**
 * 创建发运单
 * Requirements: 4.3
 */
export function createShipment(data: CreateShipmentParams) {
  return request.post<number>('/shipment/create', data)
}

/**
 * 更新发运单
 * Requirements: 4.4
 */
export function updateShipment(id: number, data: UpdateShipmentParams) {
  return request.put(`/shipment/${id}`, data)
}

/**
 * 删除发运单
 * Requirements: 4.5
 */
export function deleteShipment(id: number) {
  return request.delete(`/shipment/${id}`)
}

/**
 * 确认发货
 * Requirements: 4.6
 */
export function dispatchShipment(id: number, data: { departureTime: string }) {
  return request.post(`/shipment/${id}/dispatch`, data)
}

/**
 * 确认到货
 * Requirements: 4.7
 */
export function arriveShipment(id: number, data: { arrivalTime: string; remark?: string }) {
  return request.post(`/shipment/${id}/arrive`, data)
}

/**
 * 获取发运统计数据
 * Requirements: 4.8
 */
export function getShipmentStatistics() {
  return request.get<ShipmentStatistics>('/shipment/statistics')
}

/**
 * 获取发运轨迹（辅助接口）
 */
export function getShipmentTrack(id: number) {
  return request.get<Array<{
    time: string
    location: string
    status: string
    description?: string
  }>>(`/shipment/${id}/track`)
}
