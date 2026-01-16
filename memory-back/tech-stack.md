# 订单可视化平台 - 技术架构设计方案

> **设计原则**：KISS · YAGNI · SOLID · DRY · Good Taste

---

## 一、架构定位

### 核心目标
以销售订单为聚合根，实现业务全流程可视化、资料统一管理、数据口径统一的**领域驱动**管理系统。

### 架构哲学
> **"订单是系统的心脏，状态流转是它的脉搏，事件日志是它的记忆。"**

- **聚合根**：Order（订单）是唯一真相源，所有业务操作围绕订单展开
- **状态机**：状态是约束，不是自由变量；每一次状态变更都必须被验证
- **事件溯源**：状态是投影，事件才是真相；任何历史都可从事件流重建

---

## 二、架构全景图

```
+-----------------------------------------------------------------------------+
|                            表现层 (Presentation)                            |
|  +----------------------+  +-------------------------------------------+   |
|  |  order-web (Vue3)     |  | order-screen (地图可视化+ECharts+DataV)   |   |
|  |  Element Plus         |  | 高德地图 + 自研大屏                        |   |
|  +----------------------+  +-------------------------------------------+   |
+----------------------------------------+------------------------------------+
                                        | RESTful API
+----------------------------------------|------------------------------------+
|       接入层 (Gateway)                 v                                     |
|  Nginx (静态资源 + 反向代理 + 负载均衡 + SSL终止)                           |
+----------------------------------------|------------------------------------+
                                        v
+-----------------------------------------------------------------------------+
|                          应用层 (Application)                               |
|  +---------------------------------------------------------------------+    |
|  |              order-platform-api (Spring Boot 3.2.x)                  |    |
|  |  +------------+  +------------+  +------------+  +------------+      |    |
|  |  |  订单聚合   |  |  发运聚合   |  | 合作方聚合  |  |  看板聚合   |      |    |
|  |  |  (Order)    |  | (Shipment)  |  | (Partner)  |  | (Dashboard)|      |    |
|  |  +------------+  +------------+  +------------+  +------------+      |    |
|  |                                                                     |    |
|  |  +------------+  +------------+  +------------+  +------------+      |    |
|  |  |  附件聚合   |  |  异常聚合   |  |  用户聚合   |  | 可视化聚合 |      |    |
|  |  |(Attachment) |  | (Exception)|  |  (User)    |  |(Visualization)|     |    |
|  |  +------------+  +------------+  +------------+  +------------+      |    |
|  +---------------------------------------------------------------------+    |
|                                   |                                       |
|  +--------------------------------|-----------------------------------+   |
|  |         order-platform-common  v                                   |   |
|  |   状态机引擎 | 事件总线 | 领域事件 | 统一响应 | 权限认证 | 工具类      |   |
|  +---------------------------------------------------------------------+   |
+----------------------------------------|------------------------------------+
                                        v
+-----------------------------------------------------------------------------+
|                        基础设施层 (Infrastructure)                          |
|  +------------+  +------------+  +------------+  +------------+              |
|  |  MySQL 8.0  |  |   Redis    |  |  MinIO/OSS |  |  ES 8.11+  |              |
|  |  业务数据   |  |  缓存+会话 |  |  文件存储  |  |  全文检索  |              |
|  +------------+  +------------+  +------------+  +------------+              |
+-----------------------------------------------------------------------------+
```

---

## 三、核心设计决策

### 决策1：聚合根设计（SOLID-S）

| 聚合根 | 职责 | 边界 |
|--------|------|------|
| **Order** | 订单生命周期管理 | Order → OrderLine → Shipment → Receipt |
| **Shipment** | 发运批次管理 | Shipment → ShipmentLine → ReceiptDetail |
| **Partner** | 合作方（供应商/承运商/客户） | Partner → PartnerPerformance → Qualification |
| **Exception** | 异常处理闭环 | Exception → ExceptionHandling → ExceptionFeedback |
| **Attachment** | 附件与标签管理 | Attachment → AttachmentTag → AttachmentRelation |
| **Dashboard** | KPI口径统一 | KpiCalculateService（单一计算入口） |

**设计原则**：
> "一个聚合根负责维护其内部一致性边界。任何对聚合内部状态的修改都必须通过聚合根进行。"

### 决策2：状态机驱动（Good Taste）

```
+-------------------------------------------------------------------------+
|                         订单状态机                                      |
+-------------------------------------------------------------------------+
|                                                                         |
|   +---------+     +---------+     +---------+     +---------+           |
|   |  DRAFT  | --> |EXECUTING| --> | PARTIAL | --> |COMPLETED|           |
|   | (草稿)  |     | (执行中) |     |_RECEIVED|     | (完成)  |           |
|   +---------+     +---------+     +---------+     +---------+           |
|       |                |                |                |               |
|       |                v                |                |               |
|       |           +---------+          |                |               |
|       +---------->|ARCHIVED |          |                |               |
|    (取消/关闭)    | (已归档)|          |                |               |
|                   +---------+          |                |               |
|                                        v                v               |
|                               +-------------------------+               |
|                               |   EXCEPTION (异常)       |               |
|                               |   (可附加于任意状态)      |               |
|                               +-------------------------+               |
|                                                                         |
|   状态流转规则（存储于 t_status_transition_rule）                       |
|   - 前置条件验证（如：必须存在供应商才能从草稿->执行中）                 |
|   - 状态变更必须记录事件日志                                            |
|   - 异常状态不参与状态流转，而是附加标记                                 |
+-------------------------------------------------------------------------+
```

**实现方式**：**Spring StateMachine**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.statemachine</groupId>
    <artifactId>spring-statemachine-starter</artifactId>
    <version>3.2.0</version>
</dependency>
```

```java
// 状态枚举
public enum OrderState {
    DRAFT, EXECUTING, PARTIALLY_RECEIVED, COMPLETED, ARCHIVED
}

// 事件枚举
public enum OrderEvent {
    CREATE,     // 创建订单
    CONFIRM,    // 确认订单（供应商确认）
    SHIP,       // 发运
    RECEIVE,    // 签收
    COMPLETE,   // 完成
    ARCHIVE     // 归档
}

