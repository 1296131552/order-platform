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

#### 安全认证 (security/)

| 文件 | 职责 | 关键设计 |
|------|------|----------|
| `JwtService.java` | JWT 核心服务 | 生成/解析/验证 Token，HmacSHA384 签名 |
| `TokenInfo.java` | Token 值对象 | 封装 userId, tokenId, version, expiration；工厂方法创建；NPE 防御 |
| `TokenBlacklistService.java` | 黑名单管理 | Redis 存储，TTL = 剩余有效时间，故障时 Fail-Open |
| `RedisKeyConstants.java` | Redis 键常量 | 黑名单/版本号/活跃 Token 的键前缀与 TTL 常量 |
| `JwtProperties.java` | JWT 配置属性 | @ConfigurationProperties 绑定 application.yml |
| `CustomAuthenticationEntryPoint.java` | 未认证响应 | 返回 401 而非重定向，适配前后端分离 |


**JWT Token 结构**：
```
Header: {"alg":"HS384","typ":"JWT"}
Payload: {
  "sub": userId,              # 用户ID
  "jti": tokenId,             # Token唯一标识（UUID）
  "version": tokenVersion,    # 版本号（密码重置后递增）
  "exp": expiration           # 过期时间（Unix秒级时间戳）
}
Signature: HmacSHA384(secretKey, header.payload)
```

**Redis 数据结构**：
```
auth:blacklist:{tokenId}  →  SET/TTL (过期时间戳，TTL=Token剩余时间)
auth:version:{userId}     →  STRING (当前版本号，TTL=30天)
auth:active:{userId}      →  SET (活跃tokenId列表，TTL=7天)
```

**版本号机制**：
- 首次登录：version = 1
- 已登录：刷新 TTL（防止活跃用户版本号过期）
- 密码重置：version++（所有旧 Token 失效）
- 认证验证：Token.version == Redis.version

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

### 用户聚合 (order-platform-user)

用户模块是系统的基础服务，提供用户认证、授权和管理功能。采用 RBAC 权限模型，
支持多账号登录、数据权限控制和完整的用户生命周期管理。

#### 目录结构

```
com.company.user/
├── controller/       # API 控制层
├── service/          # 业务服务层
│   └── impl/         # 服务实现
├── mapper/           # 数据访问层
├── converter/        # 实体转换层
├── dto/              # 数据传输对象
└── entity/           # 实体模型
```

#### Controller 层

##### AuthController.java
**职责**：认证 API 入口，处理登录/登出

| 方法 | 路径 | 说明 |
|------|------|------|
| `login(LoginRequest)` | POST /api/auth/login | 用户登录，返回 JWT Token |
| `logout()` | POST /api/auth/logout | 用户登出，Token 加入黑名单 |

**关键设计**：
- 无状态认证，Token 存储在客户端
- 登录成功返回 Token 和用户信息
- 登出通过黑名单机制实现（服务端标记 Token 无效）

##### UserController.java
**职责**：用户管理 API 入口，处理 HTTP 请求/响应

| 方法 | 路径 | 说明 |
|------|------|------|
| `getUserById(Long)` | GET /api/users/{userId} | 根据ID查询用户详细信息 |
| `pageUsers(UserQueryRequest)` | GET /api/users/list | 分页查询用户，支持多条件筛选 |

**关键设计**：
- 使用 `@Tag` 和 `@Operation` 提供 Swagger 文档
- 统一返回 `Result<T>` 格式
- `@Valid` 触发参数校验
- 登录接口迁移至 AuthController（单一职责）

**TODO**：
- POST /api/users/create - 创建用户
- PUT /api/users/update - 更新用户
- DELETE /api/users/delete/{userId} - 删除用户

#### Filter 层

##### JwtAuthenticationFilter.java
**职责**：JWT 认证过滤器，拦截所有 HTTP 请求进行身份验证

