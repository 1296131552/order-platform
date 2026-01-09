# 用户注册功能 MVP 实现计划

> **订单可视化平台 - 用户注册功能**
>
> 目标：实现企业内部用户注册的核心功能
>
> 创建日期：2026-01-08

---

## 一、MVP 范围定义

### 1.1 MVP 核心原则

```
✅ 必须实现：核心安全要求（并发安全、密码安全）
✅ 必须实现：基础功能（管理员创建、邀请注册）
✅ 必须实现：操作日志审计
✅ 必须实现：首次登录强制改密

⏸️ 暂缓实现：批量导入、邮件通知、批量审核
```

### 1.2 MVP 功能清单

| 优先级 | 功能模块 | 状态 | 说明 |
|--------|----------|------|------|
| **P0-MVP** | 管理员单个创建用户 | 🔴 待实现 | 核心功能 |
| **P0-MVP** | 数据库唯一索引 | 🔴 待实现 | 并发安全基础 |
| **P0-MVP** | 初始密码生成 | 🔴 待实现 | PasswordUtil工具类 |
| **P0-MVP** | 首次登录改密 | 🔴 待实现 | 登录流程增强 |
| **P0-MVP** | 操作日志 | 🔴 待实现 | 复用现有切面 |
| **P1-MVP** | 邀请码生成 | 🔴 待实现 | 辅助功能 |
| **P1-MVP** | 邀请注册 | 🔴 待实现 | 辅助功能 |
| **P1-MVP** | 注册审核 | 🔴 待实现 | 单个审核 |
| **P1-MVP** | 乐观锁并发控制 | 🔴 待实现 | 邀请码核销 |
| ⏸️ P2 | 批量导入 | ⏸️ 暂缓 | 效率优化 |
| ⏸️ P2 | 邮件通知 | ⏸️ 暂缓 | 体验优化 |
| ⏸️ P2 | 批量审核 | ⏸️ 暂缓 | 效率优化 |
| ⏸️ P2 | 系统内通知 | ⏸️ 暂缓 | 体验优化 |

---

## 二、MVP 实现计划

### 2.1 阶段划分

```
┌─────────────────────────────────────────────────────────┐
│              MVP 实现阶段划分（共5个阶段）                │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  阶段1: 数据库准备              [1天]                    │
│  ├─ t_user 表新增字段                                    │
│  ├─ 唯一索引创建                                         │
│  └─ t_invite_code 表创建                                │
│                                                          │
│  阶段2: 管理员创建用户          [2天]                    │
│  ├─ PasswordUtil 工具类                                 │
│  ├─ UserCreateDTO                                       │
│  ├─ UserService.createUser()                            │
│  ├─ UserController.createUser()                         │
│  └─ 操作日志集成                                         │
│                                                          │
│  阶段3: 首次登录改密            [1天]                    │
│  ├─ AuthService.login() 增强                            │
│  ├─ LoginVO 增加 requireChangePassword                  │
│  ├─ AuthService.changePassword() 增强                   │
│  └─ 全局异常处理器增强                                   │
│                                                          │
│  阶段4: 邀请注册                [2天]                    │
│  ├─ InviteCode 实体和 Mapper                            │
│  ├─ InviteCodeGenerateDTO                               │
│  ├─ InviteCodeService                                   │
│  ├─ UserRegisterDTO                                     │
│  ├─ UserService.register()                              │
│  └─ 邀请码乐观锁                                          │
│                                                          │
│  阶段5: 注册审核                [1天]                    │
│  ├─ UserService.auditUser()                             │
│  ├─ UserController 审核接口                              │
│  ├─ 待审核列表查询                                       │
│  └─ 审核通知（简化版，日志记录）                         │
│                                                          │
└─────────────────────────────────────────────────────────┘

总计：7个工作日
```

### 2.2 详细任务清单

#### 阶段1：数据库准备（Day 1）

| 任务 | 文件 | 工作量 | 说明 |
|------|------|--------|------|
| 1.1 修改 t_user 表 | SQL脚本 | 1h | 新增5个字段 + 3个唯一索引 |
| 1.2 创建 t_invite_code 表 | SQL脚本 | 0.5h | 建表语句 |
| 1.3 验证数据库结构 | 测试 | 0.5h | 确认字段和索引正确 |

**SQL脚本位置**：`scripts/sql/user_registration_mvp.sql`

