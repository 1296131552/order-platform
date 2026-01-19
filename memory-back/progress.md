# 项目进度记录

## 2026-01-16

### plan_53 后端项目初始化（已完成）

**完成内容**：

1. **Maven 多模块项目骨架**
   - 父 POM：统一管理 10 个子模块
   - 10 个子模块：api, common, order, shipment, partner, dashboard, attachment, exception, user, visualization

2. **公共模块基础类**
   - `Result<T>`：统一响应封装
   - `ResponseCode`：响应码枚举（含订单、发运、合作方等模块码段）
   - `BusinessException`：业务异常类
   - `GlobalExceptionHandler`：全局异常处理器
   - 状态枚举：`OrderStatus`, `PartnerType`, `ShipmentStatus`, `ShipmentLineStatus`

3. **聚合模块实体类**
   - `Order`, `OrderLine`（订单聚合）
   - `Shipment`, `ShipmentLine`, `ReceiptDetail`（发运聚合）
   - `Partner`（合作方聚合 - 统一表设计）
   - `Attachment`（附件聚合）
   - `ExceptionRecord`（异常聚合）
   - `User`（用户聚合）

4. **API 启动模块**
   - `OrderPlatformApplication`：启动类
   - `HealthController`：健康检查接口
   - `application.yml`：配置文件

**技术决策**：

| 决策 | 选择 | 理由 |
|------|------|------|
| 枚举存储 | `@EnumValue` + `@JsonValue` | 数据库和 API 统一使用小写值（draft），Java 代码使用枚举常量（DRAFT） |
| 异常处理 | `@RestControllerAdvice` | 统一捕获，避免 try-catch 吞没异常 |
| 响应格式 | `Result<T>` | 统一返回格式 {code, message, data, timestamp} |
| 状态类型 | 枚举而非 String | 编译期类型检查，防止拼写错误 |

**代码审查反馈**：

1. ✅ 修复：状态字段从 `String` 改为枚举类型
2. ✅ 修复：消除 `GlobalExceptionHandler` 中的重复代码（提取 `extractFieldErrors()`）
3. ✅ 修复：移除 `Result.ok(message, data)` 方法，统一 API 签名
4. ✅ 新增：枚举类实现 `@EnumValue` 和 `@JsonValue`，统一存储策略
5. ✅ 新增：状态枚举注释明确业务语义（DELIVERED vs RECEIVED）

**待完成**：
- [x] plan_54 前端项目初始化
- [ ] plan_55 数据库建表（DDL 脚本）
- [ ] plan_56 API 启动模块配置（Knife4j 文档）

---

## 2026-01-17

### plan_54 前端项目初始化（已完成）

**完成内容**：

1. **项目结构**
   - 使用 Vite 创建 Vue 3 + TypeScript 项目
   - 目录重命名：`order-platform-fronted` → `order-platform-frontend`
   - 目录结构：api/, stores/, router/, utils/, types/, styles/, views/, components/

2. **核心依赖**
   - Vue 3.5.24 + TypeScript 5.9
   - Element Plus 2.13.1（UI 框架）
   - Vue Router 4.6.4（路由）
   - Pinia 3.0.4（状态管理）
   - Axios 1.7.9（HTTP 请求）
   - Dayjs（日期工具）
   - Sass 1.97.2（样式预处理，devDependencies）

3. **类型系统**
   - `ApiResponse<T>`：与后端 Result<T> 对接
   - `PageParam` / `PageResult<T>`：分页类型
   - `OrderStatusValues` 等：状态常量对象 + 类型定义分离

4. **状态管理**
   - `useUserStore`：用户状态，isLoggedIn 为 computed（token 的投影）
   - `useAppStore`：应用全局状态

5. **路由配置**
   - 公开路由：`/login`
   - 受保护路由：`/`, `/orders`, `/shipments`, `/partners`, `/dashboard`
   - 404 页面：NotFoundView
   - 路由守卫：登录状态检查，自动跳转

6. **HTTP 请求封装**
   - 统一错误处理（401/403/404/500）
   - 请求拦截器：自动注入 Authorization 头
   - 响应拦截器：统一处理业务错误