**执行流程**：
```
1. 检查已认证状态 → 已认证则跳过
2. 提取 Authorization Header → Bearer {token}
3. 解析 JWT Token → 获取 userId, tokenId, version
4. 验证 Token 有效性 → 检查过期、签名
5. 检查黑名单 → tokenId 是否在黑名单中
6. 验证版本号 → Token.version == Redis.version
7. 加载用户详情 → 设置 SecurityContext
8. 继续过滤器链
```

**关键设计**：
- 继承 `OncePerRequestFilter`，确保每请求只执行一次
- 无 Token 时跳过认证（匿名访问）
- Redis 故障时放行（Fail-Open 策略）
- finally 块保证 doFilter 一定会被调用

#### Config 层

##### SecurityConfig.java
**职责**：Spring Security 配置，定义认证规则

**配置内容**：
- 禁用 CSRF（前后端分离不需要）
- 禁用 Session（无状态认证）
- 配置异常处理入口（CustomAuthenticationEntryPoint）
- 注册 JwtAuthenticationFilter
- 白名单路径：/api/auth/login, /doc.html 等

##### UserDetailsServiceImpl.java
**职责**：UserDetailsService 实现，为 SecurityContext 提供用户详情

**方法签名**：
```java
public UserDetails loadUserById(Long userId) throws UsernameNotFoundException;
```

**关键设计**：
- 实现 Spring Security 的 UserDetails 接口
- 查询用户 + 角色权限
- 将用户状态映射为 enabled/locked/accountExpired

---

#### Service 层

##### UserService.java
**职责**：用户服务接口定义，暴露业务能力

**方法签名**：
```java
// 用户认证
LoginResponse login(LoginRequest request);
void logout(String rawToken);

// 用户查询
UserVO getUserById(Long userId);
Page<UserVO> pageUsers(UserQueryRequest request);

// 用户管理（TODO）
// Long createUser(UserCreateRequest request);
// void updateUser(UserUpdateRequest request);
// void deleteUser(Long userId);

// 密码管理（TODO，待权限模块完成）
// void changePassword(Long userId, String oldPassword, String newPassword);
// void resetPassword(Long userId, String newPassword);
```

---

##### UserServiceImpl.java
**职责**：用户服务实现，核心业务逻辑

**依赖**：
- `UserMapper` - 用户数据访问
- `UserRoleMapper` - 角色数据访问
- `UserConverter` - 实体转换
- `PasswordEncoder` - 密码加密（BCrypt）
- `JwtService` - JWT Token 生成/解析
- `TokenBlacklistService` - 黑名单管理

**核心方法**：

| 方法 | 职责 | 关键逻辑 |
|------|------|----------|
| `login()` | 用户登录 | 1. 按账号查找用户<br>2. 验证状态（启用/锁定/删除）<br>3. BCrypt 验证密码<br>4. 获取/初始化 Token 版本号<br>5. 生成 JWT Token<br>6. 记录活跃 Token<br>7. 异步更新登录信息 |
| `logout()` | 用户登出 | 1. 解析 Token 获取 tokenId/userId<br>2. 加入黑名单（TTL=剩余时间）<br>3. 清除活跃 Token |
| `findUserByAccount()` | 按账号查询 | 依次尝试 username/email/phone 字段 |
| `validateUserForLogin()` | 验证登录状态 | 检查 isEnabled, isLocked, isDeleted<br>检查是否锁定过期 |
| `updateLoginInfoAsync()` | 异步更新登录信息 | 更新最后登录时间、IP、登录次数 |
| `getUserById()` | 获取用户详情 | 查询用户 + 加载角色列表 |
| `pageUsers()` | 分页查询 | 动态构建查询条件 + 批量加载角色 |
| `buildQueryWrapper()` | 构建查询条件 | 17 个可选条件的动态组装 |
| `getUserByIdOrThrow()` | 获取或抛出 | 不存在或已删除时抛出异常 |

**事务管理**：
- `login()` - 无事务（Token 生成先于数据库操作）
- `updateLoginInfoAsync()` - `@Transactional` + `@Async`