```sql
-- ========== t_user 表修改 ==========
-- 新增字段
ALTER TABLE t_user ADD COLUMN is_first_login TINYINT(1) NOT NULL DEFAULT 0 COMMENT '首次登录标记: 0-否, 1-是';
ALTER TABLE t_user ADD COLUMN audit_status VARCHAR(20) NOT NULL DEFAULT 'NONE' COMMENT '审核状态: NONE-无需审核, PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝';
ALTER TABLE t_user ADD COLUMN audit_remark VARCHAR(200) COMMENT '审核备注';
ALTER TABLE t_user ADD COLUMN audit_by BIGINT COMMENT '审核人ID';
ALTER TABLE t_user ADD COLUMN audit_time DATETIME COMMENT '审核时间';
ALTER TABLE t_user ADD COLUMN employee_no VARCHAR(50) COMMENT '工号';

-- 新增唯一索引（并发安全基础）
ALTER TABLE t_user ADD UNIQUE INDEX uk_username (username, is_deleted);
ALTER TABLE t_user ADD UNIQUE INDEX uk_email (email, is_deleted);
ALTER TABLE t_user ADD UNIQUE INDEX uk_phone (phone, is_deleted);

-- 新增普通索引
ALTER TABLE t_user ADD INDEX idx_audit_status (audit_status);

-- ========== t_invite_code 表创建 ==========
CREATE TABLE t_invite_code (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    code VARCHAR(16) NOT NULL COMMENT '邀请码（16位随机字符串）',
    role_id BIGINT NOT NULL COMMENT '绑定的角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称（冗余）',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码（冗余）',
    valid_days INT NOT NULL COMMENT '有效期（天）',
    expire_at DATETIME NOT NULL COMMENT '过期时间',
    max_uses INT NOT NULL COMMENT '最大使用次数',
    used_count INT NOT NULL DEFAULT 0 COMMENT '已使用次数',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-有效, DISABLED-已禁用, EXPIRED-已过期',
    remark VARCHAR(200) COMMENT '备注',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    KEY idx_role_id (role_id),
    KEY idx_status (status),
    KEY idx_expire_at (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邀请码表';
```

---

#### 阶段2：管理员创建用户（Day 2-3）

| 任务 | 文件 | 工作量 | 说明 |
|------|------|--------|------|
| 2.1 PasswordUtil 工具类 | PasswordUtil.java | 1h | 密码生成和强度校验 |
| 2.2 UserCreateDTO | UserCreateDTO.java | 0.5h | 创建用户请求DTO |
| 2.3 UserVO 增强 | UserVO.java | 0.5h | 增加新字段 |
| 2.4 UserService.createUser() | UserServiceImpl.java | 2h | 创建用户核心逻辑 |
| 2.5 UserController.createUser() | AdminUserController.java | 1h | 管理员接口 |
| 2.6 操作日志集成 | 注解 | 0.5h | 复用现有切面 |
| 2.7 单元测试 | UserServiceTest.java | 1h | 核心逻辑测试 |
| 2.8 接口测试 | Postman/Apifox | 1h | 手动测试 |

**核心代码结构**：

```
order-platform-user/
├── src/main/java/com/order/platform/user/
│   ├── controller/
│   │   └── admin/
│   │       └── AdminUserController.java          [新增] 管理员接口
│   ├── service/
│   │   ├── UserService.java                     [修改] 接口定义
│   │   └── impl/
│   │       └── UserServiceImpl.java             [修改] 创建用户实现
│   ├── dto/
│   │   ├── UserCreateDTO.java                   [新增] 创建用户DTO
│   │   └── UserVO.java                          [修改] 增加新字段
│   ├── entity/
│   │   └── User.java                            [修改] 增加新字段
│   ├── mapper/
│   │   └── UserMapper.java                      [修改] 新增查询方法
│   └── util/
│       └── PasswordUtil.java                    [新增] 密码工具类
```

**PasswordUtil 核心代码**：

```java
/**
 * 密码工具类
 */
public class PasswordUtil {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*";
    private static final String ALL_CHARS = UPPER + LOWER + DIGITS + SPECIAL;
    private static final Random random = new Random();

    /**
     * 生成强随机密码
     *
     * @param length 密码长度（建议至少8位）
     * @return 随机密码
     */
    public static String generateStrongPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("密码长度至少8位");
        }

        StringBuilder password = new StringBuilder();

        // 确保包含各类字符
        password.append(UPPER.charAt(random.nextInt(UPPER.length())));
        password.append(LOWER.charAt(random.nextInt(LOWER.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        // 填充剩余长度
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        // 打乱顺序
        return shuffleString(password.toString());
    }

    /**
     * 验证密码强度
     *
     * @param password 密码
     * @return 是否符合强度要求
     */
    public static boolean validateStrength(String password) {
        if (password == null || password.length() < 6 || password.length() > 20) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }

        return hasUpper && hasLower && hasDigit;
    }

    /**
     * 打乱字符串顺序
     */
    private static String shuffleString(String input) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }
        return new String(characters);
    }
}
```

