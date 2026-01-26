# plan_55 阶段2：用户 Service + Controller 实施指导教程

```
level: 3
file_id: plan_55_phase2
parent: plan_55
status: in_progress
created: 2025-01-17
estimated_days: 2
```

---

## 一、任务概述

### 1.1 目标

在 plan_55 阶段1（数据库建表）完成后，实现用户模块的核心业务逻辑：

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 用户登录 | P0 | 用户名/密码登录，返回用户信息 |
| 密码加密 | P0 | BCrypt 加密存储 |
| 用户 CRUD | P1 | 创建/查询/更新/删除用户 |
| 用户角色关联 | P1 | 分配角色给用户 |
| 登录状态更新 | P1 | 记录登录时间、IP、次数 |

### 1.2 技术栈

| 组件 | 版本 | 用途 |
|------|------|------|
| MyBatis-Plus | 3.5.x | ORM 框架 |
| Spring Boot | 3.2.x | 应用框架 |
| BCrypt | - | 密码加密 |
| Validation | - | 参数校验 |

---

## 二、目录结构设计

```
order-platform-user/
├── src/main/java/com/company/order/visual/user/
│   ├── entity/                    # 实体层（已存在）
│   │   ├── User.java
│   │   ├── Role.java
│   │   └── UserRole.java
│   │
│   ├── mapper/                     # Mapper 层（新增）
│   │   ├── UserMapper.java
│   │   ├── RoleMapper.java
│   │   └── UserRoleMapper.java
│   │
│   ├── dto/                        # 数据传输对象（新增）
│   │   ├── LoginRequest.java       # 登录请求
│   │   ├── UserCreateRequest.java  # 创建用户请求
│   │   ├── UserUpdateRequest.java  # 更新用户请求
│   │   ├── LoginResponse.java      # 登录响应
│   │   ├── UserVO.java             # 用户视图对象
│   │   └── UserQueryRequest.java   # 查询请求
│   │
│   ├── service/                    # Service 层（新增）
│   │   ├── UserService.java        # 用户服务接口
│   │   ├── RoleService.java        # 角色服务接口
│   │   └── impl/
│   │       ├── UserServiceImpl.java
│   │       └── RoleServiceImpl.java
│   │
│   └── controller/                 # Controller 层（新增）
│       ├── UserController.java
│       └── RoleController.java
│
└── src/main/resources/mapper/      # XML 映射文件（按需）
```

---

## 三、现有基础

### 3.1 已完成的实体类

- `User.java` - 用户实体（23 字段）
- `Role.java` - 角色实体（11 字段）
- `UserRole.java` - 用户角色关联实体（8 字段）

### 3.2 已完成的公共模块

- `Result<T>` - 统一响应封装
- `ResponseCode` - 响应码枚举（含用户模块错误码）
- `BusinessException` - 业务异常类
- `GlobalExceptionHandler` - 全局异常处理器

### 3.3 已有的响应码

```java
// ========== 用户模块 ==========
USER_NOT_FOUND(4001, "用户不存在"),
USER_DISABLED(4002, "用户已禁用"),
USER_LOCKED(4003, "用户已锁定"),
LOGIN_FAILED(4004, "用户名或密码错误"),
TOKEN_INVALID(4005, "Token无效或已过期"),
```

---

## 四、Mapper 层设计

### 4.1 UserMapper.java

```java
package com.company.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // BaseMapper 已提供：insert/deleteById/updateById/selectById/selectList/selectPage
}
```

### 4.2 RoleMapper.java

```java
package com.company.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.user.entity.Role;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}
```

### 4.3 UserRoleMapper.java

```java
package com.company.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.user.dto.UserVO.RoleInfo;
import com.company.user.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 通过 JOIN 查询用户角色（一次查询，避免 N+1）
     */
    @Select("""
            SELECT r.id as roleId, r.role_code as roleCode, r.role_name as roleName,
                   r.data_scope_type as dataScopeType, ur.is_primary as isPrimary
            FROM t_user_role ur
            JOIN t_role r ON ur.role_id = r.id
            WHERE ur.user_id = #{userId} AND ur.is_deleted = false AND r.is_enabled = true
            """)
    List<RoleInfo> selectRolesByUserId(Long userId);
}
```

---

## 五、DTO 设计

### 5.1 LoginRequest.java

```java
package com.company.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
```

### 5.2 UserCreateRequest.java