**Token 生成顺序**（重要）：
1. 验证用户状态和密码
2. 获取/初始化 Token 版本号
3. 生成 JWT Token（不加事务，避免数据库失败导致 Token 丢失）
4. 记录活跃 Token
5. 异步更新登录信息（不阻塞返回）

**查询条件支持**（17 个）：
- 账号信息：username, userCode, email, phone（模糊）
- 基本信息：realName, position, employeeNo（模糊）
- 状态筛选：isEnabled, isLocked
- 组织权限：departmentId, roleId
- 时间范围：createdAt[Start/End], lastLoginTime[Start/End]

---

#### Mapper 层

##### UserMapper.java
**职责**：用户数据访问

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

**功能**：继承 MyBatis-Plus `BaseMapper`，获得基础 CRUD 能力

---

##### RoleMapper.java
**职责**：角色数据访问

```java
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}
```

**功能**：继承 MyBatis-Plus `BaseMapper`，获得基础 CRUD 能力

---

##### UserRoleMapper.java
**职责**：用户角色关联，批量查询优化

**方法签名**：
```java
// 查询单个用户的角色（JOIN 查询）
List<RoleInfo> selectRolesByUserId(@Param("userId") Long userId);

// 批量查询多个用户的角色（解决 N+1 问题）
List<UserRoleResult> selectRolesByUserIds(@Param("userIds") List<Long> userIds);
```

**性能优化**：
- 单用户查询：使用 JOIN，一次查询完成
- 批量查询：使用 `IN` + JOIN，100 用户从 101 次查询降至 2 次

**SQL 示例**：
```sql
-- 批量查询
SELECT ur.user_id, r.id, r.role_code, r.role_name,
       r.data_scope_type, ur.is_primary
FROM t_user_role ur
JOIN t_role r ON ur.role_id = r.id
WHERE ur.user_id IN (1, 2, 3, ...)
AND ur.is_deleted = false AND r.is_enabled = true
ORDER BY ur.user_id, ur.is_primary DESC
```

---

#### Converter 层

##### UserConverter.java
**职责**：Entity 与 VO 转换，解决重复代码和 N+1 问题

**方法签名**：
```java
// 单个转换（自动加载角色）
UserVO toVO(User user);

// 单个转换（角色预加载）
UserVO toVO(User user, List<RoleInfo> roles);

// 批量转换（一次查询所有角色）
List<UserVO> toVO(List<User> users);
```

**性能优化**：
- 单个转换：调用 `selectRolesByUserId()`
- 批量转换：调用 `selectRolesByUserIds()`，然后 Map 分组

**效果对比**：
| 场景 | 优化前 | 优化后 |
|------|--------|--------|
| 100 个用户查询 | 101 次 DB 查询 | 2 次 DB 查询 |

---

#### DTO 层

##### LoginRequest.java
**职责**：登录请求数据

| 字段 | 类型 | 校验规则 |
|------|------|----------|
| account | String | @NotBlank |
| password | String | @NotBlank |

---

##### LoginResponse.java
**职责**：登录响应数据

| 字段 | 类型 | 说明 |
|------|------|------|
| user | UserVO | 用户信息（复用 UserVO） |
| token | String | JWT Token（已实现） |

---

##### UserVO.java
**职责**：用户视图对象，返回给前端

**字段列表**：
- 基础信息：id, username, userCode, realName
- 联系方式：email, phone
- 状态控制：isEnabled, isLocked
- 组织信息：position, employeeNo
- 登录信息：lastLoginTime, lastLoginIp, loginCount
- 角色列表：roles（List<RoleInfo>）

**内部类 RoleInfo**：
```java
public static class RoleInfo {
    private Long roleId;
    private String roleCode;
    private String roleName;
    private Integer dataScopeType;  // 1=全部, 2=部门, 3=本人, 4=自定义
    private Boolean isPrimary;
}
```

---

##### UserQueryRequest.java
**职责**：分页查询请求