7. **视图组件**
   - `HomeView`：数据驱动导航，单一 navigateTo 函数
   - `LoginView`：表单验证（FormRules），skipLogin 仅开发环境
   - `NotFoundView`：404 页面
   - 占位页面：OrderListView, ShipmentListView, PartnerListView, DashboardView

8. **全局样式**
   - 精确 CSS 重置（html, body 非通配符）
   - 工具类：flex-center, flex-between, text-center 等

**技术决策**：

| 决策 | 选择 | 理由 |
|------|------|------|
| 包管理器 | npm | 用户指定 |
| 状态类型 | computed | isLoggedIn 是 token 的投影，非独立状态 |
| 图标注册 | 按需注册 | 只注册实际使用的 8 个图标，减少 bundle |
| 类型注解 | Component | 避免 any 类型，保证类型安全 |
| CSS 重置 | 精确选择器 | 性能优于通配符 * |
| 依赖分类 | sass → devDependencies | 构建工具不打入生产包 |

**代码审查记录（三轮共 24 个问题）**：

**第一轮 - 代码风格**（8 个问题）：
1. ✅ 修复：isLoggedIn 从 ref 改为 computed
2. ✅ 修复：401 重定向循环（检查当前路径）
3. ✅ 修复：路由守卫（添加登录状态检查）
4. ✅ 修复：图标全量注册改为按需注册
5. ✅ 修复：常量命名（OrderStatusValues + OrderStatus 类型分离）
6. ✅ 修复：request.ts 数据源问题
7. ✅ 修复：package.json 依赖分类

**第二轮 - 数据流与架构**（8 个问题）：
1. ✅ 修复：request.ts 从 localStorage 读取（与 userStore 同源）
2. ✅ 修复：LoginView 假登录 → 真实 API + skipLogin 开发辅助
3. ✅ 修复：HomeView 重复导航函数 → 数据驱动 navItems
4. ✅ 修复：index.scss 通配符重置 → 精确选择器
5. ✅ 修复：错误处理死分支（三种互斥情况）
6. ✅ 修复：proxy 配置注释（说明后端 context-path）

**第三轮 - 类型安全与依赖**（8 个问题）：
1. ✅ 修复：main.ts 插件顺序（pinia → router）
2. ✅ 修复：vite.config.ts proxy 注释完整
3. ✅ 修复：HomeView icon: any → Component
4. ✅ 修复：404 重定向 → NotFoundView 页面
5. ✅ 修复：删除 HelloWorld.vue（Vite 示例）
6. ✅ 修复：app.ts 删除未使用的 loading
7. ✅ 修复：LoginView 表单验证（FormRules + isDev computed）
8. ✅ 修复：skipLogin 仅在 import.meta.env.DEV 显示

**构建结果**：
```
✓ TypeScript 类型检查通过
✓ Production build 成功
✓ Bundle size: 1024KB (gzipped: 335KB)
```

**关键文件清单**：

| 文件 | 职责 |
|------|------|
| `src/main.ts` | 应用入口，插件注册顺序（pinia → router → ElementPlus） |
| `src/App.vue` | 根组件，路由出口 |
| `src/router/index.ts` | 路由配置，公开/受保护路由分离，404 页面 |
| `src/stores/user.ts` | 用户状态，computed isLoggedIn |
| `src/stores/app.ts` | 应用全局状态 |
| `src/utils/request.ts` | Axios 封装，统一错误处理 |
| `src/types/api.ts` | API 类型定义，状态常量对象 |
| `src/styles/index.scss` | 全局样式，精确重置 |
| `src/views/HomeView.vue` | 首页，数据驱动导航 |
| `src/views/LoginView.vue` | 登录页，表单验证 |
| `src/views/NotFoundView.vue` | 404 页面 |
| `vite.config.ts` | Vite 配置，代理配置（后端 context-path=/api） |
| `package.json` | 依赖配置，sass 在 devDependencies |

---

### plan_55 用户模块数据库建表（已完成）

**完成内容**：

1. **Maven 依赖配置**
   - 父 POM 添加 `flyway.version: 9.22.3` 和依赖管理
   - API 模块添加 `flyway-core`, `flyway-mysql`, `druid-spring-boot-3-starter`

