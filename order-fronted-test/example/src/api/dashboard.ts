/**
 * 数据看板相关 API
 */

import request from '@/utils/request'

/** 看板数据 */
export interface DashboardData {
  /** KPI 指标 */
  kpi: {
    totalOrders: number
    inTransitOrders: number
    completedOrders: number
    onTimeRate: number
    totalAmount: number
    exceptionCount: number
  }
  /** 订单趋势 */
  orderTrend: Array<{
    date: string
    orderCount: number
    amount: number
  }>
  /** 发运状态分布 */
  shipmentStatusDistribution: Array<{
    status: string
    statusLabel: string
    count: number
    percentage: number
  }>
  /** 签收异常趋势 */
  exceptionTrend: Array<{
    date: string
    count: number
  }>
  /** 承运商统计 */
  carrierStats: Array<{
    carrierId: number
    carrierName: string
    shipmentCount: number
    onTimeRate: number
    exceptionCount: number
  }>
  /** 即将到货 */
  upcomingArrivals: Array<{
    shipmentId: number
    shipmentNo: string
    orderNo: string
    carrierName: string
    estimatedArrivalTime: string
  }>
  /** 待处理异常 */
  pendingExceptions: Array<{
    id: number
    exceptionNo: string
    type: string
    level: string
    title: string
    reportTime: string
  }>
}

/**
 * 获取看板数据
 */
export function getDashboardData(params?: {
  startDate?: string
  endDate?: string
}) {
  return request.get<DashboardData>('/dashboard/data', { params })
}

/**
 * 获取 KPI 指标
 */
export function getKpiData(params?: {
  startDate?: string
  endDate?: string
}) {
  return request.get<DashboardData['kpi']>('/dashboard/kpi', { params })
}

/**
 * 获取订单趋势
 */
export function getOrderTrend(params: {
  startDate: string
  endDate: string
  interval?: 'day' | 'week' | 'month'
}) {
  return request.get<DashboardData['orderTrend']>('/dashboard/order-trend', { params })
}

/**
 * 获取地图数据（线路可视化）
 */
export function getMapData(params?: {
  status?: string
  startDate?: string
  endDate?: string
}) {
  return request.get<Array<{
    shipmentId: number
    shipmentNo: string
    status: string
    route: Array<{
      longitude: number
      latitude: number
      location: string
      time: string
    }>
  }>>(`/dashboard/map-data`, { params })
}

/**
 * 获取时间线数据
 */
export function getTimelineData(orderId: number) {
  return request.get<Array<{
    time: string
    type: string
    title: string
    description: string
    operator?: string
  }>>(`/dashboard/timeline/${orderId}`)
}
