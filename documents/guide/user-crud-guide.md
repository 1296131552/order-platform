# 用户模块 CRUD 开发教程

> **订单可视化平台 - 用户模块**
> 本教程详细讲解用户 CRUD（创建、更新、删除、密码管理）功能的完整实现。

---

## 目录

1. [概述与设计思路](#一概述与设计思路)
2. [现有代码结构分析](#二现有代码结构分析)
3. [创建用户功能](#三创建用户功能)
4. [更新用户功能](#四更新用户功能)
5. [删除用户功能](#五删除用户功能)
6. [密码管理功能](#六密码管理功能)
7. [完整代码示例](#七完整代码示例)
8. [单元测试](#八单元测试)

---

**v1.1 更新说明（2026-01-21）**：
- ✅ `userCode` 改为 VO 层动态计算，不再存储到数据库
- ✅ 审计字段（createdAt/updatedAt 等）由 `MetaObjectHandler` 自动填充
- ✅ 简化 `createUser` 方法，消除过度拆分的私有方法

---

## 一、概述与设计思路

### 1.1 业务需求

用户模块是系统的基础服务，需要支持以下核心操作：

| 功能 | 描述 | 业务规则 |
|------|------|----------|
| **创建用户** | 管理员创建新用户 | 用户名唯一、密码加密、分配角色 |
| **更新用户** | 修改用户信息 | 不能修改用户名、密码单独处理 |
| **删除用户** | 删除用户记录 | 软删除、复合唯一索引 (username, is_deleted) |
| **修改密码** | 用户自行修改密码 | 验证旧密码、新密码复杂度 |
| **重置密码** | 管理员重置用户密码 | 生成随机密码或指定新密码 |

### 1.2 技术方案

```
┌─────────────────────────────────────────────────────────────────┐
│                          用户 CRUD 架构                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐        │
│  │ Controller  │────▶│   Service   │────▶│   Mapper    │        │
│  │  (API层)    │     │  (业务层)   │     │  (数据层)   │        │
│  └─────────────┘     └─────────────┘     └─────────────┘        │
│       │                    │                    │                │
│       ▼                    ▼                    ▼                │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐        │
│  │  DTO 校验   │     │  业务逻辑   │     │  MyBatis-   │        │
│  │  @Valid    │     │  密码加密   │     │  Plus CRUD  │        │
│  └─────────────┘     │  事务控制   │     └─────────────┘        │
│                      └─────────────┘                              │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 设计原则

| 原则 | 应用 |
|------|------|
| **单一职责** | Controller 只负责 API 映射，Service 负责业务逻辑 |
| **依赖倒置** | Service 依赖 Mapper 接口，而非具体实现 |
| **防御式编程** | 所有输入参数都进行校验，不信任外部数据 |
| **最小权限** | 密码字段不在响应中返回，软删除保留原始数据 |

---

## 二、现有代码结构分析

### 2.1 项目模块结构

```
order-platform-backend/
├── order-platform-common/           # 公共模块
│   ├── response/
│   │   ├── Result.java              # 统一响应封装
│   │   └── ResponseCode.java        # 响应码枚举
│   ├── exception/
│   │   └── BusinessException.java   # 业务异常
│   └── security/
│       └── PasswordEncoder.java     # 密码加密工具
│
├── order-platform-user/             # 用户模块
│   ├── entity/                      # 实体层
│   │   ├── User.java                # 用户实体
│   │   ├── Role.java                # 角色实体
│   │   └── UserRole.java            # 用户角色关联
│   ├── mapper/                      # 数据访问层
│   │   ├── UserMapper.java
│   │   ├── RoleMapper.java
│   │   └── UserRoleMapper.java
│   ├── service/                     # 服务层
│   │   ├── UserService.java         # 服务接口
│   │   └── impl/
│   │       └── UserServiceImpl.java # 服务实现
│   ├── controller/                  # 控制层
│   │   ├── UserController.java      # 用户管理 API
│   │   └── AuthController.java      # 认证 API
│   ├── dto/                         # 数据传输对象
│   │   ├── UserCreateRequest.java   # 创建请求
│   │   ├── UserUpdateRequest.java   # 更新请求
│   │   ├── UserQueryRequest.java    # 查询请求
│   │   ├── UserVO.java              # 视图对象
│   │   └── LoginRequest.java        # 登录请求
│   └── converter/                   # 对象转换器
│       └── UserConverter.java
```

### 2.2 实体类设计

#### User 实体

```java
@Data
@TableName("t_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    // ==================== 账号信息 ====================
    private String username;        // 登录账号，唯一
    private String password;        // BCrypt 加密后的密码
    // 注意：userCode 已移除，由 UserVO 动态计算

    // ==================== 基本信息 ====================
    private String realName;        // 真实姓名
    private String email;           // 邮箱
    private String phone;           // 手机号
    private String avatar;          // 头像URL（NULL表示无头像）

    // ==================== 状态控制 ====================
    private Boolean isEnabled;      // 是否启用
    private Boolean isLocked;       // 是否锁定
    private LocalDateTime lockedTime;
    private String lockedReason;

    // ==================== 登录信息 ====================
    private LocalDateTime lastLoginTime;  // NULL表示从未登录
    private String lastLoginIp;
    private Integer loginCount;

    // ==================== 密码管理 ====================
    private LocalDateTime passwordChangedTime;  // NULL表示从未修改
    private LocalDateTime passwordExpireTime;    // NULL表示永不过期

    // ==================== 组织信息 ====================
    private Long departmentId;      // NULL表示未分配部门
    private String position;
    private String employeeNo;

    // ==================== 公共字段（自动填充）====================
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT)
    private Boolean isDeleted;
}
```

**设计要点**：
- 使用 `NULL` 表示「无值」而非空字符串，避免歧义
- Boolean 字段映射为 TINYINT(0/1)
- 时间使用 `DATETIME(3)` 精确到毫秒
- **审计字段自动填充**：`@TableField(fill = FieldFill.*)` 注解标记的字段由 `MetaObjectHandler` 自动填充

#### UserVO 设计（动态计算 userCode）

```java
@Data
@Builder
public class UserVO {
    private Long id;
    private String username;

    /**
     * 用户编码（业务编号，动态计算）
     * 格式：USER + 10位数字ID（左侧补零）
     * 示例：USER0000000001
     * <p>
     * 不在数据库存储，通过 id 动态计算，消除冗余
     */
    public String getUserCode() {
        return id != null ? String.format("USER%010d", id) : null;
    }

    private String realName;
    private String email;
    // ... 其他字段
}
```

**为什么这样设计？**

```
"Bad programmers worry about the code. Good programmers worry about data structures."
                                              — Linus Torvalds

userCode 只是把 id 格式化了一下，存储它是冗余的。
- 旧方式：insert 获取 ID → update 回填 userCode（2次数据库写入）
- 新方式：insert 一次完成，VO 层动态计算（1次数据库写入）
```

#### MetaObjectHandler 配置

```java
@Component
public class MetaObjectHandlerImpl implements MetaObjectHandler {

    private static final ThreadLocal<Long> OPERATOR_ID = new ThreadLocal<>();

    public static void setOperatorId(Long operatorId) {
        OPERATOR_ID.set(operatorId);
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        Long operatorId = getOperatorId();
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "createdBy", Long.class, operatorId);
        this.strictInsertFill(metaObject, "updatedBy", Long.class, operatorId);
        this.strictInsertFill(metaObject, "isDeleted", Boolean.class, false);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updatedBy", Long.class, getOperatorId());
    }

    private Long getOperatorId() {
        Long id = OPERATOR_ID.get();
        return id != null ? id : -1L;  // -1 表示系统操作
    }
}
```

**使用方式**：
```java
// 在 Controller 或 Filter 中设置当前操作人
MetaObjectHandlerImpl.setOperatorId(userId);

try {
    userService.createUser(request, userId);
} finally {
    MetaObjectHandlerImpl.clearOperatorId();  // 清理 ThreadLocal
}
```

### 2.3 统一响应格式

```java
@Data
public class Result<T> {
    private Integer code;        // 状态码
    private String message;      // 响应消息
    private T data;              // 响应数据
    private Long timestamp;      // 时间戳

    // 成功响应
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "success", data);
    }

    // 失败响应
    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null);
    }

    // 使用响应码枚举
    public static <T> Result<T> fail(ResponseCode responseCode) {
        return new Result<>(responseCode.getCode(),
                             responseCode.getMessage(), null);
    }
}
```

### 2.4 DTO 设计规范

| 类型 | 命名 | 用途 |
|------|------|------|
| 请求 | `*Request` | 接收客户端参数，带 `@Valid` 校验 |
| 响应 | `*Response` | 返回给客户端的数据 |
| 视图 | `*VO` | 业务层与控制层之间的数据传递 |

---

## 三、创建用户功能

### 3.1 业务流程

```
┌─────────────────────────────────────────────────────────────────┐
│                          创建用户流程                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. 参数校验                                                     │
│     ├── 用户名格式（3-20位，字母数字下划线）                       │
│     ├── 密码复杂度（6-20位）                                      │
│     ├── 邮箱格式（可选）                                          │
│     └── 手机号格式（可选）                                        │
│                                                                  │
│  2. 业务校验                                                     │
│     ├── 用户名唯一性检查                                         │
│     ├── 角色存在性检查                                           │
│     └── 部门存在性检查（如果指定）                                 │
│                                                                  │
│  3. 数据处理                                                     │
│     ├── 密码 BCrypt 加密                                         │
│     ├── 设置默认值（isEnabled=true, isLocked=false）             │
│     └── 审计字段由 MetaObjectHandler 自动填充                    │
│                                                                  │
│  4. 持久化（一次数据库写入）                                      │
│     ├── 插入 t_user 表                                          │
│     └── 批量插入 t_user_role 表（如果指定角色）                    │
│                                                                  │
│  5. 返回结果                                                     │
│     └── 返回新创建用户的 ID（userCode 由 VO 动态计算）            │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 DTO 设计

```java
/**
 * 创建用户请求
 */
@Data
public class UserCreateRequest {

    /**
     * 用户名（必填）
     * 格式：3-20位，只能包含字母、数字、下划线
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度3-20位")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字、下划线")
    private String username;

    /**
     * 密码（必填）
     * 格式：6-20位
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20位")
    private String password;

    /**
     * 真实姓名（必填）
     */
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 20, message = "姓名长度不能超过20位")
    private String realName;

    /**
     * 邮箱（可选）
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号（可选）
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 分配的角色ID列表（可选）
     */
    private List<Long> roleIds;

    /**
     * 部门ID（可选）
     */
    private Long departmentId;

    /**
     * 职位（可选）
     */
    private String position;

    /**
     * 工号（可选）
     */
    private String employeeNo;

    /**
     * 备注（可选）
     */
    @Size(max = 200, message = "备注长度不能超过200位")
    private String remark;
}
```

### 3.3 Service 实现

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    private final PasswordEncoder passwordEncoder;

    /**
     * 创建用户
     * <p>
     * 重构说明（Linus 风格）：
     * - userCode 不再存储到数据库，由 VO 层动态计算（消除冗余）
     * - 审计字段由 MetaObjectHandler 自动填充（消除手动填充代码）
     * - 简化校验逻辑，消除过度拆分的私有方法
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateRequest request, Long operatorId) {
        // 1. 校验用户名唯一性
        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .eq(User::getIsDeleted, false)) > 0) {
            throw new BusinessException(ResponseCode.USER_ALREADY_EXISTS);
        }

        // 2. 校验角色是否存在
        if (CollectionUtil.isNotEmpty(request.getRoleIds())) {
            List<Role> roles = roleMapper.selectBatchIds(request.getRoleIds());
            if (roles.size() != request.getRoleIds().size()) {
                throw new BusinessException(ResponseCode.ROLE_NOT_FOUND);
            }
        }

        // 3. 构建并保存用户（一次插入，userCode 由 VO 动态计算）
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPosition(request.getPosition());
        user.setEmployeeNo(request.getEmployeeNo());
        user.setRemark(request.getRemark());
        user.setDepartmentId(request.getDepartmentId());
        user.setIsEnabled(true);
        user.setIsLocked(false);
        // 审计字段由 MetaObjectHandler 自动填充
        userMapper.insert(user);

        // 4. 分配角色（审计字段自动填充）
        if (CollectionUtil.isNotEmpty(request.getRoleIds())) {
            for (Long roleId : request.getRoleIds()) {
                UserRole userRole = new UserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(roleId);
                userRole.setIsPrimary(false);
                userRoleMapper.insert(userRole);
            }
        }

        log.info("创建用户成功, userId={}, username={}, operatorId={}",
                user.getId(), user.getUsername(), operatorId);

        return user.getId();
    }
}
```

**代码对比**：

| 项目 | 旧版本 | 新版本 |
|------|--------|--------|
| 数据库写入 | 2 次 | 1 次 |
| 代码行数 | ~100 行 | ~50 行 |
| 私有方法 | 4 个 | 0 个 |
| 审计字段 | 手动填充 | 自动填充 |
| userCode | 存储+回填 | VO 动态计算 |

### 3.4 Controller 实现

```java
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户查询、管理")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "创建用户", description = "管理员创建新用户")
    @PostMapping
    public Result<Long> createUser(
            @Valid @RequestBody UserCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        // 从请求头获取当前用户ID（生产环境从 SecurityContext 获取）
        Long operatorId = Optional.ofNullable(userId).orElse(-1L);

        Long newUserId = userService.createUser(request, operatorId);
        return Result.ok(newUserId);
    }
}
```

### 3.5 API 响应示例

**请求**：
```http
POST /api/user
Content-Type: application/json
X-User-Id: 1

{
  "username": "zhangsan",
  "password": "123456",
  "realName": "张三",
  "email": "zhangsan@example.com",
  "phone": "13800138000",
  "roleIds": [1, 2],
  "departmentId": 10,
  "position": "客户经理",
  "employeeNo": "EMP001",
  "remark": "销售部门客户经理"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": 123,
  "timestamp": 1705334400000
}
```

---

## 四、更新用户功能

### 4.1 业务流程

```
┌─────────────────────────────────────────────────────────────────┐
│                          更新用户流程                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. 参数校验                                                     │
│     ├── 用户ID有效性                                             │
│     ├── 邮箱/手机号格式（如果提供）                                │
│     └── 字段长度限制                                             │
│                                                                  │
│  2. 业务校验                                                     │
│     ├── 用户存在性检查                                           │
│     ├── 角色存在性检查（如果更新角色）                             │
│     └── 不能修改用户名（设计决策）                                │
│                                                                  │
│  3. 数据处理                                                     │
│     ├── 只更新提供的字段（部分更新）                               │
│     └── updatedAt/updatedBy 由 MetaObjectHandler 自动填充        │
│                                                                  │
│  4. 角色处理                                                     │
│     ├── 先删除旧的角色关联                                       │
│     └── 批量插入新的角色关联（审计字段自动填充）                   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 Service 实现

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void updateUser(UserUpdateRequest request, Long operatorId) {
    // ==================== 1. 校验用户存在 ====================

    User user = userMapper.selectById(request.getId());
    if (user == null || user.getIsDeleted()) {
        throw new BusinessException(ResponseCode.USER_NOT_FOUND);
    }

    // ==================== 2. 校验角色（如果更新） ====================

    List<Long> newRoleIds = Optional.ofNullable(request.getRoleIds())
                                    .orElse(Collections.emptyList());

    if (CollectionUtil.isNotEmpty(newRoleIds)) {
        List<Role> roles = roleMapper.selectBatchIds(newRoleIds);
        if (roles.size() != newRoleIds.size()) {
            throw new BusinessException(ResponseCode.ROLE_NOT_FOUND);
        }
    }

    // ==================== 3. 更新用户基本信息 ====================

    // 只更新非空字段
    if (StringUtils.isNotBlank(request.getRealName())) {
        user.setRealName(request.getRealName());
    }
    if (request.getEmail() != null) {
        user.setEmail(request.getEmail());
    }
    if (request.getPhone() != null) {
        user.setPhone(request.getPhone());
    }
    if (request.getAvatar() != null) {
        user.setAvatar(request.getAvatar());
    }
    if (request.getIsEnabled() != null) {
        user.setIsEnabled(request.getIsEnabled());
    }
    if (request.getDepartmentId() != null) {
        user.setDepartmentId(request.getDepartmentId());
    }
    if (request.getPosition() != null) {
        user.setPosition(request.getPosition());
    }
    if (request.getEmployeeNo() != null) {
        user.setEmployeeNo(request.getEmployeeNo());
    }
    if (request.getRemark() != null) {
        user.setRemark(request.getRemark());
    }

    // 注意：updatedAt/updatedBy 由 MetaObjectHandler 自动填充
    userMapper.updateById(user);

    // ==================== 4. 更新角色关联 ====================
    // 注意：先删后插存在并发安全隐患
    // 并发场景：两个请求同时更新同一用户的角色，可能导致数据丢失
    // 解决方案：在高并发场景下，建议使用分布式锁（如 Redisson）或乐观锁

    // 先删除旧的角色关联
    LambdaQueryWrapper<UserRole> deleteWrapper = new LambdaQueryWrapper<>();
    deleteWrapper.eq(UserRole::getUserId, user.getId());
    userRoleMapper.delete(deleteWrapper);

    // 插入新的角色关联（审计字段自动填充）
    if (CollectionUtil.isNotEmpty(newRoleIds)) {
        for (Long roleId : newRoleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(roleId);
            userRole.setIsPrimary(false);
            userRoleMapper.insert(userRole);
        }
    }

    log.info("更新用户成功, userId={}, username={}, operatorId={}",
             user.getId(), user.getUsername(), operatorId);
}
```

---

## 五、删除用户功能

### 5.1 软删除策略

```
┌─────────────────────────────────────────────────────────────────┐
│                          软删除策略                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  问题：username 有唯一索引，软删除后无法创建同名用户               │
│                                                                  │
│  解决方案：使用复合唯一索引 (username, is_deleted)                 │
│                                                                  │
│  表结构修改：                                                     │
│  -- 删除原来的单列唯一索引                                         │
│  ALTER TABLE t_user DROP INDEX uk_user_username;                 │
│                                                                  │
│  -- 添加复合唯一索引（只对未删除的记录校验 username 唯一性）        │
│  CREATE UNIQUE INDEX uk_username_deleted                         │
│    ON t_user(username, is_deleted);                              │
│                                                                  │
│  优点：                                                          │
│  1. 保留原始数据用于审计                                         │
│  2. 释放 username 供新用户使用（is_deleted=1 的记录不冲突）        │
│  3. 不需要修改业务数据（username 保持原样）                        │
│  4. 符合"数据结构优先"的设计原则                                  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 Service 实现

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void deleteUser(Long userId, Long operatorId) {
    // ==================== 1. 校验用户存在 ====================

    User user = userMapper.selectById(userId);
    if (user == null || user.getIsDeleted()) {
        throw new BusinessException(ResponseCode.USER_NOT_FOUND);
    }

    // ==================== 2. 软删除 ====================
    // 注意：由于使用了复合唯一索引 (username, is_deleted)
    // 软删除后原 username 不会再与新用户冲突，无需修改 username

    user.setIsDeleted(true);
    // updatedAt/updatedBy 由 MetaObjectHandler 自动填充
    userMapper.updateById(user);

    // ==================== 3. 删除角色关联 ====================

    LambdaQueryWrapper<UserRole> deleteWrapper = new LambdaQueryWrapper<>();
    deleteWrapper.eq(UserRole::getUserId, userId);
    userRoleMapper.delete(deleteWrapper);

    log.info("删除用户成功, userId={}, username={}, operatorId={}",
             userId, user.getUsername(), operatorId);
}
```

---

## 六、密码管理功能

### 6.1 修改密码（用户自行修改）

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void changePassword(Long userId,
                          String oldPassword,
                          String newPassword,
                          Long operatorId) {
    // ==================== 1. 校验用户存在 ====================

    User user = userMapper.selectById(userId);
    if (user == null || user.getIsDeleted()) {
        throw new BusinessException(ResponseCode.USER_NOT_FOUND);
    }

    // ==================== 2. 验证旧密码 ====================

    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
        throw new BusinessException(ResponseCode.OLD_PASSWORD_ERROR);
    }

    // ==================== 3. 校验新密码复杂度 ====================

    // 不能与旧密码相同
    if (oldPassword.equals(newPassword)) {
        throw new BusinessException(ResponseCode.NEW_PASSWORD_SAME_AS_OLD);
    }

    // 密码复杂度校验（可选）
    if (!isPasswordComplexEnough(newPassword)) {
        throw new BusinessException(ResponseCode.PASSWORD_TOO_WEAK);
    }

    // ==================== 4. 更新密码 ====================

    user.setPassword(passwordEncoder.encode(newPassword));
    user.setPasswordChangedTime(LocalDateTime.now());

    // updatedAt/updatedBy 由 MetaObjectHandler 自动填充
    userMapper.updateById(user);

    log.info("修改密码成功, userId={}, username={}, operatorId={}",
             userId, user.getUsername(), operatorId);
}

/**
 * 密码复杂度校验
 * 规则：8-20位，包含大小写字母、数字、特殊字符中的至少三种
 *
 * 性能优化：使用单次遍历 O(n) 而非四次正则匹配 O(4n)
 */
private boolean isPasswordComplexEnough(String password) {
    if (password.length() < 8 || password.length() > 20) {
        return false;
    }

    boolean hasLower = false, hasUpper = false;
    boolean hasDigit = false, hasSpecial = false;

    // 单次遍历统计字符类型
    for (char c : password.toCharArray()) {
        if (Character.isLowerCase(c)) {
            hasLower = true;
        } else if (Character.isUpperCase(c)) {
            hasUpper = true;
        } else if (Character.isDigit(c)) {
            hasDigit = true;
        } else {
            hasSpecial = true;
        }
    }

    // 统计满足的类型数量
    int typeCount = (hasLower ? 1 : 0) + (hasUpper ? 1 : 0)
                  + (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);
    return typeCount >= 3;
}
```

### 6.2 重置密码（管理员操作）

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void resetPassword(Long userId,
                          String newPassword,
                          Long operatorId) {
    // ==================== 1. 校验用户存在 ====================

    User user = userMapper.selectById(userId);
    if (user == null || user.getIsDeleted()) {
        throw new BusinessException(ResponseCode.USER_NOT_FOUND);
    }

    // ==================== 2. 生成或使用新密码 ====================

    String passwordToSet;
    if (StringUtils.isBlank(newPassword)) {
        // 生成随机密码（8位，包含大小写字母和数字）
        passwordToSet = generateRandomPassword();
    } else {
        passwordToSet = newPassword;
    }

    // ==================== 3. 更新密码 ====================

    user.setPassword(passwordEncoder.encode(passwordToSet));
    user.setPasswordChangedTime(LocalDateTime.now());

    // 清除锁定状态（如果有）
    user.setIsLocked(false);
    user.setLockedTime(null);
    user.setLockedReason("");

    // updatedAt/updatedBy 由 MetaObjectHandler 自动填充
    userMapper.updateById(user);

    log.info("重置密码成功, userId={}, username={}, operatorId={}",
             userId, user.getUsername(), operatorId);

    // 返回新密码（生产环境应通过邮件/短信发送）
    // return passwordToSet;
}

/**
 * 生成随机密码
 * 格式：8位，包含大小写字母、数字和特殊字符
 *
 * 安全性：使用 SecureRandom 而非 Random
 * - Random 是伪随机，可预测
 * - SecureRandom 使用密码学安全的随机数生成器
 */
private String generateRandomPassword() {
    String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%";
    java.security.SecureRandom random = new java.security.SecureRandom();
    StringBuilder sb = new StringBuilder(8);

    for (int i = 0; i < 8; i++) {
        sb.append(chars.charAt(random.nextInt(chars.length())));
    }

    return sb.toString();
}
```

---

## 七、完整代码示例

### 7.1 UserService 接口（完整）

```java
public interface UserService {

    // ==================== 认证相关 ====================
    LoginResponse login(LoginRequest request);
    void logout(String token);

    // ==================== 用户 CRUD ====================
    Long createUser(UserCreateRequest request, Long operatorId);
    void updateUser(UserUpdateRequest request, Long operatorId);
    void deleteUser(Long userId, Long operatorId);

    // ==================== 查询相关 ====================
    UserVO getUserById(Long userId);
    Page<UserVO> pageUsers(UserQueryRequest request);

    // ==================== 密码管理 ====================
    void changePassword(Long userId, String oldPassword, String newPassword, Long operatorId);
    String resetPassword(Long userId, String newPassword, Long operatorId);
}
```

### 7.2 UserController（完整）

```java
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户查询、管理")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "创建用户")
    @PostMapping
    public Result<Long> createUser(
            @Valid @RequestBody UserCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Long operatorId = Optional.ofNullable(userId).orElse(-1L);
        Long newUserId = userService.createUser(request, operatorId);
        return Result.ok(newUserId);
    }

    @Operation(summary = "更新用户")
    @PutMapping
    public Result<Void> updateUser(
            @Valid @RequestBody UserUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Long operatorId = Optional.ofNullable(userId).orElse(-1L);
        userService.updateUser(request, operatorId);
        return Result.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long operatorId) {
        Long currentUserId = Optional.ofNullable(operatorId).orElse(-1L);
        userService.deleteUser(userId, currentUserId);
        return Result.ok();
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(@PathVariable Long userId) {
        UserVO userVO = userService.getUserById(userId);
        return Result.ok(userVO);
    }

    @Operation(summary = "分页查询用户")
    @GetMapping("/list")
    public Result<Page<UserVO>> pageUsers(UserQueryRequest request) {
        Page<UserVO> page = userService.pageUsers(request);
        return Result.ok(page);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(
            @RequestBody ChangePasswordRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Long operatorId = Optional.ofNullable(userId).orElse(-1L);
        userService.changePassword(operatorId, request.getOldPassword(),
                                   request.getNewPassword(), operatorId);
        return Result.ok();
    }

    @Operation(summary = "重置密码（管理员）")
    @PutMapping("/{userId}/reset-password")
    public Result<String> resetPassword(
            @PathVariable Long userId,
            @RequestBody(required = false) ResetPasswordRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long operatorId) {
        Long currentUserId = Optional.ofNullable(operatorId).orElse(-1L);
        String newPassword = (request != null) ? request.getNewPassword() : null;
        String generatedPassword = userService.resetPassword(userId, newPassword, currentUserId);
        return Result.ok(generatedPassword);
    }
}
```

---

## 八、单元测试

### 8.1 UserService 测试

```java
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    @Resource
    private UserMapper userMapper;

    @Test
    void testCreateUser_Success() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setPassword("123456");
        request.setRealName("测试用户");
        request.setEmail("test@example.com");

        // When
        Long userId = userService.createUser(request, 1L);

        // Then
        assertNotNull(userId);
        User user = userMapper.selectById(userId);
        assertEquals("testuser", user.getUsername());
        assertEquals("测试用户", user.getRealName());
        assertTrue(passwordEncoder.matches("123456", user.getPassword()));
    }

    @Test
    void testCreateUser_DuplicateUsername_ThrowException() {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("admin");  // 已存在
        request.setPassword("123456");
        request.setRealName("测试用户");

        // When & Then
        assertThrows(BusinessException.class, () -> {
            userService.createUser(request, 1L);
        });
    }

    @Test
    void testDeleteUser_SoftDelete() {
        // Given
        Long userId = 1L;

        // When
        userService.deleteUser(userId, 1L);

        // Then
        User user = userMapper.selectById(userId);
        assertTrue(user.getIsDeleted());
        // 注意：由于使用了复合唯一索引 (username, is_deleted)
        // 软删除后 username 保持不变，不再添加 "_deleted_" 后缀
    }

    @Test
    void testChangePassword_Success() {
        // Given
        Long userId = 1L;
        String oldPassword = "123456";
        String newPassword = "NewPassword123";

        // When
        userService.changePassword(userId, oldPassword, newPassword, 1L);

        // Then
        User user = userMapper.selectById(userId);
        assertTrue(passwordEncoder.matches(newPassword, user.getPassword()));
        assertNotNull(user.getPasswordChangedTime());
    }
}
```

### 8.2 Controller 测试

```java
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void testCreateUser_Success() throws Exception {
        // Given
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setPassword("123456");
        request.setRealName("测试用户");

        when(userService.createUser(any(), anyLong())).thenReturn(123L);

        // When & Then
        mockMvc.perform(post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testuser\",\"password\":\"123456\",\"realName\":\"测试用户\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(123));
    }

    @Test
    void testCreateUser_InvalidUsername_ThrowException() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"ab\",\"password\":\"123456\",\"realName\":\"测试用户\"}"))
                .andExpect(status().isBadRequest());
    }
}
```

---

## 总结

| 功能 | 关键点 |
|------|--------|
| **创建用户** | 用户名唯一性校验、密码 BCrypt 加密、批量插入角色、**审计字段自动填充** |
| **更新用户** | 部分更新、角色关联先删后插、**审计字段自动填充** |
| **删除用户** | 软删除、复合唯一索引 (username, is_deleted)、**审计字段自动填充** |
| **修改密码** | 验证旧密码、复杂度校验、**审计字段自动填充** |
| **重置密码** | 随机密码生成、清除锁定状态、**审计字段自动填充** |

---

## v1.1 重构要点

| 改进项 | 旧版本 | 新版本 | 收益 |
|--------|--------|--------|------|
| **userCode 存储** | 数据库字段，需 2 次写入 | VO 动态计算 | 减少 50% 数据库 I/O |
| **审计字段填充** | 手动设置（重复代码） | MetaObjectHandler 自动 | 消除样板代码 |
| **私有方法拆分** | 过度拆分（4 个方法） | 内联校验逻辑 | 代码更直观 |
| **代码行数** | ~100 行 | ~50 行 | 可维护性提升 |

**Linus 的点评**：

> "Talk is cheap. Show me the code.
>
> 代码从 100 行降到 50 行，数据库写入从 2 次降到 1 次。
> 这不是「优化」，这是「把错误的修对」。
>
> 数据结构对了，代码自然就简单了。"

---

*文档版本: v1.1*
*创建日期: 2026-01-20*
*更新日期: 2026-01-21*
*维护者: 订单平台团队*
