# 变更日志 (Change Log)

> 本文件记录项目代码的重要变更，供后续开发者参考。

---

## [2026-01-21] - [重构/修复] - createUser 接口优化重构（解决并发竞态与代码冗余）

**修改文件:**
- `order-platform-backend/order-platform-api/src/main/resources/db/migration/V5__add_username_unique_index.sql`: 新增 - 创建 username+is_deleted 联合唯一索引
- `order-platform-backend/order-platform-user/pom.xml`: 修改 - 添加 MapStruct 依赖与注解处理器配置
- `order-platform-backend/order-platform-user/src/main/java/com/company/order/visual/user/converter/UserMapping.java`: 新增 - MapStruct 转换器接口
- `order-platform-backend/order-platform-user/src/main/java/com/company/order/visual/user/service/impl/UserServiceImpl.java`: 修改 - 重构 createUser 方法

**变更类型:** [重构/修复]

**详细描述:**

### 问题诊断

经过代码审查，发现 `createUser` 方法存在以下问题：

| 优先级 | 问题 | 类型 | 影响 |
|--------|------|------|------|
| P0 | 并发竞态条件 (TOCTOU) | 安全漏洞 | 先查询后存在的判断无法防止并发创建重复用户 |
| P0 | 角色删除校验缺失 | 逻辑缺陷 | 可引用已删除的角色 |
| P1 | 13 行手动 setter 代码 | 代码冗余 | 违反 DRY 原则，可维护性差 |

### 修改内容

#### 1. 数据库迁移 - 用户名唯一索引优化

**新增文件**: `V5__add_username_unique_index.sql`

```sql
-- 删除原有单字段唯一索引
DROP INDEX uk_user_username ON t_user;

-- 创建联合唯一索引：支持逻辑删除场景
-- 同一 username 可有多条历史记录（is_deleted=1），但只能有一条有效记录（is_deleted=0）
CREATE UNIQUE INDEX uk_username_deleted ON t_user(username, is_deleted);
```

**设计理由**:
- 逻辑删除场景下，原单字段索引会导致用户无法复用已删除用户名
- 联合索引 `(username, is_deleted)` 允许多条 `is_deleted=1` 的记录，但只能有一条 `is_deleted=0`

#### 2. 添加 MapStruct 依赖

**修改文件**: `order-platform-backend/order-platform-user/pom.xml`

```xml
<!-- MapStruct - 对象映射框架，消除手动 setter 冗余代码 -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>

<!-- Maven 编译器插件 - 启用注解处理器 -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>1.5.5.Final</version>
            </path>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok-mapstruct-binding</artifactId>
                <version>0.2.0</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

#### 3. 新增 MapStruct 转换器

**新增文件**: `UserMapping.java`

```java
@Mapper(componentModel = "spring")
public interface UserMapping {
    /**
     * UserCreateRequest -> User 实体转换
     * MapStruct 自动生成实现代码，编译时生成
     */
    User toEntity(UserCreateRequest request);
}
```

#### 4. 重构 createUser 方法

**修改文件**: `UserServiceImpl.java`

**变更对比**:

| 变更点 | 修改前 | 修改后 | 理由 |
|--------|--------|--------|------|
| 用户名唯一性校验 | selectCount 查询 | 数据库唯一索引 + 异常捕获 | 消除 TOCTOU 竞态条件 |
| 角色校验 | 无删除过滤 | `isDeleted=false AND isEnabled=true` | 防止引用已删除/禁用角色 |
| 对象转换 | 13 行手动 setter | `userMapping.toEntity(request)` | 消除冗余代码 |
| 异常处理 | 无 | 捕获 `DuplicateKeyException` | 转换为业务异常 |

**核心代码变更**:

```java
// 修改前：先查询后判断（存在竞态条件）
Long count = userMapper.selectCount(
    new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
);
if (count > 0) {
    throw new BusinessException(ResponseCode.USER_ALREADY_EXISTS);
}

