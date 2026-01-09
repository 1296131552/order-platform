/**
 * 异常相关 API
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

/**
 * 分页查询异常列表
 */
export function getExceptionList(params: ExceptionQueryParams) {
  return request.get<{ list: Exception[]; total: number }>('/exception/list', { params })
}

/**
 * 获取异常详情
 */
export function getExceptionDetail(id: number) {
  return request.get<Exception>(`/exception/${id}`)
}

/**
 * 上报异常
 */
export function reportException(data: CreateExceptionParams) {
  return request.post<number>('/exception/report', data)
}

/**
 * 分配异常处理人
 */
export function assignException(id: number, handlerId: number) {
  return request.post(`/exception/${id}/assign`, { handlerId })
}

/**
 * 处理异常
 */
export function handleException(id: number, data: {
  solution: string
  remark?: string
}) {
  return request.post(`/exception/${id}/handle`, data)
}

/**
 * 关闭异常
 */
export function closeException(id: number, data: { remark?: string }) {
  return request.post(`/exception/${id}/close`, data)
}

/**
 * 获取异常统计数据
 */
export function getExceptionStatistics() {
  return request.get<{
    totalCount: number
    pendingCount: number
    handlingCount: number
    resolvedCount: number
    highLevelCount: number
  }>('/exception/statistics')
}