```java
package com.company.user.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class UserCreateRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度3-20位")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字、下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20位")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 20, message = "姓名长度不能超过20位")
    private String realName;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private List<Long> roleIds;
    private Long departmentId;
    private String position;
    private String employeeNo;

    @Size(max = 200, message = "备注长度不能超过200位")
    private String remark;
}
```

### 5.3 UserUpdateRequest.java

```java
package com.company.user.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class UserUpdateRequest {
    private Long id;

    @Size(max = 20, message = "姓名长度不能超过20位")
    private String realName;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String avatar;
    private Boolean isEnabled;
    private Long departmentId;
    private String position;
    private String employeeNo;

    @Size(max = 200, message = "备注长度不能超过200位")
    private String remark;

    private List<Long> roleIds;
}
```

### 5.4 LoginResponse.java（简化版）

```java
package com.company.user.dto;

import lombok.*;

/**
 * 登录响应
 * 设计原则：避免 DTO 重复，复用 UserVO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    /**
     * 用户信息（复用 UserVO）
     */
    private UserVO user;

    /**
     * JWT Token（JWT 阶段添加，当前暂为 null）
     */
    private String token;
}
```

### 5.5 UserVO.java

```java
package com.company.user.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {
    private Long id;
    private String username;
    private String userCode;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private Boolean isEnabled;
    private Boolean isLocked;
    private String position;
    private String employeeNo;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private Integer loginCount;
    private List<RoleInfo> roles;
    private LocalDateTime createdAt;

    /**
     * 角色信息（从 LoginResponse 移过来，统一管理）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleInfo {
        private Long roleId;
        private String roleCode;
        private String roleName;
        private Integer dataScopeType;
        private Boolean isPrimary;
    }
}
```

### 5.6 UserQueryRequest.java

```java
package com.company.user.dto;

import lombok.Data;

@Data
public class UserQueryRequest {
    private String username;      // 模糊查询
    private String realName;      // 模糊查询
    private Boolean isEnabled;
    private Boolean isLocked;
    private Long departmentId;
    private Long roleId;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
```

---

## 六、Service 层设计

### 6.1 UserService.java 接口

```java
package com.company.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.user.dto.*;

public interface UserService {
    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 创建用户
     */
    Long createUser(UserCreateRequest request);

    /**
     * 更新用户
     */
    void updateUser(UserUpdateRequest request);

    /**
     * 删除用户（软删除）
     */
    void deleteUser(Long userId);

    /**
     * 根据ID获取用户详情
     */
    UserVO getUserById(Long userId);

    /**
     * 分页查询用户
     */
    Page<UserVO> pageUsers(UserQueryRequest request);

    // ==================== 以下接口等权限模块完成后再添加 ====================
    // void changePassword(Long userId, String oldPassword, String newPassword);
    // void resetPassword(Long userId, String newPassword);
}c  
```

### 6.2 UserServiceImpl.java 实现（核心登录逻辑）