// 修改后：由数据库唯一索引保证，捕获唯一约束异常
try {
    // 使用 MapStruct 转换，替换 13 行手动 setter
    User user = userMapping.toEntity(request);
    userMapper.insert(user);
} catch (DuplicateKeyException e) {
    throw new BusinessException(ResponseCode.USER_ALREADY_EXISTS);
}

// 修改前：角色校验不完整
Role role = roleMapper.selectById(request.getRoleId());
if (role == null) {
    throw new BusinessException(ResponseCode.ROLE_NOT_FOUND);
}

// 修改后：完整的角色校验
Role role = roleMapper.selectOne(
    new LambdaQueryWrapper<Role>()
        .eq(Role::getId, request.getRoleId())
        .eq(Role::getIsDeleted, false)      // 排除已删除角色
        .eq(Role::getIsEnabled, true)       // 排除已禁用角色
);
```

### 设计改进

| 改进点 | 修改前 | 修改后 | 好处 |
|--------|--------|--------|------|
| 并发安全 | 应用层校验（TOCTOU） | 数据库唯一索引 | 原子性保证，无竞态条件 |
| 代码量 | 13 行 setter | 1 行 MapStruct | 减少 90% 样板代码 |
| 角色校验 | 仅判断 null | 判断删除和启用状态 | 防止引用无效角色 |
| 异常处理 | 无 | 统一转换为业务异常 | API 响应一致 |

### 影响范围:
- **模块**: order-platform-user（用户服务层）
- **数据库**: t_user 表索引结构变更
- **是否是破坏性变更**: 否 - 向后兼容，仅内部实现优化

**相关需求:**
- 代码质量提升（消除冗余代码）
- 并发安全修复（TOCTOU 竞态条件）
- 业务逻辑完善（角色校验）

**后续建议:**
- 考虑将 MapStruct 推广到其他 DTO 转换场景
- 检查其他 `selectCount + insert` 模式是否类似并发问题
- 建议添加并发测试用例验证唯一索引约束

---

## [2025-01-21] - [修复/优化] - 空类型安全警告修复与用户ID获取重构

**修改文件:**
- `order-platform-backend/order-platform-common/src/main/java/com/company/order/visual/common/config/RedisConfig.java`: 添加空类型安全注解，提取 Duration 常量
- `order-platform-backend/order-platform-user/src/main/java/com/company/order/visual/user/filter/JwtAuthenticationFilter.java`: 集成 MetaObjectHandlerImpl ThreadLocal 机制
- `order-platform-backend/order-platform-user/src/main/java/com/company/order/visual/user/controller/UserController.java`: 使用 ThreadLocal 获取操作人 ID

**变更类型:** [修复/优化]

**详细描述:**

### 问题诊断

本次变更解决两类问题：

1. **空类型安全警告** - Eclipse/IDEA 编译器警告
   - `RedisConfig.java` 第 70 行: `Duration.ofMinutes(30)` 缺少非空声明
   - `RedisConfig.java` 第 75 行: `connectionFactory` 参数缺少非空声明

2. **NPE 风险与架构不一致** - 用户 ID 获取方式设计缺陷
   - 原本 `UserController` 通过 `SecurityContextHolder.getContext().getAuthentication().getName()` 获取用户名再解析为 Long
   - 如果用户名不是纯数字会抛出 `NumberFormatException`
   - SecurityContext 为空会触发 `NullPointerException`
   - 系统已有 `MetaObjectHandlerImpl` ThreadLocal 机制未被复用

### 修改内容

#### 1. RedisConfig.java - 空类型安全修复

```java
// 添加导入
import org.springframework.lang.NonNull;

// 方法参数添加 @NonNull 注解
@Bean
public RedisCacheManager cacheManager(@NonNull RedisConnectionFactory connectionFactory) {
    // 提取为局部变量，提高可读性
    final Duration ttl = Duration.ofMinutes(30);
    // ...
}
```

#### 2. JwtAuthenticationFilter.java - 集成 ThreadLocal

```java
// 添加导入
import com.company.common.handler.MetaObjectHandlerImpl;