---

#### 阶段3：首次登录改密（Day 4）

| 任务 | 文件 | 工作量 | 说明 |
|------|------|--------|------|
| 3.1 LoginVO 增强 | LoginVO.java | 0.5h | 新增 requireChangePassword |
| 3.2 AuthService.login() 增强 | AuthServiceImpl.java | 1.5h | 首次登录检测逻辑 |
| 3.3 AuthService.changePassword() | AuthServiceImpl.java | 1h | 清除首次登录标记 |
| 3.4 全局异常处理器增强 | GlobalExceptionHandler.java | 1h | 唯一索引冲突处理 |
| 3.5 单元测试 | AuthServiceTest.java | 1h | 登录流程测试 |

**核心代码**：

```java
// LoginVO 新增字段
@Data
@Builder
public class LoginVO {
    private String token;
    private UserInfoVO userInfo;
    private Boolean requireChangePassword;  // ⭐ 新增
    private LocalDateTime passwordExpireTime; // ⭐ 新增
}

// AuthService.login() 增强
public LoginVO login(LoginDTO dto) {
    // ... 原有登录逻辑 ...

    // ⭐ 首次登录检测
    if (user.getIsFirstLogin()) {
        return LoginVO.builder()
            .token(token)
            .userInfo(userInfo)
            .requireChangePassword(true)
            .passwordExpireTime(user.getPasswordExpireTime())
            .build();
    }

    // ... 原有逻辑 ...
}

// 全局异常处理器
@ExceptionHandler(DuplicateKeyException.class)
public Result<Void> handleDuplicateKeyException(DuplicateKeyException e) {
    log.error("数据库唯一索引冲突", e);

    String message = e.getMessage();
    if (message.contains("uk_username")) {
        return Result.fail(ResponseCode.USER_ALREADY_EXISTS, "用户名已存在");
    } else if (message.contains("uk_email")) {
        return Result.fail(ResponseCode.EMAIL_ALREADY_EXISTS, "邮箱已被使用");
    } else if (message.contains("uk_phone")) {
        return Result.fail(ResponseCode.PHONE_ALREADY_EXISTS, "手机号已被使用");
    }

    return Result.fail(ResponseCode.SYSTEM_ERROR, "操作失败，数据冲突");
}
```

---

#### 阶段4：邀请注册（Day 5-6）

| 任务 | 文件 | 工作量 | 说明 |
|------|------|--------|------|
| 4.1 InviteCode 实体 | InviteCode.java | 0.5h | 邀请码实体 |
| 4.2 InviteCodeMapper | InviteCodeMapper.java | 0.5h | MyBatis Mapper |
| 4.3 InviteCodeGenerateDTO | InviteCodeGenerateDTO.java | 0.5h | 生成邀请码DTO |
| 4.4 InviteCodeService | InviteCodeService.java | 1.5h | 邀请码服务 |
| 4.5 UserRegisterDTO | UserRegisterDTO.java | 0.5h | 注册DTO |
| 4.6 UserService.register() | UserServiceImpl.java | 2h | 注册核心逻辑 |
| 4.7 UserController.register() | UserController.java | 0.5h | 注册接口 |
| 4.8 单元测试 | UserServiceTest.java | 1h | 注册逻辑测试 |

**核心代码结构**：

```
order-platform-user/
├── src/main/java/com/order/platform/user/
│   ├── entity/
│   │   └── InviteCode.java                      [新增] 邀请码实体
│   ├── mapper/
│   │   └── InviteCodeMapper.java                [新增] 邀请码Mapper
│   ├── service/
│   │   └── InviteCodeService.java               [新增] 邀请码服务
│   ├── controller/
│   │   ├── admin/
│   │   │   └── AdminInviteCodeController.java   [新增] 邀请码管理接口
│   │   └── UserController.java                  [新增] 注册接口
│   ├── dto/
│   │   ├── InviteCodeGenerateDTO.java           [新增] 生成邀请码DTO
│   │   ├── InviteCodeVO.java                    [新增] 邀请码VO
│   │   └── UserRegisterDTO.java                 [新增] 注册DTO
│   └── enums/
│       ├── InviteCodeStatus.java                [新增] 邀请码状态枚举
│       └── UserAuditStatus.java                 [新增] 审核状态枚举
```

**乐观锁核销逻辑**：

