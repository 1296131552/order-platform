# order-platform-common

> **订单可视化平台 - 公共模块**

> 提供统一的异常处理、认证授权、工具类等公共功能，被所有业务模块依赖。

---

## 📋 目录

- [模块概述](#模块概述)
- [目录结构](#目录结构)
- [核心功能](#核心功能)
- [开发计划](#开发计划)
- [快速开始](#快速开始)
- [依赖说明](#依赖说明)
- [修改规范](#修改规范)
- [更新记录](#更新记录)

---

## 模块概述

### 定位

**公共模块**（order-platform-common）是整个项目的基础模块，被所有业务模块依赖，提供：

- ✅ 统一的异常处理机制
- ✅ 统一的 API 响应格式
- ✅ JWT Token 认证授权
- ✅ 用户上下文管理
- ✅ 公共工具类
- ✅ 通用配置

### 设计原则

1. **零业务逻辑**：仅包含通用功能，不涉及具体业务
2. **向下兼容**：修改时保持向后兼容，避免影响其他模块
3. **高内聚低耦合**：功能独立，依赖最小化
4. **文档同步**：代码修改后及时更新本文档

---

## 目录结构

```
order-platform-common/
├── pom.xml                                  # Maven 配置
└── src/main/java/com/order/platform/common/
    ├── annotation/                          # 注解
    │   ├── RequireLogin.java                # 登录认证注解
    │   └── OperationLog.java               # 操作日志注解
    ├── aspect/                              # 切面
    │   ├── loggingAspect.java               # 请求日志切面
    │   └── OperationLogAspect.java          # 操作日志切面
    ├── config/                              # 配置类
    │   ├── AsyncConfig.java                 # 异步配置
    │   ├── SecurityConfig.java              # 安全配置（密码加密）
    │   ├── MybatisPlusMetaObjectHandler.java # MyBatis Plus 自动填充
    │   └── WebMvcConfig.java                # Web MVC 配置
    ├── dto/                                 # 数据传输对象
    │   ├── CurrentUserDTO.java              # 当前用户信息DTO
    │   └── OperationLogDTO.java             # 操作日志DTO
    ├── entity/                              # 实体类
    │   └── OperationLog.java                # 操作日志实体
    ├── enums/                               # 枚举
    │   ├── ResponseCode.java                # 响应码枚举
    │   ├── BusinessType.java                # 业务类型枚举
    │   ├── OperationType.java               # 操作类型枚举
    │   └── OperationModule.java             # 操作模块枚举
    ├── exception/                           # 异常
    │   ├── BusinessException.java            # 业务异常
    │   └── GlobalExceptionHandler.java      # 全局异常处理器
    ├── holder/                              # 持有者
    │   └── CurrentUserHolder.java           # 当前用户 ThreadLocal
    ├── interceptor/                         # 拦截器
    │   └── AuthInterceptor.java             # 认证拦截器
    ├── mapper/                              # Mapper接口
    │   └── OperationLogMapper.java          # 操作日志Mapper
    ├── provider/                            # 提供者接口
    │   └── UserRoleProvider.java            # 用户角色提供者接口
    ├── response/                            # 响应
    │   ├── Result.java                      # 统一响应封装
    │   └── PageResult.java                  # 分页响应封装
    ├── service/                             # 服务接口
    │   ├── OperationLogService.java         # 操作日志服务接口
    │   └── impl/                            # 服务实现
    │       └── OperationLogServiceImpl.java # 操作日志服务实现
    └── util/                                # 工具类
        ├── JwtUtil.java                     # JWT 工具类
        └── StringUtil.java                  # 字符串工具类
```

---

## 核心功能

### 1. 认证授权

#### @RequireLogin 注解

标记需要登录的接口：

```java
@RestController
public class OrderController {

    @RequireLogin  // 需要登录
    @GetMapping("/list")
    public Result list() {
        // 业务逻辑
    }
}
```

#### AuthInterceptor 拦截器

- 验证 Token 有效性
- 解析用户信息并存入 ThreadLocal
- 支持 Token 大小写兼容（bearer/Bearer/BEARER）
- 支持类级别注解

#### JwtUtil 工具类
```java
// 抛出业务异常
throw new BusinessException(ResponseCode.USER_NOT_FOUND);

// 带自定义消息
throw new BusinessException(ResponseCode.VALIDATION_ERROR, "用户名不能为空");
```

#### GlobalExceptionHandler 全局异常处理

自动捕获异常并返回统一格式：

```json
{
  "code": 1001,
  "message": "用户不存在",
  "data": null,
  "timestamp": "2026-01-07T12:00:00"
}
```

---

### 3. 统一响应

#### Result 响应封装

```java
// 成功响应
return Result.success(data);

// 失败响应
return Result.error(ResponseCode.USER_NOT_FOUND);

// 带消息响应
return Result.error(ResponseCode.VALIDATION_ERROR, "参数错误");
```

#### ResponseCode 响应码

| 错误码范围 | 说明 | 示例 |
|-----------|------|------|
| 200 | 成功 | SUCCESS |
| 400-499 | 客户端错误 | UNAUTHORIZED(401), FORBIDDEN(403) |
| 1000-1999 | 用户相关 | USER_NOT_FOUND(1001), EMAIL_ALREADY_EXISTS(1011), PHONE_ALREADY_EXISTS(1012) |
| 2000-2999 | 订单相关 | ORDER_NOT_FOUND(2001) |
| 3000-3999 | 发运相关 | SHIPMENT_NOT_FOUND(3001) |

**用户相关错误码（1000-1999）**：

| 错误码 | 常量名 | 说明 |
|-------|--------|------|
| 1001 | USER_NOT_FOUND | 用户不存在 |
| 1002 | USER_PASSWORD_ERROR | 密码错误 |
| 1003 | USER_ALREADY_EXISTS | 用户已存在 |
| 1004 | TOKEN_INVALID | Token 无效 |
| 1005 | TOKEN_EXPIRED | Token 已过期 |
| 1006 | VALIDATION_ERROR | 参数验证失败 |
| 1007 | USER_DISABLED | 账户已禁用 |
| 1008 | USER_LOCKED | 账户已锁定 |
| 1009 | PASSWORD_ERROR | 密码错误 |
| 1010 | PASSWORD_EXPIRED | 密码已过期 |
| 1011 | EMAIL_ALREADY_EXISTS | 邮箱已存在 |
| 1012 | PHONE_ALREADY_EXISTS | 手机号已存在 |

---

### 4. 用户上下文

#### CurrentHolder 获取当前用户

```java
// 获取当前用户
CurrentUserDTO user = CurrentUserHolder.get();

// 获取用户ID
Long userId = user.getId();

// 获取用户名
String username = user.getUsername();

// 获取角色列表
List<String> roles = user.getRoles();
```

#### CurrentUserDTO 用户信息

```java
@Data
@Builder
public class CurrentUserDTO {
    private Long id;           // 用户ID
    private String username;   // 用户名
    private List<String> roles;// 角色列表
    // ... 更多字段见源码
}
```

---

### 5. 自动填充

#### MybatisPlusMetaObjectHandler

自动填充公共字段：

```java
// 插入时自动填充
create_time, created_by, is_deleted

// 更新时自动填充
update_time, updated_by
```

---

### 6. 操作日志

#### @OperationLog 注解

标记需要记录操作日志的方法：

```java
@PostMapping("/order/create")
@OperationLog(
    business = BusinessType.ORDER,
    type = OperationType.CREATE,
    module = OperationModule.ORDER,
    description = "创建订单",
    businessId = "#result.id",
    businessNo = "#result.orderNo"
)
public Result<Order> createOrder(@RequestBody OrderDTO dto) {
    Order order = orderService.create(dto);
    return Result.ok(order);
}
```

#### 核心特性

- **多态关联**：支持订单、订单行、发运、快递单、供应商、承运商、客户、异常、附件等多种业务实体
- **混合存储**：核心信息存 MySQL，详细快照存对象存储
- **异步保存**：独立线程池异步执行，不影响业务性能
- **SpEL 支持**：支持使用 Spring 表达式语言动态获取业务关联信息

#### 存储策略

| 数据类型 | 存储方式 | 说明 |
|---------|---------|------|
| 核心信息 | MySQL 主表（t_operation_log） | 操作人、时间、结果等 |
| 详细快照 | 对象存储（OSS/MinIO） | before/after 数据 |
| 全文检索 | Elasticsearch（可选） | 日志内容搜索 |

#### 枚举类型

**BusinessType（业务类型）**：
- `ORDER`、`ORDER_LINE`、`SHIPMENT`、`SHIPMENT_LINE`
- `RECEIPT`、`CUSTOMER`、`SUPPLIER`、`CARRIER`
- `EXCEPTION`、`ATTACHMENT`、`USER`、`ROLE`

**OperationType（操作类型）**：
- `CREATE`、`UPDATE`、`DELETE`、`VIEW`
- `EXPORT`、`IMPORT`、`AUDIT`、`CONFIRM`、`APPROVE`、`CANCEL`

**OperationModule（操作模块）**：
- `ORDER`、`PARTNER`、`SHIPMENT`、`RECEIPT`
- `ATTACHMENT`、`EXCEPTION`、`VISUALIZATION`、`DASHBOARD`、`SYSTEM`、`USER`

#### 异步配置

```java
@Bean("operationLogExecutor")
public Executor operationLogExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);          // 核心线程数
    executor.setMaxPoolSize(5);            // 最大线程数
    executor.setQueueCapacity(100);        // 队列容量
    executor.setThreadNamePrefix("op-log-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
}
```

---

### 7. 分页响应

#### PageResult 分页封装

统一分页查询的响应格式：

```java
// 从 MyBatis-Plus Page 对象构建
Page page = orderService.page(new Page<>(current, size));
return Result.success(PageResult.of(page));

// 手动构建（不依赖 MyBatis-Plus）
return Result.success(PageResult.of(records, total, current, size));

// 空响应
return Result.success(PageResult.empty());
```

#### 响应结构

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [...],     // 数据列表
    "total": 100,         // 总记录数
    "current": 1,         // 当前页码
    "size": 10,           // 每页大小
    "pages": 10           // 总页数
  }
}
```

---

### 8. 密码加密

#### PasswordEncoder 密码加密器

使用 BCrypt 算法进行密码加密和验证：

```java
@Autowired
private PasswordEncoder passwordEncoder;

// 注册时加密密码
String encodedPassword = passwordEncoder.encode(rawPassword);
user.setPassword(encodedPassword);

// 登录时验证密码
boolean matches = passwordEncoder.matches(rawPassword, user.getPassword());
```

#### BCrypt 特点

- 自动加盐，每次加密结果不同
- 单向加密，不可逆
- 安全性高，业界标准

---

### 9. 字符串工具

#### StringUtil 工具类

提供字符串判空、脱敏、截断等功能：

```java
// 判空
boolean empty = StringUtil.isEmpty(str);
boolean blank = StringUtil.isBlank(str);

// 脱敏
String maskedPhone = StringUtil.maskPhone("13812345678");     // 138****5678
String maskedIdCard = StringUtil.maskIdCard("330102199001011234"); // 330102********1234
String maskedEmail = StringUtil.maskEmail("abc@example.com");  // a***@example.com

// 截断
String truncated = StringUtil.truncate(str, 20);  // 超长用...

// 随机生成
String numeric = StringUtil.randomNumeric(6);    // 6位随机数字
String random = StringUtil.randomString(10);     // 10位随机字符串
```

---

## 快速开始

### 1. 添加依赖

在其他模块的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.order.platform</groupId>
    <artifactId>order-platform-common</artifactId>
</dependency>
```

### 2. 配置 JWT

在 `application.yml` 中配置：

```yaml
jwt:
  secret: your-secret-key-at-least-256-bits
  expiration: 604800000  # 7天（毫秒）
```

### 3. 使用示例

```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DemoController {

    // 1. 使用 @RequireLogin 注解
    @RequireLogin
    @GetMapping("/user")
    public Result getUser() {
        // 2. 获取当前用户
        CurrentUserDTO user = CurrentUserHolder.get();
        return Result.success(user);
    }

    // 3. 抛出业务异常
    @GetMapping("/error")
    public Result error() {
        throw new BusinessException(ResponseCode.USER_NOT_FOUND);
    }
}
```

---

## 依赖说明

### 核心依赖

| 依赖 | 版本 | 用途 |
|------|------|------|
| spring-boot-starter-web | 3.2.x | Web 基础功能 |
| spring-boot-starter-validation | 3.2.x | 参数校验 |
| mybatis-plus-spring-boot3-starter | 3.5.x | ORM 框架 |
| hutool-all | 5.8.x | Java 工具类库 |
| jjwt-api/impl/jackson | 0.12.3 | JWT Token |
| spring-security-crypto | 6.x | 密码加密（BCrypt） |
| lombok | 最新 | 简化 Java 代码 |
| spring-boot-starter-aop | 3.2.x | AOP 切面编程 |

---

## 修改规范

> **重要**：本模块被所有业务模块依赖，修改时请严格遵守以下规范。

### 📝 修改前检查清单

- [ ] 确认修改是否影响其他模块
- [ ] 确认是否保持向后兼容
- [ ] 确认是否需要更新文档
- [ ] 确认是否需要添加单元测试

### ✅ 允许的修改

| 修改类型 | 说明 | 示例 |
|---------|------|------|
| **新增功能** | 添加新的工具类、方法 | 新增 `DateUtil.format()` |
| **新增枚举值** | 在 ResponseCode 中添加新错误码 | 新增 `ORDER_TIMEOUT(2005)` |
| **Bug 修复** | 修复已知问题 | 修复 ThreadLocal 内存泄漏 |
| **性能优化** | 优化现有实现 | 优化 JWT 解析性能 |
| **文档更新** | 更新注释和文档 | 添加方法使用示例 |

### ❌ 禁止的修改

| 修改类型 | 原因 | 替代方案 |
|---------|------|----------|
| **删除公开方法** | 会影响其他模块 | 标记为 `@Deprecated` |
| **修改方法签名** | 会破坏兼容性 | 新增重载方法 |
| **修改响应码** | 会导致前后端不一致 | 新增响应码 |
| **修改异常处理逻辑** | 可能影响错误处理 | 新增异常类型 |
| **修改包结构** | 会影响导入 | 保持现有结构 |

### 🔧 修改流程

1. **评估影响范围**
   - 检查哪些模块使用了要修改的类/方法
   - 评估是否需要通知其他开发者

2. **保持向后兼容**
   - 旧代码继续工作
   - 新代码使用新功能

3. **添加单元测试**
   - 覆盖新功能
   - 确保旧功能不被破坏

4. **更新文档**
   - 更新 README.md
   - 添加代码注释
   - 更新 [已完成功能](../docs/已完成的功能.md)

5. **提交代码**
   - Commit message 清晰说明修改内容
   - 格式：`[模块] 类型: 简短描述`
   - 示例：`[common] feat: 新增 PasswordUtil 工具类`

### 📋 修改模板

添加新功能时，请按以下模板更新文档：

```markdown
### 功能名称

**修改日期**：YYYY-MM-DD
**修改人**：你的名字
**修改类型**：新增 / 优化 / 修复

**功能说明**：
- 功能描述
- 使用场景

**使用示例**：
```java
// 代码示例
```

**影响范围**：
- 影响的模块
- 需要的配置

**相关 Issue/PR**：#123
```

---

## 更新记录

### v1.0.6 (2026-01-09)

#### 新增用户唯一性校验错误码

**背景**：
用户名、邮箱、手机号的唯一性冲突是不同的错误场景，应该使用独立的错误码，便于前端精确处理和国际化支持。

**修改内容**：

1. **新增错误码**：
   - `EMAIL_ALREADY_EXISTS(1011, "邮箱已存在")` - 邮箱唯一性冲突
   - `PHONE_ALREADY_EXISTS(1012, "手机号已存在")` - 手机号唯一性冲突

2. **保留原有错误码**：
   - `USER_ALREADY_EXISTS(1003, "用户已存在")` - 保留用于通用场景

3. **设计说明**：
   - 前端可根据 code 值精确判断冲突类型
   - 不依赖 message 文本，支持国际化
   - 符合 RESTful API 设计最佳实践

**使用示例**：
```java
// 邮箱冲突
throw new BusinessException(
    ResponseCode.EMAIL_ALREADY_EXISTS,
    "邮箱[" + email + "]已存在"
);

// 手机号冲突
throw new BusinessException(
    ResponseCode.PHONE_ALREADY_EXISTS,
    "手机号[" + phone + "]已存在"
);
```

**前端处理**：
```javascript
switch (error.response.data.code) {
  case 1011: // 邮箱冲突
    formErrors.email = message;
    break;
  case 1012: // 手机号冲突
    formErrors.phone = message;
    break;
}
```

**相关文件**：
- `enums/ResponseCode.java`

---

### v1.0.5 (2026-01-07)

#### User表迁移到user模块

**背景**：
User表是业务实体，应该归属于user模块而非common模块。将user.sql和相关代码移动到user模块，实现更清晰的模块职责划分。

**修改内容**：

1. **移动user.sql**：
   - 从 `order-platform-common/src/main/resources/sql/user.sql`
   - 移动到 `order-platform-user/src/main/resources/sql/user.sql`
   - User表相关代码集中在user模块管理

2. **模块解耦验证**：
   - ✅ Common模块不依赖User实体类
   - ✅ Common模块只使用CurrentUserDTO DTO
   - ✅ Common模块通过UserRoleProvider接口与user模块交互
   - ✅ 零影响：common模块功能完全正常

3. **设计说明**：
   - User实体类（25字段）归属于user模块
   - CurrentUserDTO DTO（11字段）保留在common模块
   - 通过AuthHelper实现User → CurrentUserDTO转换
   - Common模块提供通用功能，User模块负责用户业务

**相关文件**：
- ~~`src/main/resources/sql/user.sql`~~（已移除）
- `dto/CurrentUserDTO.java`（保留）
- `provider/UserRoleProvider.java`（保留）

**影响范围**：
- Common模块：零影响（完全解耦）
- User模块：新增User实体、UserMapper、AuthHelper

---

### v1.0.4 (2026-01-07)

#### 用户表和操作日志优化

**背景**：
根据优化后的用户表设计（25字段），同步更新 common 模块相关代码，确保用户信息完整传递和审计。

**修改内容**：

1. **CurrentUserDTO 扩展**：
   - 新增 `userCode`（用户编号）- 业务唯一标识
   - 新增 `employeeNo`（工号）- 企业内部员工编号
   - 新增 `position`（职位）- 职位信息

2. **操作日志增强**：
   - t_operation_log 表新增 5 个操作人审计字段：
     - `operator_user_code` - 操作人用户编号
     - `operator_employee_no` - 操作人工号
     - `operator_department_id` - 操作人部门ID
     - `operator_department_name` - 操作人部门名称
     - `operator_position` - 操作人职位
   - OperationLogDTO 同步添加对应字段
   - OperationLog 实体类同步添加对应字段
   - OperationLogAspect 更新，填充完整审计信息

3. **Bug 修复**：
   - 修复 OperationLogServiceImpl 中 BeanUtil 导入错误
   - 统一使用 Hutool 的 BeanUtil.copyProperties

**设计说明**：
- 这些字段用于操作日志审计，记录完整的操作人信息
- 支持按部门、工号、职位等维度进行审计查询
- 满足企业内部安全审计和合规要求

**相关文件**：
- `dto/CurrentUserDTO.java`
- `dto/OperationLogDTO.java`
- `entity/OperationLog.java`
- `aspect/OperationLogAspect.java`
- `service/impl/OperationLogServiceImpl.java`
- `resources/sql/operation_log.sql`

---

### v1.0.3 (2026-01-07)

#### 数据库设计优化

- ✅ 用户表设计优化（25字段完整版）
- ✅ 新增用户表建表脚本（`src/main/resources/sql/user.sql`）
- ✅ 支持账号安全（账户锁定、密码过期）
- ✅ 支持组织信息（部门、职位、工号）
- ✅ 支持用户行为统计（登录次数、最后登录）

**详细说明**：
- 考虑实际项目长期维护需求，避免频繁 ALTER TABLE
- 满足生产环境安全审计需求
- 支持基于部门的数据权限隔离

---

### v1.0.2 (2026-01-07)

#### 新增功能

- ✅ PageResult 分页响应封装
- ✅ PasswordEncoder 密码加密器（SecurityConfig）
- ✅ StringUtil 字符串工具类（脱敏、截断、随机生成）

#### 依赖更新

- 新增 `spring-security-crypto` 依赖（用于密码加密）

---

### v1.0.1 (2026-01-07)

#### 新增功能

- ✅ 操作日志功能（@OperationLog 注解）
- ✅ 操作日志切面（OperationLogAspect）
- ✅ 多态关联支持（business_type + business_id）
- ✅ 混合存储方案（MySQL + 对象存储）
- ✅ 异步日志保存（独立线程池）
- ✅ SpEL 表达式支持
- ✅ 业务类型、操作类型、操作模块枚举

#### 详细文档

- [操作日志功能](../docs/已完成功能/操作日志功能.md)

---

### v1.0.0 (2026-01-07)

#### 新增功能

- ✅ JWT Token 角色管理（支持角色快照）
- ✅ 用户角色提供者接口（UserRoleProvider）
- ✅ 认证拦截器优化（11个问题修复）
- ✅ Token 前缀大小写兼容（bearer/Bearer/BEARER）
- ✅ ThreadLocal 多层防护机制

#### Bug 修复

- 🐛 修复 ThreadLocal 内存泄漏问题
- 🐛 修复 Token 过期校验冗余问题
- 🐛 修复 username 校验过于严格问题

#### 优化改进

- ⚡ 移除反射调用，使用接口替代
- ⚡ JDK 版本兼容性（record → static class）
- ⚡ 启动时健康检查

#### 详细文档

- [JWT Token 角色管理](../docs/已完成功能/JWT-Token-角色管理.md)
- [用户角色查询服务](../docs/已完成功能/用户角色查询服务.md)
- [认证拦截器优化](../docs/已完成功能/认证拦截器优化.md)
- [代码质量改进](../docs/已完成功能/代码质量改进.md)

---

## 开发计划

### 当前状态（v1.0.1）

**已实现功能**：
- ✅ 认证授权（JWT、拦截器、注解、用户上下文）
- ✅ 异常处理（BusinessException、GlobalExceptionHandler）
- ✅ 统一响应（Result、ResponseCode）
- ✅ 操作日志（@OperationLog、切面、异步保存）
- ✅ 自动填充（MybatisPlusMetaObjectHandler）

**缺失功能**：
- ❌ 分页响应（PageResult）
- ❌ 密码加密（PasswordEncoder）
- ❌ 字符串工具类（StringUtil - 敏感信息脱敏）
- ❌ 日期工具类（DateUtil）
- ❌ 文件存储服务（FileStorageService）
- ❌ 地址工具类（AddressUtil）
- ❌ Redis缓存（RedisConfig、RedisUtil）

---

### 待补充功能清单

| 功能 | 优先级 | 复杂度 | 依赖模块 | 说明 |
|------|--------|--------|----------|------|
| `PageResult` | ⭐⭐⭐⭐⭐ | 低 | 无 | 所有列表查询接口必需 |
| `PasswordEncoder` | ⭐⭐⭐⭐⭐ | 低 | user | 用户登录功能必需 |
| `StringUtil` | ⭐⭐⭐ | 低 | 无 | 敏感信息脱敏（手机号、身份证等） |
| `DateUtil` | ⭐⭐⭐ | 低 | 无 | 统一日期格式化规范 |
| `FileStorageService` | ⭐⭐⭐ | 中 | attachment | 附件模块开发时需要 |
| `AddressUtil` | ⭐⭐ | 高 | visualization | 地图可视化优化时需要 |
| `RedisUtil` | ⭐⭐ | 中 | 多模块 | 性能优化时需要 |

---

### 分阶段实现计划

#### 第一阶段（当前）- 基础工具补充

**目标**：补充必需的基础工具类，支持用户登录和列表查询

| 功能 | 文件路径 | 说明 |
|------|----------|------|
| PageResult | `response/PageResult.java` | 分页响应封装，从MyBatis-Plus Page构建 |
| PasswordEncoder | `config/SecurityConfig.java` | 使用BCrypt算法，提供密码加密和验证 |
| StringUtil | `util/StringUtil.java` | 判空、脱敏、截断等常用操作 |

**实现要点**：
- `PageResult` 提供 `of(Page page)` 静态方法，快速构建分页响应
- `PasswordEncoder` 直接使用 Spring Security 的 `BCryptPasswordEncoder`
- `StringUtil` 重点实现敏感信息脱敏（手机号、身份证、邮箱）

---

#### 第二阶段（业务模块开发时）- 业务工具补充

**目标**：根据业务模块开发需求，补充相应工具类

| 功能 | 触发时机 | 说明 |
|------|----------|------|
| DateUtil | 开发订单模块时 | 统一日期格式（yyyy-MM-dd HH:mm:ss） |
| FileStorageService | 开发附件模块时 | 先实现本地存储，预留OSS/MinIO接口 |

**实现要点**：
- `DateUtil` 统一项目日期格式常量，提供快捷方法
- `FileStorageService` 定义接口，支持多种存储实现切换

---

#### 第三阶段（优化阶段）- 高级功能补充

**目标**：项目完成后，根据优化需求补充高级功能

| 功能 | 触发时机 | 说明 |
|------|----------|------|
| AddressUtil | 地图可视化优化 | 调用高德地图API，地址解析和地理编码 |
| RedisUtil | 性能优化 | 缓存配置和操作工具类 |

**实现要点**：
- `AddressUtil` 需要配置高德地图 API Key，实现 Redis 缓存减少API调用
- `RedisUtil` 优先使用 Spring Cache 注解，复杂场景才手动操作

---

### 核心设计原则

1. **最小化依赖**：common模块保持轻量，只放必需的功能
2. **统一规范**：工具类提供项目统一的格式和标准（如日期格式、脱敏规则）
3. **接口抽象**：如 `FileStorageService`，支持多种实现切换
4. **按需实现**：不是所有功能都要提前做好，用到时再补充

---

### 设计参考

#### PageResult 设计
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult {
    private List records;    // 数据列表
    private long total;      // 总记录数
    private long current;    // 当前页
    private long size;       // 每页大小
    private long pages;      // 总页数

    public static <T> PageResult of(Page page) {
        return new PageResult<>(
            page.getRecords(),
            page.getTotal(),
            page.getCurrent(),
            page.getSize(),
            page.getPages()
        );
    }
}
```

#### PasswordEncoder 使用
```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

// 使用
String encoded = passwordEncoder.encode(rawPassword);
boolean matches = passwordEncoder.matches(rawPassword, encoded);
```

#### StringUtil 脱敏规则
```java
// 手机号：138****1234（保留前3后4）
public static String maskPhone(String phone)

// 身份证：330102********1234（保留前6后4）
public static String maskIdCard(String idCard)

// 邮箱：a***@example.com（保留首字母）
public static String maskEmail(String email)
```

---

## 相关文档

- [已完成的功能](../docs/已完成的功能.md)
- [后端开发指导文档](../order-platform-backend/后端开发指导文档.md)
- [数据库设计文档](../docs/数据库/0.数据库设计文档.md)

---

## 维护者

- **开发组** - 初始开发

---

## 许可证

本项目采用 MIT 许可证。