```java
package com.company.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.common.exception.BusinessException;
import com.company.common.response.ResponseCode;
import com.company.user.dto.*;
import com.company.user.entity.User;
import com.company.user.entity.UserRole;
import com.company.user.mapper.UserMapper;
import com.company.user.mapper.UserRoleMapper;
import com.company.user.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ==================== 登录 ====================

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 查询用户（支持用户名/邮箱/手机号登录）
        User user = findUserByUsername(request.getUsername());

        // 2. 验证用户状态和密码
        if (!user.getIsEnabled()) {
            throw new BusinessException(ResponseCode.USER_DISABLED);
        }
        if (user.getIsLocked()) {
            throw new BusinessException(ResponseCode.USER_LOCKED);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResponseCode.LOGIN_FAILED);
        }

        // 3. 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLoginCount(user.getLoginCount() + 1);
        userMapper.updateById(user);

        // 4. 查询用户角色（一次 JOIN 查询，避免 N+1）
        List<UserVO.RoleInfo> roles = userRoleMapper.selectRolesByUserId(user.getId());

        // 5. 构建响应
        UserVO userVO = UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .userCode(user.getUserCode())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .isEnabled(user.getIsEnabled())
                .isLocked(user.getIsLocked())
                .position(user.getPosition())
                .employeeNo(user.getEmployeeNo())
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .loginCount(user.getLoginCount())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .build();

        return LoginResponse.builder()
                .user(userVO)
                .token(null)  // JWT 阶段添加
                .build();
    }

    private User findUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .or().eq(User::getEmail, username)
                .or().eq(User::getPhone, username)
                .eq(User::getIsDeleted, false);

        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException(ResponseCode.LOGIN_FAILED);
        }
        return user;
    }

    // ==================== 创建用户 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateRequest request) {
        checkUsernameExists(request.getUsername());
        if (StringUtils.hasText(request.getEmail())) {
            checkEmailExists(request.getEmail());
        }
        if (StringUtils.hasText(request.getPhone())) {
            checkPhoneExists(request.getPhone());
        }

        User user = buildUserEntity(request);
        userMapper.insert(user);

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            assignRoles(user.getId(), request.getRoleIds());
        }

        log.info("创建用户成功, userId={}, username={}", user.getId(), user.getUsername());
        return user.getId();
    }

    private void checkUsernameExists(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getIsDeleted, false);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
    }

    private void checkEmailExists(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
                .eq(User::getIsDeleted, false);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("邮箱已被使用");
        }
    }

    private void checkPhoneExists(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone)
                .eq(User::getIsDeleted, false);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("手机号已被使用");
        }
    }

    private User buildUserEntity(UserCreateRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setDepartmentId(request.getDepartmentId());
        user.setPosition(request.getPosition());
        user.setEmployeeNo(request.getEmployeeNo());
        user.setRemark(request.getRemark());
        user.setIsEnabled(true);
        user.setIsLocked(false);
        user.setLoginCount(0);
        user.setIsDeleted(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setPasswordChangedTime(LocalDateTime.now());
        return user;
    }

    private void assignRoles(Long userId, List<Long> roleIds) {
        for (int i = 0; i < roleIds.size(); i++) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleIds.get(i));
            userRole.setIsPrimary(i == 0);
            userRole.setCreatedAt(LocalDateTime.now());
            userRole.setUpdatedAt(LocalDateTime.now());
            userRole.setIsDeleted(false);
            userRoleMapper.insert(userRole);
        }
    }

    // ==================== 更新/删除/查询 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserUpdateRequest request) {
        User user = getUserByIdOrThrow(request.getId());

        if (StringUtils.hasText(request.getRealName())) {
            user.setRealName(request.getRealName());
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getIsEnabled() != null) {
            user.setIsEnabled(request.getIsEnabled());
        }
        if (request.getDepartmentId() != null) {
            user.setDepartmentId(request.getDepartmentId());
        }
        if (StringUtils.hasText(request.getPosition())) {
            user.setPosition(request.getPosition());
        }
        if (StringUtils.hasText(request.getEmployeeNo())) {
            user.setEmployeeNo(request.getEmployeeNo());
        }
        if (StringUtils.hasText(request.getRemark())) {
            user.setRemark(request.getRemark());
        }
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        if (request.getRoleIds() != null) {
            updateUserRoles(request.getId(), request.getRoleIds());
        }

        log.info("更新用户成功, userId={}", request.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        User user = getUserByIdOrThrow(userId);

        user.setUsername(user.getUsername() + "_deleted_" + System.currentTimeMillis() / 1000);
        user.setIsDeleted(true);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId);
        userRoleMapper.delete(wrapper);

        log.info("删除用户成功, userId={}", userId);
    }

    @Override
    public UserVO getUserById(Long userId) {
        User user = getUserByIdOrThrow(userId);
        return convertToVO(user);
    }

    @Override
    public Page<UserVO> pageUsers(UserQueryRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getIsDeleted, false);

        if (StringUtils.hasText(request.getUsername())) {
            wrapper.like(User::getUsername, request.getUsername());
        }
        if (StringUtils.hasText(request.getRealName())) {
            wrapper.like(User::getRealName, request.getRealName());
        }
        if (request.getIsEnabled() != null) {
            wrapper.eq(User::getIsEnabled, request.getIsEnabled());
        }
        if (request.getIsLocked() != null) {
            wrapper.eq(User::getIsLocked, request.getIsLocked());
        }
        if (request.getDepartmentId() != null) {
            wrapper.eq(User::getDepartmentId, request.getDepartmentId());
        }
        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<User> userPage = userMapper.selectPage(page, wrapper);

        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> vos = userPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(vos);

        return voPage;
    }

    private User getUserByIdOrThrow(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted()) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }
        return user;
    }

    private void updateUserRoles(Long userId, List<Long> roleIds) {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId);
        userRoleMapper.delete(wrapper);
        assignRoles(userId, roleIds);
    }

    private UserVO convertToVO(User user) {
        List<UserVO.RoleInfo> roles = userRoleMapper.selectRolesByUserId(user.getId());

        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .userCode(user.getUserCode())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .isEnabled(user.getIsEnabled())
                .isLocked(user.getIsLocked())
                .position(user.getPosition())
                .employeeNo(user.getEmployeeNo())
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .loginCount(user.getLoginCount())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .build();
    }

    // ==================== 以下密码管理方法等权限模块完成后再添加 ====================
    // @Override
    // public void changePassword(Long userId, String oldPassword, String newPassword) {
    //     // TODO: 等权限模块完成，需要 @PreAuthorize 验证用户只能修改自己的密码
    // }
    //
    // @Override
    // public void resetPassword(Long userId, String newPassword) {
    //     // TODO: 等权限模块完成，需要 @PreAuthorize 验证管理员权限
    // }
}
```

