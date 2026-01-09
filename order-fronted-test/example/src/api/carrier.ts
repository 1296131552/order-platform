/**
 * 承运商相关 API
 * Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7, 8.8
 */

import request from '@/utils/request'

/** 承运商查询参数 */
export interface CarrierQueryParams {
  page?: number
  pageSize?: number
  name?: string
  status?: string
  startDate?: string
  endDate?: string
  keyword?: string
}

/** 承运商信息 */
export interface Carrier {
  id: number
  carrierNo: string
  name: string
  code: string
  status: string
  contactPerson: string
  contactPhone: string
  contactEmail?: string
  address: string
  description?: string
  qualifications?: string[] // 资质文件URL
  createdAt: string
  updatedAt: string
}

/** 车辆信息 */
export interface Vehicle {
  id: number
  carrierId: number
  vehicleNo: string
  vehicleType: string
  loadCapacity: number
  driverName: string
  driverPhone: string
  status: string
}

/** 承运商统计信息 */
export interface CarrierStatistics {
  totalCount: number
  activeCount: number
  totalShipments: number
  onTimeRate: number
  exceptionRate: number
  averageRating: number
}

/** 创建承运商参数 */
export interface CreateCarrierParams {
  carrierNo?: string
  name: string
  contactPerson: string
  contactPhone: string
  contactEmail?: string
  address: string
  description?: string
  qualifications?: string[]
}

/**
 * 分页查询承运商列表
 * Requirements: 8.1
 */
export function getCarrierList(params: CarrierQueryParams) {
  return request.get<{ records: Carrier[]; total: number; size: number; current: number; pages: number }>('/carrier/list', { params })
}

/**
 * 获取承运商详情
 * Requirements: 8.2
 */
export function getCarrierDetail(id: number) {
  return request.get<Carrier>(`/carrier/${id}`)
}

/**
 * 根据编号查询承运商
 * Requirements: 8.3
 */
export function getCarrierByNo(carrierNo: string) {
  return request.get<Carrier>(`/carrier/no/${carrierNo}`)
}

/**
 * 创建承运商
 * Requirements: 8.4
 */
export function createCarrier(data: CreateCarrierParams) {
  return request.post<number>('/carrier/', data)
}

/**
 * 更新承运商
 * Requirements: 8.5
 */
export function updateCarrier(id: number, data: Partial<CreateCarrierParams>) {
  return request.put(`/carrier/${id}`, data)
}

/**
 * 删除承运商
 * Requirements: 8.6
 */
export function deleteCarrier(id: number) {
  return request.delete(`/carrier/${id}`)
}

/**
 * 激活承运商
 * Requirements: 8.7
 */
export function activateCarrier(id: number) {
  return request.put(`/carrier/${id}/activate`)
}

/**
 * 停用承运商
 * Requirements: 8.8
 */
export function deactivateCarrier(id: number) {
  return request.put(`/carrier/${id}/deactivate`)
}

/**
 * 获取承运商统计信息
 */
export function getCarrierStatistics(id: number) {
  return request.get<CarrierStatistics>(`/carrier/${id}/statistics`)
}

/**
 * 获取承运商的发运列表
 */
export function getCarrierShipments(id: number, params: {
  page?: number
  pageSize?: number
  status?: string
}) {
  return request.get<{ list: any[]; total: number }>(`/carrier/${id}/shipments`, { params })
}

/**
 * 获取承运商的车辆列表
 */
export function getCarrierVehicles(id: number) {
  return request.get<Vehicle[]>(`/carrier/${id}/vehicles`)
}

/**
 * 获取所有承运商（不分页，用于下拉选择）
 */
export function getAllCarriers() {
  return request.get<Carrier[]>('/carrier/all')
}

/**
 * 上传承运商资质文件
 */
export function uploadCarrierQualification(carrierId: number, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<string[]>(`/carrier/${carrierId}/qualification`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 获取承运商准时率排行榜
 */
export function getCarrierOnTimeRanking(params: {
  startDate?: string
  endDate?: string
  limit?: number
}) {
  return request.get<Array<{
    carrierId: number
    carrierName: string
    onTimeRate: number
    totalShipments: number
  }>>('/carrier/ranking/on-time', { params })
}

/**
 * 获取承运商异常率排行榜
 */
export function getCarrierExceptionRanking(params: {
  startDate?: string
  endDate?: string
  limit?: number
}) {
  return request.get<Array<{
    carrierId: number
    carrierName: string
    exceptionRate: number
    totalShipments: number
  }>>('/carrier/ranking/exception', { params })
}