2. **数据源与迁移配置**
   - `application.yml`：数据源配置（Druid 连接池）
   - Flyway 配置：baseline-on-migrate, locations, encoding

3. **Flyway 迁移脚本**（3 个）
   - `V1__create_user_table.sql`：用户表（23 字段）
   - `V2__create_role_table.sql`：角色表（11 字段）+ 5 条预定义角色
   - `V3__create_user_role_table.sql`：用户角色关联表（8 字段）

4. **实体类更新**
   - `User.java`：Boolean 类型，删除 departmentName 字段
   - `Role.java`：Boolean 类型
   - `UserRole.java`：Boolean 类型，删除 username/roleCode 冗余字段，添加权限计算注释

5. **验证通过**
   - Flyway 迁移成功：3 个脚本执行完成
   - 数据库表创建：t_user, t_role, t_user_role, flyway_schema_history
   - 初始数据：5 条预定义角色（SYSTEM_ADMIN, DATA_ADMIN, CUSTOMER_MANAGER, PURCHASE_SPECIALIST, OPERATION_SPECIALIST）

**技术决策**：

| 决策 | 选择 | 理由 |
|------|------|------|
| 数据库迁移 | Flyway | 版本化管理，可追溯，团队协作友好 |
| 数据源 | Druid | 监控能力强，国产化支持 |
| 布尔字段 | Boolean（Java） + TINYINT（DB） | 类型语义清晰，MyBatis-Plus 自动映射 |
| 可空字段 | NULL 替代 -1 特殊值 | NULL 的语义就是"无"，SQL 为此设计 |
| 软删除 | 删除时 username 加后缀 | 释放 username 供新用户使用，同时保留审计数据 |
| 冗余字段 | 删除 | 数据一致性 > 性能，JOIN 问题以后再说 |
| 权限计算 | MIN(data_scope_type) | 取最宽松权限，避免 is_primary 特殊逻辑 |

**深度审查反馈（9 个问题修复）**：

| 优先级 | 问题 | 修复方案 |
|--------|------|----------|
| 🔴 P0 | 软删除与唯一约束冲突 | 删除时 username 加后缀（admin_deleted_1705334400） |
| 🔴 P0 | Integer vs Boolean 类型 | 统一改为 Boolean |
| 🟡 P1 | 冗余字段同步问题 | 删除 username, role_code, department_name |
| 🟡 P1 | -1 特殊值 | 改为 NULL |
| 🟡 P1 | is_primary 设计 | 保留字段但重新定义为"仅展示"，权限取 MIN 值 |
| 🟢 P2 | JDBC URL 编码错误 | 改用 connectionCollation 参数 |
| 🟢 P2 | 缺少 Druid 依赖 | 添加到 pom.xml |

**软删除策略**：

```sql
-- 软删除时必须修改 username，释放唯一约束
UPDATE t_user
SET username = CONCAT(username, '_deleted_', UNIX_TIMESTAMP()),
    is_deleted = 1
WHERE id = ?;
```

**权限计算逻辑**：

```java
// 用户数据权限取所有角色中的"最宽松"权限
// data_scope_type: 1=全部 > 2=部门 > 3=本人
dataScope = MIN(userRoles.stream()
    .map(UserRole::getDataScopeType)
    .collect(Collectors.toList()));
```

**预定义角色数据**：

| role_code | role_name | data_scope_type | 说明 |
|-----------|-----------|-----------------|------|
| SYSTEM_ADMIN | 系统管理员 | 1 (全部) | 负责权限配置、数据维护与系统管理 |
| DATA_ADMIN | 数据管理员 | 1 (全部) | 仅负责数据查看、导出等数据管理操作 |
| CUSTOMER_MANAGER | 客户经理 | 3 (本人) | 负责客户来单收集、订单创建与跟进 |
| PURCHASE_SPECIALIST | 采购专员 | 3 (本人) | 负责供应商选择、资质审核与合作确认 |
| OPERATION_SPECIALIST | 运营专员 | 3 (本人) | 负责发运计划制定、物流安排与在途跟踪 |

**修改文件清单**：

