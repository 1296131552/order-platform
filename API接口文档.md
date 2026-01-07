# 订单可视化平台 - API 接口文档

> **后端 API 接口设计与规范文档**

> 本文档定义了订单可视化数字化管理平台的所有后端 API 接口规范，用于指导前后端开发和联调。

---

## 📋 目录

- [项目信息](#项目信息)
- [接口规范](#接口规范)
- [通用说明](#通用说明)
- [接口列表](#接口列表)
- [修改规范](#修改规范)
- [更新记录](#更新记录)

---

## 项目信息

| 项目 | 信息 |
|------|------|
| **项目名称** | 订单可视化数字化管理平台 |
| **当前版本** | v1.0.0 |
| **基础 URL** | `http://localhost:8080` |
| **文档版本** | v1.0.0 |
| **最后更新** | 2026-01-07 |

---

## 接口规范

### RESTful 风格

本 API 遵循 RESTful 设计风格：

| HTTP 方法 | 用途 | 示例 |
|-----------|------|------|
| **GET** | 查询资源 | `GET /api/order/{id}` |
| **POST** | 创建资源 | `POST /api/order` |
| **PUT** | 全量更新资源 | `PUT /api/order/{id}` |
| **PATCH** | 部分更新资源 | `PATCH /api/order/{id}/status` |
| **DELETE** | 删除资源 | `DELETE /api/order/{id}` |

### URL 设计规范

```
/api/{模块}/{资源}/{操作}
```

**示例**：
- `/api/order/list` - 订单列表
- `/api/order/{id}` - 订单详情
- `/api/order/{id}/cancel` - 取消订单

### 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| URL | 小写，连字符分隔 | `/api/shipment-line` |
| 字段名 | 驼峰命名 | `orderNo`, `customerId` |
| 状态码 | 大写下划线 | `DRAFT`, `IN_TRANSIT` |

---

## 通用说明

### 认证机制

#### JWT Token 认证

1. **登录获取 Token**
   ```bash
   POST /api/auth/login
   ```

2. **后续请求携带 Token**
   ```http
   Authorization: Bearer {token}
   ```

3. **Token 有效期**：7 天（604800000 毫秒）

4. **Token 刷新**：
   - 在过期前调用刷新接口获取新 Token
   - 或者重新登录获取新 Token

#### 无需认证的接口

以下接口无需认证（标注 `认证: 否`）：
- `/api/auth/login` - 用户登录
- `/api/auth/register` - 用户注册（如有）
- 其他公共接口（如有）

### 统一响应格式

#### 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {...},
  "timestamp": "2026-01-07T12:00:00"
}
```

#### 失败响应

```json
{
  "code": 1001,
  "message": "用户不存在",
  "data": null,
  "timestamp": "2026-01-07T12:00:00"
}
```

#### 分页响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [...],     // 数据列表
    "total": 100,         // 总记录数
    "size": 10,           // 每页大小
    "current": 1,         // 当前页码
    "pages": 10           // 总页数
  },
  "timestamp": "2026-01-07T12:00:00"
}
```

### 统一错误码

| 错误码 | 说明 | HTTP状态码 |
|--------|------|-----------|
| 200 | 操作成功 | 200 |
| 400 | 请求参数错误 | 400 |
| 401 | 未认证 | 401 |
| 403 | 无权限 | 403 |
| 404 | 资源不存在 | 404 |
| 500 | 服务器内部错误 | 500 |

#### 业务错误码

| 错误码范围 | 说明 |
|-----------|------|
| 1000-1999 | 用户相关 |
| 2000-2999 | 订单相关 |
| 3000-3999 | 发运相关 |
| 4000-4999 | 签收相关 |
| 5000-5999 | 异常相关 |

详细错误码列表见 [ResponseCode.java](./order-platform-common/src/main/java/com/order/platform/common/enums/ResponseCode.java)

### 分页参数规范

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页大小 |
| sortField | String | 否 | - | 排序字段 |
| sortOrder | String | 否 | - | 排序方向：asc/desc |

---

## 接口列表

> **说明**：以下接口按模块分类，标注 `✅` 为已实现，`⏳` 为待实现。

---

### 1. 认证模块 (Auth)

**基础路径**: `/api/auth`

| 接口 | 方法 | 路径 | 说明 | 认证 | 状态 |
|------|------|------|------|------|------|
| 用户登录 | POST | `/login` | 登录获取 Token | 否 | ⏳ |
| 用户登出 | POST | `/logout` | 登出（删除 Token） | 是 | ⏳ |
| 获取当前用户 | GET | `/user-info` | 获取当前登录用户信息 | 是 | ⏳ |
| 刷新 Token | POST | `/refresh-token` | 刷新 Token 获取新 Token | 是 | ⏳ |
| 修改密码 | PUT | `/change-password` | 修改当前用户密码 | 是 | ⏳ |

#### 1.1 用户登录

**请求**：
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "管理员",
      "email": "admin@example.com",
      "roles": ["ADMIN"]
    }
  }
}
```

---

### 2. 订单模块 (Order)

**基础路径**: `/api/order`

| 接口 | 方法 | 路径 | 说明 | 认证 | 状态 |
|------|------|------|------|------|------|
| 订单列表 | GET | `/list` | 分页查询订单列表 | 是 | ⏳ |
| 订单详情 | GET | `/{id}` | 获取订单详情 | 是 | ⏳ |
| 订单详情（含行） | GET | `/{id}/with-lines` | 获取订单详情及订单行 | 是 | ⏳ |
| 创建订单 | POST | `/create` | 创建新订单 | 是 | ⏳ |
| 更新订单 | PUT | `/{id}` | 更新订单信息 | 是 | ⏳ |
| 删除订单 | DELETE | `/{id}` | 删除订单（逻辑删除） | 是 | ⏳ |
| 更新状态 | PUT | `/{id}/status` | 更新订单状态 | 是 | ⏳ |
| 取消订单 | POST | `/{id}/cancel` | 取消订单 | 是 | ⏳ |
| 完成订单 | POST | `/{id}/complete` | 完成订单 | 是 | ⏳ |
| 订单统计 | GET | `/statistics` | 获取订单统计数据 | 是 | ⏳ |

#### 2.1 订单列表

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页大小，默认 10 |
| orderNo | String | 否 | 订单号（模糊查询） |
| customerId | Long | 否 | 客户ID |
| status | String | 否 | 订单状态 |
| startDate | String | 否 | 开始日期（yyyy-MM-dd） |
| endDate | String | 否 | 结束日期（yyyy-MM-dd） |
| keyword | String | 否 | 关键词搜索 |

**请求示例**：
```http
GET /api/order/list?page=1&pageSize=10&status=DRAFT
Authorization: Bearer {token}
```

#### 2.2 订单状态流转

**状态说明**：
| 状态码 | 说明 | 可转换状态 |
|--------|------|-----------|
| `DRAFT` | 草稿 | EXECUTING, CANCELLED |
| `EXECUTING` | 执行中 | PARTIALLY_RECEIVED, COMPLETED, CANCELLED |
| `PARTIALLY_RECEIVED` | 部分到货 | COMPLETED |
| `COMPLETED` | 已完成 | - |
| `CANCELLED` | 已取消 | - |

---

### 3. 订单行模块 (OrderLine)

**基础路径**: `/api/order-line`

| 接口 | 方法 | 路径 | 说明 | 认证 | 状态 |
|------|------|------|------|------|------|
| 订单行列表 | GET | `/list/{orderId}` | 查询订单的所有订单行 | 是 | ⏳ |
| 订单行详情 | GET | `/{id}` | 获取订单行详情 | 是 | ⏳ |
| 添加订单行 | POST | `/add` | 添加订单行 | 是 | ⏳ |
| 批量添加 | POST | `/batch-add` | 批量添加订单行 | 是 | ⏳ |
| 更新订单行 | PUT | `/update` | 更新订单行信息 | 是 | ⏳ |
| 删除订单行 | DELETE | `/{id}` | 删除订单行 | 是 | ⏳ |
| 更新状态 | PUT | `/{id}/status` | 更新订单行状态 | 是 | ⏳ |
| 总金额 | GET | `/total-amount/{orderId}` | 计算订单总金额 | 是 | ⏳ |
| 下一行号 | GET | `/next-line-no/{orderId}` | 获取下一个行号 | 是 | ⏳ |

---

### 4. 发运模块 (Shipment)

**基础路径**: `/api/shipment`

| 接口 | 方法 | 路径 | 说明 | 认证 | 状态 |
|------|------|------|------|------|------|
| 发运列表 | GET | `/list` | 分页查询发运单列表 | 是 | ⏳ |
| 发运详情 | GET | `/{id}` | 获取发运单详情 | 是 | ⏳ |
| 创建发运单 | POST | `/create` | 创建发运单 | 是 | ⏳ |
| 更新发运单 | PUT | `/{id}` | 更新发运单信息 | 是 | ⏳ |
| 删除发运单 | DELETE | `/{id}` | 删除发运单 | 是 | ⏳ |
| 确认发货 | POST | `/{id}/dispatch` | 确认发货 | 是 | ⏳ |
| 确认到货 | POST | `/{id}/arrive` | 确认到货 | 是 | ⏳ |
| 发运统计 | GET | `/statistics` | 发运统计数据 | 是 | ⏳ |

#### 4.1 发运状态

| 状态 | 说明 |
|------|------|
| `PENDING` | 待提货 |
| `IN_TRANSIT` | 在途 |
| `DELIVERED` | 已到货 |

---

### 5. 快递单模块 (ShipmentLine)

**基础路径**: `/api/shipment-line`

| 接口 | 方法 | 路径 | 说明 | 认证 | 状态 |
|------|------|------|------|------|------|
| 快递单列表 | GET | `/list/{shipmentId}` | 查询发运单的快递单 | 是 | ⏳ |
| 分页查询 | GET | `/list` | 分页查询快递单 | 是 | ⏳ |
| 快递单详情 | GET | `/{id}` | 获取快递单详情 | 是 | ⏳ |
| 添加快递单 | POST | `/add` | 添加快递单 | 是 | ⏳ |
| 批量添加 | POST | `/batch-add` | 批量添加快递单 | 是 | ⏳ |
| 更新快递单 | PUT | `/update` | 更新快递单信息 | 是 | ⏳ |
| 删除快递单 | DELETE | `/{id}` | 删除快递单 | 是 | ⏳ |
| 更新状态 | PUT | `/{id}/status` | 更新快递单状态 | 是 | ⏳ |
| 物流查询 | GET | `/tracking/{trackingNo}` | 根据物流单号查询 | 是 | ⏳ |
| 下一行号 | GET | `/next-line-no/{shipmentId}` | 获取下一个行号 | 是 | ⏳ |

---

### 6. 签收模块 (Receipt)

**基础路径**: `/api/receipt`

| 接口 | 方法 | 路径 | 说明 | 认证 | 状态 |
|------|------|------|------|------|------|
| 签收确认 | POST | `/confirm` | 确认签收 | 是 | ⏳ |
| 批量签收 | POST | `/batch-confirm` | 批量签收确认 | 是 | ⏳ |
| 快递单签收记录 | GET | `/list-by-shipment-line/{shipmentLineId}` | 查询快递单的签收记录 | 是 | ⏳ |
| 发运单签收记录 | GET | `/list-by-shipment/{shipmentId}` | 查询发运单的签收记录 | 是 | ⏳ |
| 差异记录 | GET | `/difference-records` | 查询有差异的签收记录 | 是 | ⏳ |
| 签收详情 | GET | `/{id}` | 获取签收详情 | 是 | ⏳ |

#### 6.1 签收状态

| 状态 | 说明 |
|------|------|
| `PENDING` | 待签收 |
| `RECEIVED` | 已签收 |
| `DIFFERENCE` | 有差异 |
| `PROCESSED` | 已处理 |

---

### 7. 合作方模块 (Partner)

#### 7.1 供应商 (Supplier)

**基础路径**: `/api/supplier`

| 接口 | 方法 | 路径 | 说明 | 认证 | 状态 |
|------|------|------|------|------|------|
| 供应商列表 | GET | `/list` | 分页查询供应商 | 是 | ⏳ |
| 供应商详情 | GET | `/{id}` | 获取供应商详情 | 是 | ⏳ |
| 按编号查询 | GET | `/no/{supplierNo}` | 根据编号查询供应商 | 是 | ⏳ |
| 新增供应商 | POST | `/` | 新增供应商 | 是 | ⏳ |
| 更新供应商 | PUT | `/{id}` | 更新供应商信息 | 是 | ⏳ |
| 删除供应商 | DELETE | `/{id}` | 删除供应商 | 是 | ⏳ |
| 激活供应商 | PUT | `/{id}/activate` | 激活供应商 | 是 | ⏳ |
| 停用供应商 | PUT | `/{id}/deactivate` | 停用供应商 | 是 | ⏳ |

#### 7.2 客户 (Customer)

**基础路径**: `/api/customer`

| 接口 | 方法 | 路径 | 说明 | 认证 | 状态 |
|------|------|------|------|------|------|
| 客户列表 | GET | `/list` | 分页查询客户 | 是 | ⏳ |
| 客户详情 | GET | `/{id}` | 获取客户详情 | 是 | ⏳ |
| 新增客户 | POST | `/` | 新增客户 | 是 | ⏳ |
| 更新客户 | PUT | `/{id}` | 更新客户信息 | 是 | ⏳ |
| 删除客户 | DELETE | `/{id}` | 删除客户 | 是 | ⏳ |

#### 7.3 承运商 (Carrier)

**基础路径**: `/api/carrier`

| 接口 | 方法 | 路径 | 说明 | 认证 | 状态 |
|------|------|------|------|------|------|
| 承运商列表 | GET | `/list` | 分页查询承运商 | 是 | ⏳ |
| 承运商详情 | GET | `/{id}` | 获取承运商详情 | 是 | ⏳ |
| 按编号查询 | GET | `/no/{carrierNo}` | 根据编号查询承运商 | 是 | ⏳ |
| 新增承运商 | POST | `/` | 新增承运商 | 是 | ⏳ |
| 更新承运商 | PUT | `/{id}` | 更新承运商信息 | 是 | ⏳ |
| 删除承运商 | DELETE | `/{id}` | 删除承运商 | 是 | ⏳ |
| 激活承运商 | PUT | `/{id}/activate` | 激活承运商 | 是 | ⏳ |
| 停用承运商 | PUT | `/{id}/deactivate` | 停用承运商 | 是 | ⏳ |

---

### 8. 用户模块 (User)

**基础路径**: `/api/user`

| 接口 | 方法 | 路径 | 说明 | 认证 | 状态 |
|------|------|------|------|------|------|
| 用户列表 | GET | `/list` | 分页查询用户 | 是 | ⏳ |
| 用户详情 | GET | `/{id}` | 获取用户详情 | 是 | ⏳ |
| 新增用户 | POST | `/` | 新增用户 | 是 | ⏳ |
| 更新用户 | PUT | `/{id}` | 更新用户信息 | 是 | ⏳ |
| 删除用户 | DELETE | `/{id}` | 删除用户 | 是 | ⏳ |
| 重置密码 | PUT | `/{id}/reset-password` | 重置用户密码 | 是 | ⏳ |

---

### 9. 角色权限模块 (Role)

**基础路径**: `/api/role`

| 接口 | 方法 | 路径 | 说明 | 认证 | 状态 |
|------|------|------|------|------|------|
| 角色列表 | GET | `/list` | 查询所有角色 | 是 | ⏳ |
| 角色详情 | GET | `/{id}` | 获取角色详情 | 是 | ⏳ |
| 新增角色 | POST | `/` | 新增角色 | 是 | ⏳ |
| 更新角色 | PUT | `/{id}` | 更新角色信息 | 是 | ⏳ |
| 删除角色 | DELETE | `/{id}` | 删除角色 | 是 | ⏳ |
| 分配权限 | POST | `/{id}/assign-permissions` | 为角色分配权限 | 是 | ⏳ |

---

### 10. 异常管理模块 (Exception)

**基础路径**: `/api/exception`

| 接口 | 方法 | 路径 | 说明 | 认证 | 状态 |
|------|------|------|------|------|------|
| 异常列表 | GET | `/list` | 分页查询异常记录 | 是 | ⏳ |
| 异常详情 | GET | `/{id}` | 获取异常详情 | 是 | ⏳ |
| 创建异常 | POST | `/create` | 创建异常记录 | 是 | ⏳ |
| 分配处理人 | PUT | `/{id}/assign` | 分配异常处理人 | 是 | ⏳ |
| 处理异常 | POST | `/{id}/handle` | 处理异常 | 是 | ⏳ |
| 异常统计 | GET | `/statistics` | 异常统计数据 | 是 | ⏳ |

---

### 11. 附件模块 (Attachment)

**基础路径**: `/api/attachment`

| 接口 | 方法 | 路径 | 说明 | 认证 | 状态 |
|------|------|------|------|------|------|
| 上传附件 | POST | `/upload` | 上传单个附件 | 是 | ⏳ |
| 批量上传 | POST | `/batch-upload` | 批量上传附件 | 是 | ⏳ |
| 下载附件 | GET | `/download/{id}` | 下载附件 | 是 | ⏳ |
| 删除附件 | DELETE | `/{id}` | 删除附件 | 是 | ⏳ |
| 附件列表 | GET | `/list/{bizId}` | 查询业务对象的附件 | 是 | ⏳ |

---

## 修改规范

> **重要**：本文档是前后端开发的契约，修改时请严格遵守以下规范。

### 📝 修改前检查清单

- [ ] 确认修改是否影响前端调用
- [ ] 确认是否需要通知前端团队
- [ ] 确认是否需要更新数据库设计
- [ ] 确认是否需要更新 Swagger 注解
- [ ] 更新本文档的"更新记录"

### ✅ 允许的修改

| 修改类型 | 说明 | 示例 |
|---------|------|------|
| **新增接口** | 添加新的 API 接口 | 新增 `/api/order/export` |
| **新增参数** | 在现有接口中新增可选参数 | 订单列表新增日期范围筛选 |
| **Bug 修复** | 修复接口定义错误 | 修正参数类型错误 |
| **优化响应** | 优化响应数据结构 | 新增统计字段 |

### ❌ 禁止的修改

| 修改类型 | 原因 | 替代方案 |
|---------|------|----------|
| **删除接口** | 会破坏前端功能 | 标记为 `@Deprecated` |
| **修改 URL** | 会导致前端调用失败 | 新版本接口使用 v2 前缀 |
| **修改方法名** | RESTful 规范约定 | 遵循 HTTP 方法规范 |
| **修改参数类型** | 类型不兼容会导致调用失败 | 新增参数，保留旧参数 |
| **修改响应结构** | 会破坏前端解析 | 在 data 中新增字段 |

### 🔄 接口版本控制

当需要不兼容修改时，使用版本控制：

```
/api/v1/order/list    # 旧版本
/api/v2/order/list    # 新版本
```

### 📋 新增接口流程

1. **设计接口**
   - 确定 URL 路径
   - 确定请求方法和参数
   - 确定响应格式

2. **添加到本文档**
   - 在对应模块下添加接口条目
   - 标注状态为 `⏳ 待实现`
   - 更新"更新记录"

3. **实现接口**
   - 创建 Controller
   - 实现 Service 逻辑
   - 添加 Swagger 注解

4. **测试验证**
   - 单元测试
   - 接口测试（Postman/Apifox）
   - 前后端联调

5. **更新状态**
   - 将接口状态改为 `✅ 已实现`
   - 更新"更新记录"

### 📝 接口模板

新增接口时，请按以下模板编写：

```markdown
#### X.X 接口名称

**接口**: `HTTP_METHOD /api/module/path`

**说明**: 接口功能描述

**认证**: 是/否

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| param1 | String | 是 | 参数说明 |
| param2 | Integer | 否 | 参数说明 |

**请求示例**:
```http
HTTP_METHOD /api/module/path
Authorization: Bearer {token}

{
  "param1": "value1",
  "param2": 123
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {...}
}
```

**状态说明**:
| 状态 | 说明 |
|------|------|
| 状态1 | 说明 |
| 状态2 | 说明 |
```

---

## 更新记录

### v1.0.0 (2026-01-07)

#### 新增内容

- ✅ 创建 API 接口文档框架
- ✅ 定义 RESTful 接口规范
- ✅ 定义统一响应格式
- ✅ 定义错误码规范
- ✅ 定义 11 个功能模块的接口列表

#### 待实现

- ⏳ 认证模块（5个接口）
- ⏳ 订单模块（10个接口）
- ⏳ 订单行模块（9个接口）
- ⏳ 发运模块（8个接口）
- ⏳ 快递单模块（10个接口）
- ⏳ 签收模块（6个接口）
- ⏳ 合作方模块（供应商、客户、承运商）
- ⏳ 用户模块（6个接口）
- ⏳ 角色权限模块（6个接口）
- ⏳ 异常管理模块（6个接口）
- ⏳ 附件模块（5个接口）

---

## 附录

### A. Swagger/Knife4j 文档

项目集成了 Knife4j，启动后可访问在线 API 文档：

```
http://localhost:8080/doc.html
```

### B. 状态码汇总

#### 订单状态

| 状态码 | 说明 |
|--------|------|
| `DRAFT` | 草稿 |
| `EXECUTING` | 执行中 |
| `PARTIALLY_RECEIVED` | 部分到货 |
| `COMPLETED` | 已完成 |
| `CANCELLED` | 已取消 |

#### 发运状态

| 状态码 | 说明 |
|--------|------|
| `PENDING` | 待提货 |
| `IN_TRANSIT` | 在途 |
| `DELIVERED` | 已到货 |

#### 签收状态

| 状态码 | 说明 |
|--------|------|
| `PENDING` | 待签收 |
| `RECEIVED` | 已签收 |
| `DIFFERENCE` | 有差异 |
| `PROCESSED` | 已处理 |

### C. 使用示例

#### cURL 示例

```bash
# 1. 登录获取 Token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 2. 查询订单列表
curl -X GET "http://localhost:8080/api/order/list?page=1&pageSize=10" \
  -H "Authorization: Bearer {token}"

# 3. 创建订单
curl -X POST http://localhost:8080/api/order/create \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "orderNo": "ORD20260107001",
    "customerId": 1,
    "totalAmount": 10000.00,
    "statusCode": "DRAFT"
  }'
```

#### JavaScript/Axios 示例

```javascript
import axios from 'axios';

const BASE_URL = 'http://localhost:8080';
let token = '';

// 设置请求拦截器
axios.interceptors.request.use(config => {
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 1. 登录
async function login(username, password) {
  const response = await axios.post(`${BASE_URL}/api/auth/login`, {
    username,
    password
  });
  token = response.data.data.token;
  return response.data;
}

// 2. 查询订单列表
async function getOrderList(params) {
  const response = await axios.get(`${BASE_URL}/api/order/list`, {
    params
  });
  return response.data;
}

// 3. 创建订单
async function createOrder(orderData) {
  const response = await axios.post(`${BASE_URL}/api/order/create`, orderData);
  return response.data;
}
```

### D. 项目模块结构

```
order-platform-backend/
├── order-platform-api/              # API 入口模块
├── order-platform-common/           # 公共模块
├── order-platform-order/            # 订单模块
├── order-platform-shipment/         # 发运模块（含签收）
├── order-platform-partner/          # 合作方模块（供应商/客户/承运商）
├── order-platform-user/             # 用户模块
├── order-platform-attachment/       # 附件模块
├── order-platform-visualization/    # 可视化模块
├── order-platform-dashboard/        # 看板模块
└── order-platform-exception/        # 异常管理模块
```

### E. 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.x | 后端框架 |
| JDK | 21 | Java 版本 |
| MySQL | 8.0+ | 数据库 |
| MyBatis Plus | 3.5.x | ORM 框架 |
| Knife4j | 4.x | API 文档 |
| JWT | 0.12.3 | Token 认证 |
| Hutool | 5.8.x | 工具类库 |

---

## 联系方式

如有问题或建议，请联系开发团队。
