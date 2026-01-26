# 权限管理模块开发教程

> **订单可视化平台 - 权限管理**
> 本教程详细讲解 RBAC 权限模型、Permission/Menu 实体设计、权限校验 AOP 实现、数据权限拦截器。

---

## 目录

1. [RBAC 权限模型设计](#一rbac-权限模型设计)
2. [Permission 实体与数据库设计](#二permission-实体与数据库设计)
3. [Menu 实体与数据库设计](#三menu-实体与数据库设计)
4. [权限校验注解与 AOP 实现](#四权限校验注解与-aop-实现)
5. [数据权限拦截器设计](#五数据权限拦截器设计)
6. [完整代码示例](#六完整代码示例)
7. [API 设计](#七api-设计)

---

## 一、RBAC 权限模型设计

### 1.1 模型概述

RBAC（Role-Based Access Control，基于角色的访问控制）是业界最成熟的权限模型。

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              RBAC 权限模型                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│    ┌─────────┐         ┌─────────┐         ┌─────────┐         ┌─────────┐  │
│    │  User   │────────▶│  Role   │────────▶│Permission│────────▶│ Resource│  │
│    │  用户   │   N:M   │  角色   │   N:M   │  权限   │   N:1   │  资源   │  │
│    └─────────┘         └─────────┘         └─────────┘         └─────────┘  │
│         │                   │                                                         │
│         │                   │                                                         │
│         ▼                   ▼                                                         │
│    ┌─────────┐         ┌─────────┐                                                      │
│    │  Menu   │         │DataScope│                                                      │
│    │  菜单   │         │ 数据权限│                                                      │
│    └─────────┘         └─────────┘                                                      │
│         │                   │                                                         │
│         └───────────────────┘                                                         │
│                                                                             │
│  User ─────▶ Role：用户拥有多个角色                                                │
│  Role ─────▶ Permission：角色拥有多个权限                                           │
│  Role ─────▶ Menu：角色可访问多个菜单                                               │
│  Permission ─▶ Resource：权限对应资源（API、按钮等）                                 │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.2 权限粒度层次

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              权限粒度层次                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  模块      ──▶  订单管理、用户管理、系统设置                                    │
│    │                                                                        │
│    ▼                                                                        │
│  页面      ──▶  订单列表、订单详情、用户列表                                    │
│    │                                                                        │
│    ▼                                                                        │
│  操作      ──▶  查看、新增、编辑、删除、导出                                    │
│    │                                                                        │
│    ▼                                                                        │
│  API       ──▶  GET /api/orders、POST /api/orders                            │
│                                                                             │
│  示例：order:view、order:create、order:update、order:delete                  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.3 数据权限类型

| 类型 | 值 | 说明 | 适用角色 |
|------|-----|------|----------|
| ALL | 1 | 全部数据 | 系统管理员、数据管理员 |
| DEPARTMENT | 2 | 本部门及下级部门数据 | 部门经理 |
| DEPT_ONLY | 3 | 仅本部门数据 | 部门主管 |
| SELF | 4 | 仅本人数据 | 客户经理、采购专员、运营专员 |
| CUSTOM | 5 | 自定义范围 | 预留扩展 |

**权限计算规则**：用户拥有多个角色时，取「最宽松」的数据权限（数值最小）。

```
用户数据权限 = MIN(所有角色的 dataScopeType)

示例：
  角色1: dataScopeType = 3 (仅本部门)
  角色2: dataScopeType = 1 (全部数据)
  结果: dataScopeType = 1 (全部数据)
```

### 1.4 Menu vs Permission 设计说明

> **关键设计决策**：Menu（菜单）和 Permission（权限）是两个正交的概念，不应混淆。

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        Menu vs Permission 对比                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────┐         ┌─────────────────────────┐           │
│  │        Menu             │         │      Permission          │           │
│  │     （前端导航）         │         │     （后端权限）          │           │
│  ├─────────────────────────┤         ├─────────────────────────┤           │
│  │ 关注点：UI 显示          │         │ 关注点：API 访问控制       │           │
│  │ - 菜单树结构             │         │ - 接口访问权限            │           │
│  │ - 前端路由               │         │ - 数据操作权限            │           │
│  │ - 图标、排序、可见性      │         │ - 业务操作权限            │           │
│  ├─────────────────────────┤         ├─────────────────────────┤           │
│  │ 字段：                   │         │ 字段：                   │           │
│  │ - path, component       │         │ - resource, method       │           │
│  │ - icon, visible         │         │ - action, permission_code │           │
│  ├─────────────────────────┤         ├─────────────────────────┤           │
│  │ 使用场景：               │         │ 使用场景：               │           │
│  │ - 前端动态渲染菜单        │         │ - 后端接口鉴权            │           │
│  │ - 用户导航栏显示          │         │ - 按钮权限控制            │           │
│  └─────────────────────────┘         └─────────────────────────┘           │
│                                                                             │
│  关键洞察：                                                                  │
│  - Menu 是"展示层"概念，决定用户看到什么                                      │
│  - Permission 是"安全层"概念，决定用户能做什么                                │
│  - 两者可以独立配置：某个菜单可见，但其中的操作可能无权                       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**为什么分离？**

1. **职责单一**：Menu 管导航，Permission 管安全，各司其职
2. **前后端解耦**：前端可以独立开发菜单系统，不依赖后端权限设计
3. **灵活组合**：同一个 API 可以被多个菜单调用，无需重复定义

**反面案例**（避免）：

```text
❌ 错误：用一个 Permission 表同时存储 menu/button/api
   - 导致 menu 和 button 的属性混在一起
   - 前端和后端的关注点耦合
   - 扩展时需要修改 permission_type 枚举

✅ 正确：Menu 和 Permission 各自独立，通过角色关联
   - Menu 管前端，Permission 管后端
   - 通过 Role Menu 和 Role Permission 分别关联
   - 清晰的职责边界
```

---

## 二、Permission 实体与数据库设计

### 2.1 数据库表结构

```sql
-- ============================================================
-- 权限表 (t_permission)
-- 说明: 系统权限表，定义权限点（菜单、按钮、API等）
-- 关系: Permission N:1 Resource (一个权限对应一个资源)
--       Permission N:M Role (通过 t_role_permission 关联)
-- ============================================================

CREATE TABLE `t_permission` (
  -- ========== 主键 ==========
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',

  -- ========== 基本信息 ==========
  `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码，如：order:view',
  `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称，如：查看订单',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父权限ID，0表示根权限',
  `permission_type` VARCHAR(20) NOT NULL COMMENT '权限类型：menu/button/api',

  -- ========== 资源关联 ==========
  `resource_path` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '资源路径，API路径或前端路由',
  `resource_method` VARCHAR(10) NOT NULL DEFAULT '' COMMENT 'HTTP方法：GET/POST/PUT/DELETE',

  -- ========== 排序 ==========
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号，数值越小越靠前',

  -- ========== 公共字段 ==========
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `created_by` BIGINT NULL DEFAULT NULL COMMENT '创建人ID',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `updated_by` BIGINT NULL DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`) COMMENT '权限编码唯一索引',
  KEY `idx_parent_id` (`parent_id`) COMMENT '父权限索引',
  KEY `idx_permission_type` (`permission_type`) COMMENT '权限类型索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='权限表:系统权限表，定义权限点（菜单、按钮、API等）。';

-- ============================================================
-- 角色权限关联表 (t_role_permission)
-- ============================================================

CREATE TABLE `t_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `created_by` BIGINT NULL DEFAULT NULL COMMENT '创建人ID',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`) COMMENT '角色权限唯一索引',
  KEY `idx_permission_id` (`permission_id`) COMMENT '权限ID索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='角色权限关联表';
```

### 2.2 权限数据示例

```sql
-- ==================== 订单模块权限 ====================
INSERT INTO t_permission (permission_code, permission_name, parent_id, permission_type, resource_path, resource_method, sort_order) VALUES
-- 订单管理（菜单）
('order', '订单管理', 0, 'menu', '/order', '', 1),
-- 订单列表（菜单）
('order:list', '订单列表', 1, 'menu', '/order/list', '', 1),
-- 订单详情（菜单）
('order:detail', '订单详情', 1, 'menu', '/order/detail', '', 2),
-- 订单操作（按钮/API）
('order:view', '查看订单', 2, 'button', '/api/order/{id}', 'GET', 1),
('order:create', '创建订单', 2, 'api', '/api/order', 'POST', 2),
('order:update', '编辑订单', 2, 'api', '/api/order/{id}', 'PUT', 3),
('order:delete', '删除订单', 2, 'api', '/api/order/{id}', 'DELETE', 4),
('order:export', '导出订单', 2, 'button', '/api/order/export', 'POST', 5);

-- ==================== 用户模块权限 ====================
INSERT INTO t_permission (permission_code, permission_name, parent_id, permission_type, resource_path, resource_method, sort_order) VALUES
-- 用户管理（菜单）
('user', '用户管理', 0, 'menu', '/user', '', 2),
-- 用户列表（菜单）
('user:list', '用户列表', 6, 'menu', '/user/list', '', 1),
-- 用户操作（按钮/API）
('user:view', '查看用户', 7, 'api', '/api/user/{id}', 'GET', 1),
('user:create', '创建用户', 7, 'api', '/api/user', 'POST', 2),
('user:update', '编辑用户', 7, 'api', '/api/user', 'PUT', 3),
('user:delete', '删除用户', 7, 'api', '/api/user/{id}', 'DELETE', 4),
('user:reset-password', '重置密码', 7, 'api', '/api/user/{id}/reset-password', 'PUT', 5);
```

### 2.3 Permission 实体

```java
package com.company.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 权限实体
 * <p>
 * 系统权限表，定义权限点（菜单、按钮、API等）
 * 关系: Permission N:M Role (通过 t_role_permission 中间表关联)
 *

 */
@Data
@TableName("t_permission")
public class Permission {

    @TableId(type = IdType.AUTO)
    private Long id;

    // ==================== 基本信息 ====================

    /**
     * 权限编码，如：order:view、user:create
     */
    private String permissionCode;

    /**
     * 权限名称，如：查看订单、创建用户
     */
    private String permissionName;

    /**
     * 父权限ID，0表示根权限（用于构建权限树）
     */
    private Long parentId;

    /**
     * 权限类型：menu（菜单）/ button（按钮）/ api（接口）
     */
    private String permissionType;

    // ==================== 资源关联 ====================

    /**
     * 资源路径，API路径或前端路由
     * 如：/api/order/{id}、/order/list
     */
    private String resourcePath;

    /**
     * HTTP方法：GET/POST/PUT/DELETE
     */
    private String resourceMethod;

    // ==================== 排序 ====================

    /**
     * 排序号，数值越小越靠前
     */
    private Integer sortOrder;

    // ==================== 公共字段 ====================

    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
    private Boolean isDeleted;
}
```

---

## 三、Menu 实体与数据库设计

### 3.1 数据库表结构

```sql
-- ============================================================
-- 菜单表 (t_menu)
-- 说明: 系统菜单表，定义前端菜单结构
-- 关系: Menu N:M Role (通过 t_role_menu 关联)
-- ============================================================

CREATE TABLE `t_menu` (
  -- ========== 主键 ==========
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',

  -- ========== 基本信息 ==========
  `menu_name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
  `menu_code` VARCHAR(100) NOT NULL COMMENT '菜单编码，如：order:list',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID，0表示根菜单',

  -- ========== 菜单类型 ==========
  `menu_type` CHAR(1) NOT NULL COMMENT '菜单类型：D=目录/C=菜单/F=按钮',
  `path` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '前端路由路径',
  `component` VARCHAR(200) NOT NULL DEFAULT '' COMMENT 'Vue组件路径',
  `icon` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '菜单图标',

  -- ========== 显示控制 ==========
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见（0=隐藏，1=显示）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0=禁用，1=启用）',

  -- ========== 权限关联 ==========
  `permission_id` BIGINT NULL DEFAULT NULL COMMENT '关联权限ID',

  -- ========== 公共字段 ==========
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `created_by` BIGINT NULL DEFAULT NULL COMMENT '创建人ID',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `updated_by` BIGINT NULL DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu_code` (`menu_code`) COMMENT '菜单编码唯一索引',
  KEY `idx_parent_id` (`parent_id`) COMMENT '父菜单索引',
  KEY `idx_menu_type` (`menu_type`) COMMENT '菜单类型索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='菜单表:系统菜单表，定义前端菜单结构。';

-- ============================================================
-- 角色菜单关联表 (t_role_menu)
-- ============================================================

CREATE TABLE `t_role_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `created_by` BIGINT NULL DEFAULT NULL COMMENT '创建人ID',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`) COMMENT '角色菜单唯一索引',
  KEY `idx_menu_id` (`menu_id`) COMMENT '菜单ID索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='角色菜单关联表';
```

### 3.2 Menu 实体

```java
package com.company.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;

/**
 * 菜单实体
 * <p>
 * 系统菜单表，定义前端菜单结构
 * 关系: Menu N:M Role (通过 t_role_menu 中间表关联)
 *

 */
@Data
@TableName("t_menu")
public class Menu {

    @TableId(type = IdType.AUTO)
    private Long id;

    // ==================== 基本信息 ====================

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单编码，如：order:list
     */
    private String menuCode;

    /**
     * 父菜单ID，0表示根菜单
     */
    private Long parentId;

    // ==================== 菜单类型 ====================

    /**
     * 菜单类型：D=目录，C=菜单，F=按钮
     */
    private String menuType;

    /**
     * 前端路由路径
     */
    private String path;

    /**
     * Vue组件路径
     */
    private String component;

    /**
     * 菜单图标
     */
    private String icon;

    // ==================== 显示控制 ====================

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 是否可见
     */
    private Boolean visible;

    /**
     * 状态
     */
    private Boolean status;

    // ==================== 权限关联 ====================

    /**
     * 关联权限ID
     */
    private Long permissionId;

    // ==================== 公共字段 ====================

    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
    private Boolean isDeleted;

    // ==================== 非持久化字段 ====================

    /**
     * 子菜单列表（用于构建树形结构，不持久化）
     *
     * 注意：不在此处直接初始化 TreeSet
     * - 使用懒加载 getter 方法，避免反序列化问题
     * - 树形结构的构建应在 VO 层或专门的 Builder 中完成
     */
    private Set<Menu> children;

    /**
     * 获取子菜单列表（懒加载）
     */
    public Set<Menu> getChildren() {
        if (children == null) {
            children = new TreeSet<>(
                (a, b) -> {
                    int sortCompare = Integer.compare(
                        a.getSortOrder() != null ? a.getSortOrder() : 0,
                        b.getSortOrder() != null ? b.getSortOrder() : 0
                    );
                    return sortCompare != 0 ? sortCompare : Long.compare(a.getId(), b.getId());
                }
            );
        }
        return children;
    }
}
```

---

## 四、权限校验注解与 AOP 实现

### 4.1 自定义注解

```java
package com.company.common.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * <p>
 * 用于标记需要权限校验的接口，只有拥有指定权限的用户才能访问
 *

 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 需要的权限编码
     * 支持单个权限或多个权限（多个权限之间的关系为 OR）
     * 示例：value = "order:view"
     * 示例：value = {"order:view", "order:edit"}
     */
    String[] value() default {};

    /**
     * 权限逻辑关系：AND（需要全部权限）/ OR（拥有任一权限即可）
     */
    Logical logical() default Logical.OR;

    /**
     * 权限逻辑枚举
     */
    enum Logical {
        AND,   // 需要全部权限
        OR     // 拥有任一权限即可
    }
}
```

### 4.2 权限校验 AOP 切面

```java
package com.company.common.aspect;

import com.company.common.annotation.RequirePermission;
import com.company.common.exception.ForbiddenException;
import com.company.common.holder.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * 权限校验切面
 * <p>
 * 拦截带有 @RequirePermission 注解的方法，校验用户权限
 *

 */
@Aspect
@Component
@Order(2)  // 在 JWT 认证之后执行
@Slf4j
public class PermissionCheckAspect {

    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        // ==================== 1. 获取当前用户 ====================

        CurrentUser currentUser = UserHolder.get();
        if (currentUser == null) {
            throw new ForbiddenException("用户未登录");
        }

        // ==================== 2. 获取需要的权限 ====================

        String[] requiredPermissions = requirePermission.value();
        if (requiredPermissions.length == 0) {
            // 没有指定权限，只校验登录状态
            return;
        }

        // ==================== 3. 获取用户权限 ====================

        Set<String> userPermissions = currentUser.getPermissions();
        if (userPermissions == null || userPermissions.isEmpty()) {
            throw new ForbiddenException("无权限访问");
        }

        // ==================== 4. 权限校验 ====================

        boolean hasPermission = checkPermission(userPermissions, requiredPermissions, requirePermission.logical());

        if (!hasPermission) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            log.warn("权限校验失败, userId={}, method={}, requiredPermissions={}",
                     currentUser.getUserId(), method.getName(), requiredPermissions);
            throw new ForbiddenException("无权限访问");
        }

        log.debug("权限校验通过, userId={}, method={}", currentUser.getUserId(), ((MethodSignature) joinPoint.getSignature()).getMethod().getName());
    }

    /**
     * 权限校验逻辑
     *
     * 使用 Java Stream API 简化实现
     */
    private boolean checkPermission(Set<String> userPermissions,
                                    String[] requiredPermissions,
                                    RequirePermission.Logical logical) {
        if (logical == RequirePermission.Logical.AND) {
            // AND：需要拥有全部权限
            return userPermissions.containsAll(Arrays.asList(requiredPermissions));
        } else {
            // OR：拥有任一权限即可
            return Arrays.stream(requiredPermissions)
                .anyMatch(userPermissions::contains);
        }
    }
}
```

### 4.3 Controller 使用示例

```java
@RestController
@RequestMapping("/api/order")
@Tag(name = "订单管理")
public class OrderController {

    @Resource
    private OrderService orderService;

    // 单个权限校验
    @GetMapping("/{id}")
    @RequirePermission("order:view")
    public Result<OrderVO> getOrder(@PathVariable Long id) {
        return Result.ok(orderService.getById(id));
    }

    // 多个权限校验（OR）
    @PostMapping
    @RequirePermission(value = {"order:create", "order:import"}, logical = Logical.OR)
    public Result<Long> createOrder(@RequestBody OrderCreateRequest request) {
        return Result.ok(orderService.create(request));
    }

    // 多个权限校验（AND）
    @DeleteMapping("/{id}")
    @RequirePermission(value = {"order:delete", "order:audit"}, logical = Logical.AND)
    public Result<Void> deleteOrder(@PathVariable Long id) {
        orderService.delete(id);
        return Result.ok();
    }
}
```

---

## 五、数据权限拦截器设计

### 5.1 数据权限枚举

```java
package com.company.user.enums;

import lombok.Getter;

/**
 * 数据权限类型枚举
 *

 */
@Getter
public enum DataScopeType {

    ALL(1, "全部数据"),
    DEPARTMENT(2, "本部门及下级部门数据"),
    DEPT_ONLY(3, "仅本部门数据"),
    SELF(4, "仅本人数据"),
    CUSTOM(5, "自定义范围");

    private final Integer code;
    private final String desc;

    DataScopeType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DataScopeType fromCode(Integer code) {
        for (DataScopeType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return SELF;  // 默认最严格权限
    }
}
```

### 5.2 数据权限拦截器

```java
package com.company.common.interceptor;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.company.common.holder.UserHolder;
import com.company.user.entity.CurrentUser;
import com.company.user.enums.DataScopeType;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据权限拦截器
 * <p>
 * 基于 MyBatis-Plus 拦截器，自动在 SQL 中添加数据权限条件
 *

 */
@Component
public class DataScopeInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                           RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        // ==================== 1. 获取当前用户 ====================

        CurrentUser currentUser = UserHolder.get();
        if (currentUser == null) {
            return;  // 无用户信息，不添加数据权限条件
        }

        // ==================== 2. 获取用户数据权限类型 ====================

        DataScopeType dataScopeType = DataScopeType.fromCode(currentUser.getDataScopeType());

        // ==================== 3. 构建数据权限 SQL 条件 ====================

        String dataScopeSql = buildDataScopeSql(currentUser, dataScopeType);

        if (dataScopeSql != null && !dataScopeSql.isEmpty()) {
            // 使用 MyBatis-Plus 的机制注入 SQL 条件
            // 这里简化处理，实际需要更复杂的 SQL 解析和注入
            // 可以使用 MyBatis-Plus 的 TenantLineInnerInterceptor 作为参考
        }
    }

    /**
     * 构建数据权限 SQL 条件
     */
    private String buildDataScopeSql(CurrentUser user, DataScopeType dataScopeType) {
        return switch (dataScopeType) {
            case ALL -> "1=1";  // 全部数据，不添加条件
            case DEPARTMENT -> buildDepartmentSql(user.getDepartmentId(), true);
            case DEPT_ONLY -> buildDepartmentSql(user.getDepartmentId(), false);
            case SELF -> "created_by = " + user.getUserId();
            case CUSTOM -> buildCustomSql(user);
        };
    }

    /**
     * 构建部门数据权限 SQL
     */
    private String buildDepartmentSql(Long departmentId, boolean includeChildren) {
        if (departmentId == null) {
            return "1=0";  // 无部门，无数据权限
        }

        if (includeChildren) {
            // 本部门及下级部门
            List<Long> deptIds = getDepartmentWithChildren(departmentId);
            return "department_id IN (" + deptIds + ")";
        } else {
            // 仅本部门
            return "department_id = " + departmentId;
        }
    }

    /**
     * 获取部门及其下级部门ID列表
     *
     * 注意：这是高级功能，需要完整的部门管理模块支持
     * - 需要部门表（t_department）和部门树结构
     * - 需要部门服务（DepartmentService）提供树查询能力
     *
     * 本教程专注于权限核心流程，此方法返回简化实现
     * 完整实现请参考部门管理模块文档
     */
    private List<Long> getDepartmentWithChildren(Long departmentId) {
        // 简化实现：仅返回当前部门
        // 生产环境需要递归查询所有子部门
        List<Long> deptIds = new ArrayList<>();
        deptIds.add(departmentId);
        return deptIds;
    }

    /**
     * 构建自定义数据权限 SQL
     *
     * 注意：这是高级功能，需要额外的数据权限表支持
     * - 需要自定义数据范围表（t_user_data_scope）
     * - 需要为用户/角色配置可访问的数据范围
     *
     * 本教程专注于权限核心流程，此方法返回拒绝访问
     * 完整实现请参考数据权限管理模块文档
     */
    private String buildCustomSql(CurrentUser user) {
        // 简化实现：拒绝访问
        // 生产环境需要查询用户自定义的数据范围配置
        return "1=0";
    }
}
```

---

## 六、完整代码示例

### 6.1 PermissionService

```java
package com.company.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.user.dto.PermissionAssignRequest;
import com.company.user.entity.Permission;
import com.company.user.entity.Role;
import com.company.user.vo.PermissionVO;

import java.util.List;
import java.util.Set;

public interface PermissionService {

    // ==================== 权限 CRUD ====================

    Long createPermission(Permission permission, Long operatorId);
    void updatePermission(Permission permission, Long operatorId);
    void deletePermission(Long permissionId, Long operatorId);

    // ==================== 查询相关 ====================

    Permission getPermissionById(Long permissionId);
    List<PermissionVO> getPermissionTree();
    Page<PermissionVO> pagePermissions(Page<Permission> page);

    // ==================== 角色权限关联 ====================

    void assignPermissionsToRole(Long roleId, List<Long> permissionIds, Long operatorId);
    Set<String> getPermissionsByUserId(Long userId);
    List<PermissionVO> getPermissionsByRoleId(Long roleId);
}
```

### 6.2 MenuService

```java
package com.company.user.service;

import com.company.user.entity.Menu;
import com.company.user.vo.MenuVO;

import java.util.List;

public interface MenuService {

    // ==================== 菜单 CRUD ====================

    Long createMenu(Menu menu, Long operatorId);
    void updateMenu(Menu menu, Long operatorId);
    void deleteMenu(Long menuId, Long operatorId);

    // ==================== 查询相关 ====================

    Menu getMenuById(Long menuId);
    List<MenuVO> getMenuTree();

    // ==================== 用户菜单 ====================

    List<MenuVO> getUserMenus(Long userId);

    // ==================== 角色菜单关联 ====================

    void assignMenusToRole(Long roleId, List<Long> menuIds, Long operatorId);
}
```

### 6.3 菜单树构建工具

```java
package com.company.user.util;

import com.company.user.entity.Menu;
import com.company.user.vo.MenuVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单树构建工具
 *

 */
public class MenuTreeBuilder {

    /**
     * 构建菜单树
     */
    public static List<MenuVO> buildTree(List<Menu> menus) {
        Map<Long, MenuVO> map = new HashMap<>();
        List<MenuVO> roots = new ArrayList<>();

        // 第一遍：创建所有节点
        for (Menu menu : menus) {
            MenuVO node = toVO(menu);
            map.put(menu.getId(), node);

            if (menu.getParentId() == 0 || menu.getParentId() == null) {
                roots.add(node);
            }
        }

        // 第二遍：建立父子关系
        for (Menu menu : menus) {
            if (menu.getParentId() != null && menu.getParentId() != 0) {
                MenuVO parent = map.get(menu.getParentId());
                MenuVO child = map.get(menu.getId());
                if (parent != null && child != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(child);
                }
            }
        }

        return roots;
    }

    private static MenuVO toVO(Menu menu) {
        return MenuVO.builder()
                .id(menu.getId())
                .menuName(menu.getMenuName())
                .menuCode(menu.getMenuCode())
                .parentId(menu.getParentId())
                .menuType(menu.getMenuType())
                .path(menu.getPath())
                .component(menu.getComponent())
                .icon(menu.getIcon())
                .sortOrder(menu.getSortOrder())
                .visible(menu.getVisible())
                .status(menu.getStatus())
                .permissionId(menu.getPermissionId())
                .build();
    }
}
```

---

## 七、API 设计

### 7.1 权限管理 API

| 操作 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 查询权限列表 | GET | /api/permissions | 分页查询 |
| 查询权限树 | GET | /api/permissions/tree | 树形结构 |
| 创建权限 | POST | /api/permissions | 创建权限 |
| 更新权限 | PUT | /api/permissions | 更新权限 |
| 删除权限 | DELETE | /api/permissions/{id} | 删除权限 |
| 分配角色权限 | POST | /api/role/{roleId}/permissions | 分配权限 |
| 获取角色权限 | GET | /api/role/{roleId}/permissions | 查询角色权限 |

### 7.2 菜单管理 API

| 操作 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 查询菜单树 | GET | /api/menus/tree | 全部菜单树 |
| 获取用户菜单 | GET | /api/menus/user | 当前用户菜单 |
| 创建菜单 | POST | /api/menus | 创建菜单 |
| 更新菜单 | PUT | /api/menus | 更新菜单 |
| 删除菜单 | DELETE | /api/menus/{id} | 删除菜单 |
| 分配角色菜单 | POST | /api/role/{roleId}/menus | 分配菜单 |

### 7.3 PermissionController

```java
@RestController
@RequestMapping("/api/permissions")
@Tag(name = "权限管理")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "查询权限列表")
    @GetMapping
    public Result<Page<PermissionVO>> pagePermissions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<Permission> pageParam = new Page<>(page, size);
        return Result.ok(permissionService.pagePermissions(pageParam));
    }

    @Operation(summary = "查询权限树")
    @GetMapping("/tree")
    public Result<List<PermissionVO>> getPermissionTree() {
        return Result.ok(permissionService.getPermissionTree());
    }

    @Operation(summary = "创建权限")
    @PostMapping
    public Result<Long> createPermission(@RequestBody Permission permission) {
        return Result.ok(permissionService.createPermission(permission, getCurrentUserId()));
    }

    @Operation(summary = "分配角色权限")
    @PostMapping("/role/{roleId}/assign")
    public Result<Void> assignPermissions(
            @PathVariable Long roleId,
            @RequestBody List<Long> permissionIds) {
        permissionService.assignPermissionsToRole(roleId, permissionIds, getCurrentUserId());
        return Result.ok();
    }
}
```

---

## 总结

| 组件 | 职责 |
|------|------|
| **Permission** | 权限点定义（menu/button/api） |
| **Menu** | 前端菜单结构 |
| **@RequirePermission** | 权限校验注解 |
| **PermissionCheckAspect** | AOP 权限校验 |
| **DataScopeInterceptor** | 数据权限 SQL 注入 |

---

*文档版本: v1.0*
*创建日期: 2026-01-20*
*维护者: 订单平台团队*
