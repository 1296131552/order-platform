/**
 * 客户相关 API
 * Requirements: 14.1, 14.2, 14.3, 14.4, 14.5
 */

import request from '@/utils/request'

/** 客户查询参数 */
export interface CustomerQueryParams {
  page?: number
  pageSize?: number
  name?: string
  customerNo?: string
  status?: string
  keyword?: string
}

/** 客户信息 */
export interface Customer {
  id: number
  customerNo: string
  name: string
  contactPerson: string
  contactPhone: string
  contactEmail?: string
  address: string
  status: string
  createdAt: string
  updatedAt: string
}

/** 创建客户参数 */
export interface CreateCustomerParams {
  customerNo?: string
  name: string
  contactPerson: string
  contactPhone: string
  contactEmail?: string
  address: string
}

/**
 * 分页查询客户列表
 * Requirements: 14.1
 */
export function getCustomerList(params: CustomerQueryParams) {
  return request.get<{ records: Customer[]; total: number; size: number; current: number; pages: number }>('/customer/list', { params })
}

/**
 * 获取客户详情
 * Requirements: 14.2
 */
export function getCustomerDetail(id: number) {
  return request.get<Customer>(`/customer/${id}`)
}

/**
 * 新增客户
 * Requirements: 14.3
 */
export function createCustomer(data: CreateCustomerParams) {
  return request.post<number>('/customer/', data)
}

/**
 * 更新客户
 * Requirements: 14.4
 */
export function updateCustomer(id: number, data: Partial<CreateCustomerParams>) {
  return request.put(`/customer/${id}`, data)
}

/**
 * 删除客户
 * Requirements: 14.5
 */
export function deleteCustomer(id: number) {
  return request.delete(`/customer/${id}`)
}

/**
 * 获取所有客户（不分页，用于下拉选择）
 */
export function getAllCustomers() {
  return request.get<Customer[]>('/customer/all')
}
