# User实体类和Mapper

> **订单可视化平台 - 用户模块**
> User表从common模块迁移到user模块，创建User实体类、UserMapper和AuthHelper辅助工具

> **完成日期**：2026-01-07
> **维护团队**：后端开发组

---

## 📋 目录

- [功能概述](#功能概述)
- [设计决策](#设计决策)
- [实现详情](#实现详情)
- [模块解耦](#模块解耦)
- [相关文件](#相关文件)

---

## 功能概述

### 背景

User表是业务实体，应该归属于user模块而非common模块。将user.sql从common模块移动到user模块，并创建对应的Java实体类、Mapper和辅助工具。

### 目标

- ✅ User表SQL脚本归属于user模块管理
- ✅ 创建User实体类（25字段完整版）
- ✅ 创建UserMapper（含4个查询方法）
- ✅ 创建AuthHelper（User → CurrentUser转换）
- ✅ 修复UserRole实体类（添加缺失字段）
- ✅ 修复UserRoleMapper（移除不存在的条件）
- ✅ 模块解耦：Common模块完全不依赖User实体类

---

## 设计决策

### User表归属

| 方案 | 说明 | 选择 |
|------|------|------|
| **User表在common模块** | 所有模块都可能使用User | ❌ 职责不清 |
| **User表在user模块** | User是用户业务的核心实体 | ✅ 推荐 |

### 模块解耦机制

```
┌─────────────────────────────────────────────────┐
│  Common模块（零依赖）                            │
├─────────────────────────────────────────────────┤
│  ├── CurrentUser.java（DTO，11字段）             │
│  ├── CurrentUserHolder.java（ThreadLocal）       │
│  └── UserRoleProvider.java（接口）               │
│     ↓                                            │
│  不依赖User实体类 ✅                            │
└─────────────────────────────────────────────────┘

User模块
├── User.java（实体，25字段）
├── UserMapper.java
├── AuthHelper.java（转换工具）
└── UserRoleService.java（实现UserRoleProvider）
    ↓
User → CurrentUser 转换
```

### 转换层设计

**AuthHelper.toCurrentUser()**：
- User有25字段（完整用户信息）
- CurrentUser有11字段（认证层信息）
- 只提取Token中需要携带的核心字段
- 不包含敏感信息（如password）

---

## 实现详情

### 1. User实体类

**文件路径**：`order-platform-user/src/main/java/com/order/platform/user/entity/User.java`

**字段列表**（25字段）：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户ID（主键） |
| username | String | 用户名（登录账号） |
| password | String | 密码（BCrypt加密） |
| userCode | String | 用户编号（业务唯一标识） |
| realName | String | 真实姓名 |
| email | String | 邮箱 |
| phone | String | 手机号 |
| avatar | String | 头像URL |
| isEnabled | Integer | 是否启用 |
| isLocked | Integer | 是否锁定 |
| lockedTime | LocalDateTime | 锁定时间 |
| lockedReason | String | 锁定原因 |
| lastLoginTime | LocalDateTime | 最后登录时间 |
| lastLoginIp | String | 最后登录IP |
| loginCount | Integer | 登录次数 |
| passwordChangedTime | LocalDateTime | 密码修改时间 |
| passwordExpireTime | LocalDateTime | 密码过期时间 |
| departmentId | Long | 部门ID |
| departmentName | String | 部门名称 |
| position | String | 职位 |
| employeeNo | String | 工号 |
| remark | String | 用户备注 |
| createdAt | LocalDateTime | 创建时间 |
| createdBy | Long | 创建人ID |
| updatedAt | LocalDateTime | 更新时间 |
| updatedBy | Long | 更新人ID |
| isDeleted | Integer | 是否删除 |

**注解使用**：
- `@TableName("t_user")`：指定数据库表名
- `@TableId(type = IdType.AUTO)`：主键自增
- `@TableField(fill = FieldFill.INSERT)`：插入时自动填充
- `@TableField(fill = FieldFill.INSERT_UPDATE)`：插入和更新时自动填充
- `@TableLogic`：逻辑删除字段

### 2. UserMapper

**文件路径**：`order-platform-user/src/main/java/com/order/platform/user/mapper/UserMapper.java`

**查询方法**：

| 方法 | 说明 | SQL条件 |
|------|------|----------|
| selectByUsername | 根据用户名查询 | username = ? AND is_deleted = 0 |
| selectByUserCode | 根据用户编号查询 | user_code = ? AND is_deleted = 0 |
| selectByEmail | 根据邮箱查询 | email = ? AND is_deleted = 0 |
| selectByPhone | 根据手机号查询 | phone = ? AND is_deleted = 0 |

**设计要点**：
- 继承`BaseMapper<User>`，提供基础CRUD操作
- 使用`@Select`注解编写SQL，简洁明了
- 查询条件不包含`is_enabled`和`is_locked``，状态检查在Service层进行

### 3. AuthHelper（认证辅助工具）

**文件路径**：`order-platform-user/src/main/java/com/order/platform/user/service/AuthHelper.java`

**功能**：
- User实体 → CurrentUser DTO转换
- 解耦User模块与Common模块
- 只提取Token中需要的11个核心字段

**转换字段映射**：

| User字段 | CurrentUser字段 | 说明 |
|---------|----------------|------|
| id | id | 用户ID |
| username | username | 用户名 |
| realName | realName | 真实姓名 |
| email | email | 邮箱 |
| phone | phone | 手机号 |
| avatar | avatar | 头像 |
| departmentId | departmentId | 部门ID |
| departmentName | departmentName | 部门名称 |
| userCode | userCode | 用户编号 |
| employeeNo | employeeNo | 工号 |
| position | position | 职位 |
| - | roles | 角色代码列表（参数传入） |

**不转换的字段**（User有但CurrentUser没有）：
- password：敏感信息
- isEnabled、isLocked：状态信息
- loginCount、lastLoginTime、lastLoginIp：统计信息
- passwordChangedTime、passwordExpireTime：密码管理信息
- remark、createdAt、createdBy、updatedAt、updatedBy：系统字段

### 4. UserRole实体类修复

**修复内容**：

| 操作 | 字段 | 说明 |
|------|------|------|
| **添加** | username | 用户名（冗余字段） |
| **添加** | is_primary | 是否主角色 |
| **添加** | createdAt | 创建时间 |
| **添加** | createdBy | 创建人ID |
| **添加** | updatedAt | 更新时间 |
| **添加** | updatedBy | 更新人ID |
| **删除** | isEnabled | 表中不存在此字段 |

### 5. UserRoleMapper修复

**修复内容**：
- 移除SQL中的`AND is_enabled = 1`条件
- 角色启用状态过滤在RoleService中处理，通过JOIN t_role表实现

---

## 模块解耦

### Common模块依赖验证

```
✅ 检查项：Common模块是否导入User实体类
结果：❌ 没有找到任何导入

验证命令：
grep -r "import.*\.user\.entity\.User" order-platform-common/
```

### 依赖关系图

```
┌──────────────────────────────────────────────────────┐
│                   依赖关系图                          │
├──────────────────────────────────────────────────────┤
│                                                        │
│  order-platform-common                              │
│  ├── dto/CurrentUser.java                          │
│  ├── holder/CurrentUserHolder.java                   │
│  └── provider/UserRoleProvider.java  ────────┐    │
│                                                  │    │
│  （不依赖User实体类 ✅）                       │    │
│                                                  │    │
│  order-platform-user                            │    │
│  ├── entity/User.java                            │    │
│  ├── mapper/UserMapper.java                      │    │
│  ├── service/AuthHelper.java  ─────────────────┘    │
│  └── service/UserRoleService.java ───────┐        │
│                                              │        │
│  （实现UserRoleProvider接口）                │        │
│                                              │        │
└──────────────────────────────────────────────┴────────┘

数据流：
AuthService.login()
  ↓
UserMapper.selectByUsername() → User实体（25字段）
  ↓
UserRoleService.getRoleCodesByUserId() → List<String>
  ↓
AuthHelper.toCurrentUser(user, roles) → CurrentUser DTO（11字段）
  ↓
JwtUtil.generateToken(userId, username, roles) → Token
  ↓
CurrentUserHolder.set(currentUser) → ThreadLocal
  ↓
Common模块 → 从ThreadLocal获取CurrentUser
```

---

## 相关文件

### Java文件

| 文件 | 路径 | 说明 |
|------|------|------|
| User.java | order-platform-user/src/main/java/com/order/platform/user/entity/ | 用户实体类（25字段） |
| UserMapper.java | order-platform-user/src/main/java/com/order/platform/user/mapper/ | 用户Mapper |
| AuthHelper.java | order-platform-user/src/main/java/com/order/platform/user/service/ | 认证辅助工具 |
| UserRole.java | order-platform-user/src/main/java/com/order/platform/user/entity/ | 用户角色关联实体（已修复） |
| UserRoleMapper.java | order-platform-user/src/main/java/com/order/platform/user/mapper/ | 用户角色Mapper（已修复） |

### SQL文件

| 文件 | 路径 | 说明 |
|------|------|------|
| user.sql | order-platform-user/src/main/resources/sql/ | 用户表SQL脚本（已移动） |
| role.sql | order-platform-user/src/main/resources/sql/ | 角色表SQL脚本 |
| user_role.sql | order-platform-user/src/main/resources/sql/ | 用户角色关联表SQL脚本 |
| role_permission.sql | order-platform-user/src/main/resources/sql/ | 角色权限关联表SQL脚本 |

### 文档更新

| 文档 | 状态 | 说明 |
|------|------|------|
| order-platform-user/README.md | ✅ 已更新 | 添加User表说明、更新开发进度、新增v1.0.2版本记录 |
| order-platform-common/README.md | ✅ 已更新 | 添加v1.0.5版本记录，说明user.sql已移除 |
| 数据库设计文档.md | ✅ 已更新 | 添加V1.0.4版本记录 |
| docs/已完成的功能.md | ✅ 已更新 | 添加User实体类和Mapper功能条目 |

---

## 更新记录

| 日期 | 版本 | 更新内容 | 更新人 |
|------|------|----------|--------|
| 2026-01-07 | v1.0.0 | 初始化文档，记录User实体类和Mapper的实现 | 开发组 |
