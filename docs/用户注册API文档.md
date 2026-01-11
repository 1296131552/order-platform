# 用户注册 API 文档

> **订单可视化平台 - 用户注册相关接口文档**
>
> **版本**：v2.2
> **创建时间**：2026-01-09
> **最后更新**：2026-01-10
> **作者**：开发组
> **目标读者**：前端开发人员

---

## v2.2 重要更新

> **邮箱和手机号二选一必填**：降低注册门槛，支持只有邮箱或只有手机号的用户
>
> **三种登录方式**：支持用户名、邮箱、手机号登录（后端自动识别）
>
> **注册申请管理**：新增注册申请列表、审核接口

---

## 目录

- [1. 快速开始](#1-快速开始)
- [2. 统一响应格式](#2-统一响应格式)
- [3. 自主注册接口](#3-自主注册接口)
- [4. 登录接口](#4-登录接口)
- [5. 注册申请管理接口](#5-注册申请管理接口)
- [6. 管理员创建用户接口](#6-管理员创建用户接口)
- [7. 修改密码接口](#7-修改密码接口)
- [8. 错误码说明](#8-错误码说明)
- [9. 前端集成示例](#9-前端集成示例)

---

## 1. 快速开始

### 1.1 接口概览

| 接口名称 | 方法 | 路径 | 是否需要登录 |
|---------|------|------|-------------|
| 自主注册 | POST | `/api/auth/register` | ❌ |
| 登录 | POST | `/api/auth/login` | ❌ |
| 注册申请列表 | GET | `/api/registration/list` | ✅ 管理员 |
| 审核通过 | PUT | `/api/registration/{id}/approve` | ✅ 管理员 |
| 审核拒绝 | PUT | `/api/registration/{id}/reject` | ✅ 管理员 |
| 管理员创建用户 | POST | `/api/user/create` | ✅ 管理员 |
| 修改密码 | PUT | `/api/user/password/change` | ✅ |

### 1.2 核心规则说明

#### 邮箱和手机号二选一

```
注册时 email 和 phone 至少填写一个

支持场景：
├── 只填邮箱  → 国外用户、企业用户
├── 只填手机号 → 个体司机、承运商
└── 两者都填  → 内部员工
```

#### 三种登录方式

```
登录接口会自动识别账号类型：
├── 用户名：zhangsan
├── 邮箱：zhangsan@example.com
└── 手机号：13800138000

前端无需判断，直接传值即可
```

---

## 2. 统一响应格式

### 2.1 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    // 业务数据
  },
  "timestamp": "2026-01-10T12:00:00"
}
```

### 2.2 失败响应

```json
{
  "code": 1013,
  "message": "用户名已存在",
  "data": null,
  "timestamp": "2026-01-10T12:00:00"
}
```

### 2.3 分页响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [],     // 数据列表
    "total": 100,      // 总记录数
    "size": 10,        // 每页大小
    "current": 1,      // 当前页码
    "pages": 10        // 总页数
  },
  "timestamp": "2026-01-10T12:00:00"
}
```

### 2.4 HTTP状态码说明

| HTTP状态码 | 说明 |
|-----------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或Token过期 |
| 403 | 无权限访问 |
| 500 | 服务器内部错误 |

---

## 3. 自主注册接口

### 3.1 接口信息

| 项目 | 内容 |
|------|------|
| **接口地址** | `/api/auth/register` |
| **请求方式** | POST |
| **Content-Type** | application/json |
| **是否需要登录** | 否 |

### 3.2 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| username | String | ✅ | 用户名（4-20位字母数字下划线） | zhangsan |
| password | String | ✅ | 密码（6-20位） | Abc123@ |
| email | String | ⚠️ | 邮箱（与phone二选一） | zhang@example.com |
| phone | String | ⚠️ | 手机号（与email二选一） | 13800138000 |
| realName | String | ❌ | 真实姓名 | 张三 |
| employeeNo | String | ❌ | 工号 | E001 |
| position | String | ❌ | 职位 | 采购员 |
| applyRole | String | ❌ | 申请角色代码 | SUPPLIER |
| applyReason | String | ❌ | 申请原因 | 供应商注册 |

### 3.3 请求示例

```json
{
  "username": "zhangsan",
  "password": "Abc123@",
  "email": "zhangsan@example.com",
  "realName": "张三",
  "applyRole": "SUPPLIER",
  "applyReason": "供应商申请注册"
}
```

### 3.4 响应示例（成功）

```json
{
  "code": 200,
  "message": "注册成功，请等待管理员审核",
  "data": {
    "registrationId": 1,
    "username": "zhangsan",
    "auditStatus": "PENDING",
    "createdAt": "2026-01-10T12:00:00"
  },
  "timestamp": "2026-01-10T12:00:00"
}
```

### 3.5 响应示例（失败）

```json
{
  "code": 1013,
  "message": "用户名已存在",
  "data": null,
  "timestamp": "2026-01-10T12:00:00"
}
```

### 3.6 前端校验规则

```javascript
// 用户名：4-20位，字母数字下划线
const usernamePattern = /^[a-zA-Z0-9_]{4,20}$/;

// 邮箱格式
const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// 手机号格式
const phonePattern = /^1[3-9]\d{9}$/;

// 密码强度：6-20位，至少满足3项（大小写字母、数字、特殊字符）
const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{6,20}$/;

// 二选一校验
if (!email && !phone) {
  return '邮箱和手机号至少填写一个';
}
```

---

## 4. 登录接口

### 4.1 接口信息

| 项目 | 内容 |
|------|------|
| **接口地址** | `/api/auth/login` |
| **请求方式** | POST |
| **Content-Type** | application/json |
| **是否需要登录** | 否 |

> **重要**：支持用户名、邮箱、手机号三种方式登录，后端自动识别

### 4.2 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| account | String | ✅ | 账号（用户名/邮箱/手机号） | zhangsan 或 zhang@example.com 或 13800138000 |
| password | String | ✅ | 密码 | Abc123@ |

### 4.3 请求示例

```json
{
  "account": "zhangsan",
  "password": "Abc123@"
}
```

### 4.4 响应示例（成功）

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenExpireTime": "2026-01-17T12:00:00",
    "user": {
      "id": 1,
      "username": "zhangsan",
      "realName": "张三",
      "email": "zhangsan@example.com",
      "phone": "13800138000",
      "avatar": null,
      "auditStatus": "APPROVED"
    },
    "requireChangePassword": false
  },
  "timestamp": "2026-01-10T12:00:00"
}
```

### 4.5 响应字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| token | String | JWT令牌，后续请求需在Header中携带 |
| tokenExpireTime | String | Token过期时间（7天后） |
| user | Object | 用户信息 |
| requireChangePassword | Boolean | 是否强制修改密码（管理员创建的用户为true） |

### 4.6 响应示例（失败）

```json
{
  "code": 1012,
  "message": "账号正在审核中，请耐心等待",
  "data": null,
  "timestamp": "2026-01-10T12:00:00"
}
```

```json
{
  "code": 1012,
  "message": "账号审核未通过，请重新注册或联系管理员",
  "data": null,
  "timestamp": "2026-01-10T12:00:00"
}
```

```json
{
  "code": 1012,
  "message": "密码错误",
  "data": null,
  "timestamp": "2026-01-10T12:00:00"
}
```

### 4.7 前端Token使用

```javascript
// 登录成功后，存储Token
localStorage.setItem('token', response.data.token);

// 后续请求携带Token
axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
```

---

## 5. 注册申请管理接口

### 5.1 注册申请列表

#### 接口信息

| 项目 | 内容 |
|------|------|
| **接口地址** | `/api/registration/list` |
| **请求方式** | GET |
| **是否需要登录** | 是（管理员权限） |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| current | Integer | ❌ | 当前页码，默认1 | 1 |
| size | Integer | ❌ | 每页大小，默认10 | 10 |
| auditStatus | String | ❌ | 审核状态筛选 | PENDING/APPROVED/REJECTED |
| username | String | ❌ | 用户名模糊搜索 | zhang |
| source | String | ❌ | 来源筛选 | SELF_REGISTER/ADMIN_CREATE |

#### 请求示例

```
GET /api/registration/list?current=1&size=10&auditStatus=PENDING
```

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "zhangsan",
        "email": "zhangsan@example.com",
        "phone": "13800138000",
        "realName": "张三",
        "applyRole": "SUPPLIER",
        "applyReason": "供应商申请注册",
        "source": "SELF_REGISTER",
        "auditStatus": "PENDING",
        "createdAt": "2026-01-10T10:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": "2026-01-10T12:00:00"
}
```

---

### 5.2 审核通过

#### 接口信息

| 项目 | 内容 |
|------|------|
| **接口地址** | `/api/registration/{id}/approve` |
| **请求方式** | PUT |
| **Content-Type** | application/json |
| **是否需要登录** | 是（管理员权限） |

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| id | Long | ✅ | 注册申请ID | 1 |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| roleCode | String | ❌ | 分配的角色代码 | SUPPLIER |
| auditRemark | String | ❌ | 审核备注 | 审核通过 |

#### 请求示例

```json
{
  "roleCode": "SUPPLIER",
  "auditRemark": "审核通过"
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "审核通过",
  "data": {
    "userId": 1,
    "username": "zhangsan",
    "roleCode": "SUPPLIER"
  },
  "timestamp": "2026-01-10T12:00:00"
}
```

---

### 5.3 审核拒绝

#### 接口信息

| 项目 | 内容 |
|------|------|
| **接口地址** | `/api/registration/{id}/reject` |
| **请求方式** | PUT |
| **Content-Type** | application/json |
| **是否需要登录** | 是（管理员权限） |

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| id | Long | ✅ | 注册申请ID | 1 |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| auditRemark | String | ✅ | 拒绝原因 | 资料不完整 |

#### 请求示例

```json
{
  "auditRemark": "资料不完整，请补充完整后重新提交"
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "已拒绝该申请",
  "data": null,
  "timestamp": "2026-01-10T12:00:00"
}
```

---

## 6. 管理员创建用户接口

### 6.1 接口信息

| 项目 | 内容 |
|------|------|
| **接口地址** | `/api/user/create` |
| **请求方式** | POST |
| **Content-Type** | application/json |
| **是否需要登录** | 是（管理员权限） |

> **说明**：管理员创建的用户无需审核，可直接登录，但首次登录需修改密码

### 6.2 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| username | String | ✅ | 用户名 | lisi |
| email | String | ⚠️ | 邮箱（与phone二选一） | lisi@example.com |
| phone | String | ⚠️ | 手机号（与email二选一） | 13900139000 |
| realName | String | ❌ | 真实姓名 | 李四 |
| employeeNo | String | ❌ | 工号 | E002 |
| position | String | ❌ | 职位 | 采购主管 |
| roles | Array | ❌ | 角色代码列表 | ["HR_ADMIN"] |

### 6.3 请求示例

```json
{
  "username": "lisi",
  "email": "lisi@example.com",
  "phone": "13900139000",
  "realName": "李四",
  "employeeNo": "E002",
  "position": "采购主管",
  "roles": ["HR_ADMIN"]
}
```

### 6.4 响应示例

```json
{
  "code": 200,
  "message": "用户创建成功",
  "data": {
    "userId": 2,
    "username": "lisi",
    "email": "lisi@example.com",
    "defaultPassword": "123456",
    "requireFirstLoginChange": true
  },
  "timestamp": "2026-01-10T12:00:00"
}
```

### 6.5 前端提示

```
创建成功！
用户名：lisi
默认密码：123456
请告知用户首次登录后需修改密码
```

---

## 7. 修改密码接口

### 7.1 接口信息

| 项目 | 内容 |
|------|------|
| **接口地址** | `/api/user/password/change` |
| **请求方式** | PUT |
| **Content-Type** | application/json |
| **是否需要登录** | 是 |

### 7.2 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| oldPassword | String | ✅ | 旧密码 | 123456 |
| newPassword | String | ✅ | 新密码 | Abc123@ |

### 7.3 请求示例

```json
{
  "oldPassword": "123456",
  "newPassword": "Abc123@"
}
```

### 7.4 响应示例

```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null,
  "timestamp": "2026-01-10T12:00:00"
}
```

### 7.5 响应示例（失败）

```json
{
  "code": 1015,
  "message": "旧密码不正确",
  "data": null,
  "timestamp": "2026-01-10T12:00:00"
}
```

---

## 8. 错误码说明

### 8.1 通用错误码

| 错误码 | 说明 | HTTP状态码 |
|--------|------|-----------|
| 200 | 操作成功 | 200 |
| 1000 | 系统错误 | 500 |
| 1001 | 参数错误 | 400 |
| 1002 | 未登录 | 401 |
| 1003 | 无权限 | 403 |

### 8.2 用户相关错误码

| 错误码 | 说明 | 建议前端提示 |
|--------|------|-------------|
| 1011 | 用户不存在 | "账号不存在" |
| 1012 | 密码错误 | "密码错误" |
| 1013 | 用户名已存在 | "用户名已存在" |
| 1014 | 邮箱已被使用 | "邮箱已被使用" |
| 1015 | 手机号已被使用 | "手机号已被使用" |
| 1016 | 旧密码不正确 | "旧密码不正确" |
| 1017 | 账号正在审核中 | "账号正在审核中，请耐心等待" |
| 1018 | 账号审核未通过 | "账号审核未通过，请重新注册或联系管理员" |
| 1019 | 邮箱和手机号至少填写一个 | "邮箱和手机号至少填写一个" |
| 1020 | 密码强度不足 | "密码强度不足，请使用6-20位包含大小写字母、数字或特殊字符的密码" |

---

## 9. 前端集成示例

### 9.1 注册表单校验（Vue 3）

```vue
<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const form = reactive({
  username: '',
  password: '',
  email: '',
  phone: '',
  realName: '',
  applyRole: '',
  applyReason: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{4,20}$/, message: '用户名为4-20位字母数字下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  // 邮箱和手机号二选一校验
  email: [
    {
      validator: (rule, value, callback) => {
        if (!value && !form.phone) {
          callback(new Error('邮箱和手机号至少填写一个'))
        } else if (value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
          callback(new Error('邮箱格式不正确'))
        } else {
          // 清除手机号的错误
          formRef.value?.clearValidate('phone')
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  phone: [
    {
      validator: (rule, value, callback) => {
        if (!value && !form.email) {
          callback(new Error('邮箱和手机号至少填写一个'))
        } else if (value && !/^1[3-9]\d{9}$/.test(value)) {
          callback(new Error('手机号格式不正确'))
        } else {
          // 清除邮箱的错误
          formRef.value?.clearValidate('email')
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleSubmit = async () => {
  try {
    const response = await axios.post('/api/auth/register', form)
    ElMessage.success('注册成功，请等待管理员审核')
    // 跳转到等待审核页面
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '注册失败')
  }
}
</script>
```

### 9.2 登录示例（Vue 3）

```vue
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loginForm = reactive({
  account: '',
  password: ''
})

const handleLogin = async () => {
  try {
    const response = await axios.post('/api/auth/login', loginForm)

    // 存储Token
    localStorage.setItem('token', response.data.data.token)
    localStorage.setItem('user', JSON.stringify(response.data.data.user))

    // 设置axios默认header
    axios.defaults.headers.common['Authorization'] = `Bearer ${response.data.data.token}`

    // 检查是否需要强制修改密码
    if (response.data.data.requireChangePassword) {
      router.push('/change-password')
    } else {
      router.push('/dashboard')
    }

    ElMessage.success('登录成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '登录失败')
  }
}
</script>
```

### 9.3 Axios拦截器配置

```javascript
// request拦截器
axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// response拦截器
axios.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response?.status === 401) {
      // Token过期，跳转登录页
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)
```

### 9.4 审核列表页面示例

```vue
<script setup>
import { ref, onMounted } from 'vue'

const tableData = ref([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 获取列表
const fetchList = async () => {
  try {
    const response = await axios.get('/api/registration/list', {
      params: {
        current: pagination.current,
        size: pagination.size,
        auditStatus: 'PENDING'
      }
    })
    tableData.value = response.data.records
    pagination.total = response.data.total
  } catch (error) {
    ElMessage.error('获取列表失败')
  }
}

// 审核通过
const handleApprove = async (id) => {
  try {
    await axios.put(`/api/registration/${id}/approve`, {
      roleCode: 'SUPPLIER',
      auditRemark: '审核通过'
    })
    ElMessage.success('审核通过')
    fetchList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 审核拒绝
const handleReject = async (id) => {
  try {
    await axios.put(`/api/registration/${id}/reject`, {
      auditRemark: '资料不完整'
    })
    ElMessage.success('已拒绝')
    fetchList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchList()
})
</script>
```

---

## 附录

### A. 角色代码列表

| 角色代码 | 角色名称 | 说明 |
|---------|---------|------|
| SUPER_ADMIN | 超级管理员 | 拥有所有权限 |
| HR_ADMIN | 人事管理员 | 人事管理权限 |
| DEPT_ADMIN | 部门管理员 | 部门管理权限 |
| SUPPLIER | 供应商 | 供应商权限 |
| CARRIER | 承运商 | 承运商权限 |
| CUSTOMER | 客户 | 客户权限 |

### B. 审核状态说明

| 状态 | 说明 | 可登录 |
|------|------|--------|
| PENDING | 待审核 | 否 |
| APPROVED | 审核通过 | 是 |
| REJECTED | 审核拒绝 | 否 |
| NONE | 无需审核 | 是（管理员创建用户） |

### C. 注册来源说明

| 来源 | 说明 | 是否需要审核 |
|------|------|-------------|
| SELF_REGISTER | 自主注册 | 是 |
| ADMIN_CREATE | 管理员创建 | 否 |
| INVITE_CODE | 邀请码注册 | 否 |
| BATCH_IMPORT | 批量导入 | 否 |

---

## 版本历史

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| **v2.2** | **2026-01-10** | **邮箱和手机号二选一必填**；支持三种登录方式；新增注册申请管理接口 |
| v2.1 | 2026-01-10 | 管理员创建用户接口 |
| v2.0 | 2026-01-10 | 初始版本 |

---

**文档版本**：v2.2
**最后更新**：2026-01-10
**作者**：开发组

**相关文档**：
- [用户注册方案](./用户注册方案.md)
- [数据库设计文档](../数据库/0.数据库设计文档.md)