---

## 七、Controller 层设计

### 7.1 UserController.java

```java
package com.company.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.common.response.Result;
import com.company.user.dto.*;
import com.company.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理", description = "用户 CRUD、登录相关接口")
@RestController
@RequestMapping("/api/users")
public class UserController { 

    @Resource
    private UserService userService;

    @Operation(summary = "用户登录", description = "支持用户名/邮箱/手机号登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.ok(response);
    }

    @Operation(summary = "创建用户", description = "管理员创建新用户")
    @PostMapping("/create")
    public Result<Long> createUser(@Valid @RequestBody UserCreateRequest request) {
        Long userId = userService.createUser(request);
        return Result.ok(userId);
    }

    @Operation(summary = "更新用户", description = "更新用户信息和角色")
    @PutMapping("/update")
    public Result<Void> updateUser(@Valid @RequestBody UserUpdateRequest request) {
        userService.updateUser(request);
        return Result.ok();
    }

    @Operation(summary = "删除用户", description = "软删除用户")
    @DeleteMapping("/delete/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return Result.ok();
    }

    @Operation(summary = "获取用户详情", description = "根据ID查询用户详细信息")
    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(@PathVariable Long userId) {
        UserVO userVO = userService.getUserById(userId);
        return Result.ok(userVO);
    }

    @Operation(summary = "分页查询用户", description = "支持多条件查询")
    @GetMapping("/list")
    public Result<Page<UserVO>> pageUsers(UserQueryRequest request) {
        Page<UserVO> page = userService.pageUsers(request);
        return Result.ok(page);
    }

    // ==================== 以下接口等权限模块完成后再添加 ====================
    // 密码管理接口需要权限校验，暂时不实现
    // 等完成 Spring Security 配置后，添加 @PreAuthorize 注解再启用
}
```

---

## 八、登录流程图

```
客户端发起登录请求
        ↓
接收 LoginRequest
        ↓
  参数校验 @Valid
        ↓
    [失败] → 返回 400 参数错误
        ↓
    [通过]
        ↓
查询用户 (支持用户名/邮箱/手机号)
        ↓
   [不存在] → 返回 401 登录失败
        ↓
   [存在]
        ↓
验证用户状态
        ↓
  [已禁用] → 返回 402 用户已禁用
  [已锁定] → 返回 403 用户已锁定
        ↓
   [正常]
        ↓
验证密码 (BCrypt.matches)
        ↓
   [失败] → 返回 401 登录失败
        ↓
   [通过]
        ↓
更新登录信息 (时间/次数)
        ↓
查询用户角色列表 (JOIN 查询，一次完成)
        ↓
构建 LoginResponse {user, token}
        ↓
返回 200 登录成功
```

---

## 九、依赖配置

### 9.1 order-platform-user/pom.xml

```xml
<dependencies>
    <!-- 公共模块 -->
    <dependency>
        <groupId>com.company.order</groupId>
        <artifactId>order-platform-common</artifactId>
    </dependency>

    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    </dependency>

    <!-- Spring Security Crypto (BCrypt) -->
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-crypto</artifactId>
    </dependency>

    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

---

## 十、测试数据准备

### 10.1 插入测试用户

```sql
-- 插入测试用户（密码: 123456）
INSERT INTO t_user (
    username, password, user_code, real_name, email, phone,
    is_enabled, is_locked, login_count, is_deleted,
    created_at, updated_at, password_changed_time
) VALUES (
    'admin',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',
    'U001',
    '系统管理员',
    'admin@example.com',
    '13800138000',
    true, false, 0, false,
    NOW(), NOW(), NOW()
);