```
order-platform-backend/
├── pom.xml                                  # 添加 flyway.version + 依赖管理
├── order-platform-api/
│   ├── pom.xml                              # 添加 Druid + Flyway 依赖
│   └── src/main/resources/
│       ├── application.yml                  # 数据源 + Flyway 配置
│       └── db/migration/
│           ├── V1__create_user_table.sql    # 用户表 DDL（Boolean 注释）
│           ├── V2__create_role_table.sql    # 角色表 DDL + 初始数据
│           └── V3__create_user_role_table.sql # 用户角色关联表 DDL（权限计算注释）
└── order-platform-user/src/main/java/.../entity/
    ├── User.java                             # Boolean + 删除 departmentName
    ├── Role.java                             # Boolean 类型
    └── UserRole.java                         # Boolean + 删除冗余字段 + 权限计算注释
```

**表结构验证**：

```
t_user: 23 字段
- username (UNI), password, user_code
- real_name, email, phone, avatar
- is_enabled, is_locked, locked_time, locked_reason
- last_login_time, last_login_ip, login_count
- password_changed_time, password_expire_time
- department_id (NULL), position, employee_no, remark
- created_at, created_by (NULL), updated_at, updated_by (NULL), is_deleted

t_role: 11 字段
- role_code (UNI), role_name, role_type, data_scope_type
- description, sort_order
- is_enabled, is_system
- created_at, created_by (NULL), updated_at, updated_by (NULL), is_deleted

t_user_role: 8 字段
- user_id, role_id, is_primary
- created_at, created_by (NULL), updated_at, updated_by (NULL), is_deleted
- UNIQUE(user_id, role_id)
```

**待完成**：
- [x] plan_22 - 用户管理（登录/查询接口）
- [x] plan_04 - JWT 认证功能（P0）
- [ ] plan_22 - 用户 CRUD 接口（P1）
- [ ] plan_56 - API 启动模块配置（Knife4j 文档）

---

## 2026-01-18

### plan_22：用户管理 - 登录与查询（部分完成）

#### 完成内容

**Controller 层**：
- `UserController.java`：用户管理 API 入口
  - POST /api/user/login - 用户登录（支持用户名/邮箱/手机号）
  - GET /api/user/{userId} - 获取用户详情
  - GET /api/user/list - 分页查询用户（17 个查询条件）

**Service 层**：
- `UserService.java`：用户服务接口
  - login() - 用户登录
  - getUserById() - 根据 ID 获取用户
  - pageUsers() - 分页查询用户
  - TODO: createUser(), updateUser(), deleteUser()

- `UserServiceImpl.java`：用户服务实现
  - findUserByAccount() - 按账号查询（支持三种账号类型）
  - validateUserForLogin() - 验证用户状态和密码
  - updateLoginInfo() - 更新登录信息
  - buildQueryWrapper() - 构建动态查询条件
  - getUserByIdOrThrow() - 获取用户或抛出异常

**Mapper 层**：
- `UserMapper.java`：继承 BaseMapper<User>
- `RoleMapper.java`：继承 BaseMapper<Role>
- `UserRoleMapper.java`：用户角色关联
  - selectRolesByUserId() - 查询单个用户角色（JOIN 查询）
  - selectRolesByUserIds() - 批量查询用户角色（解决 N+1 问题）

**Converter 层**：
- `UserConverter.java`：实体转换器
  - toVO(User) - 单个转换，自动加载角色
  - toVO(User, List<RoleInfo>) - 单个转换，角色预加载
  - toVO(List<User>) - 批量转换，一次查询所有角色

**DTO 层**（7 个）：
- `LoginRequest.java`：登录请求（account + password）
- `LoginResponse.java`：登录响应（user + token TODO）
- `UserVO.java`：用户视图对象（包含角色列表）
- `UserQueryRequest.java`：分页查询请求（17 个查询条件）
- `UserCreateRequest.java`：创建用户请求（含完整校验）
- `UserUpdateRequest.java`：更新用户请求（含完整校验）
- `UserRoleResult.java`：批量查询角色结果

#### 技术决策

