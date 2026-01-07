-- ============================================================
-- 角色权限关联表 (t_role_permission)
-- 说明: 角色权限关联中间表,实现角色与权限的N:M关联
-- 关系: RolePermission N:1 Role (关联角色)
-- 设计要点: 不使用t_permission表,权限代码直接存储,权限是代码层面的硬编码
-- ============================================================

CREATE TABLE `t_role_permission` (
  -- ========== 主键 ==========
  -- 关联ID,主键,自增
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',

  -- ========== 关联角色信息 ==========
  -- 关系: RolePermission N:1 Role (关联角色)
  -- 角色ID,外键关联t_role表
  `role_id` BIGINT NOT NULL COMMENT '角色ID',

  -- 角色代码(冗余字段,便于查询和展示)
  `role_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '角色代码(冗余字段)',

  -- ========== 权限信息 ==========
  -- 权限代码,格式: {模块}:{操作}
  -- 示例: ORDER:VIEW(订单查看)、ORDER:CREATE(订单创建)、SHIPMENT:UPDATE(发运更新)
  -- 注意: 不使用t_permission表,权限是代码层面的硬编码,不需要动态管理
  `permission_code` VARCHAR(100) NOT NULL COMMENT '权限代码',

  -- ========== 公共字段 ==========
  -- 创建时间(精确到毫秒)
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

  -- 创建人ID,记录关联关系的创建人
  -- 默认-1表示系统创建,避免使用NULL防止空指针问题和简化查询逻辑
  `created_by` BIGINT NOT NULL DEFAULT -1 COMMENT '创建人ID:默认-1表示系统创建',

  -- 更新时间(精确到毫秒)
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

  -- 更新人ID,记录关联关系的最后更新人
  -- 默认-1表示系统更新,避免使用NULL防止空指针问题和简化查询逻辑
  `updated_by` BIGINT NOT NULL DEFAULT -1 COMMENT '更新人ID:默认-1表示系统更新',

  -- 是否删除: 0-未删除, 1-已删除
  -- 默认0表示未删除,避免使用NULL防止索引失效和简化查询逻辑
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除,1-已删除',

  PRIMARY KEY (`id`),

  -- ========== 唯一约束 ==========
  -- 角色ID+权限代码唯一约束,保证同一角色不会重复关联同一权限
  -- 注意: 考虑软删除,避免同一角色重复关联同一权限
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_code`, `is_deleted`) COMMENT '角色权限关联唯一约束',

  -- ========== 索引设计 ==========
  -- 索引设计原则: 只保留核心查询场景的索引,避免过度索引导致写入性能下降
  -- InnoDB联合索引遵循最左前缀原则,单字段索引被联合索引覆盖时需删除以避免冗余

  -- 角色ID+软删除联合索引,用于查询角色的所有权限(高频场景: 角色权限验证)
  -- 覆盖场景: 按角色ID查询权限、按角色ID+删除标记查询权限
  KEY `idx_role_permission_role` (`role_id`, `is_deleted`) COMMENT '角色ID索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='角色权限关联表: 角色权限关联中间表,实现角色与权限的N:M关联。'
  '【权限设计】不使用t_permission表,权限代码直接存储,权限是代码层面的硬编码。'
  '【权限代码格式】{模块}:{操作},如ORDER:VIEW、SHIPMENT:CREATE。'
  '【关联关系】每个关联记录关联一个角色和一个权限。一个角色可以有多个权限,一个权限可以分配给多个角色。';

-- ============================================================
-- 角色权限初始化数据
-- 说明: 根据甲方需求文档定义的5个角色的权限分配
-- ============================================================

-- ========== 系统管理员权限 ==========
-- 系统管理员拥有所有权限

-- 用户管理权限
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'USER:*', -1, -1 FROM t_role WHERE role_code = 'SYSTEM_ADMIN' AND is_deleted = 0;