-- 分配系统管理员角色
INSERT INTO t_user_role (user_id, role_id, is_primary, created_at, updated_at, is_deleted)
VALUES (
    (SELECT id FROM t_user WHERE username = 'admin'),
    (SELECT id FROM t_role WHERE role_code = 'SYSTEM_ADMIN'),
    true, NOW(), NOW(), false
);
```

### 10.2 生成 BCrypt 密码

```java
public class PasswordEncoderTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode("123456");
        System.out.println(encoded);
    }
}
```

---

## 十一、接口测试

### 11.1 登录接口

```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "123456"}'

# 响应（DTO 合并后的结构）
{
  "code": 200,
  "message": "success",
  "data": {
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "roles": [{"roleCode": "SYSTEM_ADMIN", ...}]
    },
    "token": null
  }
}
```

---

## 十二、验收标准

### 功能验收

- [ ] 登录接口支持用户名/邮箱/手机号登录
- [ ] 登录失败返回统一错误码
- [ ] 登录成功返回用户信息（UserVO）和角色列表
- [ ] 登录成功后更新 lastLoginTime 和 loginCount
- [ ] 创建用户时密码自动 BCrypt 加密
- [ ] 创建用户时自动分配角色
- [ ] 更新用户时同步更新角色关联
- [ ] 删除用户为软删除
- [ ] 分页查询支持多条件筛选

### 质量验收

- [ ] DTO 无重复，LoginResponse 复用 UserVO
- [ ] 角色查询使用 JOIN，无 N+1 问题
- [ ] 所有 public 方法有日志记录
- [ ] Service 层使用 @Transactional 确保事务一致性
- [ ] 参数校验使用 @Valid 注解
- [ ] 异常使用 BusinessException 抛出
- [ ] Controller 统一返回 Result<T>
- [ ] 密码管理接口已删除，等权限模块完成后再添加

---

## 十三、本次修复说明（v1.1）

### P0 问题修复

| 问题 | 修复方案 | 状态 |
|------|----------|------|
| DTO 重复 | LoginResponse 改为 {token, UserVO user} | ✅ |
| N+1 查询 | UserRoleMapper 添加 @Select JOIN 查询 | ✅ |
| 安全漏洞 | 删除密码管理接口，等权限模块 | ✅ |

### 代码减少量

- UserServiceImpl：减少 ~60 行（删除密码管理 + 简化登录逻辑）
- UserController：减少 ~20 行（删除密码接口）
- DTO：减少 ~30 行（合并重复字段）

---

## 十四、已知问题 / 技术债务

### P2 - 登录防暴力破解（等权限模块）

**位置**：`UserServiceImpl.login()` 第 434-482 行

**问题**：无登录失败次数限制、无 IP 限制、无速率限制

**影响**：攻击者可以无限次尝试密码

**解决方案（待实施）**：
```java
// 方案：使用 Redis 计数器
String key = "login_fail:" + request.getUsername();
Long count = redisTemplate.opsForValue().increment(key);
if (count != null && count > 5) {
    throw new BusinessException("登录失败次数过多，请15分钟后再试");
}
redisTemplate.expire(key, 15, TimeUnit.MINUTES);
```

**计划**：等 Spring Security 配置时统一处理

---

### P3 - if/else 地狱（能用，暂不处理）

**位置**：`UserServiceImpl.updateUser()` 第 599-639 行

**问题**：7 个 `if (StringUtils.hasText())` 连续判断

**影响**：代码难看，但功能正常

**Linus 评价**："丑，但能跑。等你被这 7 个 if 恶心到的时候，再来重构它。"

---

### P3 - 软删除设计问题（能用，暂不处理）

**位置**：`UserServiceImpl.deleteUser()` 第 646 行

**问题**：修改用户名来释放唯一约束（`username + "_deleted_" + timestamp`）

**更好的方案**：
```sql
-- 唯一索引改为条件索引
CREATE UNIQUE INDEX uk_username ON t_user(username, is_deleted);
```

**影响**：现有数据库已部署，修改索引需要数据迁移

**计划**：暂不处理，等有真实需求时再优化

---

## 十五、下一步

完成本阶段后：

1. **plan_04 JWT**：实现 JWT 工具类，在 LoginResponse 中添加 token
2. **Spring Security**：配置认证链，保护 API 接口
3. **plan_56**：重新配置 OpenApiConfig，添加 JWT 认证文档

---

*文档生成时间：2025-01-17*
