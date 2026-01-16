# 架构文档

## 模块结构

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