// 状态机配置
@Configuration
@EnableStateMachineFactory
public class OrderStateMachineConfig
        extends StateMachineConfigurerAdapter<OrderState, OrderEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<OrderState, OrderEvent> states)
            throws Exception {
        states.withStates()
            .initial(OrderState.DRAFT)
            .states(EnumSet.allOf(OrderState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderState, OrderEvent> transitions)
            throws Exception {
        transitions
            // 草稿 → 执行中
            .withExternal()
                .source(OrderState.DRAFT).target(OrderState.EXECUTING)
                .event(OrderEvent.CONFIRM)
                .guard(confirmGuard())
                .action(confirmAction())
            .and()
            // 执行中 → 部分到货
            .withExternal()
                .source(OrderState.EXECUTING).target(OrderState.PARTIALLY_RECEIVED)
                .event(OrderEvent.RECEIVE)
            .and()
            // 部分到货 → 完成
            .withExternal()
                .source(OrderState.PARTIALLY_RECEIVED).target(OrderState.COMPLETED)
                .event(OrderEvent.COMPLETE)
                .guard(completeGuard())
            .and()
            // 任意状态 → 归档
            .withExternal()
                .source(OrderState.COMPLETED).target(OrderState.ARCHIVED)
                .event(OrderEvent.ARCHIVE);
    }

    // 前置条件守卫
    public Guard<OrderState, OrderEvent> confirmGuard() {
        return context -> {
            Long orderId = (Long) context.getMessageHeader("orderId");
            // 验证：必须存在供应商
            Order order = orderRepository.findById(orderId);
            return order.getSupplierId() != null;
        };
    }

    // 状态变更动作
    public Action<OrderState, OrderEvent> confirmAction() {
        return context -> {
            Long orderId = (Long) context.getMessageHeader("orderId");
            // 1. 记录事件日志
            // 2. 发布领域事件
            eventPublisher.publish(new OrderConfirmedEvent(orderId));
        };
    }
}

// 状态机服务
@Service
public class OrderStateMachineService {

    @Autowired
    private StateMachineFactory<OrderState, OrderEvent> stateMachineFactory;

    @Transactional
    public void transition(Long orderId, OrderEvent event) {
        Order order = orderRepository.findById(orderId);

        StateMachine<OrderState, OrderEvent> sm =
            stateMachineFactory.getStateMachine(orderId.toString());

        // 恢复状态
        sm.getStateMachineAccessor().doWithAllRegions(sma ->
            sma.resetStateMachine(new DefaultStateMachineContext<>(
                order.getState(), null, null, null)));

        sm.start();

        // 发送事件，触发状态变更
        Message<OrderEvent> message =
            MessageBuilder.withPayload(event)
                .setHeader("orderId", orderId)
                .build();

        boolean accepted = sm.sendEvent(message);

        if (!accepted) {
            throw new IllegalStateTransitionException(
                order.getState(), event);
        }

        // 保存新状态
        order.setState(sm.getState().getId());
        orderRepository.save(order);
    }
}
```

**Spring StateMachine 优势**：
- 成熟框架，文档完善
- 支持守卫条件、动作、嵌套状态
- 支持状态持久化和恢复
- 可视化状态机（Spring Integration）

### 决策3：完整事件溯源

**核心思想**：
> "状态是事件的投影。任何历史都可从事件流重建。"

```sql
-- 统一事件表（不可变，追加-only）
CREATE TABLE t_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id CHAR(36) NOT NULL UNIQUE,        -- 事件唯一标识
    aggregate_id BIGINT NOT NULL,             -- 聚合根ID
    aggregate_type VARCHAR(50) NOT NULL,      -- 聚合根类型：ORDER, SHIPMENT
    event_version BIGINT NOT NULL,            -- 事件版本号（乐观锁）
    event_type VARCHAR(100) NOT NULL,         -- 事件类型
    event_data JSON NOT NULL,                 -- 事件载荷（完整状态快照）
    operator_id BIGINT,                       -- 操作人
    operator_name VARCHAR(100),               -- 操作人姓名（冗余，防用户删除）
    occurred_at DATETIME(3) NOT NULL,         -- 发生时间（精确到毫秒）
    created_at DATETIME(3) NOT NULL,          -- 记录创建时间
    INDEX idx_aggregate (aggregate_id, aggregate_type, occurred_at),
    INDEX idx_event_type (event_type),
    INDEX idx_occurred_at (occurred_at),
    INDEX idx_aggregate_version (aggregate_id, aggregate_type, event_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**完整事件类型枚举**：
| 领域 | 事件类型 | 含义 | 事件载荷 |
|------|---------|------|----------|
| Order | ORDER_CREATED | 订单创建 | orderNo, customerId, totalAmount, lines[] |
| Order | ORDER_CONFIRMED | 订单确认（供应商确认） | supplierId, confirmedAt |
| Order | ORDER_STATUS_CHANGED | 状态变更 | fromStatus, toStatus, reason |
| Order | ORDER_ARCHIVED | 订单归档 | archivedAt, archiveReason |
| Shipment | SHIPMENT_CREATED | 发运批次创建 | orderId, shipmentNo, supplierId, carrierId |
| Shipment | SHIPMENT_IN_TRANSIT | 发运在途 | trackingNo, estimatedArrival |
| Shipment | SHIPMENT_DELIVERED | 发运到货 | deliveredAt, receiverId |
| Receipt | RECEIPT_CONFIRMED | 签收确认 | receiptId, quantity, attachmentIds |
| Receipt | RECEIPT_DIFFERENCE_RECORDED | 差异记录 | differenceType, differenceAmount, description |
| Exception | EXCEPTION_REPORTED | 异常上报 | exceptionType, description, severity |
| Exception | EXCEPTION_ASSIGNED | 异常分配 | assignedTo, assignedAt |
| Exception | EXCEPTION_RESOLVED | 异常解决 | resolution, resolvedAt |
| Attachment | ATTACHMENT_UPLOADED | 附件上传 | attachmentId, fileName, fileUrl, tags[] |
| Attachment | ATTACHMENT_LINKED | 附件关联 | attachmentId, businessType, businessId |

**事件存储与重建设计**：

```java
// 事件存储接口
public interface EventStore {
    void append(DomainEvent event);
    List<DomainEvent> getEvents(AggregateId id);
    List<DomainEvent> getEvents(AggregateId id, long fromVersion);
}

// 聚合根基类
public abstract class AggregateRoot<T extends AggregateId> {

    private final T id;
    private long version = 0;
    private final List<DomainEvent> pendingEvents = new ArrayList<>();

    protected void apply(DomainEvent event) {
        // 1. 应用事件到当前状态
        mutate(event);
        // 2. 记录事件（待保存）
        pendingEvents.add(event);
        // 3. 版本号递增
        version++;
    }

    // 从事件流重建聚合根
    public static <T extends AggregateRoot<?>> T rebuild(
            Class<T> type, List<DomainEvent> events) {
        try {
            T instance = type.getDeclaredConstructor().newInstance();
            for (DomainEvent event : events) {
                instance.mutate(event);
                instance.version = event.getVersion();
            }
            return instance;
        } catch (Exception e) {
            throw new EventRebuildException("Failed to rebuild aggregate", e);
        }
    }

    protected abstract void mutate(DomainEvent event);
}

// 事件服务
@Service
public class EventService {

    @Autowired
    private EventStore eventStore;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public void save(AggregateRoot<?> aggregate) {
        for (DomainEvent event : aggregate.getPendingEvents()) {
            // 1. 保存到事件存储
            eventStore.append(event);
            // 2. 发布到应用层（触发投影更新、通知等）
            eventPublisher.publishEvent(event);
        }
        aggregate.clearPendingEvents();
    }

    // 重建聚合根
    public <T extends AggregateRoot<?>> T load(Class<T> type, AggregateId id) {
        List<DomainEvent> events = eventStore.getEvents(id);
        return AggregateRoot.rebuild(type, events);
    }
}
```

**事件重建设计要点**：
1. **不可变性**：事件一旦写入永不修改
2. **版本控制**：通过 `event_version` 实现乐观锁
3. **完整快照**：`event_data` 存储事件发生时的完整状态，便于重建
4. **定期快照**：为避免重放过长事件流，可定期生成聚合根快照
5. **事件发布**：事件保存后发布，供其他模块订阅

### 决策4：模块依赖规则（防止循环依赖）

```
                     +-----------------+
                     |   API Module    |
                     +--------+--------+
                               |
                     +--------+--------+
                     |  Common Module  |
                     | (状态机/事件/工具)|
                     +--------+--------+
                               |
            所有业务模块依赖，但互不依赖
        +----------+-----------+-----------+
        |                      |           |
   +----+----+           +----+----+   +----+----+
   |  Order  |           | Shipment |   | Partner |
   +----+----+           +----+----+   +----+----+
        |                      |             |
        +----------+-----------+-----------+
                   |
          可通过Service接口调用
              +-----+-----+
              |           |
      +-------+-------+   |
      | Visualization  |   |
      |   Dashboard    |   |
      |   Attachment   |   |
      |   Exception    |   |
      |   User        |   |
      +---------------+   |
```

**依赖规则**：
1. Common 不依赖任何业务模块
2. Order / Shipment / Partner 是核心聚合，互不依赖
3. Visualization / Dashboard 通过接口调用核心聚合
4. Exception / Attachment 可被所有模块依赖
5. User 是基础服务，被所有模块依赖

---

## 四、技术选型与论证

### 后端技术栈

| 技术 | 版本 | 选型理由 | 权衡 |
|------|------|---------|------|
| **JDK** | 21 | LTS版本，Virtual Threads提升并发性能 | 新特性，社区支持需验证 |
| **Spring Boot** | 3.2.x | 生态成熟，AOP支持状态机切面 | 略重 |
| **Spring StateMachine** | 3.2.0 | 状态机框架，支持守卫、动作、持久化 | 配置较复杂 |
| **MyBatis Plus** | 3.5.x | 简化CRUD，代码生成 | 复杂查询仍需手写SQL |
| **MySQL** | 8.0+ | 成熟稳定，JSON支持 | 分库分表需额外方案 |
| **Redis** | 7.0+ | 缓存+分布式锁 | 需处理缓存一致性 |
| **Elasticsearch** | 8.11+ | 附件全文检索 | 运维成本 |
| **MinIO** | Latest | 对象存储，S3兼容 | 自建运维 |
| **Knife4j** | 4.4.0 | API文档增强 | 依赖Swagger |
| **Hutool** | 5.8.x | 工具类，减少重复代码 | 包较大 |
| **EasyExcel** | 3.3.2+ | Excel导入导出 | - |

### 前端技术栈

| 技术 | 版本 | 选型理由 | 权衡 |
|------|------|---------|------|
| **Vue** | 3.3.4+ | 响应式系统，组合式API | 学习曲线 |
| **TypeScript** | 5.2+ | 类型安全 | 编译成本 |
| **Vite** | 4.4.5+ | 快速热更新 | 生态较新 |
| **Element Plus** | 2.4.2+ | 组件丰富 | 包体积 |
| **ECharts** | 5.4.3+ | 图表能力强大 | 配置复杂 |
| **高德地图** | Latest | 国内地图服务 | 需注册Key |
| **Pinia** | 2.1.7 | 状态管理 | - |

### 大屏方案（混合架构）

**决策**：**自研地图 + BI工具看板**

```
+-------------------------------------------------------------------------+
|                        混合大屏架构                                     |
+-------------------------------------------------------------------------+
|                                                                         |
|   +-----------------------+        +-----------------------+             |
|   |    自研地图可视化      |        |      BI工具看板         |             |
|   |    Vue3 + 高德地图    | <----> |  DataEase/Superset     |             |
|   |    - 发货->收货路线   |        |  - KPI指标卡片          |             |
|   |    - 多线路叠加       |        |  - 趋势图表             |             |
|   |    - 节点状态标注     |        |  - 排行榜               |             |
|   |    - 异常高亮         |        |  - 数据钻取             |             |
|   +-----------------------+        +-----------------------+             |
|            |                                   |                        |
|            +------------------+----------------+                        |
|                               v                                         |
|                 统一筛选条件（订单/客户/时间范围）                       |
|                                                                         |
+-------------------------------------------------------------------------+
```

**职责划分**：
| 组件 | 职责 | 技术 |
|------|------|------|
| **自研地图** | 空间可视化：线路、节点、地理分布 | Vue3 + 高德地图 API |
| **BI看板** | 数据分析：KPI、趋势、排行榜、对比 | DataEase / Superset |
| **数据同步** | BI数据源：通过数据视图/定时任务同步 | MySQL View / DataX |

**联动设计**：
- 自研地图和BI看板共享同一套筛选条件
- 点击地图节点/线路，跳转到BI看板查看明细
- BI看板支持下钻到订单详情（自研页面）

---

## 五、模块设计

### 1. order-platform-common（公共模块）

**职责**：提供跨模块的通用能力

**核心组件**：
```
common/
|-- statemachine/           # 状态机引擎
|   |-- IStateMachine.java
|   |-- StateTransition.java
|   |-- StateTransitionRule.java
|-- event/                  # 事件总线
|   |-- EventBus.java
|   |-- DomainEvent.java
|   |-- EventStore.java
|-- annotation/             # 自定义注解
|   |-- RequireLogin.java
|   |-- RequireRole.java
|   |-- LogOperation.java
|-- exception/              # 异常处理
|   |-- BusinessException.java
|   |-- IllegalStateTransitionException.java
|   |-- GlobalExceptionHandler.java
|-- response/               # 统一响应
|   |-- Result<T>.java
|-- security/               # 安全认证
|   |-- JwtUtil.java
|   |-- PasswordEncoder.java
|-- holder/                 # 上下文
|   |-- UserHolder.java (ThreadLocal)
|-- util/                   # 工具类
    |-- DateUtil.java
    |-- AddressUtil.java
    |-- FileUtil.java
```

### 2. order-platform-order（订单聚合）

**职责**：订单生命周期管理

**核心类**：
```java
// 聚合根
@Entity
public class Order {
    private OrderId id;
    private OrderNo orderNo;
    private CustomerId customerId;
    private OrderStatus status;
    private List<OrderLine> lines;
    private List<Shipment> shipments;  // 一对多聚合

    // 业务方法（不暴露setter）
    public void addLine(OrderLine line) { ... }
    public void transitionTo(OrderStatus newStatus) { ... }
    public boolean canShip() { ... }
    public boolean canComplete() { ... }
}

// 仓储接口（依赖倒置）
public interface OrderRepository {
    Order save(Order order);
    Order findById(OrderId id);
    Order findByOrderNo(OrderNo orderNo);
}
```

### 3. order-platform-shipment（发运聚合）

**职责**：发运批次 + 签收管理

**状态机**：
```
PENDING --> IN_TRANSIT --> DELIVERED
   |           |            |
   +-- 异常可附加于任意状态
```

### 4. order-platform-dashboard（看板聚合）

**职责**：KPI口径统一

**核心服务**：
```java
@Service
public class KpiCalculateService {

    /**
     * 统一KPI计算入口
     * 所有口径变更只需修改此处
     */
    public KpiDashboard calculate(KpiQuery query) {
        return KpiDashboard.builder()
            .orderCount(countOrders(query))        // 订单总数
            .inTransitCount(countInTransit(query)) // 在途订单
            .onTimeRate(calculateOnTimeRate(query))// 准时率
            .exceptionCount(countExceptions(query))// 异常件数
            .build();
    }

    private long countInTransit(KpiQuery query) {
        // 统一定义：发运已启动但未完成签收的订单
        return orderRepository.count(
            Criteria.where("status").in(EXECUTING, PARTIALLY_RECEIVED)
                .and("exists shipment where status = IN_TRANSIT")
        );
    }
}
```

### 5. order-platform-user（用户聚合）

**职责**：用户身份管理、认证授权、角色分配

**核心实体关系**：
```
User (用户) ←→ UserRole (用户角色关联) ←→ Role (角色) ←→ RolePermission (角色权限关联) ←→ Permission (权限)
                                                           ↓
                                                    DataScope (数据权限范围)
```

#### 5.1 用户状态机

```
+-----------------------------------------------------------+
|                        用户状态机                          |
+-----------------------------------------------------------+
|                            |                              |
|    +------+     +------+     +------+     +------+        |
|    | 待激活| --> |  正常| --> | 锁定 | --> | 已删除|        |
|    +------+     +------+     +------+     +------+        |
|                      |                                    |
|                      | 禁用/锁定                           |
|                      v                                    |
|                   锁定状态                                 |
+-----------------------------------------------------------+
```

**状态枚举**：
```java
public enum UserStatus {
    PENDING(0, "待激活"),
    NORMAL(1, "正常"),
    LOCKED(2, "锁定"),
    DELETED(3, "已删除");

    private final int code;
    private final String desc;
}
```

#### 5.2 认证授权流程

```
+-----------------------------------------------------------+
|                     认证授权流程                          |
+-----------------------------------------------------------+
|                                                           |
|     客户端                      服务端                    |
|       |                          |                        |
|       |   1.登录请求               |                        |
|       |   (account+password)      |                        |
|       | -------->                 |                        |
|       |                          |                        |
|       |                          | 2.验证用户名密码         |
|       |                          | 3.检查用户状态          |
|       |                          | 4.生成JWT Token          |
|       |   <--------               |                        |
|       |   {token, userInfo,       |                        |
|       |    roles, permissions}    |                        |
|       |                          |                        |
|       |   5.后续请求               |                        |
|       |   (携带Token)             |                        |
|       | -------->                 |                        |
|       |                          | 6.JWT解析+权限校验       |
|       |   <--------               |                        |
|       |   响应数据                 |                        |
|                                                           |
+-----------------------------------------------------------+
```

#### 5.3 RBAC权限模型

```
+-----------------------------------------------------------+
|                      RBAC 权限模型                         |
+-----------------------------------------------------------+
|                                                           |
|   +----------+       +----------+       +----------+        |
|   |   User   | <---> |   Role   | <---> |Permission |        |
|   +----------+       +----------+       +----------+        |
|        ^                                      ^            |
|        |                                      |            |
|   +----------+                              +------------+  |
|   |DataScope |                              |  Resource  |  |
|   +----------+                              +------------+  |
|                                                           |
|   权限粒度：模块 -> 功能 -> 操作 (CRUD)                     |
|   数据权限：全部 | 本部门 | 仅本人 | 自定义                    |
+-----------------------------------------------------------+
```

#### 5.4 数据权限设计

```
+-----------------------------------------------------------+
|                      数据权限设计                           |
+-----------------------------------------------------------+
|                                                           |
|  数据权限类型（枚举 DataScopeType）：                       |
|  - ALL(1)        : 全部数据                               |
|  - DEPT(2)       : 本部门及下级部门数据                     |
|  - DEPT_ONLY(3)  : 本部门数据                              |
|  - SELF(4)       : 仅本人数据                               |
|  - CUSTOM(5)     : 自定义（通过关联表指定数据范围）            |
|                                                           |
|  实现方式：                                               |
|  1. 在用户/角色上配置 data_scope_type                       |
|  2. 自定义类型通过 t_user_data_scope 关联具体数据            |
|  3. 查询时通过 MyBatis 拦截器自动添加 WHERE 条件             |
|                                                           |
+-----------------------------------------------------------+
```

#### 5.5 核心实体类

```java
// ==================== 用户实体 ====================
@Entity
@Table(name = "t_user")
public class User {
    private Long id;
    private String username;
    private String password;              // BCrypt加密后
    private String realName;
    private String email;
    private String phone;
    private UserStatus status;            // 待激活/正常/锁定/已删除
    private DataScopeType dataScope;      // 数据权限范围
    private Long deptId;                  // 部门ID（如需部门管理）
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private LocalDateTime passwordUpdateTime; // 密码更新时间
    private Set<Role> roles = new HashSet<>();

    // ==================== 业务方法 ====================

    /**
     * 用户登录验证
     */
    public void login(String rawPassword, String ip) {
        if (this.status != UserStatus.NORMAL) {
            throw new UserStatusException(this.status);
        }
        if (!PasswordUtil.matches(rawPassword, this.password)) {
            throw new AuthenticationException("密码错误");
        }
        this.lastLoginTime = LocalDateTime.now();
        this.lastLoginIp = ip;
    }

    /**
     * 检查是否有指定权限
     */
    public boolean hasPermission(String permissionCode) {
        return roles.stream()
            .flatMap(role -> role.getPermissions().stream())
            .anyMatch(p -> p.getPermissionCode().equals(permissionCode));
    }

    /**
     * 检查是否有指定角色
     */
    public boolean hasRole(String roleCode) {
        return roles.stream()
            .anyMatch(r -> r.getRoleCode().equals(roleCode));
    }

    /**
     * 修改密码
     */
    public void changePassword(String oldPassword, String newPassword) {
        if (!PasswordUtil.matches(oldPassword, this.password)) {
            throw new AuthenticationException("原密码错误");
        }
        this.password = PasswordUtil.encode(newPassword);
        this.passwordUpdateTime = LocalDateTime.now();
    }
}

// ==================== 角色实体 ====================
@Entity
@Table(name = "t_role")
public class Role {
    private Long id;
    private String roleCode;              // 角色编码：ADMIN, MANAGER, STAFF
    private String roleName;              // 角色名称
    private String description;
    private Boolean status;                // 启用/禁用
    private DataScopeType dataScope;      // 角色级数据权限（与用户权限取并集）
    private Integer sortOrder;
    private Set<Permission> permissions = new HashSet<>();
    private Set<Menu> menus = new HashSet<>();
}

// ==================== 权限实体 ====================
@Entity
@Table(name = "t_permission")
public class Permission {
    private Long id;
    private Long parentId;                // 父权限ID（树形结构）
    private String permissionCode;        // 权限编码：order:view, order:create
    private String permissionName;        // 权限名称
    private String resourceType;          // menu/button/api
    private String resourcePath;          // API路径或前端路由
    private String method;                // GET/POST/PUT/DELETE
    private Integer sortOrder;
}

// ==================== 菜单实体 ====================
@Entity
@Table(name = "t_menu")
public class Menu {
    private Long id;
    private Long parentId;
    private String menuName;
    private String menuCode;
    private MenuType menuType;            // DIR/CTGRY/MENU/BUTTON
    private String path;                  // 前端路由路径
    private String component;             // Vue组件路径
    private String icon;
    private Integer sortOrder;
    private Boolean visible;
    private Boolean status;
    private Long permissionId;            // 关联权限表
    private Set<Menu> children = new TreeSet<>(
        Comparator.comparing(Menu::getSortOrder)
    );

    /**
     * 构建菜单树
     */
    public static List<MenuVO> buildTree(List<Menu> menus) {
        Map<Long, MenuVO> map = new HashMap<>();
        List<MenuVO> roots = new ArrayList<>();

        for (Menu menu : menus) {
            MenuVO node = MenuVO.from(menu);
            map.put(menu.getId(), node);

            if (menu.getParentId() == 0) {
                roots.add(node);
            } else {
                MenuVO parent = map.get(menu.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(node);
                }
            }
        }
        return roots;
    }
}
```

#### 5.6 认证服务

```java
// ==================== 认证服务 ====================
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     */
    public LoginVO login(LoginDTO dto) {
        // 1. 查询用户（支持用户名/邮箱/手机号登录）
        User user = userRepository.findByAccount(dto.getAccount())
            .orElseThrow(() -> new AuthenticationException("用户不存在"));

        // 2. 验证状态和密码
        user.login(dto.getPassword(), getClientIp());

        // 3. 生成JWT Token
        String token = jwtUtil.createToken(user.getId());

        // 4. 加载用户权限
        Set<String> roles = user.getRoles().stream()
            .map(Role::getRoleCode)
            .collect(Collectors.toSet());
        Set<String> permissions = user.getRoles().stream()
            .flatMap(r -> r.getPermissions().stream())
            .map(Permission::getPermissionCode)
            .collect(Collectors.toSet());

        // 5. 更新最后登录信息
        userRepository.save(user);

        return LoginVO.builder()
            .token(token)
            .userInfo(UserVO.from(user))
            .roles(roles)
            .permissions(permissions)
            .build();
    }

    /**
     * 用户登出
     */
    public void logout(String token) {
        // Token过期或加入Redis黑名单
        jwtUtil.invalidateToken(token);
    }

    /**
     * 获取当前用户信息
     */
    public CurrentUser getCurrentUser(String token) {
        Long userId = jwtUtil.getUserId(token);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AuthenticationException("用户不存在"));
        return CurrentUser.from(user);
    }
}

// ==================== 权限校验切面 ====================
@Aspect
@Component
public class PermissionCheckAspect {

    @Autowired
    private UserHolder userHolder;

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint pjp,
            RequirePermission requirePermission) throws Throwable {
        CurrentUser user = userHolder.get();

        if (!user.hasPermission(requirePermission.value())) {
            throw new ForbiddenException("无权限访问");
        }

        return pjp.proceed();
    }
}

// ==================== 数据权限拦截器 ====================
@Component
public class DataScopeInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms,
                            Object parameter, RowBounds rowBounds,
                            ResultHandler resultHandler, BoundSql boundSql) {
        CurrentUser user = UserHolder.get();
        if (user == null) return;

        // 根据用户数据权限范围生成SQL条件
        String dataScopeSql = buildDataScopeSql(user);

        // 修改原始SQL，添加数据权限条件
        String originalSql = boundSql.getSql();
        String newSql = "SELECT * FROM (" + originalSql + ") AS original_data " +
                       "WHERE " + dataScopeSql;

        // 通过反射更新BoundSql
        setFieldValue(boundSql, "sql", newSql);
    }

    private String buildDataScopeSql(CurrentUser user) {
        switch (user.getDataScopeType()) {
            case ALL:
                return "1=1";
            case DEPT:
                return "dept_id IN (" + user.getDeptIdWithChildren() + ")";
            case DEPT_ONLY:
                return "dept_id = " + user.getDeptId();
            case SELF:
                return "creator_id = " + user.getUserId();
            case CUSTOM:
                return buildCustomDataScope(user);
            default:
                return "1=0";
        }
    }
}
```

#### 5.7 用户注册流程

```
+-----------------------------------------------------------+
|                      用户注册流程                           |
+-----------------------------------------------------------+
|                                                           |
|  方式一：管理员创建（企业内常用）                            |
|  +-----------------------------------------------------+   |
|  | 1. 管理员填写用户基本信息（用户名、姓名、邮箱/手机）   |   |
|  | 2. 系统生成初始随机密码                               |   |
|  | 3. 发送激活邮件/短信（含初始密码）                     |   |
|  | 4. 用户首次登录强制修改密码                           |   |
|  | 5. 修改成功后状态变为"正常"                          |   |
|  +-----------------------------------------------------+   |
|                                                           |
|  方式二：自主注册（可选，根据企业安全策略决定）             |
|  +-----------------------------------------------------+   |
|  | 1. 用户填写注册信息（用户名、姓名、邮箱/手机）       |   |
|  | 2. 发送验证码（邮件/短信）                            |   |
|  | 3. 验证通过后创建用户，状态为"待激活"                 |   |
|  | 4. 发送激活邮件/短信                                  |   |
|  | 5. 用户点击激活链接或输入激活码                      |   |
|  | 6. 设置初始密码                                      |   |
|  | 7. 激活完成，状态变为"正常"                         |   |
|  +-----------------------------------------------------+   |
|                                                           |
+-----------------------------------------------------------+
```

#### 5.8 菜单权限设计

```
+-----------------------------------------------------------+
|                      菜单权限架构                           |
+-----------------------------------------------------------+
|                                                           |
|  权限类型划分：                                            |
|  +-----------------------------------------------------+   |
|  | 1. 菜单权限 (menu)      - 控制前端菜单显示              |   |
|  | 2. 按钮权限 (button)    - 控制页面内按钮显示            |   |
|  | 3. API权限 (api)        - 控制后端接口访问            |   |
|  +-----------------------------------------------------+   |
|                                                           |
|  权限粒度层次：                                            |
|  模块 -> 页面 -> 操作 -> API                               |
|  订单 -> 订单列表 -> 查看 -> GET /api/orders               |
|  订单 -> 订单列表 -> 新增 -> POST /api/orders              |
|  订单 -> 订单列表 -> 编辑 -> PUT /api/orders/{id}          |
|  订单 -> 订单列表 -> 删除 -> DELETE /api/orders/{id}       |
|                                                           |
|  菜单树结构（前端路由配置）：                              |
|  +-----------------------------------------------------+   |
|  | {                                                   |   |
|  |   id: 1,                                            |   |
|  |   parentId: 0,                                     |   |
|  |   name: '订单管理',                                  |   |
|  |   path: '/order',                                   |   |
|  |   icon: 'Document',                                 |   |
|  |   type: 'MENU',                                     |   |
|  |   children: [...]                                  |   |
|  | }                                                   |   |
|  +-----------------------------------------------------+   |
|                                                           |
+-----------------------------------------------------------+
```

```java
// ==================== 菜单服务 ====================
@Service
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;

    /**
     * 获取当前用户的菜单树
     */
    public List<MenuVO> getUserMenus(CurrentUser user) {
        // 1. 查询用户所有角色
        Set<Long> roleIds = user.getRoleIds();

        // 2. 查询角色关联的菜单
        Set<Long> menuIds = menuRepository.findMenuIdsByRoleIds(roleIds);

        // 3. 查询菜单详情（只查询启用且可见的）
        List<Menu> menus = menuRepository.findByIdInAndStatusAndVisible(
            menuIds, true, true
        );

        // 4. 构建菜单树
        return Menu.buildTree(menus);
    }

    /**
     * 获取所有菜单树（用于角色分配菜单）
     */
    public List<MenuVO> getAllMenus() {
        List<Menu> menus = menuRepository.findAll();
        return Menu.buildTree(menus);
    }
}
```

#### 5.9 数据库表设计

用户管理模块共计 **11张表**：

```sql
-- ==================== 用户表 ====================
CREATE TABLE t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,        -- BCrypt加密
    real_name VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    status TINYINT DEFAULT 1,             -- 0:待激活 1:正常 2:锁定 3:已删除
    data_scope TINYINT DEFAULT 4,         -- 数据权限范围
    dept_id BIGINT,                      -- 部门ID（如需部门管理）
    last_login_time DATETIME,
    last_login_ip VARCHAR(50),
    password_update_time DATETIME,       -- 密码更新时间（用于定期强制修改）
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_dept (dept_id),
    INDEX idx_account (username, email, phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== 角色表 ====================
CREATE TABLE t_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    status TINYINT DEFAULT 1,             -- 0:禁用 1:启用
    data_scope TINYINT DEFAULT 1,         -- 角色级数据权限（与用户权限取并集）
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== 权限表 ====================
CREATE TABLE t_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    permission_name VARCHAR(50) NOT NULL,
    resource_type VARCHAR(20),            -- menu/button/api
    resource_path VARCHAR(200),           -- API路径或前端路由
    method VARCHAR(10),                   -- GET/POST/PUT/DELETE
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent (parent_id),
    INDEX idx_type (resource_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== 菜单表 ====================
CREATE TABLE t_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0,
    menu_name VARCHAR(50) NOT NULL,
    menu_code VARCHAR(100) NOT NULL UNIQUE,
    menu_type CHAR(1) NOT NULL,           -- M:目录 C:菜单 F:按钮
    path VARCHAR(200),                   -- 前端路由路径
    component VARCHAR(200),              -- Vue组件路径
    icon VARCHAR(100),
    sort_order INT DEFAULT 0,
    visible TINYINT DEFAULT 1,           -- 0:隐藏 1:显示
    status TINYINT DEFAULT 1,            -- 0:禁用 1:启用
    permission_id BIGINT,                -- 关联权限表
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent (parent_id),
    INDEX idx_type (menu_type),
    INDEX idx_permission (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== 用户角色关联表 ====================
CREATE TABLE t_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== 角色权限关联表 ====================
CREATE TABLE t_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== 角色菜单关联表 ====================
CREATE TABLE t_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== 用户菜单关联表（可选）====================
CREATE TABLE t_user_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_menu (user_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== 用户自定义数据权限范围 ====================
CREATE TABLE t_user_data_scope (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    scope_type VARCHAR(50),                -- dept/user/custom
    scope_id BIGINT,                      -- 部门ID/用户ID等
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== 验证码表 ====================
CREATE TABLE t_verification_code (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    scene VARCHAR(20) NOT NULL,          -- 场景：REGISTER/LOGIN/RESET_PWD
    target VARCHAR(100) NOT NULL,        -- 接收目标：邮箱或手机号
    code VARCHAR(10) NOT NULL,           -- 验证码
    expire_time DATETIME NOT NULL,       -- 过期时间
    used TINYINT DEFAULT 0,              -- 是否已使用
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_target (target, scene),
    INDEX idx_expire (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==================== 注册邀请码表（可选）====================
CREATE TABLE t_invite_code (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    inviter_id BIGINT,                   -- 邀请人ID
    max_use_count INT DEFAULT 1,         -- 最大使用次数
    used_count INT DEFAULT 0,            -- 已使用次数
    expire_time DATETIME,                -- 过期时间
    status TINYINT DEFAULT 1,            -- 0:已失效 1:有效
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 5.10 API设计

```
+-----------------------------------------------------------+
|                    用户管理 API（完整版）                     |
+-----------------------------------------------------------+
|                                                           |
|  认证相关：                                                |
|  POST   /api/auth/login           - 用户登录               |
|  POST   /api/auth/logout          - 用户登出               |
|  POST   /api/auth/refresh         - 刷新Token              |
|  POST   /api/auth/change-password - 修改密码               |
|  POST   /api/auth/reset-password  - 找回密码               |
|  POST   /api/auth/send-code       - 发送验证码              |
|  POST   /api/auth/verify-code     - 验证验证码              |
|  POST   /api/auth/register        - 用户注册                |
|  GET    /api/auth/current         - 获取当前用户信息        |
|                                                           |
|  用户管理：                                                |
|  GET    /api/users/page          - 分页查询用户            |
|  GET    /api/users/{id}          - 获取用户详情            |
|  POST   /api/users               - 创建用户                |
|  PUT    /api/users/{id}          - 更新用户                |
|  DELETE /api/users/{id}          - 删除用户                |
|  PATCH  /api/users/{id}/status   - 启用/禁用用户          |
|  PATCH  /api/users/{id}/reset-password - 重置密码（管理员） |
|  PATCH  /api/users/{id}/roles    - 分配角色               |
|                                                           |
|  角色管理：                                                |
|  GET    /api/roles               - 角色列表                |
|  GET    /api/roles/{id}          - 角色详情                |
|  POST   /api/roles               - 创建角色                |
|  PUT    /api/roles/{id}          - 更新角色                |
|  DELETE /api/roles/{id}          - 删除角色                |
|  PATCH  /api/roles/{id}/permissions - 分配权限           |
|  PATCH  /api/roles/{id}/menus    - 分配菜单               |
|                                                           |
|  菜单管理：                                                |
|  GET    /api/menus/tree         - 菜单树（全部）          |
|  GET    /api/menus/user          - 当前用户菜单树          |
|  GET    /api/menus/{id}          - 菜单详情                |
|  POST   /api/menus               - 创建菜单                |
|  PUT    /api/menus/{id}          - 更新菜单                |
|  DELETE /api/menus/{id}          - 删除菜单                |
|                                                           |
|  权限管理：                                                |
|  GET    /api/permissions         - 权限列表（树形）        |
|  GET    /api/permissions/user    - 获取当前用户权限        |
|  GET    /api/permissions/role/{roleId} - 角色权限          |
|                                                           |
+-----------------------------------------------------------+
```

#### 5.11 安全设计要点

```
+-----------------------------------------------------------+
|                      安全设计要点                           |
+-----------------------------------------------------------+
|                                                           |
|  1. 密码安全：                                            |
|     - 使用 BCrypt 加密（工作量因子10-12）                  |
|     - 定期强制修改密码（可配置天数）                       |
|     - 新用户首次登录强制修改密码                           |
|     - 密码复杂度校验（8位+大小写字母+数字+特殊字符）          |
|                                                           |
|  2. Token 安全：                                           |
|     - JWT Token 有效期7天，Refresh Token 30天              |
|     - Token 存储在 Redis，支持主动注销                     |
|     - 敏感操作（修改密码、角色分配）需要重新验证密码         |
|                                                           |
|  3. 会话安全：                                            |
|     - 单点登录控制（同一账号只能有一个有效Token）           |
|     - 异常登录检测（IP变化、设备变化时告警）                |
|     - 登录失败5次锁定账号30分钟                             |
|                                                           |
|  4. 注册安全：                                            |
|     - 验证码有效期5分钟，同一手机号/邮箱1分钟内只能发送1次   |
|     - 注册邀请码机制（可选，控制注册来源）                   |
|     - 防止批量注册（图形验证码/滑块验证）                   |
|                                                           |
|  5. 审计日志：                                            |
|     - 记录所有登录/登出操作                                |
|     - 记录敏感操作（用户创建/删除、角色分配等）             |
|     - 日志包含操作人、时间、IP、操作结果                     |
|                                                           |
+-----------------------------------------------------------+
```

---

## 六、数据架构

### 核心表设计

| 表名 | 聚合根 | 用途 |
|------|--------|------|
| t_order | Order | 订单主表 |
| t_order_line | Order | 订单行 |
| t_shipment | Shipment | 发运批次 |
| t_shipment_line | Shipment | 发运明细 |
| t_receipt_detail | Shipment | 签收明细 |
| t_partner | Partner | 合作方统一表（多态：供应商/承运商/客户） |
| t_exception | Exception | 异常记录 |
| t_attachment | Attachment | 附件 |
| t_event | - | 事件日志 |
| t_status_dict | - | 状态字典 |
| t_status_transition_rule | - | 状态流转规则 |

### 索引策略

```sql
-- 订单表索引
CREATE UNIQUE INDEX uk_order_no ON t_order(order_no);
CREATE INDEX idx_customer_status ON t_order(customer_id, status);
CREATE INDEX idx_created_at ON t_order(created_at DESC);

-- 发运表索引
CREATE INDEX idx_shipment_order ON t_shipment(order_id);
CREATE INDEX idx_shipment_status ON t_shipment(status);
CREATE INDEX idx_shipment_carrier ON t_shipment(carrier_id, status);

-- 事件表索引
CREATE INDEX idx_event_aggregate ON t_event(aggregate_id, aggregate_type, occurred_at DESC);
CREATE INDEX idx_event_type_time ON t_event(event_type, occurred_at DESC);
```

---

## 七、非功能性需求

### 性能指标

| 指标 | 目标值 | 实现方案 |
|------|--------|----------|
| API响应时间 | P95 < 500ms | Redis缓存 + 数据库索引 |
| 订单列表查询 | P95 < 200ms | 分页 + 覆盖索引 |
| 附件检索 | P95 < 2s | Elasticsearch |
| 并发支持 | 1000 QPS | 连接池 + Virtual Threads |

### 安全设计

| 措施 | 实现方式 |
|------|----------|
| 认证 | JWT + Spring Security |
| 授权 | RBAC + 数据权限 |
| 加密 | BCrypt（密码）+ AES（敏感信息） |
| 审计 | 操作日志 + 事件日志 |
| 防护 | SQL注入防护 + XSS防护 + CSRF防护 |

### 可扩展性

```
当前：单体架构
    ↓ 扩展点
1. 状态机：通过配置表驱动，无需改代码
2. 事件总线：支持异步处理（后续引入消息队列）
3. 存储层：预留分表分库接口（按order_id取模）
4. 服务拆分：模块边界清晰，可按聚合拆分微服务
```

---

## 八、开发规范

### 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | PascalCase | OrderService, OrderController |
| 方法名 | camelCase | createOrder, getById |
| 常量 | UPPER_SNAKE_CASE | MAX_RETRY_COUNT |
| 数据库表 | t_ + snake_case | t_order, t_shipment |
| 数据库字段 | snake_case | order_no, created_at |

### 分层规范

```
Controller → Service → Repository
    ↓          ↓           ↓
  验证参数    业务逻辑    数据访问
  组装DTO    状态管理    SQL执行
```

### 异常处理规范

```java
// 业务异常
throw new BusinessException(ResponseCode.ORDER_NOT_FOUND);

// 状态流转异常
throw new IllegalStateTransitionException(current, target);

// 全局处理器统一返回
@ExceptionHandler(Exception.class)
public Result<?> handleException(Exception e) {
    // 记录日志
    // 返回统一格式
}
```

---

## 九、部署架构

```
+-------------------------------------------------------------------------+
|                        生产环境部署图                                   |
+-------------------------------------------------------------------------+
|                                                                         |
|   +-------------+                                                       |
|   |    Nginx    |  (反向代理 + SSL + 静态资源)                          |
|   +-----+-----+                                                       |
|         |                                                              |
|   +-----+-----+     +-------------+     +-------------+               |
|   | App Server  | -->|    MySQL    |     |    Redis    |               |
|   | (2 instances)|   |   (Master)  |     |  (Cluster)   |               |
|   +-------------+   +-----+-----+     +-------------+               |
|                            |                                          |
|                       +----+----+                                    |
|                       |  MySQL  |                                    |
|                       |  Slave  |                                    |
|                       +---------+                                    |
|                                                                         |
|   +-------------+     +-------------+     +-------------+               |
|   |   MinIO     |     |     ES      |     |  XXL-Job    |               |
|   | (文件存储)  |     | (全文检索)  |     | (定时任务)  |               |
|   +-------------+     +-------------+     +-------------+               |
|                                                                         |
+-------------------------------------------------------------------------+
```

---

## 十、原则应用总结

| 原则 | 应用方式 | 好处 |
|------|---------|------|
| **KISS** | 单体架构，状态机表驱动，不过度抽象 | 简单可维护 |
| **YAGNI** | 不引入消息队列/微服务，按需扩展 | 避免过度设计 |
| **SOLID-S** | 每个聚合根一个职责 | 模块边界清晰 |
| **SOLID-O** | 状态机通过配置表扩展 | 新增状态无需改代码 |
| **SOLID-D** | 依赖Repository接口而非实现 | 可替换数据源 |
| **DRY** | Common模块提供统一能力 | 避免重复代码 |
| **Good Taste** | 异常作为标记而非状态，消除特殊情况 | 状态机更简洁 |

---

## 十一、附录

### A. 关键文件路径

| 文件 | 路径 |
|------|------|
| 架构设计 | `order-platform-backend/系统架构设计.md` |
| 数据库设计 | `docs/数据库__参考/0.数据库设计文档.md` |
| API文档 | `order-platform-backend/API接口文档.md` |
| 业务流程 | `docs/业务和流程/业务/业务流程.md` |

### B. 参考文档

- Spring State Machine: https://docs.spring.io/spring-statemachine/
- Domain-Driven Design: Eric Evans
- Clean Architecture: Robert C. Martin

---

*文档版本: v1.0*
*创建日期: 2026-01-16*
*维护者: 架构组*
