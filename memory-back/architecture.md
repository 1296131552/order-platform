# 架构文档

## 项目结构

```
order-platform/
├── order-platform-backend/      # 后端项目
│   ├── order-platform-api          # API启动模块（入口）
│   ├── order-platform-common       # 公共模块（共享能力）
│   ├── order-platform-order        # 订单聚合模块
│   ├── order-platform-shipment     # 发运聚合模块
│   ├── order-platform-partner      # 合作方聚合模块
│   ├── order-platform-dashboard    # 看板聚合模块
│   ├── order-platform-attachment   # 附件聚合模块
│   ├── order-platform-exception    # 异常聚合模块
│   ├── order-platform-user         # 用户聚合模块
│   └── order-platform-visualization # 可视化聚合模块
└── order-platform-frontend/     # 前端项目
    └── src/
        ├── api/           # API 接口定义
        ├── assets/        # 静态资源
        ├── components/    # 公共组件
        ├── router/        # 路由配置
        ├── stores/        # Pinia 状态管理
        ├── styles/        # 全局样式
        ├── types/         # TypeScript 类型
        ├── utils/         # 工具函数
        └── views/         # 页面组件
```

---

## 后端模块结构

```
com.company.order.visual
├── order-platform-api          # API启动模块（入口）
├── order-platform-common       # 公共模块（共享能力）
├── order-platform-order        # 订单聚合模块
├── order-platform-shipment     # 发运聚合模块
├── order-platform-partner      # 合作方聚合模块
├── order-platform-dashboard    # 看板聚合模块
├── order-platform-attachment   # 附件聚合模块
├── order-platform-exception    # 异常聚合模块
├── order-platform-user         # 用户聚合模块
└── order-platform-visualization # 可视化聚合模块
```

## 依赖规则

```
                    ┌─────────────────────────┐
                    │   order-platform-api    │
                    └────────────┬─────────────┘
                                 │
                    ┌────────────▼─────────────┐
                    │  order-platform-common   │
                    └────────────┬─────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         │                       │                       │
┌────────▼─────┐    ┌───────────▼──────┐    ┌────────▼────────┐
│ order-platform│    │ order-platform   │    │ order-platform  │
│   -order      │    │  -shipment       │    │   -partner     │
└───────────────┘    └──────────────────┘    └─────────────────┘
         ...                        其他聚合模块
```

**规则**：
1. Common 不依赖任何业务模块
2. 各业务模块互不依赖
3. Dashboard / Visualization 可调用核心聚合模块

---

## 核心文件说明

### 公共模块 (order-platform-common)

| 文件 | 职责 | 关键设计 |
|------|------|----------|
| `Result.java` | 统一响应封装 | 泛型 `<T>`，时间戳自动生成 |
| `ResponseCode.java` | 响应码枚举 | 分模块码段（1xxx-订单，2xxx-发运，3xxx-合作方等） |
| `BusinessException.java` | 业务异常 | 携带 code 字段，支持 ResponseCode 构造 |
| `GlobalExceptionHandler.java` | 全局异常处理 | `@RestControllerAdvice`，提取 `extractFieldErrors()` 消除重复 |
| `OrderStatus.java` | 订单状态枚举 | `@EnumValue` 统一存储策略 |
| `PartnerType.java` | 合作方类型枚举 | `@EnumValue` 统一存储策略 |
| `ShipmentStatus.java` | 发运状态枚举 | `@EnumValue` 统一存储策略 |
| `ShipmentLineStatus.java` | 快递单状态枚举 | `@EnumValue` 统一存储策略，明确 DELIVERED vs RECEIVED |

### 订单聚合 (order-platform-order)

| 文件 | 职责 | 关键设计 |
|------|------|----------|
| `Order.java` | 订单实体 | `OrderStatus status` 类型安全 |
| `OrderLine.java` | 订单行实体 | `OrderStatus status` 类型安全 |

### 发运聚合 (order-platform-shipment)

| 文件 | 职责 | 关键设计 |
|------|------|----------|
| `Shipment.java` | 发运批次实体 | `ShipmentStatus status` 类型安全 |
| `ShipmentLine.java` | 快递单实体 | `ShipmentLineStatus status` 类型安全 |
| `ReceiptDetail.java` | 签收明细实体 | 记录签收数量和差异 |

### 合作方聚合 (order-platform-partner)

| 文件 | 职责 | 关键设计 |
|------|------|----------|
| `Partner.java` | 合作方统一表 | `PartnerType` 区分供应商/承运商/客户 |

---

## 枚举存储策略（重要）

所有状态枚举统一采用以下模式：

```java
public enum OrderStatus {
    DRAFT("draft", "草稿");

    @EnumValue      // MyBatis-Plus：数据库存储此值
    private final String value;

    @JsonValue      // Jackson：API 序列化返回此值
    public String getValue() { return value; }
}
```

**效果**：
- 数据库存：`draft`（小写）
- API 返回：`{ "status": "draft" }`
- Java 代码：`order.setStatus(OrderStatus.DRAFT)`