**查询条件（17 个）**：
- 账号信息：username, userCode, email, phone（模糊）
- 基本信息：realName, position, employeeNo（模糊）
- 状态筛选：isEnabled, isLocked
- 组织权限：departmentId, roleId
- 时间范围：createdAtStart/End, lastLoginTimeStart/End
- 分页参数：pageNum（默认1）, pageSize（默认10）

---

##### UserCreateRequest.java
**职责**：创建用户请求

**必填字段**：
- username: 3-20 位，字母数字下划线
- password: 6-20 位
- realName: 最多 20 位

**可选字段**：
- email: 邮箱格式
- phone: 手机号格式（1[3-9]\d{9}）
- roleIds: 角色列表
- departmentId, position, employeeNo, remark

---

##### UserUpdateRequest.java
**职责**：更新用户请求

**可更新字段**：
- realName, email, phone, avatar
- isEnabled, departmentId
- position, employeeNo, remark
- roleIds（角色同步）

---

##### UserRoleResult.java
**职责**：批量查询角色结果

**字段列表**：
- userId（用于分组）
- roleId, roleCode, roleName
- dataScopeType, isPrimary

**方法**：
- `toRoleInfo()` - 转换为 RoleInfo（去除 userId）

---

#### 架构设计要点

1. **无状态认证**
   - JWT Token 存储在客户端（Authorization Header）
   - 服务端通过黑名单实现登出（而非会话销毁）
   - Redis 故障时放行（Fail-Open），优先保证可用性

2. **Token 版本号机制**
   - 密码重置后版本号递增，所有旧 Token 失效
   - 版本号 TTL 30 天，防止活跃用户版本号过期导致旧 Token 复活
   - 登录时刷新版本号 TTL

3. **黑名单过期策略**
   - TTL = Token 剩余有效时间
   - Token 过期后黑名单键自动清理，无需人工维护

4. **多账号登录**
   - 统一 `account` 字段接收
   - 依次尝试 username/email/phone 匹配
   - 前端无需区分账号类型

5. **性能优化**
   - 批量角色查询消除 N+1 问题
   - Converter 层统一转换逻辑
   - 动态查询条件构建

6. **安全设计**
   - BCrypt 密码加密（单向哈希）
   - HmacSHA384 JWT 签名（比 HS256 更强）
   - 账号锁定机制（isLocked + lockedTime）
   - 软删除保护（isDeleted）

7. **异步更新**
   - 登录信息异步更新（不阻塞返回）
   - 采用最终一致性

8. **TODO 事项**
   - 用户 CRUD（P1）
   - 密码管理接口（P2）
   - 权限注解与拦截（P2）

---

#### Entity 层

| 文件 | 职责 | 关键设计 |
|------|------|----------|
| `User.java` | 用户实体 | Boolean 类型，软删除设计，23 字段 |
| `Role.java` | 角色实体 | 预定义 5 个系统角色，4 级数据权限 |
| `UserRole.java` | 用户角色关联 | 权限计算取 MIN(data_scope_type) |

---

## 数据库设计

### 迁移策略

使用 **Flyway** 进行版本化数据库迁移：

```
order-platform-api/
└── src/main/resources/
    └── db/migration/
        ├── V1__create_user_table.sql      # 用户表
        ├── V2__create_role_table.sql      # 角色表 + 初始数据
        └── V3__create_user_role_table.sql # 用户角色关联表
```

**命名规则**：
- `V{版本}__{描述}.sql`
- 版本号递增，描述用下划线分隔
- Flyway 按版本顺序执行，记录在 `flyway_schema_history` 表

### 表结构

#### t_user（用户表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(50) | 用户名，唯一索引 |
| password | VARCHAR(255) | 密码（BCrypt 加密） |
| is_enabled | TINYINT | 是否启用（Boolean 映射） |
| is_locked | TINYINT | 是否锁定（Boolean 映射） |
| department_id | BIGINT | 部门 ID（NULL 表示未分配） |
| created_by | BIGINT | 创建人 ID（NULL 表示系统创建） |
| is_deleted | TINYINT | 是否删除（Boolean 映射） |

