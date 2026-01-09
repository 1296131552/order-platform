# 用户注册API文档

> **订单可视化平台 - 用户注册相关接口文档**
>
> **版本**：v1.0
> **创建时间**：2026-01-09
> **作者**：开发组
> **目标读者**：前端开发人员

---

## 📋 目录

- [1. 接口概述](#1-接口概述)
- [2. 自主注册接口](#2-自主注册接口)
- [3. 管理员创建用户接口](#3-管理员创建用户接口)
- [4. 用户审核接口](#4-用户审核接口)
- [5. 登录接口（含审核检查）](#5-登录接口含审核检查)
- [6. 修改密码接口](#6-修改密码接口)
- [7. 错误码说明](#7-错误码说明)
- [8. 前端集成示例](#8-前端集成示例)

---

## 1. 接口概述

### 1.1 接口列表

| 接口名称 | 方法 | 路径 | 说明 |
|---------|------|------|------|
| **自主注册** | POST | `/api/auth/register` | 用户自主注册（需审核） |
| **管理员创建用户** | POST | `/api/user/create` | 管理员创建用户（无需审核） |
| **用户审核** | PUT | `/api/user/audit` | 审核用户注册申请 |
| **登录** | POST | `/api/auth/login` | 用户登录（含审核状态检查） |
| **修改密码** | PUT | `/api/user/password/change` | 修改密码（清除首次登录标记） |

### 1.2 统一响应格式

所有接口返回统一的JSON格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {...},
  "timestamp": "2026-01-09T12:00:00"
}
```

**字段说明**：
- `code`：响应码（200=成功，其他=失败）
- `message`：响应消息
- `data`：响应数据（可选）
- `timestamp`：响应时间戳

---

## 2. 自主注册接口

### 2.1 接口信息

- **接口路径**：`POST /api/auth/register`
- **是否需要登录**：❌ 否
- **功能说明**：用户自主注册，提交后需等待管理员审核

### 2.2 请求参数

**Headers**：
```json
{
  "Content-Type": "application/json"
}
```

**Body（JSON）**：
```json
{
  "username": "zhangsan",
  "password": "Password@123",
  "email": "zhangsan@example.com",
  "phone": "13800138000",
  "realName": "张三",
  "applyRole": "SUPPLIER"
}
```

**参数说明**：

| 参数名 | 类型 | 必填 | 说明 | 验证规则 |
|--------|------|------|------|----------|
| `username` | String | ✅ | 用户名 | 4-20位字母数字下划线 |
| `password` | String | ✅ | 密码 | 8-20位，包含大小写字母、数字 |
| `email` | String | ✅ | 邮箱 | 有效的邮箱格式 |
| `phone` | String | ✅ | 手机号 | 11位手机号 |
| `realName` | String | ✅ | 真实姓名 | 2-20位字符 |
| `applyRole` | String | ❌ | 申请角色 | SUPPLIER/CARRIER/CUSTOMER |

### 2.3 响应示例

**成功响应**：
```json
{
  "code": 200,
  "message": "注册成功，请等待管理员审核",
  "data": {
    "userId": 123,
    "username": "zhangsan",
    "auditStatus": "PENDING"
  },
  "timestamp": "2026-01-09T12:00:00"
}
```

**失败响应**：
```json
{
  "code": 1013,
  "message": "用户名已存在",
  "data": null,
  "timestamp": "2026-01-09T12:00:00"
}
```

### 2.4 前端处理建议

```javascript
// 注册成功提示
if (response.code === 200) {
  showMessage('注册成功，请等待管理员审核', 'success');
  // 跳转到登录页
  router.push('/login');
}

// 注册失败处理
switch (response.code) {
  case 1013: showMessage('用户名已存在，请更换', 'error'); break;
  case 1011: showMessage('邮箱已被注册', 'error'); break;
  case 1012: showMessage('手机号已被注册', 'error'); break;
  case 1006: showMessage(response.message, 'error'); break; // 参数验证失败
  default: showMessage('注册失败：' + response.message, 'error');
}
```

---

## 3. 管理员创建用户接口

### 3.1 接口信息

- **接口路径**：`POST /api/user/create`
- **是否需要登录**：✅ 是（需要管理员权限）
- **功能说明**：管理员创建用户，无需审核，首次登录需改密

### 3.2 请求参数

**Headers**：
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer {token}"
}
```

**Body（JSON）**：
```json
{
  "username": "lisi",
  "password": "123456",
  "email": "lisi@company.com",
  "phone": "13900139000",
  "realName": "李四",
  "roleIds": [1, 3],
  "deptId": 10,
  "remark": "销售部员工"
}
```

**参数说明**：

| 参数名 | 类型 | 必填 | 说明 | 备注 |
|--------|------|------|------|------|
| `username` | String | ✅ | 用户名 | 必须唯一 |
| `password` | String | ❌ | 密码 | 默认123456，首次登录强制改密 |
| `email` | String | ✅ | 邮箱 | 必须唯一 |
| `phone` | String | ✅ | 手机号 | 必须唯一 |
| `realName` | String | ✅ | 真实姓名 | - |
| `roleIds` | Array | ✅ | 角色ID列表 | 至少分配一个角色 |
| `deptId` | Long | ❌ | 部门ID | 可选 |
| `remark` | String | ❌ | 备注 | 可选 |

### 3.3 响应示例

**成功响应**：
```json
{
  "code": 200,
  "message": "用户创建成功",
  "data": {
    "userId": 124,
    "username": "lisi",
    "email": "lisi@company.com",
    "isFirstLogin": 1,
    "auditStatus": "NONE"
  },
  "timestamp": "2026-01-09T12:00:00"
}
```

**失败响应**：
```json
{
  "code": 1013,
  "message": "用户名已存在",
  "data": null,
  "timestamp": "2026-01-09T12:00:00"
}
```

### 3.4 前端处理建议

```javascript
// 创建成功提示
if (response.code === 200) {
  showMessage('用户创建成功，首次登录需要修改密码', 'success');
  // 刷新用户列表
  fetchUserList();
}

// 创建失败处理
if (response.code === 1013) {
  showMessage('用户名已存在，请更换', 'error');
} else if (response.code === 1023) {
  showMessage('请至少分配一个角色', 'error');
}
```

---

## 4. 用户审核接口

### 4.1 接口信息

- **接口路径**：`PUT /api/user/audit`
- **是否需要登录**：✅ 是（需要审核权限）
- **功能说明**：管理员审核用户注册申请

### 4.2 请求参数

**Headers**：
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer {token}"
}
```

**Body（JSON）**：
```json
{
  "userId": 123,
  "auditStatus": "APPROVED",
  "auditRemark": "审核通过",
  "roleIds": [2]
}
```

**参数说明**：

| 参数名 | 类型 | 必填 | 说明 | 可选值 |
|--------|------|------|------|--------|
| `userId` | Long | ✅ | 待审核用户ID | - |
| `auditStatus` | String | ✅ | 审核状态 | APPROVED（通过）/ REJECTED（拒绝） |
| `auditRemark` | String | ❌ | 审核备注 | 拒绝时必填 |
| `roleIds` | Array | ✅ | 分配的角色ID列表 | 通过审核时必填 |

### 4.3 响应示例

**审核通过**：
```json
{
  "code": 200,
  "message": "审核通过",
  "data": {
    "userId": 123,
    "auditStatus": "APPROVED",
    "roleIds": [2]
  },
  "timestamp": "2026-01-09T12:00:00"
}
```

**审核拒绝**：
```json
{
  "code": 200,
  "message": "已拒绝该用户",
  "data": {
    "userId": 123,
    "auditStatus": "REJECTED",
    "auditRemark": "资料不完整"
  },
  "timestamp": "2026-01-09T12:00:00"
}
```

### 4.4 前端处理建议

```javascript
// 审核操作
function auditUser(userId, status, remark, roleIds) {
  if (status === 'REJECTED' && !remark) {
    showMessage('拒绝时必须填写审核备注', 'warning');
    return;
  }

  if (status === 'APPROVED' && (!roleIds || roleIds.length === 0)) {
    showMessage('通过审核时必须分配角色', 'warning');
    return;
  }

  // 调用审核接口...
}
```

---

## 5. 登录接口（含审核检查）

### 5.1 接口信息

- **接口路径**：`POST /api/auth/login`
- **是否需要登录**：❌ 否
- **功能说明**：用户登录，包含审核状态检查和首次登录检测

### 5.2 请求参数

**Headers**：
```json
{
  "Content-Type": "application/json"
}
```

**Body（JSON）**：
```json
{
  "username": "zhangsan",
  "password": "Password@123"
}
```

### 5.3 响应示例

**正常登录**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "userId": 123,
      "username": "zhangsan",
      "realName": "张三",
      "email": "zhangsan@example.com",
      "roles": ["SUPPLIER"]
    },
    "requireChangePassword": false,
    "passwordExpireTime": null
  },
  "timestamp": "2026-01-09T12:00:00"
}
```

**首次登录（需改密）**：
```json
{
  "code": 200,
  "message": "登录成功，请修改密码",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "userId": 124,
      "username": "lisi",
      "realName": "李四"
    },
    "requireChangePassword": true,
    "passwordExpireTime": "2026-01-09T13:00:00"
  },
  "timestamp": "2026-01-09T12:00:00"
}
```

**审核中（无法登录）**：
```json
{
  "code": 1021,
  "message": "账号正在审核中，请耐心等待或联系管理员",
  "data": null,
  "timestamp": "2026-01-09T12:00:00"
}
```

**审核拒绝（无法登录）**：
```json
{
  "code": 1022,
  "message": "账号审核未通过，如需帮助请联系管理员",
  "data": null,
  "timestamp": "2026-01-09T12:00:00"
}
```

### 5.4 前端处理建议

```javascript
// 登录成功处理
if (response.code === 200) {
  const { token, userInfo, requireChangePassword } = response.data;

  // 保存Token
  localStorage.setItem('token', token);
  localStorage.setItem('userInfo', JSON.stringify(userInfo));

  // 检查是否需要改密
  if (requireChangePassword) {
    showMessage('首次登录，请修改密码', 'warning');
    // 跳转到改密页面
    router.push('/password/change');
  } else {
    showMessage('登录成功', 'success');
    // 跳转到首页
    router.push('/dashboard');
  }
}

// 登录失败处理
if (response.code === 1021) {
  showMessage('账号正在审核中，请耐心等待', 'warning');
} else if (response.code === 1022) {
  showMessage('账号审核未通过，请联系管理员', 'error');
} else if (response.code === 1001 || response.code === 1002) {
  showMessage('用户名或密码错误', 'error');
}
```

---

## 6. 修改密码接口

### 6.1 接口信息

- **接口路径**：`PUT /api/user/password/change`
- **是否需要登录**：✅ 是
- **功能说明**：修改密码，首次登录改密后清除 `is_first_login` 标记

### 6.2 请求参数

**Headers**：
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer {token}"
}
```

**Body（JSON）**：
```json
{
  "oldPassword": "123456",
  "newPassword": "NewPassword@123",
  "confirmPassword": "NewPassword@123"
}
```

**参数说明**：

| 参数名 | 类型 | 必填 | 说明 | 验证规则 |
|--------|------|------|------|----------|
| `oldPassword` | String | ✅ | 旧密码 | - |
| `newPassword` | String | ✅ | 新密码 | 8-20位，包含大小写字母、数字 |
| `confirmPassword` | String | ✅ | 确认新密码 | 必须与newPassword一致 |

### 6.3 响应示例

**成功响应**：
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null,
  "timestamp": "2026-01-09T12:00:00"
}
```

**失败响应**：
```json
{
  "code": 1002,
  "message": "旧密码错误",
  "data": null,
  "timestamp": "2026-01-09T12:00:00"
}
```

### 6.4 前端处理建议

```javascript
// 改密成功
if (response.code === 200) {
  showMessage('密码修改成功，请重新登录', 'success');
  // 清除Token
  localStorage.removeItem('token');
  // 跳转到登录页
  router.push('/login');
}

// 改密失败
if (response.code === 1002) {
  showMessage('旧密码错误，请重新输入', 'error');
}
```

---

## 7. 错误码说明

### 7.1 通用错误码

| 错误码 | 说明 | HTTP状态码 |
|--------|------|-----------|
| 200 | 操作成功 | 200 |
| 400 | 请求参数错误 | 400 |
| 401 | 未登录或登录已过期 | 401 |
| 403 | 无权限访问 | 403 |
| 404 | 资源不存在 | 404 |
| 500 | 服务器内部错误 | 500 |

### 7.2 用户相关错误码

| 错误码 | 说明 | 场景 |
|--------|------|------|
| 1001 | 用户不存在 | 登录时用户名错误 |
| 1002 | 密码错误 | 登录时密码错误 |
| 1003 | 用户已存在 | 注册时用户名重复 |
| 1004 | Token无效 | Token格式错误 |
| 1005 | Token已过期 | Token超时 |
| 1006 | 参数验证失败 | 请求参数不符合规则 |
| 1007 | 账户已禁用 | 用户被管理员禁用 |
| 1008 | 账户已锁定 | 密码错误次数过多 |
| 1009 | 密码错误 | 旧密码验证失败 |
| 1010 | 密码已过期 | 密码超过90天未修改 |
| 1011 | 邮箱已存在 | 注册时邮箱重复 |
| 1012 | 手机号已存在 | 注册时手机号重复 |
| 1013 | 用户名已存在 | 注册时用户名重复 |
| 1021 | 账号正在审核中 | PENDING状态用户登录 |
| 1022 | 账号审核未通过 | REJECTED状态用户登录 |
| 1023 | 账号未分配角色 | 用户没有角色 |

---

## 8. 前端集成示例

### 8.1 注册页面示例

```vue
<template>
  <el-form :model="registerForm" :rules="rules" ref="registerForm">
    <el-form-item label="用户名" prop="username">
      <el-input v-model="registerForm.username" placeholder="4-20位字母数字下划线" />
    </el-form-item>

    <el-form-item label="密码" prop="password">
      <el-input v-model="registerForm.password" type="password" placeholder="8-20位，包含大小写字母、数字" />
    </el-form-item>

    <el-form-item label="确认密码" prop="confirmPassword">
      <el-input v-model="registerForm.confirmPassword" type="password" placeholder="再次输入密码" />
    </el-form-item>

    <el-form-item label="邮箱" prop="email">
      <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
    </el-form-item>

    <el-form-item label="手机号" prop="phone">
      <el-input v-model="registerForm.phone" placeholder="11位手机号" />
    </el-form-item>

    <el-form-item label="真实姓名" prop="realName">
      <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" />
    </el-form-item>

    <el-form-item label="申请角色" prop="applyRole">
      <el-select v-model="registerForm.applyRole" placeholder="请选择角色类型">
        <el-option label="供应商" value="SUPPLIER" />
        <el-option label="承运商" value="CARRIER" />
        <el-option label="客户" value="CUSTOMER" />
      </el-select>
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="handleRegister">注册</el-button>
      <el-button @click="handleReset">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
export default {
  data() {
    return {
      registerForm: {
        username: '',
        password: '',
        confirmPassword: '',
        email: '',
        phone: '',
        realName: '',
        applyRole: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { pattern: /^[a-zA-Z0-9_]{4,20}$/, message: '4-20位字母数字下划线', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{8,20}$/,
            message: '8-20位，包含大小写字母、数字', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }
        ]
      }
    };
  },
  methods: {
    async handleRegister() {
      this.$refs.registerForm.validate(async (valid) => {
        if (!valid) return;

        if (this.registerForm.password !== this.registerForm.confirmPassword) {
          this.$message.warning('两次输入的密码不一致');
          return;
        }

        try {
          const response = await this.$axios.post('/api/auth/register', {
            username: this.registerForm.username,
            password: this.registerForm.password,
            email: this.registerForm.email,
            phone: this.registerForm.phone,
            realName: this.registerForm.realName,
            applyRole: this.registerForm.applyRole
          });

          if (response.data.code === 200) {
            this.$message.success('注册成功，请等待管理员审核');
            this.$router.push('/login');
          } else {
            this.$message.error(response.data.message);
          }
        } catch (error) {
          this.$message.error('注册失败：' + error.message);
        }
      });
    },
    handleReset() {
      this.$refs.registerForm.resetFields();
    }
  }
};
</script>
```

### 8.2 登录页面示例

```vue
<template>
  <el-form :model="loginForm" :rules="rules" ref="loginForm">
    <el-form-item label="用户名" prop="username">
      <el-input v-model="loginForm.username" placeholder="请输入用户名" />
    </el-form-item>

    <el-form-item label="密码" prop="password">
      <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" />
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="handleLogin" :loading="loading">登录</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
export default {
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      loading: false,
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    };
  },
  methods: {
    async handleLogin() {
      this.$refs.loginForm.validate(async (valid) => {
        if (!valid) return;

        this.loading = true;
        try {
          const response = await this.$axios.post('/api/auth/login', {
            username: this.loginForm.username,
            password: this.loginForm.password
          });

          if (response.data.code === 200) {
            const { token, userInfo, requireChangePassword } = response.data.data;

            // 保存Token和用户信息
            localStorage.setItem('token', token);
            localStorage.setItem('userInfo', JSON.stringify(userInfo));

            // 检查是否需要改密
            if (requireChangePassword) {
              this.$message.warning('首次登录，请修改密码');
              this.$router.push('/password/change');
            } else {
              this.$message.success('登录成功');
              this.$router.push('/dashboard');
            }
          } else {
            this.$message.error(response.data.message);
          }
        } catch (error) {
          this.$message.error('登录失败：' + error.message);
        } finally {
          this.loading = false;
        }
      });
    }
  }
};
</script>
```

### 8.3 管理员创建用户示例

```vue
<template>
  <el-dialog title="创建用户" :visible.sync="dialogVisible">
    <el-form :model="userForm" :rules="rules" ref="userForm">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="userForm.username" />
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input v-model="userForm.email" />
      </el-form-item>

      <el-form-item label="手机号" prop="phone">
        <el-input v-model="userForm.phone" />
      </el-form-item>

      <el-form-item label="真实姓名" prop="realName">
        <el-input v-model="userForm.realName" />
      </el-form-item>

      <el-form-item label="分配角色" prop="roleIds">
        <el-select v-model="userForm.roleIds" multiple placeholder="请选择角色">
          <el-option v-for="role in roles" :key="role.id" :label="role.name" :value="role.id" />
        </el-select>
      </el-form-item>
    </el-form>

    <span slot="footer">
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleCreate">创建</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  data() {
    return {
      dialogVisible: false,
      userForm: {
        username: '',
        email: '',
        phone: '',
        realName: '',
        roleIds: []
      },
      roles: [],
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        email: [{ required: true, type: 'email', message: '请输入有效的邮箱', trigger: 'blur' }],
        phone: [{ required: true, pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }],
        realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
        roleIds: [{ type: 'array', required: true, message: '请至少分配一个角色', trigger: 'change' }]
      }
    };
  },
  methods: {
    async handleCreate() {
      this.$refs.userForm.validate(async (valid) => {
        if (!valid) return;

        try {
          const response = await this.$axios.post('/api/user/create', this.userForm);

          if (response.data.code === 200) {
            this.$message.success('用户创建成功，首次登录需要修改密码');
            this.dialogVisible = false;
            this.$emit('refresh'); // 刷新用户列表
          } else {
            this.$message.error(response.data.message);
          }
        } catch (error) {
          this.$message.error('创建失败：' + error.message);
        }
      });
    }
  }
};
</script>
```

---

**文档版本**：v1.0
**最后更新**：2026-01-09
**作者**：开发组

**相关文档**：
- [用户注册方案](./用户注册方案.md)
- [已完成功能 - 登录认证](./已完成功能/)