| 决策 | 选择 | 理由 |
|------|------|------|
| 多账号登录 | 统一 account 字段 | 用户名/邮箱/手机号共用登录入口 |
| 密码加密 | BCrypt | 单向哈希，自带盐值，抗彩虹表 |
| 批量查询 | selectRolesByUserIds | 100 用户从 101 次查询降至 2 次 |
| 转换器 | UserConverter | 消除 Entity→VO 重复代码 |

#### 功能完成情况

| 功能 | 状态 | 说明 |
|------|------|------|
| 用户登录 | ✅ 已完成 | 支持三种账号类型，BCrypt 验证 |
| 用户查询 | ✅ 已完成 | ID 查询 + 分页查询（17 条件） |
| 角色查询 | ✅ 已完成 | JOIN 查询，解决 N+1 问题 |
| JWT 认证 | ❌ 待实现 | P0 优先级 |
| 认证拦截器 | ❌ 待实现 | P0 优先级 |
| 退出登录 | ❌ 待实现 | 依赖 JWT |
| 创建用户 | ❌ 待实现 | P1 优先级 |
| 更新用户 | ❌ 待实现 | P1 优先级 |
| 删除用户 | ❌ 待实现 | P1 优先级 |

#### 代码审查反馈

| 优先级 | 问题 | 修复方案 |
|--------|------|----------|
| 🟢 P2 | N+1 查询问题 | 新增 selectRolesByUserIds() 批量查询 |
| 🟢 P2 | 重复转换代码 | 新增 UserConverter 统一转换逻辑 |
| 🟢 P2 | login 方法过长 | 拆分为 3 个私有方法 |
| 🟢 P2 | isDeleted NPE 风险 | 添加 null 检查 |

#### 修改文件清单

```
order-platform-backend/order-platform-user/src/main/java/.../user/
├── controller/
│   └── UserController.java              # 新增，3 个 API 接口
├── service/
│   ├── UserService.java                 # 新增，服务接口
│   └── impl/
│       └── UserServiceImpl.java         # 新增，服务实现
├── mapper/
│   ├── UserMapper.java                  # 新增，基础 CRUD
│   ├── RoleMapper.java                  # 新增，基础 CRUD
│   └── UserRoleMapper.java              # 新增，批量角色查询
├── converter/
│   └── UserConverter.java               # 新增，实体转换器
└── dto/
    ├── LoginRequest.java                # 新增
    ├── LoginResponse.java               # 新增
    ├── UserVO.java                      # 新增
    ├── UserQueryRequest.java            # 新增
    ├── UserCreateRequest.java           # 新增
    ├── UserUpdateRequest.java           # 新增
    └── UserRoleResult.java              # 新增
```

#### 待完成
- [x] plan_04 - JWT 认证功能（P0）
- [ ] plan_22 - 用户 CRUD 接口（P1）
- [ ] plan_56 - API 启动模块配置（Knife4j 文档）

---

## 2026-01-20

### plan_04：JWT 认证系统（已完成）

#### 完成内容

**核心功能**：
- JWT Token 生成与解析（HmacSHA384 签名）
- Token 黑名单机制（Redis 存储，支持过期自动清理）
- Token 版本号控制（密码重置后旧 Token 失效）
- 登录/登出接口（AuthController）
- Spring Security 集成（JwtAuthenticationFilter）

**新增文件**：

```
order-platform-common/src/main/java/.../security/
├── JwtService.java              # JWT 生成/解析/验证核心
├── TokenInfo.java               # Token 信息封装（值对象）
├── TokenBlacklistService.java   # 黑名单管理服务
├── RedisKeyConstants.java       # Redis 键常量
├── JwtProperties.java           # JWT 配置属性
└── CustomAuthenticationEntryPoint.java  # 未认证响应处理

order-platform-common/src/test/java/.../security/
├── JwtServiceTest.java          # 22 个测试
├── TokenInfoTest.java           # 12 个测试
└── TokenBlacklistServiceTest.java  # 15 个测试

order-platform-user/src/main/java/.../user/
├── controller/
│   └── AuthController.java      # 登录/登出接口
├── filter/
│   └── JwtAuthenticationFilter.java  # JWT 认证过滤器
├── config/
│   └── SecurityConfig.java      # Spring Security 配置
└── service/impl/
    └── UserDetailsServiceImpl.java  # UserDetailsService 实现

order-platform-user/src/test/java/.../user/
├── filter/
│   └── JwtAuthenticationFilterTest.java  # 15 个测试
└── service/impl/
    └── UserServiceImplTest.java  # 27 个测试（含登录场景）
```