```java
/**
 * 核销邀请码（乐观锁）
 */
@Transactional
public void consumeInviteCode(String code) {
    InviteCode inviteCode = inviteCodeMapper.selectByCode(code);
    if (inviteCode == null) {
        throw new BusinessException(ResponseCode.INVITE_CODE_INVALID, "邀请码不存在");
    }

    if (inviteCode.getUsedCount() >= inviteCode.getMaxUses()) {
        throw new BusinessException(ResponseCode.INVITE_CODE_EXHAUSTED, "邀请码已用完");
    }

    // 乐观锁更新（MyBatis Plus 自动处理 version）
    inviteCode.setUsedCount(inviteCode.getUsedCount() + 1);
    int affected = inviteCodeMapper.updateById(inviteCode);

    if (affected == 0) {
        throw new BusinessException(ResponseCode.INVITE_CODE_CONFLICT, "邀请码正在被使用，请稍后重试");
    }
}
```

---

#### 阶段5：注册审核（Day 7）

| 任务 | 文件 | 工作量 | 说明 |
|------|------|--------|------|
| 5.1 UserAuditDTO | UserAuditDTO.java | 0.5h | 审核DTO |
| 5.2 UserService.auditUser() | UserServiceImpl.java | 1.5h | 审核逻辑 |
| 5.3 UserController 审核接口 | AdminUserController.java | 0.5h | 审核接口 |
| 5.4 待审核列表查询 | AdminUserController.java | 1h | 列表查询 |
| 5.5 单元测试 | UserServiceTest.java | 0.5h | 审核逻辑测试 |

---

## 三、MVP 验收标准

### 3.1 功能验收

| 功能 | 验收标准 | 测试方式 |
|------|----------|----------|
| **管理员创建用户** | 成功创建用户，生成初始密码，返回用户信息 | 接口测试 |
| **唯一性校验** | 重复用户名/邮箱/手机号返回友好错误提示 | 接口测试 |
| **并发安全** | 并发创建重复用户时，数据库拦截并返回友好提示 | 并发测试 |
| **首次登录改密** | 首次登录返回 requireChangePassword=true，改密后清除标记 | 流程测试 |
| **邀请码生成** | 生成16位邀请码，包含角色、有效期、次数限制 | 接口测试 |
| **邀请注册** | 使用有效邀请码成功注册，用户状态为待审核 | 接口测试 |
| **邀请码核销** | 使用次数正确累加，超限后拒绝注册 | 接口测试 |
| **注册审核** | 审核通过后用户启用，审核拒绝用户保持禁用 | 流程测试 |
| **操作日志** | 所有关键操作正确记录日志，敏感信息脱敏 | 日志检查 |

### 3.2 性能验收

| 指标 | 目标 | 测试方式 |
|------|------|----------|
| 创建用户响应时间 | < 500ms | 接口压测 |
| 邀请注册响应时间 | < 500ms | 接口压测 |
| 并发创建安全性 | 无脏数据 | 10并发测试 |

---

## 四、技术栈和依赖

### 4.1 使用现有依赖

```xml
<!-- 无需新增依赖，使用项目现有技术栈 -->

<!-- MyBatis Plus（已有） -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
</dependency>

<!-- Validation（已有） -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Lombok（已有） -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>

<!-- Hutool（已有） -->
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-all</artifactId>
</dependency>
```

### 4.2 复用现有组件

| 组件 | 位置 | 用途 |
|------|------|------|
| Result | order-platform-common | 统一响应格式 |
| BusinessException | order-platform-common | 业务异常 |
| ResponseCode | order-platform-common | 响应码枚举 |
| OperationLogAspect | order-platform-common | 操作日志切面 |
| AuthInterceptor | order-platform-common | 认证拦截器 |
| UserContext | order-platform-common | 用户上下文 |

---

## 五、风险评估和应对

| 风险 | 影响 | 概率 | 应对措施 |
|------|------|------|----------|
| 数据库索引创建失败 | 高 | 低 | 提前在测试环境验证，备份原有数据 |
| 并发测试不充分 | 中 | 中 | 增加并发测试用例，使用JMeter压测 |
| 时间延期 | 中 | 中 | 按阶段交付，优先完成P0功能 |
| 与现有认证冲突 | 高 | 低 | 充分测试登录流程，确保向后兼容 |

---

## 六、后续优化方向（MVP后）

| 优先级 | 功能 | 预计工作量 |
|--------|------|-----------|
| P2 | 批量导入用户 | 1天 |
| P2 | 邮件通知 | 1天 |
| P2 | 批量审核 | 0.5天 |
| P2 | 系统内通知 | 0.5天 |
| P2 | 失败数据导出 | 0.5天 |

---

## 七、联系人和支持

- **开发负责人**：开发组
- **文档维护**：保持与代码同步更新

---

**MVP 实现预计完成时间**：7个工作日

**下一次评审时间**：阶段3完成后（第4天）
