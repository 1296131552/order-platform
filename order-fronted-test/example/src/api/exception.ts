/**
 * 异常管理相关 API
 * Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6
 */

import request from '@/utils/request'

/** 异常查询参数 */
export interface ExceptionQueryParams {
  page?: number
  pageSize?: number
  exceptionNo?: string
  type?: string
  level?: string
  status?: string
  startDate?: string
  endDate?: string
  keyword?: string
}

/** 异常信息 */
export interface Exception {
  id: number
  exceptionNo: string
  type: string
  level: string
  status: string
  title: string
  description: string
  relatedType: string
  relatedId: number
  reporterId: number
  reporterName: string
  handlerId?: number
  handlerName?: string
  reportTime: string
  handleTime?: string
  solution?: string
  remark?: string
  createTime: string
  updateTime: string
}

/** 创建异常参数 */
export interface CreateExceptionParams {
  type: string
  level: string
  title: string
  description: string
  relatedType: string
  relatedId: number
  remark?: string
}

/** 处理异常参数 */
export interface HandleExceptionParams {
  solution: string
  remark?: string
}

/** 异常统计数据 */
export interface ExceptionStatistics {
  totalCount: number
  pendingCount: number
  handlingCount: number
  resolvedCount: number
  highLevelCount: number
}

/**
 * 分页查询异常列表
 * GET /api/exception/list
 * Requirements: 11.1
 */
export function getExceptionList(params: ExceptionQueryParams) {
  return request.get('/exception/list', { params })
}

/**
 * 获取异常详情
 * GET /api/exception/{id}
 * Requirements: 11.2
 */
export function getExceptionDetail(id: number) {
  return request.get<Exception>(`/exception/${id}`)
}

/**
 * 创建异常记录
 * POST /api/exception/create
 * Requirements: 11.3
 */
export function createException(data: CreateExceptionParams) {
  return request.post<number>('/exception/create', data)
}

/**
 * 分配异常处理人
 * PUT /api/exception/{id}/assign
 * Requirements: 11.4
 */
export function assignException(id: number, handlerId: number) {
  return request.put(`/exception/${id}/assign`, { handlerId })
}

/**
 * 处理异常
 * POST /api/exception/{id}/handle
 * Requirements: 11.5
 */
export function handleException(id: number, data: HandleExceptionParams) {
  return request.post(`/exception/${id}/handle`, data)
}

/**
 * 获取异常统计数据
 * GET /api/exception/statistics
 * Requirements: 11.6
 */
export function getExceptionStatistics() {
  return request.get<ExceptionStatistics>('/exception/statistics')
}