-- 角色管理权限
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'ROLE:*', -1, -1 FROM t_role WHERE role_code = 'SYSTEM_ADMIN' AND is_deleted = 0;

-- 订单管理权限
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'ORDER:*', -1, -1 FROM t_role WHERE role_code = 'SYSTEM_ADMIN' AND is_deleted = 0;

-- 合作方管理权限
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'PARTNER:*', -1, -1 FROM t_role WHERE role_code = 'SYSTEM_ADMIN' AND is_deleted = 0;

-- 发运管理权限
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'SHIPMENT:*', -1, -1 FROM t_role WHERE role_code = 'SYSTEM_ADMIN' AND is_deleted = 0;

-- 附件管理权限
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'ATTACHMENT:*', -1, -1 FROM t_role WHERE role_code = 'SYSTEM_ADMIN' AND is_deleted = 0;

-- 异常管理权限
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'EXCEPTION:*', -1, -1 FROM t_role WHERE role_code = 'SYSTEM_ADMIN' AND is_deleted = 0;

-- 看板管理权限
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'DASHBOARD:*', -1, -1 FROM t_role WHERE role_code = 'SYSTEM_ADMIN' AND is_deleted = 0;

-- 数据管理权限
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'DATA:*', -1, -1 FROM t_role WHERE role_code = 'SYSTEM_ADMIN' AND is_deleted = 0;

-- ========== 数据管理员权限 ==========
-- 数据管理员: 查看和导出权限

-- 订单查看
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'ORDER:VIEW', -1, -1 FROM t_role WHERE role_code = 'DATA_ADMIN' AND is_deleted = 0;

-- 合作方查看
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'PARTNER:VIEW', -1, -1 FROM t_role WHERE role_code = 'DATA_ADMIN' AND is_deleted = 0;

-- 发运查看
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'SHIPMENT:VIEW', -1, -1 FROM t_role WHERE role_code = 'DATA_ADMIN' AND is_deleted = 0;

-- 附件查看
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'ATTACHMENT:VIEW', -1, -1 FROM t_role WHERE role_code = 'DATA_ADMIN' AND is_deleted = 0;

-- 附件下载
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'ATTACHMENT:DOWNLOAD', -1, -1 FROM t_role WHERE role_code = 'DATA_ADMIN' AND is_deleted = 0;

-- 异常查看
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'EXCEPTION:VIEW', -1, -1 FROM t_role WHERE role_code = 'DATA_ADMIN' AND is_deleted = 0;

-- 看板查看
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'DASHBOARD:VIEW', -1, -1 FROM t_role WHERE role_code = 'DATA_ADMIN' AND is_deleted = 0;

-- 数据导出
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'DATA:EXPORT', -1, -1 FROM t_role WHERE role_code = 'DATA_ADMIN' AND is_deleted = 0;

-- ========== 客户经理权限 ==========
-- 客户经理: 订单管理权限

-- 订单查看
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'ORDER:VIEW', -1, -1 FROM t_role WHERE role_code = 'CUSTOMER_MANAGER' AND is_deleted = 0;

-- 订单创建
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'ORDER:CREATE', -1, -1 FROM t_role WHERE role_code = 'CUSTOMER_MANAGER' AND is_deleted = 0;

-- 订单更新
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'ORDER:UPDATE', -1, -1 FROM t_role WHERE role_code = 'CUSTOMER_MANAGER' AND is_deleted = 0;

-- 合作方查看
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'PARTNER:VIEW', -1, -1 FROM t_role WHERE role_code = 'CUSTOMER_MANAGER' AND is_deleted = 0;

-- 发运查看
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'SHIPMENT:VIEW', -1, -1 FROM t_role WHERE role_code = 'CUSTOMER_MANAGER' AND is_deleted = 0;

-- 附件查看
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'ATTACHMENT:VIEW', -1, -1 FROM t_role WHERE role_code = 'CUSTOMER_MANAGER' AND is_deleted = 0;

