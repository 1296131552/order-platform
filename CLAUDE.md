# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目状态

**当前阶段：规划/设计阶段**

本项目尚未开始实际编码。当前项目结构包含：
- 完整的技术设计文档 (`memory-back/tech-stack.md`)
- 详细的业务设计文档 (`memory-back/design-document.md`)
- 57个任务分解计划 (`plan/` 目录)

**注意**：由于项目尚未初始化代码库，暂无 build/lint/test 命令。参考 `plan/plan_52_环境搭建.md` 开始项目初始化。

---

## [ALWAYS] 强制阅读规则

> **这些规则必须在生成任何代码前强制执行，不可跳过！**

```bash
# 写任何代码前必须完整阅读以下文档：
- memory-back/tech-stack.md           # 完整技术架构设计（状态机/事件溯源/聚合根设计）
- memory-back/design-document.md      # 业务需求与数据库设计

# 每完成一个重大功能或里程碑后，必须更新：
- memory-back/architecture.md         # 同步最新的架构变更（当前为空，需补充）
- memory-back/progress.md             # 更新项目进度
```

---

## 项目定位

**订单可视化数字化管理平台** — 以销售订单为聚合根的领域驱动管理系统

核心业务链：客户下单 → 对接产地供应商 → 安排第三方物流 → 多收货点签收

---

## 技术栈

| 层次 | 技术 | 版本 |
|------|------|------|
| 前端 | Vue 3 + TypeScript + Vite | 3.3+ / 5.2+ / 4.4+ |
| UI组件 | Element Plus | 2.4+ |
| 地图 | 高德地图 API | - |
| 图表 | ECharts | 5.4+ |
| 后端 | Spring Boot | 3.2.x |
| JDK | 21 (LTS, Virtual Threads) |
| ORM | MyBatis-Plus | 3.5.x |
| 数据库 | MySQL | 8.0+ |
| 缓存 | Redis | 7.0+ |
| 全文检索 | Elasticsearch | 8.11+ |
| 对象存储 | MinIO / OSS | - |
| 状态机 | Spring StateMachine | 3.2.0 |
| API文档 | Knife4j (Swagger增强) | 4.4.0 |

---

## 核心架构原则

### 聚合根设计（SOLID-S）
| 聚合根 | 职责 | 边界 |
|--------|------|------|
| **Order** | 订单生命周期管理 | Order → OrderLine → Shipment → Receipt |
| **Shipment** | 发运批次管理 | Shipment → ShipmentLine → ReceiptDetail |
| **Partner** | 合作方（供应商/承运商/客户） | Partner → PartnerPerformance → Qualification |
| **Exception** | 异常处理闭环 | Exception → ExceptionHandling → ExceptionFeedback |
| **Attachment** | 附件与标签管理 | Attachment → AttachmentTag → AttachmentRelation |
| **Dashboard** | KPI口径统一 | KpiCalculateService（单一计算入口） |

### 状态机驱动（Good Taste）
```
DRAFT → EXECUTING → PARTIALLY_RECEIVED → COMPLETED → ARCHIVED
                    |
                    +-- EXCEPTION (可附加于任意状态，不参与流转)
```
- 使用 **Spring StateMachine** 实现
- 状态变更必须记录事件日志
- 异常是标记而非状态，消除特殊情况分支

### 事件溯源
- 状态是事件的投影，事件才是真相
- 统一事件表 `t_event`，不可变、追加-only
- 支持从事件流重建聚合根状态

---

## 模块结构

```
com.company.order.visual
├── order-platform-api          # API模块（启动入口）
├── order-platform-common       # 公共模块
│   ├── statemachine/           # 状态机引擎
│   ├── event/                  # 事件总线
│   ├── annotation/             # 自定义注解
│   ├── exception/              # 异常处理
│   ├── response/               # 统一响应
│   ├── security/               # 安全认证
│   ├── holder/                 # 上下文（ThreadLocal）
│   └── util/                   # 工具类
├── order-platform-order        # 订单聚合
├── order-platform-shipment     # 发运聚合
├── order-platform-partner      # 合作方聚合
├── order-platform-dashboard    # 看板聚合
├── order-platform-attachment   # 附件聚合
├── order-platform-exception    # 异常聚合
├── order-platform-user         # 用户聚合
└── order-platform-visualization # 可视化聚合
```