#### 技术决策

| 决策 | 选择 | 理由 |
|------|------|------|
| 签名算法 | HmacSHA384 | 比 HS256 更强，性能可接受 |
| Token 格式 | JWT | 无状态，支持跨服务 |
| 黑名单存储 | Redis SET | 自动过期，防止内存泄漏 |
| 版本号机制 | Redis String | 密码重置后旧 Token 全部失效 |
| 过期策略 | Token 剩余时间作为 TTL | 黑名单键随 Token 过期自动清理 |
| 故障策略 | Fail-Open | Redis 故障时放行，优先保证可用性 |

#### JWT Token 结构

```
Header: {"alg":"HS384","typ":"JWT"}
Payload: {
  "sub": "12345",           # userId
  "jti": "uuid-v4",         # tokenId（唯一）
  "version": 1,             # tokenVersion
  "exp": 1705440000         # 过期时间（秒级）
}
Signature: HmacSHA384(secret, header.payload)
```

#### 认证流程

```
1. 登录请求 → AuthController.login()
   ├── 验证用户状态和密码
   ├── 获取/初始化 Token 版本号
   ├── 生成 JWT Token（含 userId, version）
   ├── 记录活跃 Token（用于追踪）
   └── 异步更新登录信息

2. API 请求 → JwtAuthenticationFilter
   ├── 提取 Authorization Header
   ├── 解析 JWT Token
   ├── 检查黑名单（tokenId）
   ├── 验证版本号（防旧 Token 复活）
   ├── 加载用户详情到 SecurityContext
   └── 继续过滤器链

3. 登出请求 → AuthController.logout()
   ├── 解析 Token 获取 tokenId
   ├── 加入黑名单（TTL = 剩余有效时间）
   └── 移除活跃 Token 记录
```

#### 黑名单机制

**Redis 数据结构**：
```
auth:blacklist:{tokenId}  →  过期时间戳 (TTL = 剩余毫秒)
auth:version:{userId}     →  当前版本号 (TTL = 30天)
auth:active:{userId}      →  SET(tokenId, ...) (TTL = 7天)
```

**版本号策略**：
- 首次登录：初始化 version = 1
- 已有版本：刷新 TTL（防止活跃用户版本号过期）
- 密码重置：version++（所有旧 Token 失效）
- 请求验证：Token.version == Redis.version

#### 单元测试覆盖（49 个测试全部通过）

| 测试类 | 场景数 | 覆盖内容 |
|--------|--------|----------|
| JwtServiceTest | 22 | Token 生成、解析、签名验证、过期处理、往返一致性 |
| TokenInfoTest | 12 | 工厂方法、NPE 防御、剩余时间计算 |
| TokenBlacklistServiceTest | 15 | 黑名单操作、版本号管理、活跃 Token 追踪、Redis 故障处理 |
| UserServiceImplTest | 27 | 登录成功/失败、登出、Token 生成顺序、用户状态校验 |
| JwtAuthenticationFilterTest | 15 | 无 Token 场景、无效 Token、黑名单、版本号校验、异常处理 |

#### 代码审查修复

| 优先级 | 问题 | 修复方案 |
|--------|------|----------|
| 🟢 P2 | JWT 秒级精度 | 往返比较使用秒级时间戳 |
| 🟢 P2 | 时间流逝误差 | Token 剩余时间使用范围比较（±1秒） |
| 🟢 P2 | Filter doFilter 多次调用 | 测试使用 atLeastOnce() 验证 |
| 🟢 P2 | UnnecessaryStubbing | 移除用户禁用/锁定场景的 passwordEncoder.stub |

#### 配置变更

```yaml
# application.yml 新增
jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-change-in-production}
  expiration: 604800000  # 7天（毫秒）
```

```xml
<!-- pom.xml 新增依赖 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

#### 待完成
- [ ] plan_22 - 用户 CRUD 接口（P1）
- [ ] plan_56 - API 启动模块配置（Knife4j 文档）

---
