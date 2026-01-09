/**
 * 用户管理相关 API
 * Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6
 */

import request from '@/utils/request'

/** 用户查询参数 */
export interface UserQueryParams {
  page?: number
  pageSize?: number
  username?: string
  realName?: string
  status?: string
  keyword?: string
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
  roles: string[]
  roleIds?: number[]
  createdAt: string
  updatedAt: string
}

/** 创建用户参数 */
export interface CreateUserParams {
  username: string
  realName: string
  email?: string
  phone?: string
  password: string
  roleIds: number[]
}

/** 更新用户参数 */
export interface UpdateUserParams {
  realName?: string
  email?: string
  phone?: string
  roleIds?: number[]
  status?: string
}

/**
 * 分页查询用户列表
 * Requirements: 9.1
 */
export function getUserList(params: UserQueryParams) {
  return request.get<{ records: User[]; total: number; size: number; current: number; pages: number }>('/user/list', { params })
}

/**
 * 获取用户详情
 * Requirements: 9.2
 */
export function getUserDetail(id: number) {
  return request.get<User>(`/user/${id}`)
}

/**
 * 创建用户
 * Requirements: 9.3
 */
export function createUser(data: CreateUserParams) {
  return request.post<number>('/user/', data)
}

/**
 * 更新用户
 * Requirements: 9.4
 */
export function updateUser(id: number, data: UpdateUserParams) {
  return request.put(`/user/${id}`, data)
}

/**
 * 删除用户
 * Requirements: 9.5
 */
export function deleteUser(id: number) {
  return request.delete(`/user/${id}`)
}

/**
 * 重置用户密码
 * Requirements: 9.6
 */
export function resetUserPassword(id: number) {
  return request.put(`/user/${id}/reset-password`)
}

/**
 * 获取所有用户（不分页，用于下拉选择）
 */
export function getAllUsers() {
  return request.get<User[]>('/user/all')
}

/**
 * 启用用户
 */
export function enableUser(id: number) {
  return request.put(`/user/${id}/enable`)
}

/**
 * 禁用用户
 */
export function disableUser(id: number) {
  return request.put(`/user/${id}/disable`)
}