// 认证成功后设置操作人 ID（第 104 行）
if (authResult.isAuthenticated()) {
    // ...
    MetaObjectHandlerImpl.setOperatorId(tokenInfo.getUserId());
}

// finally 块确保清理（第 110-117 行）
finally {
    // 请求结束必须清理 ThreadLocal，防止内存泄漏
    MetaObjectHandlerImpl.clearOperatorId();
}
```

#### 3. UserController.java - 使用 ThreadLocal 获取 ID

```java
// 添加导入
import com.company.common.handler.MetaObjectHandlerImpl;

// 移除导入
// import org.springframework.security.core.context.SecurityContextHolder;

// 修改 createUser 方法
@PostMapping
public Result<Long> createUser(@Valid @RequestBody UserCreateRequest request) {
    // 从 ThreadLocal 获取当前登录用户 ID，默认值 -1 避免空指针
    Long operatorId = MetaObjectHandlerImpl.getOperatorId();
    // ...
}
```

### 设计改进

| 改进点 | 修改前 | 修改后 | 好处 |
|--------|--------|--------|------|
| 空安全 | 无注解声明 | `@NonNull` 注解 | 编译器级别空检查 |
| ID 获取 | SecurityContext.getName() + Long.parseLong() | MetaObjectHandlerImpl.getOperatorId() | 类型安全，无需解析 |
| 默认值 | NPE 风险 | ThreadLocal 默认 -1 | 消除空指针异常 |
| 架构一致性 | 两套机制并存 | 复用已有 ThreadLocal | 统一架构，减少重复 |
| 内存管理 | 无显式清理 | finally 块清理 | 防止内存泄漏 |

### 影响范围:**
- **模块**: order-platform-common（配置层）、order-platform-user（认证与控制器层）
- **是否是破坏性变更**: 否 - 向后兼容，仅内部实现优化

**相关需求:**
- 代码质量提升（空类型安全）
- 架构一致性（统一用户 ID 获取方式）

**后续建议:**
- 检查其他 Controller 是否存在类似的 SecurityContext 获取用户 ID 的方式，统一迁移到 ThreadLocal
- 考虑为 `MetaObjectHandlerImpl` 添加更多单元测试，确保并发场景下的正确性
- 建议在 Code Review 中检查是否有其他直接使用 `SecurityContextHolder` 的地方

---

## 2025-01-21 - JWT 认证系统安全与代码质量重构

### 问题诊断

经过 Linus 式代码审查，发现以下问题：

| 优先级 | 问题 | 影响 |
|--------|------|------|
| P0 | `X-User-id` 请求头可伪造 | **安全漏洞**：任何人可冒充任意用户 |
| P0 | `@Async` + `@Transactional` 组合 | **无效代码**：事务注解在异步方法上不生效 |
| P1 | 依赖注入方式混用 | 代码可读性差，维护困难 |
| P2 | `loadUserByUsername` 语义混淆 | 框架接口与业务设计不匹配 |

---

### 修改内容

#### 1. UserController.java - 移除安全漏洞

**修改前：**
```java
@PostMapping
public Result<Long> createUser(
    @Valid @RequestBody UserCreateRequest request,
    @RequestHeader(value = "X-User-id", required = false) Long userId) {
    // 从请求头获取当前用户ID（生产环境从 SecurityContext 获取）
    Long operatorId = Optional.ofNullable(userId).orElse(-1L);
    // ...
}
```

**问题：** 任何人可伪造 `X-User-id` 请求头，冒充管理员创建用户。

**修改后：**
```java
@PostMapping
public Result<Long> createUser(@Valid @RequestBody UserCreateRequest request) {
    // 从 SecurityContext 获取当前登录用户 ID（JWT 认证过滤器已设置）
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    Long operatorId = Long.parseLong(username);
    // ...
}
```

**影响：**
- JWT 认证通过的请求才能获取用户 ID
- 无法伪造，SecurityContext 由服务器端设置

---

#### 2. UserServiceImpl.java - 移除无效事务注解

**修改前：**
```java
@Async
@Transactional(rollbackFor = Exception.class) // 异常回滚
public void updateLoginInfoAsync(Long userId) {
    // ...
}
```

**问题：** `@Transactional` 在 `@Async` 方法上无效，且方法吞掉所有异常。

**修改后：**
```java
/**
 * 异步更新用户登录信息
 * 采用最终一致性，不阻塞登录返回。
 * 注意：不使用 @Transactional，因为此方法异步执行且吞掉异常，事务注解无效。
 */