-- 附件上传
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'ATTACHMENT:UPLOAD', -1, -1 FROM t_role WHERE role_code = 'CUSTOMER_MANAGER' AND is_deleted = 0;

-- 附件下载
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'ATTACHMENT:DOWNLOAD', -1, -1 FROM t_role WHERE role_code = 'CUSTOMER_MANAGER' AND is_deleted = 0;

-- 异常查看
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'EXCEPTION:VIEW', -1, -1 FROM t_role WHERE role_code = 'CUSTOMER_MANAGER' AND is_deleted = 0;

-- ========== 采购专员权限 ==========
-- 采购专员: 合作方管理权限

-- 订单查看
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'ORDER:VIEW', -1, -1 FROM t_role WHERE role_code = 'PURCHASE_SPECIALIST' AND is_deleted = 0;

-- 合作方查看
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'PARTNER:VIEW', -1, -1 FROM t_role WHERE role_code = 'PURCHASE_SPECIALIST' AND is_deleted = 0;

-- 合作方创建
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'PARTNER:CREATE', -1, -1 FROM t_role WHERE role_code = 'PURCHASE_SPECIALIST' AND is_deleted = 0;

-- 合作方审核
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'PARTNER:AUDIT', -1, -1 FROM t_role WHERE role_code = 'PURCHASE_SPECIALIST' AND is_deleted = 0;

-- 发运查看
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'SHIPMENT:VIEW', -1, -1 FROM t_role WHERE role_code = 'PURCHASE_SPECIALIST' AND is_deleted = 0;

-- 附件查看
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'ATTACHMENT:VIEW', -1, -1 FROM t_role WHERE role_code = 'PURCHASE_SPECIALIST' AND is_deleted = 0;

-- 附件上传
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'ATTACHMENT:UPLOAD', -1, -1 FROM t_role WHERE role_code = 'PURCHASE_SPECIALIST' AND is_deleted = 0;

-- 附件下载
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'ATTACHMENT:DOWNLOAD', -1, -1 FROM t_role WHERE role_code = 'PURCHASE_SPECIALIST' AND is_deleted = 0;

-- ========== 运营专员权限 ==========
-- 运营专员: 发运管理权限

-- 订单查看
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'ORDER:VIEW', -1, -1 FROM t_role WHERE role_code = 'OPERATION_SPECIALIST' AND is_deleted = 0;

-- 合作方查看
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'PARTNER:VIEW', -1, -1 FROM t_role WHERE role_code = 'OPERATION_SPECIALIST' AND is_deleted = 0;

-- 发运查看
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'SHIPMENT:VIEW', -1, -1 FROM t_role WHERE role_code = 'OPERATION_SPECIALIST' AND is_deleted = 0;

-- 发运创建
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'SHIPMENT:CREATE', -1, -1 FROM t_role WHERE role_code = 'OPERATION_SPECIALIST' AND is_deleted = 0;

-- 发运更新
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'SHIPMENT:UPDATE', -1, -1 FROM t_role WHERE role_code = 'OPERATION_SPECIALIST' AND is_deleted = 0;

-- 附件查看
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'ATTACHMENT:VIEW', -1, -1 FROM t_role WHERE role_code = 'OPERATION_SPECIALIST' AND is_deleted = 0;

-- 附件上传
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'ATTACHMENT:UPLOAD', -1, -1 FROM t_role WHERE role_code = 'OPERATION_SPECIALIST' AND is_deleted = 0;

-- 附件下载
INSERT INTO `t_role_permission` (`role_id`, `permission_code`, `created_by`, `updated_by`)
SELECT id, 'ATTACHMENT:DOWNLOAD', -1, -1 FROM t_role WHERE role_code = 'OPERATION_SPECIALIST' AND is_deleted = 0;

-- ============================================================
-- 索引使用说明
-- ============================================================