**设计要点**：
- 布尔字段存储为 `TINYINT`，Java 映射为 `Boolean`
- 可空字段用 `NULL` 而非 `-1` 特殊值
- 软删除时修改 username 加后缀释放唯一约束

#### t_role（角色表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| role_code | VARCHAR(50) | 角色代码，唯一索引 |
| role_name | VARCHAR(50) | 角色名称 |
| data_scope_type | TINYINT | 数据权限：1-全部，2-部门，3-本人 |
| is_system | TINYINT | 是否系统角色（Boolean 映射） |

**预定义角色**：

| role_code | role_name | data_scope_type |
|-----------|-----------|-----------------|
| SYSTEM_ADMIN | 系统管理员 | 1 (全部) |
| DATA_ADMIN | 数据管理员 | 1 (全部) |
| CUSTOMER_MANAGER | 客户经理 | 3 (本人) |
| PURCHASE_SPECIALIST | 采购专员 | 3 (本人) |
| OPERATION_SPECIALIST | 运营专员 | 3 (本人) |

#### t_user_role（用户角色关联表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户 ID |
| role_id | BIGINT | 角色 ID |
| is_primary | TINYINT | 是否主角色（Boolean 映射，仅展示用） |

**唯一约束**：`UNIQUE(user_id, role_id)`

**权限计算**：取所有角色中权限最宽松的（`data_scope_type` 最小值）

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

---

## 后端基础设施配置

### 数据源配置（Druid）

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/order_platform?serverTimezone=Asia/Shanghai&connectionCollation=utf8mb4_unicode_ci
    username: root
    password: ${DB_PASSWORD:root}
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
```

**选择理由**：
- 监控能力强（内置监控页面）
- 国产化支持良好
- 连接池性能稳定

### Flyway 配置

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 0
    encoding: UTF-8
    validate-on-migrate: true
```

**选择理由**：
- 版本化管理，可追溯
- 团队协作友好，自动执行
- 失败即回滚，保证一致性

### MyBatis-Plus 配置

```yaml
mybatis-plus:
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

**布尔字段映射**：
- 数据库：`TINYINT` (0/1)
- Java：`Boolean` (true/false)
- MyBatis-Plus 自动处理转换

---

## 架构设计原则

### 1. 类型一致性

**布尔语义统一用 Boolean**：
```java
// ✅ 正确
private Boolean isEnabled;
private Boolean isLocked;
private Boolean isDeleted;

// ❌ 错误
private Integer isEnabled;  // 布尔语义不该用 Integer
```

### 2. 可空语义用 NULL

**"无"的语义用 NULL，不用特殊值**：
```sql
-- ✅ 正确
department_id BIGINT NULL DEFAULT NULL
created_by BIGINT NULL DEFAULT NULL

-- ❌ 错误
department_id BIGINT NOT NULL DEFAULT -1  -- -1 是 magic number
```

### 3. 软删除策略

**删除时修改唯一键字段**：
```sql
-- 软删除时必须修改 username，释放唯一约束
UPDATE t_user
SET username = CONCAT(username, '_deleted_', UNIX_TIMESTAMP()),
    is_deleted = 1
WHERE id = ?;
```

### 4. 权限计算简化

**避免特殊情况分支**：
```java
// ✅ 好品味：统一计算
dataScope = userRoles.stream()
    .map(UserRole::getDataScopeType)
    .min(Integer::compareTo)
    .orElse(3);

// ❌ 坏品味：特殊情况
if (user.getPrimaryRole() != null) {
    dataScope = user.getPrimaryRole().getDataScopeType();
} else {
    // 复杂逻辑...
}
```

### 5. 冗余字段删除

**数据一致性 > 性能**：
```sql
-- ✅ 正确：通过 JOIN 获取
SELECT u.*, r.*
FROM t_user u
JOIN t_user_role ur ON u.id = ur.user_id
JOIN t_role r ON ur.role_id = r.id;

-- ❌ 错误：冗余字段同步问题
t_user_role (user_id, role_id, username, role_code)
-- username/role_code 谁负责同步？
```

---
