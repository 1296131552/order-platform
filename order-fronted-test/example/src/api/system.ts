/**
 * 系统管理相关 API
 */

import request from '@/utils/request'

/** 用户查询参数 */
export interface UserQueryParams {
  page?: number
  pageSize?: number
  username?: string
  realName?: string
  status?: string
  roleId?: number
}

/** 用户信息 */
export interface User {
  id: number
  username: string
  realName: string
  email?: string
  phone?: string
  avatar?: string
  status: string
  roles: Array<{ id: number; name: string }>
  remark?: string
  createTime: string
  updateTime: string
}

/** 角色查询参数 */
export interface RoleQueryParams {
  page?: number
  pageSize?: number
  name?: string
  code?: string
  status?: string
}

/** 角色信息 */
export interface Role {
  id: number
  name: string
  code: string
  status: string
  permissions: string[]
  remark?: string
  createTime: string
  updateTime: string
}

/** 字典类型 */
export interface DictType {
  id: number
  dictCode: string
  dictName: string
  remark?: string
  createTime: string
}

/** 字典数据 */
export interface DictData {
  id: number
  dictType: string
  dictLabel: string
  dictValue: string
  dictSort: number
  status: string
  remark?: string
}

/**
 * 获取用户列表
 */
export function getUserList(params: UserQueryParams) {
  return request.get<{ list: User[]; total: number }>('/system/user/list', { params })
}

/**
 * 创建用户
 */
export function createUser(data: {
  username: string
  password: string
  realName: string
  email?: string
  phone?: string
  roleIds: number[]
  remark?: string
}) {
  return request.post<number>('/system/user/create', data)
}

/**
 * 更新用户
 */
export function updateUser(id: number, data: {
  realName?: string
  email?: string
  phone?: string
  roleIds?: number[]
  status?: string
  remark?: string
}) {
  return request.put(`/system/user/${id}`, data)
}

/**
 * 删除用户
 */
export function deleteUser(id: number) {
  return request.delete(`/system/user/${id}`)
}

/**
 * 重置用户密码
 */
export function resetUserPassword(id: number, newPassword: string) {
  return request.post(`/system/user/${id}/reset-password`, { newPassword })
}

/**
 * 获取角色列表
 */
export function getRoleList(params: RoleQueryParams) {
  return request.get<{ list: Role[]; total: number }>('/system/role/list', { params })
}

/**
 * 获取所有角色（不分页）
 */
export function getAllRoles() {
  return request.get<Role[]>('/system/role/all')
}

/**
 * 创建角色
 */
export function createRole(data: {
  name: string
  code: string
  permissions: string[]
  remark?: string
}) {
  return request.post<number>('/system/role/create', data)
}

/**
 * 更新角色
 */
export function updateRole(id: number, data: {
  name?: string
  permissions?: string[]
  remark?: string
}) {
  return request.put(`/system/role/${id}`, data)
}

/**
 * 删除角色
 */
export function deleteRole(id: number) {
  return request.delete(`/system/role/${id}`)
}

/**
 * 获取字典类型列表
 */
export function getDictTypeList() {
  return request.get<DictType[]>('/system/dict/type/list')
}

/**
 * 获取字典数据
 */
export function getDictData(dictType: string) {
  return request.get<DictData[]>(`/system/dict/data/${dictType}`)
}

/**
 * 获取操作日志
 */
export function getOperationLogs(params: {
  page?: number
  pageSize?: number
  username?: string
  module?: string
  startDate?: string
  endDate?: string
}) {
  return request.get<{ list: any[]; total: number }>('/system/log/operation', { params })
}

/**
 * 获取登录日志
 */
export function getLoginLogs(params: {
  page?: number
  pageSize?: number
  username?: string
  startDate?: string
  endDate?: string
}) {
  return request.get<{ list: any[]; total: number }>('/system/log/login', { params })
}

/**
 * 获取系统配置
 */
export function getSystemConfig() {
  return request.get<{
    systemName: string
    logo?: string
    theme?: string
    features: string[]
  }>('/system/config')
}

/**
 * 更新系统配置
 */
export function updateSystemConfig(data: {
  systemName?: string
  logo?: string
  theme?: string
}) {
  return request.put('/system/config', data)
}
