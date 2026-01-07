# 订单可视化平台 - 后端开发指南

> **开发组 (claude.ai/code) 后端开发指导文档**

> 本文档为 开发组 AI 助手提供后端开发的最佳实践和规则，确保生成高质量的代码。

---

## 📋 目录

- [项目概述](#项目概述)
- [技术栈](#技术栈)
- [设计限制与临时方案](#设计限制与临时方案)
- [AI代码质量十大规则](#ai代码质量十大规则)
- [后端开发规范](#后端开发规范)
- [数据库开发规范](#数据库开发规范)
- [文档维护规范](#文档维护规范)
- [常用命令](#常用命令)

---

## 项目概述

### 项目定位

**订单可视化数字化管理平台** - 以销售订单为核心，实现业务全流程可视化、资料统一管理、数据看板决策支持的业务管理系统。

### 后端架构

**分层架构**：
```
┌─────────────────────────────────────────────────────────┐
│              API 入口层 (order-platform-api)              │
│              Spring Boot 3.2.x + Java 21                 │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│              业务服务层 (各业务模块)                      │
│  订单 | 发运 | 合作方 | 附件 | 可视化 | 看板 | 异常     │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│              基础支撑层 (order-platform-common)           │
│  权限 | 日志 | 工具类 | 统一响应 | 异常处理               │
└─────────────────────────────────────────────────────────┘
```

### 核心特性

- 🏗️ **分层架构**：清晰分层，职责明确
- 🔄 **模块化设计**：业务模块独立，易于扩展
- ⚡ **多级缓存**：Caffeine + Redis，性能优化
- 🔍 **全文检索**：Elasticsearch 附件检索
- 📝 **软删除机制**：数据可恢复，支持审计
- 🎯 **状态机管理**：统一的状态字典和流转规则

---

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| **JDK** | 21 | Java 运行环境（LTS 版本） |
| **Spring Boot** | 3.2.x | 核心框架 |
| **Spring MVC** | 内置 | Web 框架（RESTful API） |
| **Spring Security** | 6.x | 安全框架（认证授权） |
| **MyBatis Plus** | 3.5.x | ORM 框架（推荐使用注解形式） |
| **MySQL** | 8.0+ | 关系数据库 |
| **Druid** | 1.2.20+ | 数据库连接池 |
| **Redis** | 6.2+ / 7.0+ | 缓存/Session 存储 |
| **Caffeine** | 3.1+ | 本地缓存 |
| **Elasticsearch** | 8.11+ | 全文检索 |
| **JWT** | 0.12.3 | Token 认证 |
| **Knife4j** | 4.4.0+ | API 文档（OpenAPI 3.0） |
| **Lombok** | 最新 | 简化 Java 代码 |
| **Hutool** | 5.8.x | Java 工具类库 |
| **EasyExcel** | 3.3.2+ | Excel 导入导出 |

---

## 设计限制与临时方案

> **重要说明**：本项目设计基于 [设计分析文档](./设计分析文档.md) 的评估结果。在项目完成前，保持现有设计稳定，采用临时方案处理已知问题。

### 设计符合度

| 维度 | 符合度 | 说明 |
|------|--------|------|
| **数据库设计** | 85% | 核心表完备，缺少部分业务流程表 |
| **系统架构** | 90% | 模块划分清晰，缺少部分业务模块 |
| **API接口** | 80% | 基础接口齐全，缺少部分高级功能接口 |

### 已知问题与优先级

| 优先级 | 数量 | 主要问题 |
|--------|------|----------|
| **P0** | 3个 | 对账表、结算模块、看板接口 |
| **P1** | 4个 | 地址设计、地图接口、时间线接口 |
| **P2** | 9个 | 来单表、确认表、命名规范等 |

详细问题列表和解决方案请参考 [设计分析文档](./设计分析文档.md)。

### 项目期间开发规则

#### 1. 不修改现有设计

- ✅ 不修改数据库表结构
- ✅ 不新增模块
- ✅ 不修改模块职责
- ✅ 不调整架构分层

#### 2. 采用临时方案

**对账功能（P0）**：
```java
// 临时方案：在订单模块中添加对账方法
@Service
public class OrderService {
    // 对账功能（项目期间临时方案）
    public void settleOrder(Long orderId, SettlementDTO dto) {
        // 1. 创建对账记录（使用订单备注或其他方式存储）
        // 2. 更新订单状态为"已对账"
    }
}
```

**地址解析（P1）**：
```java
// 临时方案：在common模块创建简化的AddressUtil
public class AddressUtil {
    // 调用高德地图API进行地理编码
    public static GeoCodeDTO geocode(String address) {
        // 1. 先查Redis缓存
        // 2. 缓存未命中，调用高德API
        // 3. 结果存入Redis
    }
}
```

**地图和时间线功能（P1）**：
- 项目期间暂不实现地图可视化功能
- 项目期间暂不实现流程时间线功能
- 在订单详情页面展示基础信息即可

**看板功能（P0）**：
- 项目期间暂不实现大屏看板功能
- 可实现简单的统计接口供前端展示

#### 3. 核心功能优先

项目期间优先实现以下功能：

1. **订单模块**：订单CRUD、状态流转、订单行管理
2. **发运模块**：发运批次管理、快递单管理
3. **签收模块**：签收确认、差异记录
4. **合作方模块**：供应商、承运商、客户管理
5. **用户模块**：用户、角色、权限管理
6. **附件模块**：附件上传、下载、查询
7. **异常模块**：异常上报、处理、反馈

#### 4. 高级功能延后

以下功能留待项目完成后优化阶段实现：

1. **地图可视化**：发货地→收货地线路展示
2. **流程时间线**：来单至对账归档的完整时间线
3. **大屏看板**：KPI指标、趋势图、排行榜
4. **对账模块**：独立的结算模块
5. **地址标准化**：结构化地址字段

### 项目完成后优化计划

项目完成后，按照 [设计分析文档](./设计分析文档.md) 中的优化方案，分6个阶段进行优化：

| 阶段 | 内容 | 预计时间 |
|------|------|----------|
| 第一阶段 | 补全核心业务表（来单、确认、对账） | 1周 |
| 第二阶段 | 优化地址字段设计 | 1周 |
| 第三阶段 | 新增结算模块 | 2周 |
| 第四阶段 | 完善可视化功能（地图、时间线） | 2周 |
| 第五阶段 | 完善看板功能 | 1周 |
| 第六阶段 | 完善导入导出 | 1周 |
| **总计** | **6个阶段** | **8周** |

---

## AI代码质量十大规则

> **遵循这些规则可以显著提高AI生成代码的质量**

### 1. 分步执行规则 ⭐⭐⭐⭐⭐

**问题**：AI一次性输出大量代码可能导致卡顿、错误累积、难以调试

**规则**：
- 复杂任务拆分为多个小步骤
- 每步完成后等待用户确认
- 并行调用独立工具，避免单次调用过多
- 及时输出进度信息

**示例**：
```bash
# ❌ 不好：一次性读取所有文档
"读取所有文档然后分析"

# ✅ 好：分步进行
1. 读取文档A → 分析
2. 读取文档B → 分析
3. 综合分析
```

---

### 2. 上下文优先规则 ⭐⭐⭐⭐⭐

**问题**：AI不了解项目背景，写出不符合项目规范的代码

**规则**：
- 修改代码前必须先Read文件
- 遵循项目现有的代码风格
- 参考项目中类似的实现
- 复用项目中已有的工具类和组件

**示例**：
```java
// ❌ 不好：直接写代码，不考虑项目规范
public List<Order> getOrders() {
    // 随意实现
}

// ✅ 好：先读取现有代码，遵循项目规范
// 1. Read OrderService.java
// 2. 参考现有方法风格
// 3. 使用项目统一的Result响应
@RequireLogin
@GetMapping("/list")
public Result list() {
    // 遵循项目规范
}
```

---

### 3. 文档同步规则 ⭐⭐⭐⭐⭐

**问题**：代码修改后文档不更新，导致文档与代码脱节

**规则**：
- 代码修改完成前，先确认需要更新哪些文档
- 代码修改后立即更新相关文档
- 文档更新记录中标注修改人、日期、原因
- 使用清晰的文档格式（表格、代码块、列表）

**示例**：
```markdown
### v1.0.1 (2026-01-07)

#### 新增功能
- ✅ 用户角色查询服务

**修改人**：开发组
**相关文件**：
- UserRoleProvider.java
- UserRoleService.java
- 已完成的功能.md（已更新）
```

---

### 4. 渐进式复杂度规则 ⭐⭐⭐⭐

**问题**：AI直接写出复杂代码，可能包含多个问题

**规则**：
- 先写简单版本，确保功能正确
- 再逐步添加优化和异常处理
- 每个层次都经过验证
- 保持代码可读性优于过度优化

**示例**：
```java
// 第一版：简单实现
public List<String> getRoles(Long userId) {
    return userRoleMapper.selectRoleCodesByUserId(userId);
}

// 第二版：添加异常处理
public List<String> getRoles(Long userId) {
    try {
        return userRoleMapper.selectRoleCodesByUserId(userId);
    } catch (Exception e) {
        log.error("查询用户角色失败", e);
        return List.of();
    }
}

// 第三版：添加缓存
@Cacheable(value = "user-roles", key = "#userId")
public List<String> getRoles(Long userId) {
    // ...
}
```

---

### 5. 防御性编程规则 ⭐⭐⭐⭐⭐

**问题**：AI写的代码缺少边界检查和异常处理

**规则**：
- 所有外部输入必须校验
- 所有可能为null的对象必须检查
- 使用Optional或默认值避免NPE
- 关键操作必须有日志记录
- 异常必须有明确的错误信息

**示例**：
```java
// ❌ 不好：缺少空值检查
public void updateOrder(Long orderId, OrderDTO dto) {
    Order order = orderMapper.selectById(orderId);
    order.setCustomerName(dto.getCustomerName()); // 可能为null
    orderMapper.updateById(order);
}

// ✅ 好：完整的防御性编程
public void updateOrder(Long orderId, OrderDTO dto) {
    // 1. 参数校验
    if (orderId == null) {
        throw new BusinessException(ResponseCode.VALIDATION_ERROR, "订单ID不能为空");
    }
    if (dto == null) {
        throw new BusinessException(ResponseCode.VALIDATION_ERROR, "订单信息不能为空");
    }

    // 2. 查询订单
    Order order = orderMapper.selectById(orderId);
    if (order == null) {
        throw new BusinessException(ResponseCode.ORDER_NOT_FOUND);
    }

    // 3. 更新字段
    if (dto.getCustomerName() != null && !dto.getCustomerName().isEmpty()) {
        order.setCustomerName(dto.getCustomerName());
    }

    // 4. 执行更新
    orderMapper.updateById(order);
    log.info("订单更新成功: orderId={}, customerName={}", orderId, order.getCustomerName());
}
```

---

### 6. 代码审查自检规则 ⭐⭐⭐⭐

**问题**：AI写完代码后不自检，留下明显问题

**规则**：
代码生成后进行自检：
- [ ] 是否有TODO未处理？
- [ ] 是否有硬编码的配置？
- [ ] 是否有SQL注入风险？
- [ ] 是否有性能问题（N+1查询）？
- [ ] 异常处理是否完整？
- [ ] 日志级别是否合理？
- [ ] 命名是否清晰？
- [ ] 注释是否充分？

---

### 7. 接口兼容性规则 ⭐⭐⭐⭐⭐

**问题**：AI修改接口时不考虑向后兼容

**规则**：
- 修改API时优先使用新增而非修改
- 废弃的接口标记@Deprecated
- 使用版本控制（v1/v2）处理不兼容变更
- 响应结构保持向后兼容（在data中新增字段）
- 修改前检查是否有其他模块调用

**示例**：
```java
// ❌ 不好：直接修改响应结构
public class Result {
    private int code;
    private String message;
    private Object data;         // 修改为 Map
    private Timestamp timestamp; // 新增字段
}

// ✅ 好：保持兼容，在data中新增
public class Result {
    private int code;
    private String message;
    private Object data;         // 保持不变
    // 在data对象中新增字段，不影响旧代码
}
```

---

### 8. 测试驱动规则 ⭐⭐⭐

**问题**：AI写的代码缺少测试用例

**规则**：
- 生成代码时同时生成单元测试
- 测试覆盖正常路径和异常路径
- 测试覆盖边界条件
- 使用Mock避免依赖外部服务

---

### 9. 性能意识规则 ⭐⭐⭐⭐

**问题**：AI写的代码可能存在性能隐患

**规则**：
- 避免N+1查询问题
- 使用批量操作代替循环单条操作
- 合理使用缓存
- 避免大事务
- 数据库查询使用索引字段
- 分页查询避免深分页

**示例**：
```java
// ❌ 不好：N+1查询
List<Order> orders = orderMapper.selectList(wrapper);
for (Order order : orders) {
    List<OrderLine> lines = orderLineMapper.selectByOrderId(order.getId()); // N次查询
}

// ✅ 好：批量查询
List<Order> orders = orderMapper.selectList(wrapper);
List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
Map<Long, List<OrderLine>> linesMap = orderLineMapper.selectByOrderIds(orderIds)
    .stream().collect(Collectors.groupingBy(OrderLine::getOrderId));
```

---

### 10. 文档驱动规则 ⭐⭐⭐⭐⭐

**问题**：代码缺少必要的注释和文档

**规则**：
- 类级注释说明功能和职责
- 方法级注释说明业务规则
- 关键业务逻辑必须有行内注释
- 复杂算法必须有详细说明
- 公共API必须有Javadoc

---

## 后端开发规范

### 代码注释规范

作为指导教程项目，后端代码必须遵循以下注释规范：

**1. 类级注释**：
```java
/**
 * 订单服务实现类
 *
 * 功能说明：
 * - 订单 CRUD 操作
 * - 订单状态流转
 * - 订单统计
 *
 * @author 开发组
 * @since 1.0.0
 */
public class OrderServiceImpl implements OrderService {
    // ...
}
```

**2. 方法级注释**：
```java
/**
 * 根据订单号查询订单
 *
 * 业务规则：
 * - 订单号唯一性检查
 * - 逻辑删除的订单不返回
 * - 查询结果缓存 5 分钟
 *
 * @param orderNo 订单号
 * @return 订单实体，不存在返回 null
 */
public Order getByOrderNo(String orderNo) {
    // ...
}
```

**3. 关键业务逻辑注释**：
```java
// 1. 验证用户状态：只允许激活状态的用户登录
if (!"active".equals(user.getStatus())) {
    throw new BusinessException("用户已被禁用");
}

// 2. 密码加密：使用 BCrypt 算法加密
String encryptedPassword = passwordEncoder.encode(rawPassword);

// 3. 生成 JWT Token，有效期 7 天
String token = JwtUtil.generateToken(user.getId(), 7 * 24 * 60 * 60);
```

### MyBatis 使用规范

**推荐使用注解形式编写 SQL**：

```java
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT * FROM t_order " +
            "WHERE customer_id = #{customerId} " +
            "AND is_deleted = 0 " +
            "ORDER BY created_at DESC")
    List<Order> selectByCustomerId(@Param("customerId") Long customerId);
}
```

**优点**：
- SQL 与 Java 代码在同一文件，便于维护
- 无需维护 XML 映射文件
- 简单查询一目了然

**适用场景**：
- ✅ 简单查询（单表、字段少）
- ✅ SQL 固定（无需动态拼接）

**不适用场景**：
- ❌ 复杂多表联查
- ❌ 需要动态 SQL

### API 规范

**RESTful 风格**：

| HTTP 方法 | 用途 | 示例 |
|-----------|------|------|
| **GET** | 查询资源 | `GET /api/order/{id}` |
| **POST** | 创建资源 | `POST /api/order` |
| **PUT** | 全量更新资源 | `PUT /api/order/{id}` |
| **PATCH** | 部分更新资源 | `PATCH /api/order/{id}/status` |
| **DELETE** | 删除资源 | `DELETE /api/order/{id}` |

**统一响应格式**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {...},
  "timestamp": "2026-01-07T12:00:00"
}
```

### 异常处理规范

```java
// 抛出业务异常
throw new BusinessException(ResponseCode.ORDER_NOT_FOUND);

// 自定义消息
throw new BusinessException(ResponseCode.VALIDATION_ERROR, "订单号不能为空");

// 全局异常处理器统一处理
```

---

## 数据库开发规范

### 表命名规范

- **前缀**：统一使用 `t_` 前缀
- **命名方式**：使用下划线分隔（snake_case）
- **命名风格**：全小写字母
- **示例**：`t_order`、`t_order_line`、`t_user_role`

### 字段命名规范

- **命名方式**：使用下划线分隔（snake_case）
- **命名风格**：全小写字母
- **主键**：统一使用 `id`
- **外键**：使用 `表名_id` 格式，如 `order_id`、`user_id`
- **时间字段**：`created_at`、`updated_at`、`deleted_at`
- **状态字段**：`status`、`is_deleted`、`is_enabled`

### NOT NULL 规范

**原则**：尽可能使用 `NOT NULL` 和合适的默认值，避免使用 NULL

```sql
-- ✅ 正确：使用 NOT NULL + 默认值
`status_code` VARCHAR(30) NOT NULL DEFAULT '' COMMENT '订单状态',
`created_by` BIGINT NOT NULL DEFAULT 0 COMMENT '创建人ID:0-系统创建',

-- ❌ 错误：允许 NULL
`status_code` VARCHAR(30) COMMENT '订单状态',
`created_by` BIGINT COMMENT '创建人ID',
```

### 软删除规范

所有查询必须过滤 `is_deleted = 0`：

```sql
-- ✅ 正确
SELECT * FROM t_order WHERE is_deleted = 0;

-- ❌ 错误
SELECT * FROM t_order;
```

---

## 文档维护规范

### 必须维护的文档

1. **数据库设计文档**：`backend/数据库设计文档.md`
   - 数据库修改后必须更新"更新记录"部分
   - 新增表/字段/索引后更新表结构说明

2. **API接口文档**：`backend/API接口文档.md`
   - 新增接口后添加到文档
   - 接口状态从 `⏳` 更新为 `✅`

3. **系统架构设计**：`backend/系统架构设计.md`
   - 架构调整后更新文档
   - 新增模块后更新模块说明

4. **已完成功能**：`backend/docs/已完成的功能.md`
   - 完成功能后添加详细文档
   - 更新功能列表

### 文档更新记录格式

```markdown
### v1.0.1 (2026-01-07)

#### 新增功能
- ✅ 用户角色查询服务

**修改人**：开发组
**修改内容**：
- 新增 UserRoleProvider 接口
- 实现 UserRoleService 查询服务
- 支持从 Token 和数据库混合获取角色

**相关文件**：
- common/provider/UserRoleProvider.java
- user/service/UserRoleService.java
- user/mapper/UserRoleMapper.java

**相关文档**：
- docs/已完成功能/用户角色查询服务.md（新建）
```

---

## 常用命令

### 后端开发

```bash
# 构建项目
mvn clean install

# 运行API模块（主入口）
cd order-platform-api
mvn spring-boot:run

# 打包
mvn clean package -DskipTests

# 跳过测试打包
mvn clean package -Dmaven.test.skip=true
```

### 数据库

```bash
# 初始化数据库
mysql -u root -p < scripts/sql/init.sql

# 连接数据库
mysql -u root -p order_platform
```

### Git 提交

```bash
# 添加所有更改
git add .

# 提交（使用规范格式）
git commit -m "[模块] 类型: 简短描述"

# 示例
git commit -m "[common] feat: 新增 UserRoleProvider 接口"
git commit -m "[order] fix: 修复订单状态流转问题"
```

---

## 核心业务概念

### 订单状态流转
```
草稿 → 执行中 → 部分到货 → 完成
```

### 发运状态流转
```
待提货 → 在途 → 已到货
```

### 签收状态流转
```
待签收 → 已签收 → 有差异 → 已处理
```

### 核心指标定义（统一口径）
- **订单数量**：按创建时间统计
- **在途订单**：发运已启动但未完成签收的订单
- **准时率**：按时签收订单数/总完成订单数×100%
- **异常件数**：存在到货差异或运输异常的订单数

---

## 相关文档

### 核心设计文档
- [设计分析文档](./设计分析文档.md) - 设计符合度分析与优化方案 ⭐
- [数据库设计文档](./数据库设计文档.md) - 27张表的完整设计
- [API接口文档](./API接口文档.md) - RESTful API接口规范
- [系统架构设计](./系统架构设计.md) - 分层架构与模块设计

### 项目文档
- [已完成功能](./docs/已完成的功能.md) - 功能实现进度追踪
- [父级项目文档](../CLAUDE.md) - 整体项目说明

### 甲方文档（不可修改）
- [解决方案文档](../docs/业务和流程/可视化数字化管理平台解决方案v1217.md) - 甲方需求文档

---

## 维护者

- **开发组** - 文档维护与规则制定

---

## 许可证

本项目采用 MIT 许可证。