**优势**：一次定义，数据库和 API 统一使用，无转换层。

---

## 状态流转设计

### 订单状态
```
DRAFT → EXECUTING → PARTIALLY_RECEIVED → COMPLETED → ARCHIVED
```

### 快递单状态（ShipmentLineStatus）
```
CREATED → PICKED_UP → IN_TRANSIT → DELIVERED → RECEIVED
```

**关键区分**：
- `DELIVERED`：快递员已送达（放在门口/快递柜），快递公司责任完成
- `RECEIVED`：收货人已签收，触发签收流程，记录签收数量和差异

---

## API 响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { /* 业务数据 */ },
  "timestamp": 1705334400000
}
```

**统一方法**：
- `Result.ok()` / `Result.ok(data)` - 成功
- `Result.fail(message)` / `Result.fail(code, message)` / `Result.fail(ResponseCode)` - 失败

---

## 前端模块结构

### 目录说明

| 目录 | 用途 |
|------|------|
| `src/api/` | API 接口定义（按业务模块划分） |
| `src/assets/` | 静态资源（图片、字体等） |
| `src/components/` | 公共组件（可复用组件） |
| `src/router/` | 路由配置 |
| `src/stores/` | Pinia 状态管理 |
| `src/styles/` | 全局样式 |
| `src/types/` | TypeScript 类型定义 |
| `src/utils/` | 工具函数 |
| `src/views/` | 页面组件 |

### 核心文件说明

#### 入口与配置

| 文件 | 职责 | 关键设计 |
|------|------|----------|
| `src/main.ts` | 应用入口 | **插件顺序重要**：pinia → router → ElementPlus |
| `src/App.vue` | 根组件 | `<router-view />` 路由出口 |
| `vite.config.ts` | Vite 配置 | 代理 `/api` → `http://localhost:8080/api`（后端 context-path=/api） |
| `tsconfig.app.json` | TS 配置 | 路径别名 `@/*` → `src/*` |

#### 状态管理 (stores/)

| 文件 | 职责 | 关键设计 |
|------|------|----------|
| `stores/user.ts` | 用户状态 | `isLoggedIn` 为 computed，是 token 的投影（非独立状态） |
| `stores/app.ts` | 应用全局状态 | `sidebarCollapsed` 侧边栏状态 |

#### 路由 (router/)

| 文件 | 职责 | 关键设计 |
|------|------|----------|
| `router/index.ts` | 路由配置 | 公开路由（meta.public=true）vs 受保护路由，路由守卫检查登录状态 |

#### 工具函数 (utils/)

| 文件 | 职责 | 关键设计 |
|------|------|----------|
| `utils/request.ts` | Axios 封装 | 统一错误处理（401/403/404/500），请求拦截器注入 token |

#### 类型定义 (types/)

| 文件 | 职责 | 关键设计 |
|------|------|----------|
| `types/api.ts` | API 类型 | `ApiResponse<T>` 与后端 Result<T> 对接，状态常量对象 + 类型分离 |

#### 样式 (styles/)

| 文件 | 职责 | 关键设计 |
|------|------|----------|
| `styles/index.scss` | 全局样式 | 精确 CSS 重置（html, body），避免通配符 `*` |

#### 视图 (views/)

| 文件 | 职责 | 关键设计 |
|------|------|----------|
| `views/HomeView.vue` | 首页 | 数据驱动导航（navItems 数组 + 单一 navigateTo 函数） |
| `views/LoginView.vue` | 登录页 | FormRules 表单验证，skipLogin 仅开发环境 |
| `views/NotFoundView.vue` | 404 页面 | 友好的错误提示和返回首页按钮 |
| `views/DashboardView.vue` | 数据看板 | TODO: 接入真实 KPI 数据 |

### 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5.24 | 前端框架 |
| TypeScript | 5.9 | 类型系统 |
| Vite | 7.2.4 | 构建工具 |
| Element Plus | 2.13.1 | UI 组件库 |
| Vue Router | 4.6.4 | 路由管理 |
| Pinia | 3.0.4 | 状态管理 |
| Axios | 1.7.9 | HTTP 客户端 |
| Dayjs | - | 日期处理 |
| Sass | 1.97.2 | CSS 预处理 |

### 前端关键设计原则

1. **类型安全优先**
   - 避免 `any` 类型，使用 `Component` 或具体类型
   - 状态常量对象与类型定义分离（`OrderStatusValues` + `OrderStatus`）

2. **数据驱动**
   - 导航项用数组配置，不重复函数
   - `isLoggedIn` 是 computed，不是 ref

3. **环境区分**
   - `import.meta.env.DEV` 用 computed 包装（模板不能直接使用）
   - skipLogin 仅开发环境显示

4. **插件初始化顺序**
   ```
   pinia → router → ElementPlus
   ```
   因为 router 守卫会使用 userStore

5. **API 代理配置**
   ```
   前端：/api/xxx
   代理到：http://localhost:8080/api/xxx
   后端：context-path=/api，Controller=@RequestMapping("/xxx")
   完整路径：http://localhost:8080/api/xxx
   ```