**依赖规则**：
1. Common 不依赖任何业务模块
2. Order / Shipment / Partner 是核心聚合，互不依赖
3. Visualization / Dashboard 通过接口调用核心聚合
4. Exception / Attachment 可被所有模块依赖
5. User 是基础服务，被所有模块依赖

---

## 数据库命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 表名 | t_ + snake_case | t_order, t_shipment_batch, t_partner |
| 字段 | snake_case | order_no, customer_id, created_at |
| 主键 | id | BIGINT, 自增 |
| 外键 | {entity}_id | customer_id, supplier_id |
| 唯一索引 | uk_{column} | uk_order_no |
| 普通索引 | idx_{column}_{column} | idx_customer_status |

---

## 订单状态枚举

```java
public enum OrderStatus {
    DRAFT,             // 草稿
    EXECUTING,         // 执行中
    PARTIALLY_RECEIVED,// 部分到货
    COMPLETED,         // 完成
    ARCHIVED           // 已归档
}
```

---

## API 接口规范

**统一响应格式**：
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1705334400000
}
```

**RESTful 命名**：
| 操作 | 方法 | 路径 |
|------|------|------|
| 查询列表 | GET | /api/{resource}/list |
| 查询详情 | GET | /api/{resource}/{id} |
| 创建 | POST | /api/{resource}/create |
| 更新 | PUT | /api/{resource}/update |
| 删除 | DELETE | /api/{resource}/delete |

---

## 核心KPI定义

| KPI | 定义 |
|-----|------|
| 订单数量 | 按创建时间统计 |
| 在途订单 | 发运已启动但未完成签收（状态=EXECUTING或PARTIALLY_RECEIVED，且存在在途发运批次） |
| 准时率 | 按时签收订单数 / 总完成订单数 × 100% |
| 异常件数 | 存在到货差异或运输异常的订单数 |

**重要**：所有KPI计算必须通过 `KpiCalculateService` 统一入口，确保口径一致。

---

## 代码风格

- **命名**：类名用 PascalCase，方法名用 camelCase，常量用 UPPER_SNAKE_CASE
- **注释**：对人看的内容用中文，对机器的结构用英文
- **分层**：Controller → Service → Repository，职责清晰
- **函数**：短小单一职责，>20行需检查是否可拆分
- **缩进**：超过三层缩进需重构设计

---

## 参考文档

| 文档 | 路径 | 说明 |
|------|------|------|
| 实施计划 | `/memory-back/implementation-plan.md` | 项目实施计划（107天） |
| 项目进度 | `/memory-back/progress.md` | 当前开发进度跟踪 |
| 详细任务计划 | `/plan/` | 57个任务分解文件 |

---

## 开发最佳实践

### 后端开发规范
- **实体层**：使用 MyBatis-Plus 注解，字段使用 `@TableField`
- **服务层**：接口定义在 `service` 包，实现类在 `service.impl` 包
- **控制器**：统一使用 `@RestController`，返回 `Result<T>` 包装
- **异常处理**：使用 `@ControllerAdvice` 全局捕获，禁止 try-catch 吞没异常
- **事务管理**：只在 Service 层使用 `@Transactional`，Controller 层禁止使用

### 前端开发规范
- **组件命名**：PascalCase，如 `OrderList.vue`
- **API调用**：统一通过 `src/api/` 目录下的函数
- **状态管理**：使用 Pinia，按业务模块划分 store
- **路由配置**：懒加载组件，`component: () => import('...')`
- **类型定义**：使用 TypeScript 接口定义所有数据结构

### 安全规范
- **敏感数据**：禁止在日志中输出密码、token等敏感信息
- **SQL注入**：禁止字符串拼接SQL，使用参数化查询
- **权限校验**：所有API接口必须进行权限验证
- **输入验证**：使用 `@Valid` 注解验证请求参数

### 性能优化规范
- **数据库查询**：避免 N+1 查询，使用 `@TableField(select = false)` 延迟加载
- **缓存策略**：热点数据使用 Redis 缓存，设置合理过期时间
- **分页查询**：大量数据必须分页，使用 MyBatis-Plus `Page<T>`
- **异步处理**：耗时操作使用 `@Async` 或消息队列

---