-- 1. 查询角色的所有权限代码
-- SELECT permission_code FROM t_role_permission WHERE role_id = ? AND is_deleted = 0;

-- 2. 查询角色的所有权限(包含角色信息)
-- SELECT rp.*, r.role_name
-- FROM t_role_permission rp
-- LEFT JOIN t_role r ON rp.role_id = r.id AND r.is_deleted = 0
-- WHERE rp.role_id = ? AND rp.is_deleted = 0;

-- 3. 查询拥有某个权限的所有角色
-- SELECT rp.*, r.role_name
-- FROM t_role_permission rp
-- LEFT JOIN t_role r ON rp.role_id = r.id AND r.is_deleted = 0
-- WHERE rp.permission_code = ? AND rp.is_deleted = 0;

-- 4. 为角色分配权限
-- INSERT INTO t_role_permission (role_id, role_code, permission_code, created_by, updated_by)
-- VALUES (?, ?, ?, ?, ?);

-- 5. 撤销角色权限(软删除)
-- UPDATE t_role_permission SET is_deleted = 1, updated_by = ? WHERE role_id = ? AND permission_code = ?;

-- 6. 批量分配权限
-- INSERT INTO t_role_permission (role_id, role_code, permission_code, created_by, updated_by)
-- SELECT ?, role_code, ?, ?, ?
-- FROM (VALUES ('ORDER:VIEW'), ('ORDER:CREATE'), ('ORDER:UPDATE')) AS perms(permission_code);

-- ============================================================
-- 权限代码规范
-- ============================================================

-- 权限代码格式: {模块}:{操作}

-- 权限模块(Module):
-- - USER: 用户管理
-- - ROLE: 角色管理
-- - ORDER: 订单管理
-- - PARTNER: 合作方管理(供应商、承运商)
-- - SHIPMENT: 发运管理
-- - ATTACHMENT: 附件管理
-- - EXCEPTION: 异常管理
-- - DASHBOARD: 看板管理
-- - DATA: 数据管理(导入导出)

-- 权限操作(Action):
-- - *: 所有权限
-- - VIEW: 查看
-- - CREATE: 创建
-- - UPDATE: 更新
-- - DELETE: 删除
-- - AUDIT: 审核
-- - UPLOAD: 上传
-- - DOWNLOAD: 下载
-- - EXPORT: 导出
-- - IMPORT: 导入

-- 权限代码示例:
-- - ORDER:*          订单所有权限
-- - ORDER:VIEW        订单查看权限
-- - ORDER:CREATE      订单创建权限
-- - PARTNER:VIEW      合作方查看权限
-- - SHIPMENT:CREATE   发运创建权限
-- - ATTACHMENT:UPLOAD  附件上传权限
-- - DATA:EXPORT       数据导出权限

-- ============================================================
-- 权限验证流程
-- ============================================================

-- 1. 用户登录时,查询用户的角色和权限
-- SELECT
--   u.id, u.username, u.real_name,
--   ur.role_code,
--   GROUP_CONCAT(rp.permission_code) AS permissions
-- FROM t_user u
-- LEFT JOIN t_user_role ur ON u.id = ur.user_id AND ur.is_deleted = 0
-- LEFT JOIN t_role_permission rp ON ur.role_id = rp.role_id AND rp.is_deleted = 0
-- WHERE u.id = ? AND u.is_deleted = 0
-- GROUP BY u.id;

-- 2. 接口权限验证(在代码层面判断)
-- @RequireLogin
-- @PreAuthorize("hasPermission('ORDER:CREATE')")
-- @PostMapping("/order/create")
-- public Result createOrder(...) {
--     // 业务逻辑
-- }

-- 3. 数据权限过滤(MyBatis-Plus拦截器)
-- 根据用户角色的data_scope_type自动添加WHERE条件
-- - ALL: 不添加过滤条件
-- - DEPARTMENT: AND department_id = #{user.departmentId}
-- - SELF: AND created_by = #{userId}
