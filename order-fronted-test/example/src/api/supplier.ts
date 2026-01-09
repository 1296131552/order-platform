/**
 * 供应商相关 API
 */

import request from '@/utils/request'

/** 供应商查询参数 */
export interface SupplierQueryParams {
  page?: number
  pageSize?: number
  name?: string
  status?: string
  startDate?: string
  endDate?: string
  keyword?: string
}

/** 供应商信息 */
export interface Supplier {
  id: number
  name: string
  code: string
  status: string
  contactPerson: string
  contactPhone: string
  contactEmail?: string
  address: string
  province?: string
  city?: string
  description?: string
  qualifications?: string[] // 资质文件URL
  createTime: string
  updateTime: string
}

/** 供应商统计信息 */
export interface SupplierStatistics {
  totalCount: number
  activeCount: number
  totalOrders: number
  totalAmount: number
  onTimeRate: number
  exceptionRate: number
}

/** 创建供应商参数 */
export interface CreateSupplierParams {
  name: string
  code: string
  contactPerson: string
  contactPhone: string
  contactEmail?: string
  address: string
  province?: string
  city?: string
  description?: string
  qualifications?: string[]
}

/**
 * 分页查询供应商列表
 */
export function getSupplierList(params: SupplierQueryParams) {
  return request.get<{ list: Supplier[]; total: number }>('/supplier/list', { params })
}

/**
 * 获取供应商详情
 */
export function getSupplierDetail(id: number) {
  return request.get<Supplier>(`/supplier/${id}`)
}

/**
 * 创建供应商
 */
export function createSupplier(data: CreateSupplierParams) {
  return request.post<number>('/supplier/create', data)
}

/**
 * 更新供应商
 */
export function updateSupplier(id: number, data: Partial<CreateSupplierParams>) {
  return request.put(`/supplier/${id}`, data)
}

/**
 * 删除供应商
 */
export function deleteSupplier(id: number) {
  return request.delete(`/supplier/${id}`)
}

/**
 * 获取供应商统计信息
 */
export function getSupplierStatistics(id: number) {
  return request.get<SupplierStatistics>(`/supplier/${id}/statistics`)
}

/**
 * 获取供应商的订单列表
 */
export function getSupplierOrders(id: number, params: {
  page?: number
  pageSize?: number
  status?: string
}) {
  return request.get<{ list: any[]; total: number }>(`/supplier/${id}/orders`, { params })
}

/**
 * 获取所有供应商（不分页，用于下拉选择）
 */
export function getAllSuppliers() {
  return request.get<Supplier[]>('/supplier/all')
}

/**
 * 上传供应商资质文件
 */
export function uploadSupplierQualification(supplierId: number, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<string[]>(`/supplier/${supplierId}/qualification`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