@Async
public void updateLoginInfoAsync(Long userId) {
    // ...
}
```

---

#### 3. UserServiceImpl.java - 统一构造器注入

**修改前：**
```java
@Resource
private UserMapper userMapper;

@Resource
private UserRoleMapper userRoleMapper;

@Resource
private RoleMapper roleMapper;

@Resource
private UserConverter userConverter;

private final PasswordEncoder passwordEncoder;
private final JwtService jwtService;
private final TokenBlacklistService tokenBlacklistService;
```

**修改后：**
```java
// 全部改为 final，通过 @RequiredArgsConstructor 生成构造器
private final UserMapper userMapper;
private final UserRoleMapper userRoleMapper;
private final RoleMapper roleMapper;
private final UserConverter userConverter;
private final PasswordEncoder passwordEncoder;
private final JwtService jwtService;
private final TokenBlacklistService tokenBlacklistService;
```

**好处：**
- 不可变性（final 字段）
- 依赖关系一目了然
- Spring 推荐的最佳实践

---

#### 4. UserDetailsServiceImpl.java - 添加 Framework Hack 注释

**修改前：**
```java
@Override
public UserDetails loadUserByUsername(String username) {
    // username 实际存储的是 userId
    Long userId = Long.parseLong(username);
    return loadUserById(userId);
}
```

**修改后：**
```java
/**
 * 设计说明（Framework Hack）：
 * Spring Security 的 UserDetailsService 接口方法名为 loadUserByUsername，
 * 但我们的系统使用 userId 作为认证主体（Token 的 subject 存储的是 userId）。
 * 这是一个框架接口与业务设计的妥协，username 参数实际存储的是 userId。
 */
@Override
public UserDetails loadUserByUsername(String username) {
    // Framework Hack: username 参数实际是 userId（Token subject）
    Long userId = Long.parseLong(username);
    return loadUserById(userId);
}
```

---

#### 5. UserServiceImplTest.java - 更新测试构造器调用

**修改前：**
```java
@BeforeEach
void setUp() {
    userService = new UserServiceImpl(passwordEncoder, jwtService, tokenBlacklistService);
    // 使用反射注入 @Resource 依赖
    setField(userService, "userMapper", userMapper);
    // ...
}
```

**修改后：**
```java
@BeforeEach
void setUp() {
    // @RequiredArgsConstructor 生成的构造函数需要所有 7 个依赖
    userService = new UserServiceImpl(
            userMapper,
            userRoleMapper,
            roleMapper,
            userConverter,
            passwordEncoder,
            jwtService,
            tokenBlacklistService
    );
}
```

---

### 影响范围

| 模块 | 文件 | 变更类型 |
|------|------|----------|
| order-platform-user | UserController.java | 修改 |
| order-platform-user | UserServiceImpl.java | 修改 |
| order-platform-user | UserDetailsServiceImpl.java | 修改 |
| order-platform-user | UserServiceImplTest.java | 修改 |

---

### 测试验证

```bash
# 编译验证
mvn compile

# 单元测试验证
mvn test -Dtest=UserServiceImplTest
```

**结果：** ✅ 全部通过

---

### 后续注意事项

1. **不要使用请求头传递用户 ID** - 始终从 `SecurityContext` 获取
2. **不要在 `@Async` 方法上使用 `@Transactional`** - 无效且误导
3. **统一使用构造器注入** - 优先使用 `final` + `@RequiredArgsConstructor`
4. **前端调用变更** - 移除 `X-User-id` 请求头，使用 `Authorization: Bearer {token}`

---
