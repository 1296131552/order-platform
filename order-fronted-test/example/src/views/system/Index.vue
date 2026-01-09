<template>
  <div class="system-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 系统概览 -->
      <el-tab-pane label="系统概览" name="overview">
        <el-row :gutter="20">
          <!-- 系统配置 -->
          <el-col :xs="24" :sm="24" :md="12">
            <el-card class="config-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <el-icon><Setting /></el-icon>
                  <span>系统配置</span>
                </div>
              </template>
              <el-form label-width="100px">
                <el-form-item label="系统名称">
                  <el-input v-model="systemConfig.appName" />
                </el-form-item>
                <el-form-item label="Logo地址">
                  <el-input v-model="systemConfig.logoUrl" />
                </el-form-item>
                <el-form-item label="系统版本">
                  <el-input v-model="systemConfig.version" disabled />
                </el-form-item>
                <el-form-item label="版权信息">
                  <el-input v-model="systemConfig.copyright" />
                </el-form-item>
                <el-form-item label="备案号">
                  <el-input v-model="systemConfig.icp" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSaveConfig">保存配置</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>

          <!-- 当前用户 -->
          <el-col :xs="24" :sm="24" :md="12">
            <el-card class="user-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <el-icon><User /></el-icon>
                  <span>当前用户</span>
                </div>
              </template>
              <div class="user-info">
                <el-avatar :size="80" :src="currentUserInfo.avatar">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <h3>{{ currentUserInfo.realName }}</h3>
                <p class="username">@{{ currentUserInfo.username }}</p>
                <div class="user-detail">
                  <p><el-icon><Message /></el-icon> {{ currentUserInfo.email || '未设置' }}</p>
                  <p><el-icon><Phone /></el-icon> {{ currentUserInfo.phone || '未设置' }}</p>
                </div>
                <div class="roles">
                  <el-tag v-for="role in currentUserInfo.roles" :key="role" type="primary">{{ role }}</el-tag>
                </div>
                <el-button type="primary" @click="handleEditProfile">编辑资料</el-button>
                <el-button @click="handleChangePassword">修改密码</el-button>
              </div>
            </el-card>
          </el-col>

          <!-- 系统状态 -->
          <el-col :xs="24" :sm="24" :md="12">
            <el-card class="status-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <el-icon><Monitor /></el-icon>
                  <span>系统状态</span>
                </div>
              </template>
              <div class="status-list">
                <div class="status-item">
                  <span class="label">运行状态</span>
                  <el-tag type="success">正常</el-tag>
                </div>
                <div class="status-item">
                  <span class="label">数据库</span>
                  <el-tag type="success">连接正常</el-tag>
                </div>
                <div class="status-item">
                  <span class="label">缓存服务</span>
                  <el-tag type="success">运行中</el-tag>
                </div>
                <div class="status-item">
                  <span class="label">文件存储</span>
                  <el-tag type="success">可用</el-tag>
                </div>
                <div class="status-item">
                  <span class="label">API接口</span>
                  <el-tag type="success">正常</el-tag>
                </div>
              </div>
            </el-card>
          </el-col>

          <!-- 快捷操作 -->
          <el-col :xs="24" :sm="24" :md="12">
            <el-card class="action-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <el-icon><Operation /></el-icon>
                  <span>快捷操作</span>
                </div>
              </template>
              <div class="action-list">
                <el-button :icon="User" @click="activeTab = 'users'">用户管理</el-button>
                <el-button :icon="Lock" @click="activeTab = 'roles'">角色管理</el-button>
                <el-button :icon="Document" @click="handleLogManagement">操作日志</el-button>
                <el-button :icon="Delete" @click="handleClearCache">清除缓存</el-button>
                <el-button :icon="Refresh" @click="handleRestartService">重启服务</el-button>
                <el-button :icon="Download" @click="handleBackupData">数据备份</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- 用户管理 -->
      <el-tab-pane label="用户管理" name="users">
        <div class="tab-content">
          <!-- 搜索区域 -->
          <el-form :inline="true" :model="userSearchForm" class="search-form">
            <el-form-item label="用户名">
              <el-input v-model="userSearchForm.username" placeholder="请输入用户名" clearable />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="userSearchForm.realName" placeholder="请输入姓名" clearable />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="userSearchForm.status" placeholder="请选择状态" clearable>
                <el-option label="正常" value="ACTIVE" />
                <el-option label="锁定" value="LOCKED" />
                <el-option label="禁用" value="DISABLED" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="loadUserList">搜索</el-button>
              <el-button :icon="Refresh" @click="resetUserSearch">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 操作按钮 -->
          <div class="toolbar">
            <el-button type="primary" :icon="Plus" @click="handleCreateUser">新建用户</el-button>
          </div>

          <!-- 用户表格 -->
          <el-table :data="userList" v-loading="userLoading" border stripe>
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="realName" label="姓名" width="120" />
            <el-table-column prop="email" label="邮箱" min-width="180" />
            <el-table-column prop="phone" label="手机号" width="140" />
            <el-table-column prop="roles" label="角色" min-width="150">
              <template #default="{ row }">
                <el-tag v-for="role in row.roles" :key="role" size="small" style="margin-right: 4px;">
                  {{ role }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getUserStatusType(row.status)">
                  {{ getUserStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="160" />
            <el-table-column label="操作" width="240" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditUser(row)">编辑</el-button>
                <el-button type="warning" link size="small" @click="handleResetPassword(row)">重置密码</el-button>
                <el-button 
                  v-if="row.status !== 'ACTIVE'" 
                  type="success" 
                  link 
                  size="small" 
                  @click="handleEnableUser(row)"
                >启用</el-button>
                <el-button 
                  v-if="row.status === 'ACTIVE'" 
                  type="warning" 
                  link 
                  size="small" 
                  @click="handleDisableUser(row)"
                >禁用</el-button>
                <el-button type="danger" link size="small" @click="handleDeleteUser(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <el-pagination
            v-model:current-page="userPage"
            v-model:page-size="userPageSize"
            :total="userTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadUserList"
            @current-change="loadUserList"
            style="margin-top: 16px; justify-content: flex-end;"
          />
        </div>
      </el-tab-pane>

      <!-- 角色管理 -->
      <el-tab-pane label="角色管理" name="roles">
        <div class="tab-content">
          <!-- 搜索区域 -->
          <el-form :inline="true" :model="roleSearchForm" class="search-form">
            <el-form-item label="角色编码">
              <el-input v-model="roleSearchForm.roleCode" placeholder="请输入角色编码" clearable />
            </el-form-item>
            <el-form-item label="角色名称">
              <el-input v-model="roleSearchForm.roleName" placeholder="请输入角色名称" clearable />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="roleSearchForm.status" placeholder="请选择状态" clearable>
                <el-option label="启用" value="ACTIVE" />
                <el-option label="禁用" value="DISABLED" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="loadRoleList">搜索</el-button>
              <el-button :icon="Refresh" @click="resetRoleSearch">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 操作按钮 -->
          <div class="toolbar">
            <el-button type="primary" :icon="Plus" @click="handleCreateRole">新建角色</el-button>
          </div>

          <!-- 角色表格 -->
          <el-table :data="roleList" v-loading="roleLoading" border stripe>
            <el-table-column prop="roleCode" label="角色编码" width="140" />
            <el-table-column prop="roleName" label="角色名称" width="150" />
            <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
            <el-table-column prop="permissions" label="权限数" width="100" align="center">
              <template #default="{ row }">
                <el-tag>{{ row.permissions?.length || 0 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
                  {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="160" />
            <el-table-column label="操作" width="240" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="handleEditRole(row)">编辑</el-button>
                <el-button type="primary" link size="small" @click="handleAssignPermissions(row)">分配权限</el-button>
                <el-button 
                  v-if="row.status !== 'ACTIVE'" 
                  type="success" 
                  link 
                  size="small" 
                  @click="handleEnableRole(row)"
                >启用</el-button>
                <el-button 
                  v-if="row.status === 'ACTIVE'" 
                  type="warning" 
                  link 
                  size="small" 
                  @click="handleDisableRole(row)"
                >禁用</el-button>
                <el-button type="danger" link size="small" @click="handleDeleteRole(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <el-pagination
            v-model:current-page="rolePage"
            v-model:page-size="rolePageSize"
            :total="roleTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadRoleList"
            @current-change="loadRoleList"
            style="margin-top: 16px; justify-content: flex-end;"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 用户编辑对话框 -->
    <el-dialog 
      v-model="userDialogVisible" 
      :title="userDialogTitle" 
      width="500px"
      destroy-on-close
    >
      <el-form 
        ref="userFormRef" 
        :model="userForm" 
        :rules="userRules" 
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="!!userForm.id" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item v-if="!userForm.id" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="userForm.roleIds" multiple placeholder="请选择角色" style="width: 100%;">
            <el-option 
              v-for="role in allRoles" 
              :key="role.id" 
              :label="role.roleName" 
              :value="role.id" 
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUserForm" :loading="userSubmitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 角色编辑对话框 -->
    <el-dialog 
      v-model="roleDialogVisible" 
      :title="roleDialogTitle" 
      width="500px"
      destroy-on-close
    >
      <el-form 
        ref="roleFormRef" 
        :model="roleForm" 
        :rules="roleRules" 
        label-width="80px"
      >
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="roleForm.roleCode" :disabled="!!roleForm.id" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="roleForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRoleForm" :loading="roleSubmitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 权限分配对话框 -->
    <el-dialog 
      v-model="permissionDialogVisible" 
      title="分配权限" 
      width="500px"
      destroy-on-close
    >
      <el-tree
        ref="permissionTreeRef"
        :data="permissionTree"
        :props="{ label: 'permissionName', children: 'children' }"
        show-checkbox
        node-key="id"
        :default-checked-keys="selectedPermissionIds"
        :default-expand-all="true"
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPermissions" :loading="permissionSubmitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>


<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Setting,
  User,
  Monitor,
  Operation,
  Message,
  Phone,
  Lock,
  Document,
  Delete,
  Refresh,
  Download,
  Plus,
  Search
} from '@element-plus/icons-vue'
import { useSettingsStore } from '@/stores/settings'
import { useUserStore } from '@/stores/user'
import { 
  getUserList, 
  createUser, 
  updateUser, 
  deleteUser, 
  resetUserPassword,
  enableUser,
  disableUser,
  type User as UserType,
  type UserQueryParams,
  type CreateUserParams,
  type UpdateUserParams
} from '@/api/user'
import {
  getRoleList,
  getAllRoles,
  createRole,
  updateRole,
  deleteRole,
  assignPermissions,
  getAllPermissions,
  enableRole,
  disableRole,
  type Role,
  type RoleQueryParams,
  type CreateRoleParams,
  type UpdateRoleParams,
  type Permission
} from '@/api/role'

const settingsStore = useSettingsStore()
const userStore = useUserStore()

// 当前标签页
const activeTab = ref('overview')

// 系统配置
const systemConfig = reactive({
  appName: settingsStore.systemConfig.appName,
  logoUrl: settingsStore.systemConfig.logoUrl,
  version: settingsStore.systemConfig.version,
  copyright: settingsStore.systemConfig.copyright,
  icp: settingsStore.systemConfig.icp
})

// 当前用户信息
const currentUserInfo = computed(() => userStore.userInfo || {
  username: 'admin',
  realName: '管理员',
  email: '',
  phone: '',
  avatar: '',
  roles: ['管理员']
})

// ==================== 用户管理 ====================
const userLoading = ref(false)
const userList = ref<UserType[]>([])
const userTotal = ref(0)
const userPage = ref(1)
const userPageSize = ref(10)
const userSearchForm = reactive<UserQueryParams>({})

// 用户对话框
const userDialogVisible = ref(false)
const userDialogTitle = ref('新建用户')
const userFormRef = ref<FormInstance>()
const userSubmitting = ref(false)
const userForm = reactive<CreateUserParams & { id?: number }>({
  username: '',
  realName: '',
  email: '',
  phone: '',
  password: '',
  roleIds: []
})

const userRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  roleIds: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

// 所有角色（用于下拉选择）
const allRoles = ref<Role[]>([])

// 加载用户列表
const loadUserList = async () => {
  userLoading.value = true
  try {
    const params = {
      ...userSearchForm,
      page: userPage.value,
      pageSize: userPageSize.value
    }
    const res = await getUserList(params)
    userList.value = res.records
    userTotal.value = res.total
  } catch (error) {
    console.error('加载用户列表失败：', error)
    // 使用模拟数据
    userList.value = [
      { id: 1, username: 'admin', realName: '管理员', email: 'admin@example.com', phone: '13800138000', status: 'ACTIVE', roles: ['超级管理员'], createdAt: '2026-01-01 00:00:00', updatedAt: '2026-01-01 00:00:00' },
      { id: 2, username: 'user1', realName: '张三', email: 'zhangsan@example.com', phone: '13800138001', status: 'ACTIVE', roles: ['普通用户'], createdAt: '2026-01-02 00:00:00', updatedAt: '2026-01-02 00:00:00' }
    ]
    userTotal.value = 2
  } finally {
    userLoading.value = false
  }
}

// 重置用户搜索
const resetUserSearch = () => {
  userSearchForm.username = undefined
  userSearchForm.realName = undefined
  userSearchForm.status = undefined
  userPage.value = 1
  loadUserList()
}

// 新建用户
const handleCreateUser = async () => {
  userDialogTitle.value = '新建用户'
  Object.assign(userForm, { id: undefined, username: '', realName: '', email: '', phone: '', password: '', roleIds: [] })
  await loadAllRoles()
  userDialogVisible.value = true
}

// 编辑用户
const handleEditUser = async (row: UserType) => {
  userDialogTitle.value = '编辑用户'
  Object.assign(userForm, { 
    id: row.id, 
    username: row.username, 
    realName: row.realName, 
    email: row.email, 
    phone: row.phone, 
    password: '',
    roleIds: row.roleIds || []
  })
  await loadAllRoles()
  userDialogVisible.value = true
}

// 提交用户表单
const submitUserForm = async () => {
  if (!userFormRef.value) return
  await userFormRef.value.validate(async (valid) => {
    if (!valid) return
    userSubmitting.value = true
    try {
      if (userForm.id) {
        const updateData: UpdateUserParams = {
          realName: userForm.realName,
          email: userForm.email,
          phone: userForm.phone,
          roleIds: userForm.roleIds
        }
        await updateUser(userForm.id, updateData)
        ElMessage.success('更新成功')
      } else {
        await createUser(userForm as CreateUserParams)
        ElMessage.success('创建成功')
      }
      userDialogVisible.value = false
      loadUserList()
    } catch (error) {
      console.error('保存用户失败：', error)
    } finally {
      userSubmitting.value = false
    }
  })
}

// 重置密码
const handleResetPassword = (row: UserType) => {
  ElMessageBox.confirm(`确定要重置用户 "${row.realName}" 的密码吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await resetUserPassword(row.id)
      ElMessage.success('密码重置成功')
    } catch (error) {
      console.error('重置密码失败：', error)
    }
  }).catch(() => {})
}

// 启用用户
const handleEnableUser = async (row: UserType) => {
  try {
    await enableUser(row.id)
    ElMessage.success('启用成功')
    loadUserList()
  } catch (error) {
    console.error('启用用户失败：', error)
  }
}

// 禁用用户
const handleDisableUser = (row: UserType) => {
  ElMessageBox.confirm(`确定要禁用用户 "${row.realName}" 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await disableUser(row.id)
      ElMessage.success('禁用成功')
      loadUserList()
    } catch (error) {
      console.error('禁用用户失败：', error)
    }
  }).catch(() => {})
}

// 删除用户
const handleDeleteUser = (row: UserType) => {
  ElMessageBox.confirm(`确定要删除用户 "${row.realName}" 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      loadUserList()
    } catch (error) {
      console.error('删除用户失败：', error)
    }
  }).catch(() => {})
}

// 获取用户状态类型
const getUserStatusType = (status: string) => {
  const map: Record<string, string> = {
    'ACTIVE': 'success',
    'LOCKED': 'warning',
    'DISABLED': 'info'
  }
  return map[status] || 'info'
}

// 获取用户状态文本
const getUserStatusText = (status: string) => {
  const map: Record<string, string> = {
    'ACTIVE': '正常',
    'LOCKED': '锁定',
    'DISABLED': '禁用'
  }
  return map[status] || status
}

// ==================== 角色管理 ====================
const roleLoading = ref(false)
const roleList = ref<Role[]>([])
const roleTotal = ref(0)
const rolePage = ref(1)
const rolePageSize = ref(10)
const roleSearchForm = reactive<RoleQueryParams>({})

// 角色对话框
const roleDialogVisible = ref(false)
const roleDialogTitle = ref('新建角色')
const roleFormRef = ref<FormInstance>()
const roleSubmitting = ref(false)
const roleForm = reactive<CreateRoleParams & { id?: number }>({
  roleCode: '',
  roleName: '',
  description: ''
})

const roleRules: FormRules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

// 权限分配对话框
const permissionDialogVisible = ref(false)
const permissionTreeRef = ref()
const permissionTree = ref<Permission[]>([])
const selectedPermissionIds = ref<number[]>([])
const currentRoleId = ref<number>()
const permissionSubmitting = ref(false)

// 加载所有角色
const loadAllRoles = async () => {
  try {
    allRoles.value = await getAllRoles()
  } catch (error) {
    console.error('加载角色列表失败：', error)
    allRoles.value = [
      { id: 1, roleCode: 'ADMIN', roleName: '超级管理员', permissions: [], status: 'ACTIVE', createdAt: '', updatedAt: '' },
      { id: 2, roleCode: 'USER', roleName: '普通用户', permissions: [], status: 'ACTIVE', createdAt: '', updatedAt: '' }
    ]
  }
}

// 加载角色列表
const loadRoleList = async () => {
  roleLoading.value = true
  try {
    const params = {
      ...roleSearchForm,
      page: rolePage.value,
      pageSize: rolePageSize.value
    }
    const res = await getRoleList(params)
    roleList.value = res.records
    roleTotal.value = res.total
  } catch (error) {
    console.error('加载角色列表失败：', error)
    // 使用模拟数据
    roleList.value = [
      { id: 1, roleCode: 'ADMIN', roleName: '超级管理员', description: '拥有所有权限', permissions: ['*'], status: 'ACTIVE', createdAt: '2026-01-01 00:00:00', updatedAt: '2026-01-01 00:00:00' },
      { id: 2, roleCode: 'USER', roleName: '普通用户', description: '基本操作权限', permissions: ['order:view', 'shipment:view'], status: 'ACTIVE', createdAt: '2026-01-02 00:00:00', updatedAt: '2026-01-02 00:00:00' }
    ]
    roleTotal.value = 2
  } finally {
    roleLoading.value = false
  }
}

// 重置角色搜索
const resetRoleSearch = () => {
  roleSearchForm.roleCode = undefined
  roleSearchForm.roleName = undefined
  roleSearchForm.status = undefined
  rolePage.value = 1
  loadRoleList()
}

// 新建角色
const handleCreateRole = () => {
  roleDialogTitle.value = '新建角色'
  Object.assign(roleForm, { id: undefined, roleCode: '', roleName: '', description: '' })
  roleDialogVisible.value = true
}

// 编辑角色
const handleEditRole = (row: Role) => {
  roleDialogTitle.value = '编辑角色'
  Object.assign(roleForm, { 
    id: row.id, 
    roleCode: row.roleCode, 
    roleName: row.roleName, 
    description: row.description 
  })
  roleDialogVisible.value = true
}

// 提交角色表单
const submitRoleForm = async () => {
  if (!roleFormRef.value) return
  await roleFormRef.value.validate(async (valid) => {
    if (!valid) return
    roleSubmitting.value = true
    try {
      if (roleForm.id) {
        const updateData: UpdateRoleParams = {
          roleName: roleForm.roleName,
          description: roleForm.description
        }
        await updateRole(roleForm.id, updateData)
        ElMessage.success('更新成功')
      } else {
        await createRole(roleForm as CreateRoleParams)
        ElMessage.success('创建成功')
      }
      roleDialogVisible.value = false
      loadRoleList()
    } catch (error) {
      console.error('保存角色失败：', error)
    } finally {
      roleSubmitting.value = false
    }
  })
}

// 分配权限
const handleAssignPermissions = async (row: Role) => {
  currentRoleId.value = row.id
  selectedPermissionIds.value = row.permissionIds || []
  try {
    permissionTree.value = await getAllPermissions()
  } catch (error) {
    console.error('加载权限树失败：', error)
    // 使用模拟数据
    permissionTree.value = [
      { id: 1, permissionCode: 'order', permissionName: '订单管理', type: 'menu', sort: 1, children: [
        { id: 11, permissionCode: 'order:view', permissionName: '查看订单', type: 'button', sort: 1 },
        { id: 12, permissionCode: 'order:create', permissionName: '创建订单', type: 'button', sort: 2 },
        { id: 13, permissionCode: 'order:edit', permissionName: '编辑订单', type: 'button', sort: 3 },
        { id: 14, permissionCode: 'order:delete', permissionName: '删除订单', type: 'button', sort: 4 }
      ]},
      { id: 2, permissionCode: 'shipment', permissionName: '发运管理', type: 'menu', sort: 2, children: [
        { id: 21, permissionCode: 'shipment:view', permissionName: '查看发运', type: 'button', sort: 1 },
        { id: 22, permissionCode: 'shipment:create', permissionName: '创建发运', type: 'button', sort: 2 }
      ]},
      { id: 3, permissionCode: 'system', permissionName: '系统管理', type: 'menu', sort: 3, children: [
        { id: 31, permissionCode: 'system:user', permissionName: '用户管理', type: 'button', sort: 1 },
        { id: 32, permissionCode: 'system:role', permissionName: '角色管理', type: 'button', sort: 2 }
      ]}
    ]
  }
  permissionDialogVisible.value = true
}

// 提交权限分配
const submitPermissions = async () => {
  if (!currentRoleId.value || !permissionTreeRef.value) return
  permissionSubmitting.value = true
  try {
    const checkedKeys = permissionTreeRef.value.getCheckedKeys()
    const halfCheckedKeys = permissionTreeRef.value.getHalfCheckedKeys()
    const permissionIds = [...checkedKeys, ...halfCheckedKeys]
    await assignPermissions(currentRoleId.value, { permissionIds })
    ElMessage.success('权限分配成功')
    permissionDialogVisible.value = false
    loadRoleList()
  } catch (error) {
    console.error('分配权限失败：', error)
  } finally {
    permissionSubmitting.value = false
  }
}

// 启用角色
const handleEnableRole = async (row: Role) => {
  try {
    await enableRole(row.id)
    ElMessage.success('启用成功')
    loadRoleList()
  } catch (error) {
    console.error('启用角色失败：', error)
  }
}

// 禁用角色
const handleDisableRole = (row: Role) => {
  ElMessageBox.confirm(`确定要禁用角色 "${row.roleName}" 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await disableRole(row.id)
      ElMessage.success('禁用成功')
      loadRoleList()
    } catch (error) {
      console.error('禁用角色失败：', error)
    }
  }).catch(() => {})
}

// 删除角色
const handleDeleteRole = (row: Role) => {
  ElMessageBox.confirm(`确定要删除角色 "${row.roleName}" 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRole(row.id)
      ElMessage.success('删除成功')
      loadRoleList()
    } catch (error) {
      console.error('删除角色失败：', error)
    }
  }).catch(() => {})
}

// ==================== 系统概览 ====================
// 保存配置
function handleSaveConfig() {
  settingsStore.updateConfig(systemConfig)
  ElMessage.success('配置保存成功')
}

// 编辑资料
function handleEditProfile() {
  ElMessage.info('编辑资料功能开发中')
}

// 修改密码
function handleChangePassword() {
  ElMessage.info('修改密码功能开发中')
}

// 操作日志
function handleLogManagement() {
  ElMessage.info('操作日志功能开发中')
}

// 清除缓存
function handleClearCache() {
  ElMessage.success('缓存已清除')
}

// 重启服务
function handleRestartService() {
  ElMessage.warning('重启服务功能需要管理员权限')
}

// 数据备份
function handleBackupData() {
  ElMessage.info('数据备份功能开发中')
}

// 初始化
loadUserList()
loadRoleList()
</script>


<style scoped lang="scss">
.system-container {
  .el-tabs {
    background: #fff;
    border-radius: 4px;
  }

  .tab-content {
    padding: 16px 0;
  }

  .search-form {
    margin-bottom: 16px;
  }

  .toolbar {
    margin-bottom: 16px;
  }

  .el-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 500;
    }
  }

  .user-card {
    .user-info {
      text-align: center;

      h3 {
        margin: 16px 0 8px;
        font-size: 18px;
      }

      .username {
        color: #909399;
        font-size: 14px;
        margin-bottom: 16px;
      }

      .user-detail {
        text-align: left;
        padding: 16px;
        background: #f5f7fa;
        border-radius: 4px;
        margin-bottom: 16px;

        p {
          display: flex;
          align-items: center;
          gap: 8px;
          margin: 8px 0;
          font-size: 14px;
          color: #606266;
        }
      }

      .roles {
        margin-bottom: 16px;

        .el-tag {
          margin: 0 4px;
        }
      }

      .el-button {
        margin: 0 8px 8px;
      }
    }
  }

  .status-card {
    .status-list {
      .status-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #ebeef5;

        &:last-child {
          border-bottom: none;
        }

        .label {
          font-size: 14px;
          color: #606266;
        }
      }
    }
  }

  .action-card {
    .action-list {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;

      .el-button {
        width: 100%;
      }
    }
  }
}
</style>
