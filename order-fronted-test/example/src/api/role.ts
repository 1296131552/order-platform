/**
 * 角色权限管理相关 API
 * Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6
 */

import request from '@/utils/request'

/** 角色查询参数 */
export interface RoleQueryParams {
  page?: number
  pageSize?: number
  roleCode?: string
  roleName?: string
  status?: string
  keyword?: string
}

/** 角色信息 */
export interface Role {
  id: number
  roleCode: string
  roleName: string
  description?: string
  permissions: string[]
  permissionIds?: number[]
  status: string
  createdAt: string
  updatedAt: string
}

/** 权限信息 */
export interface Permission {
  id: number
  permissionCode: string
  permissionName: string
  description?: string
  parentId?: number
  type: string
  path?: string
  icon?: string
  sort: number
  children?: Permission[]
}

/** 创建角色参数 */
export interface CreateRoleParams {
  roleCode: string
  roleName: string
  description?: string
  permissionIds?: number[]
}

/** 更新角色参数 */
export interface UpdateRoleParams {
  roleName?: string
  description?: string
  status?: string
}

/** 分配权限参数 */
export interface AssignPermissionsParams {
  permissionIds: number[]
}

/**
 * 分页查询角色列表
 * Requirements: 10.1
 */
export function getRoleList(params: RoleQueryParams) {
  return request.get<{ records: Role[]; total: number; size: number; current: number; pages: number }>('/role/list', { params })
}

/**
 * 获取角色详情
 * Requirements: 10.2
 */
export function getRoleDetail(id: number) {
  return request.get<Role>(`/role/${id}`)
}

/**
 * 创建角色
 * Requirements: 10.3
 */
export function createRole(data: CreateRoleParams) {
  return request.post<number>('/role/', data)
}

/**
 * 更新角色
 * Requirements: 10.4
 */
export function updateRole(id: number, data: UpdateRoleParams) {
  return request.put(`/role/${id}`, data)
}

/**
 * 删除角色
 * Requirements: 10.5
 */
export function deleteRole(id: number) {
  return request.delete(`/role/${id}`)
}

/**
 * 为角色分配权限
 * Requirements: 10.6
 */
export function assignPermissions(id: number, data: AssignPermissionsParams) {
  return request.post(`/role/${id}/assign-permissions`, data)
}

/**
 * 获取所有角色（不分页，用于下拉选择）
 */
export function getAllRoles() {
  return request.get<Role[]>('/role/all')
}

/**
 * 获取角色的权限列表
 */
export function getRolePermissions(id: number) {
  return request.get<Permission[]>(`/role/${id}/permissions`)
}

/**
 * 获取所有权限列表（树形结构）
 */
export function getAllPermissions() {
  return request.get<Permission[]>('/permission/tree')
}

/**
 * 启用角色
 */
export function enableRole(id: number) {
  return request.put(`/role/${id}/enable`)
}

/**
 * 禁用角色
 */
export function disableRole(id: number) {
  return request.put(`/role/${id}/disable`)
}
