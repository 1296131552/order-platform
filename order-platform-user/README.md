# order-platform-user

> **订单可视化平台 - 用户模块**

> 提供用户管理、角色权限、认证登录、数据权限控制等核心功能。

---

## 📋 目录

- [模块概述](#模块概述)
- [功能清单](#功能清单)
- [目录结构](#目录结构)
- [数据库设计](#数据库设计)
- [API接口设计](#api接口设计)
- [核心业务逻辑](#核心业务逻辑)
- [数据权限设计](#数据权限设计)
- [开发进度](#开发进度)
- [文档更新规范](#文档更新规范)

---

## 模块概述

### 定位

**用户模块**（order-platform-user）是系统的基础模块，提供：

- ✅ 用户认证登录（支持用户名/邮箱/手机号）
- ✅ 用户管理（CRUD、状态管理、重置密码）
- ✅ 角色管理（预定义角色、权限分配）
- ✅ 数据权限控制（ALL/DEPARTMENT/SELF/CUSTOM）
- ✅ 个人中心（修改资料、上传头像、操作日志）

### 业务背景

根据甲方需求文档，系统有以下业务角色：

| 角色 | 职责 | 数据权限范围 |
|------|------|-------------|
| **客户经理** | 客户来单收集、订单创建与跟进 | 本人数据 |
| **采购专员** | 供应商选择、资质审核与合作确认 | 本人数据 |
| **运营专员** | 发运计划制定、物流安排与在途跟踪 | 本人数据 |
| **数据管理员** | 数据查看、导出 | 全部数据 |
| **系统管理员** | 权限配置、系统管理 | 全部数据 |

### 设计原则

1. **数据权限优先**：不同角色只能查看/操作自己的数据
2. **三层权限控制**：功能权限 + 数据权限 + 字段权限
3. **预定义角色**：5个标准角色，满足业务需求
4. **安全第一**：密码加密、Token认证、操作审计

---

## 功能清单

### 1. 认证登录

| 功能 | 接口 | 状态 | 说明 |
|------|------|------|------|
| 用户登录 | `POST /api/auth/login` | ⏳ 待开发 | 支持用户名/邮箱/手机号登录 |
| 获取当前用户 | `GET /api/auth/current` | ⏳ 待开发 | 返回用户信息、角色、权限 |
| 用户登出 | `POST /api/auth/logout` | ⏳ 待开发 | 清除Token，记录登出日志 |
| 刷新Token | `POST /api/auth/refresh` | ⏳ 待开发 | Token无感刷新 |
| 修改密码 | `POST /api/auth/change-password` | ⏳ 待开发 | 旧密码验证后修改 |

### 2. 用户管理

| 功能 | 接口 | 权限 | 状态 |
|------|------|------|------|
| 用户列表 | `GET /api/user` | `USER:VIEW` | ⏳ 待开发 |
| 用户详情 | `GET /api/user/{id}` | `USER:VIEW` | ⏳ 待开发 |
| 新增用户 | `POST /api/user` | `USER:CREATE` | ⏳ 待开发 |
| 编辑用户 | `PUT /api/user/{id}` | `USER:UPDATE` | ⏳ 待开发 |
| 删除用户 | `DELETE /api/user/{id}` | `USER:DELETE` | ⏳ 待开发 |
| 重置密码 | `POST /api/user/{id}/reset-password` | `USER:RESET` | ⏳ 待开发 |
| 启用/禁用 | `PATCH /api/user/{id}/status` | `USER:UPDATE` | ⏳ 待开发 |
| 分配角色 | `POST /api/user/{id}/roles` | `USER:UPDATE` | ⏳ 待开发 |

### 3. 角色管理

| 功能 | 接口 | 权限 | 状态 |
|------|------|------|------|
| 角色列表 | `GET /api/role` | `ROLE:VIEW` | ⏳ 待开发 |
| 角色详情 | `GET /api/role/{id}` | `ROLE:VIEW` | ⏳ 待开发 |
| 新增角色 | `POST /api/role` | `ROLE:CREATE` | ⏳ 待开发 |
| 编辑角色 | `PUT /api/role/{id}` | `ROLE:UPDATE` | ⏳ 待开发 |
| 删除角色 | `DELETE /api/role/{id}` | `ROLE:DELETE` | ⏳ 待开发 |
| 分配权限 | `POST /api/role/{id}/permissions` | `ROLE:UPDATE` | ⏳ 待开发 |

### 4. 个人中心

| 功能 | 接口 | 状态 |
|------|------|------|
| 个人信息 | `GET /api/profile` | ⏳ 待开发 |
| 修改资料 | `PUT /api/profile` | ⏳ 待开发 |
| 上传头像 | `POST /api/profile/avatar` | ⏳ 待开发 |
| 修改密码 | `POST /api/profile/password` | ⏳ 待开发 |
| 操作日志 | `GET /api/profile/operation-logs` | ⏳ 待开发 |

---

## 目录结构

```
order-platform-user/
├── pom.xml                                  # Maven 配置
└── src/main/java/com/order/platform/user/
    ├── controller/                          # 控制器层
    │   ├── AuthController.java              # 认证登录
    │   ├── UserController.java              # 用户管理
    │   ├── RoleController.java              # 角色管理
    │   ├── PermissionController.java        # 权限管理
    │   └── ProfileController.java           # 个人中心
    ├── service/                             # 服务层
    │   ├── AuthService.java                 # 认证服务接口
    │   ├── UserService.java                 # 用户服务接口
    │   ├── RoleService.java                 # 角色服务接口
    │   ├── PermissionService.java           # 权限服务接口
    │   ├── UserRoleService.java             # 用户角色服务接口
    │   └── impl/                            # 服务实现
    │       ├── AuthServiceImpl.java
    │       ├── UserServiceImpl.java
    │       ├── RoleServiceImpl.java
    │       ├── PermissionServiceImpl.java
    │       └── UserRoleServiceImpl.java
    ├── mapper/                              # Mapper层
    │   ├── UserMapper.java                  # 用户Mapper
    │   ├── RoleMapper.java                  # 角色Mapper
    │   ├── UserRoleMapper.java              # 用户角色Mapper
    │   └── RolePermissionMapper.java        # 角色权限Mapper
    ├── entity/                              # 实体类
    │   ├── User.java                        # 用户实体（25字段）
    │   ├── Role.java                        # 角色实体
    │   ├── UserRole.java                    # 用户角色关联
    │   └── RolePermission.java              # 角色权限关联
    ├── dto/                                 # 数据传输对象
    │   ├── request/                         # 请求DTO
    │   │   ├── LoginDTO.java                # 登录请求
    │   │   ├── UserCreateDTO.java           # 创建用户
    │   │   ├── UserUpdateDTO.java           # 更新用户
    │   │   ├── RoleCreateDTO.java           # 创建角色
    │   │   └── ChangePasswordDTO.java       # 修改密码
    │   └── response/                        # 响应DTO
    │       ├── LoginVO.java                 # 登录响应
    │       ├── UserVO.java                  # 用户信息
    │       ├── RoleVO.java                  # 角色信息
    │       └── CurrentUserVO.java           # 当前用户信息
    ├── enums/                               # 枚举
    │   ├── DataScopeType.java               # 数据权限类型
    │   ├── RoleType.java                    # 角色类型
    │   └── PermissionCode.java              # 权限码
    ├── exception/                           # 异常
    │   └── PasswordErrorException.java      # 密码错误异常
    ├── interceptor/                         # 拦截器
    │   └── DataScopeInterceptor.java        # 数据权限拦截器
    ├── context/                             # 上下文
    │   └── DataScopeContext.java            # 数据权限上下文
    └── utils/                               # 工具类
        └── PermissionUtil.java              # 权限工具类
```

---

## 数据库设计

### 1. 用户表（t_user）

**SQL文件**：`src/main/resources/sql/user.sql`

**实体类**：`entity/User.java` ✅ 已创建

**核心字段**：
- `username`：用户名（登录账号）
- `password`：密码（BCrypt加密）
- `userCode`：用户编号（业务唯一标识，如USER001）
- `realName`：真实姓名
- `email`/`phone`：联系方式
- `departmentId`/`departmentName`：部门信息
- `position`：职位
- `employeeNo`：工号
- `isEnabled`：是否启用
- `isLocked`：是否锁定
- `loginCount`：登录次数
- `lastLoginTime`：最后登录时间

**设计要点**：
- 25字段完整设计，满足实际项目长期维护需求
- 支持账号安全（账户锁定、密码过期）
- 支持登录统计（登录次数、最后登录）
- 支持组织信息（部门、职位、工号）
- 支持基于部门的数据权限隔离

### 2. 角色表（t_role）

**SQL文件**：`src/main/resources/sql/role.sql`

**核心字段**：
- `role_code`：角色代码，唯一标识（如 SYSTEM_ADMIN、CUSTOMER_MANAGER）
- `role_name`：角色名称（如 系统管理员、客户经理）
- `role_type`：角色类型（BUSINESS/SYSTEM）
- `data_scope_type`：数据权限类型（1-全部、2-本部门、3-本人、4-自定义）⭐核心字段
- `is_system`：是否系统内置角色（防止误删）

**预定义角色**（5个）：
| 角色代码 | 角色名称 | 数据权限 | 职责 |
|---------|---------|---------|------|
| SYSTEM_ADMIN | 系统管理员 | 全部数据 | 权限配置、系统管理 |
| DATA_ADMIN | 数据管理员 | 全部数据 | 数据查看、导出 |
| CUSTOMER_MANAGER | 客户经理 | 本人数据 | 订单创建与跟进 |
| PURCHASE_SPECIALIST | 采购专员 | 本人数据 | 合作方管理、资质审核 |
| OPERATION_SPECIALIST | 运营专员 | 本人数据 | 发运计划、物流跟踪 |

### 3. 用户角色关联表（t_user_role）

**SQL文件**：`src/main/resources/sql/user_role.sql`

**核心字段**：
- `user_id`：用户ID
- `role_id`：角色ID
- `role_code`：角色代码（冗余字段，便于查询）
- `is_primary`：是否主角色（⭐用于数据权限判断）

**设计要点**：
- 用户可以拥有多个角色，通过 `is_primary` 标识主角色
- 数据权限以主角色的 `data_scope_type` 为准
- 适度冗余 `username` 和 `role_code`，减少 JOIN 查询

### 4. 角色权限关联表（t_role_permission）

**SQL文件**：`src/main/resources/sql/role_permission.sql`

**核心字段**：
- `role_id`：角色ID
- `permission_code`：权限代码（格式：{模块}:{操作}）

**权限代码格式**：
```
{模块}:{操作}
例如：ORDER:VIEW（订单查看）、SHIPMENT:CREATE（发运创建）
```

**权限模块**（Module）：
- USER：用户管理
- ROLE：角色管理
- ORDER：订单管理
- PARTNER：合作方管理
- SHIPMENT：发运管理
- ATTACHMENT：附件管理
- EXCEPTION：异常管理
- DASHBOARD：看板管理
- DATA：数据管理

**权限操作**（Action）：
- *：所有权限
- VIEW：查看
- CREATE：创建
- UPDATE：更新
- DELETE：删除
- AUDIT：审核
- UPLOAD：上传
- DOWNLOAD：下载
- EXPORT：导出
- IMPORT：导入

**设计要点**：
- 不使用 `t_permission` 表，权限是代码层面的硬编码
- 修改权限需要修改代码，不需要动态管理权限表
- 每个角色关联多个权限代码，实现功能权限控制

---

## API接口设计

### 1. 认证登录

#### 1.1 用户登录

**接口**：`POST /api/auth/login`

**请求参数**：
```json
{
  "account": "zhangsan",      // 用户名/邮箱/手机号
  "password": "123456",       // 密码
  "captcha": "abcd",          // 图形验证码（可选）
  "captchaKey": "uuid"        // 验证码Key（可选）
}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 604800,

    "userInfo": {
      "id": 1,
      "username": "zhangsan",
      "realName": "张三",
      "email": "zhangsan@example.com",
      "phone": "13800138000",
      "avatar": "https://...",
      "userCode": "USER001",
      "employeeNo": "E001",
      "position": "客户经理",
      "departmentId": 10,
      "departmentName": "华东大区",

      // 权限信息
      "roles": ["CUSTOMER_MANAGER"],
      "permissions": [
        "ORDER:VIEW",
        "ORDER:CREATE",
        "ORDER:UPDATE",
        "SHIPMENT:VIEW",
        "ATTACHMENT:VIEW",
        "ATTACHMENT:UPLOAD"
      ],

      // 数据权限范围
      "dataScope": {
        "type": "SELF",
        "departmentId": 10,
        "departmentName": "华东大区"
      }
    }
  }
}
```

### 2. 用户管理

#### 2.1 用户列表

**接口**：`GET /api/user`

**查询参数**：
```
current: 当前页码
size: 每页大小
username: 用户名（模糊查询）
realName: 真实姓名（模糊查询）
departmentId: 部门ID
position: 职位
isEnabled: 是否启用
```

**响应数据**：
```json
{
  "code": 200,
  "data": {
    "records": [...],
    "total": 100,
    "current": 1,
    "size": 10,
    "pages": 10
  }
}
```

---

## 核心业务逻辑

### 1. 登录流程

```java
// 1. 查询用户（支持用户名/邮箱/手机号）
User user = userMapper.findByAccount(account);

// 2. 检查用户状态
checkUserStatus(user);  // is_enabled, is_locked

// 3. 验证密码
if (!passwordEncoder.matches(password, user.getPassword())) {
    handlePasswordError(user.getId());  // 密码错误次数+1
    throw new BusinessException("密码错误");
}

// 4. 检查密码过期
checkPasswordExpiration(user);

// 5. 查询用户角色和权限
List<Role> roles = userRoleService.getRolesByUserId(user.getId());
List<String> permissions = permissionService.getPermissionsByRoles(roles);

// 6. 查询数据权限范围
DataScopeContext dataScope = buildDataScope(user, roles);

// 7. 生成Token
String token = jwtUtil.generateToken(user.getId(), user.getUsername(),
    roles.stream().map(Role::getRoleCode).collect(Collectors.toList()));

// 8. 更新登录信息
updateLoginInfo(user.getId(), clientIp);

// 9. 返回完整用户信息
return buildLoginVO(token, user, roles, permissions, dataScope);
```

### 2. 数据权限控制

```java
/**
 * MyBatis-Plus 查询拦截器
 * 自动添加数据权限过滤条件
 */
@Component
public class DataScopeInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms,
                           Object parameter, RowBounds rowBounds,
                           ResultHandler resultHandler, BoundSql boundSql) {

        CurrentUser user = CurrentUserHolder.get();
        if (user == null) return;

        DataScopeContext dataScope = getDataScopeByUserId(user.getId());

        // 根据数据权限类型添加WHERE条件
        String dataScopeSql = buildDataScopeSql(dataScope);

        // 修改原始SQL
        // ... 具体实现
    }

    private String buildDataScopeSql(DataScopeContext dataScope) {
        switch (dataScope.getDataScopeType()) {
            case ALL:
                return "";  // 全部数据，不添加过滤
            case DEPARTMENT:
                return " AND department_id = " + dataScope.getDepartmentId();
            case SELF:
                return " AND created_by = " + dataScope.getUserId();
            case CUSTOM:
                return buildCustomDataScopeSql(dataScope);
            default:
                return "";
        }
    }
}
```

---

## 数据权限设计

### 数据权限类型

| 类型 | 代码 | 说明 | 适用角色 |
|------|------|------|----------|
| 全部数据 | ALL | 可查看和操作全部数据 | 系统管理员、数据管理员 |
| 本部门数据 | DEPARTMENT | 只能查看本部门的数据 | 部门经理 |
| 本人数据 | SELF | 只能查看自己创建的数据 | 客户经理、采购专员、运营专员 |
| 自定义范围 | CUSTOM | 自定义数据范围（按客户/供应商等） | 未来扩展 |

### 权限矩阵

| 模块 | 客户经理 | 采购专员 | 运营专员 | 数据管理员 | 系统管理员 |
|------|----------|----------|----------|------------|------------|
| **订单管理** | | | | | |
| - 查看订单 | ✅ 本人 | ✅ 本人 | ✅ 本人 | ✅ 全部 | ✅ 全部 |
| - 创建订单 | ✅ | ❌ | ❌ | ❌ | ✅ |
| - 修改订单 | ✅ 本人 | ❌ | ❌ | ❌ | ✅ |
| - 删除订单 | ❌ | ❌ | ❌ | ❌ | ✅ |
| **发运管理** | | | | | |
| - 查看发运 | ✅ 本人 | ✅ 本人 | ✅ 本人 | ✅ 全部 | ✅ 全部 |
| - 创建发运 | ❌ | ❌ | ✅ | ❌ | ✅ |
| **合作方管理** | | | | | |
| - 查看合作方 | ✅ 全部 | ✅ 全部 | ✅ 全部 | ✅ 全部 | ✅ 全部 |
| - 创建合作方 | ❌ | ✅ | ❌ | ❌ | ✅ |
| - 资质审核 | ❌ | ✅ | ❌ | ❌ | ✅ |
| **附件管理** | | | | | |
| - 查看附件 | ✅ 本人 | ✅ 本人 | ✅ 本人 | ✅ 全部 | ✅ 全部 |
| - 上传附件 | ✅ | ✅ | ✅ | ❌ | ✅ |
| - 下载附件 | ✅ | ✅ | ✅ | ✅ | ✅ |
| **系统管理** | | | | | |
| - 用户管理 | ❌ | ❌ | ❌ | ❌ | ✅ |
| - 角色管理 | ❌ | ❌ | ❌ | ❌ | ✅ |
| - 数据导出 | ❌ | ❌ | ❌ | ✅ | ✅ |

---

## 开发进度

### 第一阶段：核心功能（进行中）

- [x] 1.1 数据库表创建 ✅ 已完成（2026-01-07）
  - [x] 角色表（t_role）- 13字段，5个预定义角色
  - [x] 用户角色关联表（t_user_role）- 11字段，支持主角色
  - [x] 角色权限关联表（t_role_permission）- 10字段，权限代码硬编码
  - [x] 预定义角色数据（5个标准角色）
  - [x] 预定义权限数据（完整权限初始化）

- [ ] 1.2 实体类和Mapper
  - [x] User.java（用户实体，25字段）✅ 已创建
  - [ ] Role.java（角色实体）
  - [x] UserRole.java（用户角色关联）✅ 已创建并修复
  - [ ] RolePermission.java（角色权限关联）
  - [x] UserMapper.java ✅ 已创建
  - [ ] RoleMapper.java
  - [x] UserRoleMapper.java ✅ 已创建并修复
  - [ ] RolePermissionMapper.java

- [ ] 1.2.1 认证辅助工具
  - [x] AuthHelper.java（User → CurrentUser转换）✅ 已创建

- [ ] 1.3 认证登录功能
  - [ ] 用户登录（支持用户名/邮箱/手机号）
  - [ ] 密码错误锁定（连续5次锁定30分钟）
  - [ ] 密码过期检查
  - [ ] 用户状态检查
  - [ ] 查询用户角色和权限
  - [ ] 查询数据权限范围
  - [ ] 生成JWT Token
  - [ ] 更新登录信息
  - [ ] 记录操作日志

- [ ] 1.4 用户管理功能
  - [ ] 用户列表（分页、多条件筛选）
  - [ ] 用户详情
  - [ ] 新增用户
  - [ ] 编辑用户
  - [ ] 删除用户（软删除）
  - [ ] 重置密码
  - [ ] 启用/禁用
  - [ ] 分配角色

- [ ] 1.5 角色管理功能
  - [ ] 角色列表
  - [ ] 角色详情
  - [ ] 新增角色
  - [ ] 编辑角色
  - [ ] 删除角色
  - [ ] 分配权限

### 第二阶段：数据权限

- [ ] 2.1 数据权限设计
  - [ ] DataScopeType 枚举定义
  - [ ] DataScopeContext 上下文
  - [ ] 角色与数据权限关联

- [ ] 2.2 数据权限拦截器
  - [ ] MyBatis-Plus 拦截器实现
  - [ ] 自动添加WHERE条件
  - [ ] 支持ALL/DEPARTMENT/SELF/CUSTOM

- [ ] 2.3 业务模块数据权限
  - [ ] 订单查询数据权限
  - [ ] 发运查询数据权限
  - [ ] 合作方查询数据权限
  - [ ] 统计接口数据权限

### 第三阶段：增强功能

- [ ] 3.1 个人中心
  - [ ] 个人信息查询
  - [ ] 修改资料
  - [ ] 上传头像
  - [ ] 修改密码
  - [ ] 操作日志查询

- [ ] 3.2 安全增强
  - [ ] 图形验证码
  - [ ] 手机验证码登录
  - [ ] Token刷新
  - [ ] 记住登录状态

- [ ] 3.3 用户统计
  - [ ] 用户总数统计
  - [ ] 今日新增统计
  - [ ] 活跃用户统计
  - [ ] 部门分布统计
  - [ ] 登录趋势统计

---

## 文档更新规范

### 更新原则

1. **及时更新**：代码修改后立即更新文档
2. **保持同步**：文档与代码保持一致
3. **版本记录**：每次更新添加版本记录
4. **清晰描述**：更新内容描述清晰详细

### 更新时机

| 场景 | 是否需要更新 | 更新内容 |
|------|-------------|----------|
| 新增功能 | ✅ 必须更新 | 功能说明、API接口、数据库设计 |
| 修改功能 | ✅ 必须更新 | 修改说明、变更记录 |
| 修复Bug | ✅ 建议更新 | Bug说明、修复方案 |
| 优化性能 | ⏸️ 可选更新 | 优化说明、性能对比 |
| 调整代码格式 | ❌ 无需更新 | - |

### 更新流程

1. **修改代码前**：先确定需要更新哪些文档部分
2. **修改代码时**：同步更新相关文档
3. **修改代码后**：检查文档是否完整
4. **提交代码时**：检查文档是否已更新

### 版本记录格式

```markdown
### v1.x.x (YYYY-MM-DD)

#### 更新类型
- [功能/优化/修复]

**更新人**：你的名字

**更新内容**：
- 更新项1：具体描述
- 更新项2：具体描述

**影响范围**：
- 影响的模块
- 需要注意的事项

**相关文件**：
- 文件路径1
- 文件路径2

**相关Issue/PR**：#123
```

### 更新模板

#### 新增功能时

```markdown
#### 新增功能

**功能名称**：[功能名称]

**功能说明**：
- 功能描述
- 使用场景
- 注意事项

**API接口**：
- 接口路径：`[METHOD] /api/path`
- 请求参数：[参数列表]
- 响应数据：[响应结构]

**数据库变更**：
- 新增表：[表名]
- 新增字段：[表名].[字段名]

**使用示例**：
```java
// 代码示例
```

**影响范围**：
- 影响的模块
- 需要的配置
```

#### 修改功能时

```markdown
#### 功能优化

**原功能**：[功能描述]

**优化内容**：
- 优化项1：[具体说明]
- 优化项2：[具体说明]

**优化原因**：
[为什么需要优化]

**优化效果**：
- 性能提升：[具体数据]
- 代码简化：[具体说明]

**兼容性说明**：
- 是否向后兼容
- 是否需要迁移数据
```

#### 修复Bug时

```markdown
#### Bug修复

**Bug描述**：
[Bug现象]

**Bug原因**：
[Bug根本原因]

**修复方案**：
[修复方案说明]

**测试验证**：
- 测试场景1：[结果]
- 测试场景2：[结果]

**影响范围**：
- 影响的功能
- 是否需要回归测试
```

---

## 更新记录

### v1.0.0 (2026-01-07)

#### 初始化文档

**更新人**：开发组

**更新内容**：
- 创建用户模块 README 文档
- 定义功能清单（4大模块、20+接口）
- 设计数据库表结构（4张表）
- 设计 API 接口规范
- 规划数据权限体系
- 制定开发进度计划
- 制定文档更新规范

**设计要点**：
- 基于甲方需求文档，定义5个预定义角色
- 三层权限控制：功能权限 + 数据权限 + 字段权限
- 数据权限类型：ALL/DEPARTMENT/SELF/CUSTOM
- 登录返回完整权限信息（角色、权限、数据权限）
- MyBatis-Plus 拦截器实现数据权限自动过滤

**相关文件**：
- README.md（本文档）
- src/main/resources/sql/role.sql（✅ 已创建）
- src/main/resources/sql/user_role.sql（✅ 已创建）
- src/main/resources/sql/role_permission.sql（✅ 已创建）

---

### v1.0.1 (2026-01-07)

#### 新增用户权限模块数据库表

**更新人**：开发组

**更新内容**：
- ✅ 创建角色表 SQL 脚本（role.sql）
  - 13字段精简设计（相比参考设计减少41%冗余）
  - 5个预定义角色完整初始化数据
  - data_scope_type 作为数据权限核心字段
  - is_system 标识系统内置角色

- ✅ 创建用户角色关联表 SQL 脚本（user_role.sql）
  - 11字段精简设计（相比参考设计减少50%冗余）
  - is_primary 标识主角色，用于数据权限判断
  - 适度冗余设计，优化查询性能

- ✅ 创建角色权限关联表 SQL 脚本（role_permission.sql）
  - 10字段精简设计（相比参考设计减少44%冗余）
  - 权限代码直接存储，不使用 t_permission 表
  - 5个预定义角色的完整权限初始化数据

**设计决策**：
1. **不使用 t_permission 表**：权限是代码层面的硬编码，修改权限需要修改代码
2. **data_scope_type 在角色表**：数据权限是角色特性，不是用户特性
3. **支持用户多角色**：通过 is_primary 标识主角色，用于数据权限判断
4. **适度冗余优化**：关联表存储 role_code 和 username，减少 JOIN 查询

**权限代码格式**：
- 格式：{模块}:{操作}
- 示例：ORDER:VIEW（订单查看）、SHIPMENT:CREATE（发运创建）
- 模块：USER、ROLE、ORDER、PARTNER、SHIPMENT、ATTACHMENT、EXCEPTION、DASHBOARD、DATA
- 操作：*（所有）、VIEW、CREATE、UPDATE、DELETE、AUDIT、UPLOAD、DOWNLOAD、EXPORT、IMPORT

**相关文件**：
- src/main/resources/sql/role.sql（已创建）
- src/main/resources/sql/user_role.sql（已创建）
- src/main/resources/sql/role_permission.sql（已创建）
- ../数据库设计文档.md（已更新 - 新增第11-13章节）

---

### v1.0.2 (2026-01-07)

#### 新增用户实体类和辅助工具

**更新人**：开发组

**更新内容**：

- ✅ 创建 User.java（用户实体类）
  - 25字段完整设计
  - 支持账号安全（isLocked、passwordExpireTime）
  - 支持登录统计（loginCount、lastLoginTime）
  - 支持组织信息（departmentId、position、employeeNo）
  - 从 common 模块迁移到 user 模块

- ✅ 创建 UserMapper.java（用户Mapper）
  - selectByUsername：根据用户名查询（登录验证）
  - selectByUserCode：根据用户编号查询
  - selectByEmail：根据邮箱查询
  - selectByPhone：根据手机号查询

- ✅ 创建 AuthHelper.java（认证辅助工具）
  - User 实体 → CurrentUser DTO 转换
  - 解耦 User 模块与 Common 模块
  - 只提取Token中需要的11个核心字段

- ✅ 修复 UserRole.java（用户角色关联实体）
  - 添加缺失字段：username、is_primary、createdAt、createdBy、updatedAt、updatedBy
  - 删除多余字段：isEnabled（表中不存在）
  - 使用 @TableField 注解实现自动填充

- ✅ 修复 UserRoleMapper.java
  - 移除SQL中不存在的 is_enabled 条件
  - 更新查询条件注释

- ✅ 移动 user.sql 到 user 模块
  - 从 order-platform-common 移动到 order-platform-user
  - User表相关代码集中在user模块管理

**设计决策**：
1. **User表归属**：User是业务实体，归属于user模块而非common模块
2. **模块解耦**：Common模块只依赖CurrentUser DTO，不依赖User实体类
3. **转换层**：通过AuthHelper实现User → CurrentUser的转换
4. **职责清晰**：User模块负责用户业务，Common模块提供通用功能

**影响范围**：
- Common模块：零影响（不依赖User实体类）
- User模块：新增User实体、UserMapper、AuthHelper

**相关文件**：
- entity/User.java（已创建）
- entity/UserRole.java（已修复）
- mapper/UserMapper.java（已创建）
- mapper/UserRoleMapper.java（已修复）
- service/AuthHelper.java（已创建）
- resources/sql/user.sql（已移动）

---

## 相关文档

- [订单可视化平台解决方案](../可视化数字化管理平台解决方案v1217.md)
- [Common 模块文档](../order-platform-common/README.md)
- [数据库设计文档](../docs/数据库/0.数据库设计文档.md)
- [后端开发指导文档](../order-platform-backend/后端开发指导文档.md)

---

## 维护者

- **开发组** - 初始开发

---

## 许可证

本项目采用 MIT 许可证。
