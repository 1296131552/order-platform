# 订单可视化数字化管理平台 - 实施计划

## 项目概述

**项目名称**：订单可视化数字化管理平台

**项目定位**：以销售订单为聚合根的领域驱动管理系统

**核心业务链**：客户下单 → 对接产地供应商 → 安排第三方物流 → 多收货点签收

**预估工期**：107个工作日

**生成日期**：2025-01-16

---

## 系统架构

### 技术栈

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

### 聚合根设计

| 聚合根 | 职责 | 边界 |
|--------|------|------|
| **Order** | 订单生命周期管理 | Order → OrderLine → Shipment → Receipt |
| **Shipment** | 发运批次管理 | Shipment → ShipmentLine → ReceiptDetail |
| **Partner** | 合作方（供应商/承运商/客户） | Partner → PartnerPerformance → Qualification |
| **Exception** | 异常处理闭环 | Exception → ExceptionHandling → ExceptionFeedback |
| **Attachment** | 附件与标签管理 | Attachment → AttachmentTag → AttachmentRelation |
| **Dashboard** | KPI口径统一 | KpiCalculateService（单一计算入口） |

### 模块结构

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

---

## 实施阶段划分

### 阶段一：环境搭建（5.0天）

| 任务 | 工期 | 交付物 |
|------|------|--------|
| 后端项目初始化 | 1.5天 | Maven多模块项目骨架 |
| 前端项目初始化 | 1.0天 | Vite + Vue3项目 |
| 数据库建表 | 1.5天 | DDL脚本 |
| API启动模块配置 | 1.0天 | 可启动的API模块 |

### 阶段二：公共模块（8.0天）

| 任务 | 工期 | 交付物 |
|------|------|--------|
| 统一响应与异常处理 | 1.5天 | Result<T>、全局异常处理器 |
| 安全认证JWT | 2.0天 | JwtProvider、JWT过滤器 |
| 状态机引擎 | 2.5天 | StateMachineManager |
| 事件总线 | 2.0天 | EventBus、DomainEvent |

### 阶段三：订单聚合模块（14.0天）

| 任务 | 工期 | 交付物 |
|------|------|--------|
| 订单数据模型 | 2.0天 | Order、OrderLine实体 |
| 订单状态机 | 3.0天 | 订单状态流转配置 |
| 订单CRUD服务 | 3.0天 | OrderService |
| 订单API接口 | 4.0天 | OrderController |
| 订单事件溯源 | 2.0天 | EventStore、EventReplayer |

### 阶段四：发运聚合模块（10.0天）

| 任务 | 工期 | 交付物 |
|------|------|--------|
| 发运数据模型 | 1.5天 | Shipment、ShipmentLine实体 |
| 批次管理 | 2.5天 | 发运批次管理服务 |
| 签收管理 | 2.0天 | 签收记录服务 |
| 物流跟踪 | 4.0天 | 物流状态同步服务 |

### 阶段五：合作方聚合模块（8.0天）

| 任务 | 工期 | 交付物 |
|------|------|--------|
| 合作方数据模型 | 1.5天 | Partner实体 |
| 合作方CRUD | 2.5天 | PartnerService |
| 履约统计 | 4.0天 | PerformanceService |

### 阶段六：支撑模块（28.0天）

| 模块 | 工期 | 内容 |
|------|------|------|
| 用户权限 | 6.0天 | 用户管理、角色权限 |
| 异常聚合 | 6.0天 | 异常上报、处理流程 |
| 附件聚合 | 8.0天 | 文件上传、标签、检索 |
| 看板聚合 | 10.0天 | KPI计算、数据聚合 |
| 可视化聚合 | 8.0天 | 地图服务、时间线服务 |

### 阶段七：前端应用（18.0天）

| 任务 | 工期 | 交付物 |
|------|------|--------|
| Vue3项目初始化 | 2.0天 | 项目骨架、路由配置 |
| 订单管理页面 | 3.0天 | 列表、详情、表单 |
| 发运管理页面 | 2.0天 | 批次列表、签收录入 |
| 合作方管理页面 | 2.0天 | 供应商、承运商管理 |
| 附件中心页面 | 2.0天 | 文件上传、检索 |
| 系统管理页面 | 3.0天 | 用户、角色、权限 |
| 大屏可视化 | 4.0天 | 数据看板、地图展示 |

### 阶段八：测试与部署（8.0天）

| 任务 | 工期 | 交付物 |
|------|------|--------|
| 单元测试与集成测试 | 2.0天 | 测试用例、覆盖率报告 |
| 性能测试与优化 | 2.0天 | JMeter脚本、性能报告 |
| 部署方案设计 | 2.0天 | Dockerfile、docker-compose |
| 文档编写与培训 | 2.0天 | 用户手册、API文档 |

### 阶段九：详情页整合（3.0天）

| 任务 | 工期 | 交付物 |
|------|------|--------|
| 详情页组件整合 | 3.0天 | 订单详情页 |

---

## 订单状态定义

```java
public enum OrderStatus {
    DRAFT,             // 草稿
    EXECUTING,         // 执行中
    PARTIALLY_RECEIVED,// 部分到货
    COMPLETED,         // 完成
    ARCHIVED           // 已归档
}
```

**状态流转**：`DRAFT → EXECUTING → PARTIALLY_RECEIVED → COMPLETED → ARCHIVED`

**异常处理**：异常是标记而非状态，可附加于任意状态

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

## API接口规范

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

## 部署架构

```
┌─────────────────────────────────────────┐
│              Nginx (80/443)              │
│         (反向代理 + 负载均衡 + SSL)     │
├─────────────────────────────────────────┤
│  ┌────────────┐  ┌────────────┐       │
│  │ Spring App1│  │ Spring App2│       │
│  │  (8081)    │  │  (8082)    │       │
│  └────────────┘  └────────────┘       │
├─────────────────────────────────────────┤
│  ┌────────────┐  ┌────────────┐       │
│  │ MySQL主库  │  │ MySQL从库  │       │
│  │  (3306)    │  │  (3307)    │       │
│  └────────────┘  └────────────┘       │
│  ┌────────────┐                       │
│  │ Redis      │                       │
│  │  (6379)    │                       │
│  └────────────┘                       │
└─────────────────────────────────────────┘
```

---

## 验收标准

### 功能验收
- [ ] 所有模块功能完整实现
- [ ] API接口符合规范
- [ ] 前端页面交互流畅
- [ ] 数据一致性保证

### 性能验收
- [ ] API响应时间 P95 < 500ms
- [ ] 订单列表 P95 < 200ms
- [ ] 附件检索 P95 < 2s
- [ ] 并发支持 1000 QPS

### 质量验收
- [ ] 代码符合规范
- [ ] 单元测试覆盖率 > 70%
- [ ] 集成测试通过
- [ ] API文档完整

---

## 文档索引

详细计划文档位于 `plan/` 目录，共57个文件：

- **plan_01_总体计划.md** - 项目整体规划
- **plan_02 ~ plan_70** - 各模块详细任务说明

---

*本文档由融合计划自动生成*
*源计划：plan (73个文件) + plan1 (49个文件)*
*融合版本：57个统一格式文件*
