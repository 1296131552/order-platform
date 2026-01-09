/**
 * 发运相关 API
 */

import request from '@/utils/request'

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
  status: string
  vehicleNo?: string
  driverName?: string
  driverPhone?: string
  departureTime?: string
  estimatedArrivalTime?: string
  actualArrivalTime?: string
  route?: string
  remark?: string
  createTime: string
  updateTime: string
}

/** 创建发运参数 */
export interface CreateShipmentParams {
  orderId: number
  carrierId: number
  vehicleNo?: string
  driverName?: string
  driverPhone?: string
  estimatedArrivalTime?: string
  route?: string
  remark?: string
  items: {
    orderId: number
    productId: number
    quantity: number
  }[]
}

/**
 * 分页查询发运列表
 */
export function getShipmentList(params: ShipmentQueryParams) {
  return request.get<{ list: Shipment[]; total: number }>('/shipment/list', { params })
}

/**
 * 获取发运详情
 */
export function getShipmentDetail(id: number) {
  return request.get<Shipment>(`/shipment/${id}`)
}

/**
 * 创建发运
 */
export function createShipment(data: CreateShipmentParams) {
  return request.post<number>('/shipment/create', data)
}

/**
 * 更新发运
 */
export function updateShipment(id: number, data: Partial<CreateShipmentParams>) {
  return request.put(`/shipment/${id}`, data)
}

/**
 * 删除发运
 */
export function deleteShipment(id: number) {
  return request.delete(`/shipment/${id}`)
}

/**
 * 发货
 */
export function dispatchShipment(id: number, data: { departureTime: string }) {
  return request.post(`/shipment/${id}/dispatch`, data)
}

/**
 * 确认到货
 */
export function confirmArrival(id: number, data: { arrivalTime: string; remark?: string }) {
  return request.post(`/shipment/${id}/arrive`, data)
}

/**
 * 获取发运轨迹
 */
export function getShipmentTrack(id: number) {
  return request.get<Array<{
    time: string
    location: string
    status: string
    description?: string
  }>>(`/shipment/${id}/track`)
}

/**
 * 获取发运统计数据
 */
export function getShipmentStatistics() {
  return request.get<{
    totalCount: number
    pendingCount: number
    inTransitCount: number
    deliveredCount: number
  }>('/shipment/statistics')
}
