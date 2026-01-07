-- ============================================================
-- 订单可视化平台 - 测试数据库初始化脚本
-- ============================================================
--
-- 功能：创建测试数据库、表结构和测试数据
-- 数据库：opv-test1
-- 测试账号：admin/admin123, zhangsan/123456
--
-- 使用方法：
-- mysql -u root -p < scripts/sql/init-test-db.sql
--
-- ============================================================

-- 1. 创建数据库
DROP DATABASE IF EXISTS `opv-test1`;
CREATE DATABASE `opv-test1` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `opv-test1`;

-- ============================================================
-- 2. 创建角色表 (t_role)
-- ============================================================
CREATE TABLE `t_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色代码',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `role_type` VARCHAR(30) NOT NULL DEFAULT 'BUSINESS' COMMENT '角色类型',
  `data_scope_type` TINYINT NOT NULL DEFAULT 3 COMMENT '数据权限',
  `description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '角色描述',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统角色',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `created_by` BIGINT NOT NULL DEFAULT -1 COMMENT '创建人ID',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `updated_by` BIGINT NOT NULL DEFAULT -1 COMMENT '更新人ID',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`, `is_deleted`),
  KEY `idx_role_type_enabled` (`role_type`, `is_enabled`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ============================================================
-- 3. 创建用户表 (t_user)
-- ============================================================
CREATE TABLE `t_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码(加密)',
  `user_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '用户编号',
  `real_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '真实姓名',
  `email` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '邮箱',
  `phone` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '手机号',
  `avatar` VARCHAR(500) NULL DEFAULT NULL COMMENT '头像URL',
  `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  `is_locked` TINYINT NOT NULL DEFAULT 0 COMMENT '是否锁定',
  `locked_time` DATETIME(3) NULL DEFAULT NULL COMMENT '锁定时间',
  `locked_reason` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '锁定原因',
  `last_login_time` DATETIME(3) NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '最后登录IP',
  `login_count` INT NOT NULL DEFAULT 0 COMMENT '登录次数',
  `password_changed_time` DATETIME(3) NULL DEFAULT NULL COMMENT '密码修改时间',
  `password_expire_time` DATETIME(3) NULL DEFAULT NULL COMMENT '密码过期时间',
  `department_id` BIGINT NOT NULL DEFAULT -1 COMMENT '部门ID',
  `department_name` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '部门名称',
  `position` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '职位',
  `employee_no` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '工号',
  `remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '用户备注',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `created_by` BIGINT NOT NULL DEFAULT -1 COMMENT '创建人ID',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `updated_by` BIGINT NOT NULL DEFAULT -1 COMMENT '更新人ID',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`, `is_deleted`),
  KEY `idx_user_enabled_locked` (`is_enabled`, `is_locked`, `is_deleted`),
  KEY `idx_user_department` (`department_id`, `is_enabled`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 4. 创建用户角色关联表 (t_user_role)
-- ============================================================
CREATE TABLE `t_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '用户名',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `role_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '角色代码',
  `is_primary` TINYINT NOT NULL DEFAULT 0 COMMENT '是否主角色',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `created_by` BIGINT NOT NULL DEFAULT -1 COMMENT '创建人ID',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `updated_by` BIGINT NOT NULL DEFAULT -1 COMMENT '更新人ID',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`, `is_deleted`),
  KEY `idx_user_role_user` (`user_id`, `is_deleted`),
  KEY `idx_user_role_role` (`role_id`, `is_deleted`),
  KEY `idx_user_role_primary` (`user_id`, `is_primary`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ============================================================
-- 5. 创建角色权限关联表 (t_role_permission)
-- ============================================================
CREATE TABLE `t_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `role_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '角色代码',
  `permission_code` VARCHAR(100) NOT NULL COMMENT '权限代码',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `created_by` BIGINT NOT NULL DEFAULT -1 COMMENT '创建人ID',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `updated_by` BIGINT NOT NULL DEFAULT -1 COMMENT '更新人ID',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_code`, `is_deleted`),
  KEY `idx_role_permission_role` (`role_id`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- ============================================================
-- 6. 创建操作日志表 (t_operation_log)
-- ============================================================
CREATE TABLE `t_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '操作日志ID',
  `operator_id` BIGINT NOT NULL DEFAULT -1 COMMENT '操作人ID',
  `operator_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作人姓名',
  `operator_user_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作人用户编号',
  `operator_employee_no` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作人工号',
  `operator_department_id` BIGINT NOT NULL DEFAULT -1 COMMENT '操作人部门ID',
  `operator_department_name` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '操作人部门名称',
  `operator_position` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作人职位',
  `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型',
  `business_id` BIGINT NOT NULL COMMENT '业务ID',
  `business_no` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '业务实体编号',
  `operation_type` VARCHAR(30) NOT NULL COMMENT '操作类型',
  `operation_module` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作模块',
  `operation_desc` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '操作描述',
  `operation_result` VARCHAR(30) NOT NULL DEFAULT 'SUCCESS' COMMENT '操作结果',
  `result_desc` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '操作结果描述',
  `operation_ip` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作IP地址',
  `request_path` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '请求路径',
  `request_method` VARCHAR(10) NOT NULL DEFAULT '' COMMENT '请求方法',
  `operation_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '操作时间',
  `operation_duration` INT NULL DEFAULT NULL COMMENT '操作耗时',
  `snapshot_key` VARCHAR(500) NULL DEFAULT NULL COMMENT '数据快照文件Key',
  `extra_info` JSON DEFAULT NULL COMMENT '扩展信息',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_operation_log_operator_time` (`operator_id`, `operation_time`, `is_deleted`),
  KEY `idx_operation_log_business_time` (`business_type`, `business_id`, `operation_time`, `is_deleted`),
  KEY `idx_operation_log_type_module` (`operation_type`, `operation_module`, `operation_time`, `is_deleted`),
  KEY `idx_operation_log_time_result` (`operation_time`, `operation_result`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================================
-- 7. 插入角色数据
-- ============================================================
INSERT INTO `t_role` (`id`, `role_code`, `role_name`, `role_type`, `data_scope_type`, `description`, `sort_order`, `is_enabled`, `is_system`) VALUES
(1, 'SYSTEM_ADMIN', '系统管理员', 'SYSTEM', 1, '负责权限配置、数据维护与系统管理', 1, 1, 1),
(2, 'DATA_ADMIN', '数据管理员', 'SYSTEM', 1, '负责数据查看、导出等数据管理操作', 2, 1, 1),
(3, 'CUSTOMER_MANAGER', '客户经理', 'BUSINESS', 3, '负责客户来单收集、订单创建与跟进', 3, 1, 1),
(4, 'PURCHASE_SPECIALIST', '采购专员', 'BUSINESS', 3, '负责供应商选择、资质审核与合作确认', 4, 1, 1),
(5, 'OPERATION_SPECIALIST', '运营专员', 'BUSINESS', 3, '负责发运计划制定、物流安排与在途跟踪', 5, 1, 1);

-- ============================================================
-- 8. 插入用户数据
-- ============================================================
-- admin 用户，密码 admin (BCrypt加密，strength=10)
INSERT INTO `t_user` (`id`, `username`, `password`, `user_code`, `real_name`, `email`, `phone`, `is_enabled`, `department_id`, `position`, `employee_no`) VALUES
(1, 'admin', '$2a$10$GnJQ0Hqb4OCmNcid17KMk.JK0x3fa2N9qFc.fenMyFR2EgB6u1vqi', 'USER001', '系统管理员', 'admin@example.com', '13800138000', 1, -1, '系统管理员', 'EMP001');

-- zhangsan 用户，密码 123456 (BCrypt加密，strength=10)
INSERT INTO `t_user` (`id`, `username`, `password`, `user_code`, `real_name`, `email`, `phone`, `is_enabled`, `department_id`, `position`, `employee_no`) VALUES
(2, 'zhangsan', '$2a$10$dDrYYWJ4rYiMjHGKS8mJbuSCbz.LO.J5m0S9mbVuCRc4J8OtsnzPm', 'USER002', '张三', 'zhangsan@example.com', '13800138001', 1, -1, '客户经理', 'EMP002');

-- ============================================================
-- 9. 插入用户角色关联
-- ============================================================
-- admin 分配系统管理员角色
INSERT INTO `t_user_role` (`user_id`, `username`, `role_id`, `role_code`, `is_primary`) VALUES
(1, 'admin', 1, 'SYSTEM_ADMIN', 1);

-- zhangsan 分配客户经理角色
INSERT INTO `t_user_role` (`user_id`, `username`, `role_id`, `role_code`, `is_primary`) VALUES
(2, 'zhangsan', 3, 'CUSTOMER_MANAGER', 1);

-- ============================================================
-- 10. 插入角色权限数据
-- ============================================================
-- 系统管理员所有权限
INSERT INTO `t_role_permission` (`role_id`, `permission_code`) VALUES
(1, 'USER:*'), (1, 'ROLE:*'), (1, 'ORDER:*'), (1, 'PARTNER:*'), (1, 'SHIPMENT:*'),
(1, 'ATTACHMENT:*'), (1, 'EXCEPTION:*'), (1, 'DASHBOARD:*'), (1, 'DATA:*');

-- 数据管理员权限
INSERT INTO `t_role_permission` (`role_id`, `permission_code`) VALUES
(2, 'ORDER:VIEW'), (2, 'PARTNER:VIEW'), (2, 'SHIPMENT:VIEW'),
(2, 'ATTACHMENT:VIEW'), (2, 'ATTACHMENT:DOWNLOAD'), (2, 'EXCEPTION:VIEW'),
(2, 'DASHBOARD:VIEW'), (2, 'DATA:EXPORT');

-- 客户经理权限
INSERT INTO `t_role_permission` (`role_id`, `permission_code`) VALUES
(3, 'ORDER:VIEW'), (3, 'ORDER:CREATE'), (3, 'ORDER:UPDATE'),
(3, 'PARTNER:VIEW'), (3, 'SHIPMENT:VIEW'),
(3, 'ATTACHMENT:VIEW'), (3, 'ATTACHMENT:UPLOAD'), (3, 'ATTACHMENT:DOWNLOAD'),
(3, 'EXCEPTION:VIEW');

-- 采购专员权限
INSERT INTO `t_role_permission` (`role_id`, `permission_code`) VALUES
(4, 'ORDER:VIEW'), (4, 'PARTNER:VIEW'), (4, 'PARTNER:CREATE'), (4, 'PARTNER:AUDIT'),
(4, 'SHIPMENT:VIEW'), (4, 'ATTACHMENT:VIEW'), (4, 'ATTACHMENT:UPLOAD'), (4, 'ATTACHMENT:DOWNLOAD');

-- 运营专员权限
INSERT INTO `t_role_permission` (`role_id`, `permission_code`) VALUES
(5, 'ORDER:VIEW'), (5, 'PARTNER:VIEW'), (5, 'SHIPMENT:VIEW'),
(5, 'SHIPMENT:CREATE'), (5, 'SHIPMENT:UPDATE'),
(5, 'ATTACHMENT:VIEW'), (5, 'ATTACHMENT:UPLOAD'), (5, 'ATTACHMENT:DOWNLOAD');

-- ============================================================
-- 11. 显示初始化结果
-- ============================================================
SELECT '========================================' AS '';
SELECT '✅ 数据库初始化完成！' AS '状态';
SELECT '========================================' AS '';
SELECT '' AS '';
SELECT '📋 测试账号信息：' AS '';
SELECT '1. admin / admin (系统管理员)' AS '';
SELECT '2. zhangsan / 123456 (客户经理)' AS '';
SELECT '========================================' AS '';
SELECT '' AS '';
SELECT '👥 用户列表：' AS '';
SELECT id, username, real_name, email, phone, position FROM t_user WHERE is_deleted = 0;
SELECT '========================================' AS '';
SELECT '' AS '';
SELECT '🔐 角色列表：' AS '';
SELECT id, role_code, role_name, role_type FROM t_role WHERE is_deleted = 0 ORDER BY sort_order;
SELECT '========================================' AS '';
SELECT '' AS '';
SELECT '🔗 用户角色关联：' AS '';
SELECT u.username, r.role_name FROM t_user_role ur LEFT JOIN t_user u ON ur.user_id = u.id LEFT JOIN t_role r ON ur.role_id = r.id WHERE ur.is_deleted = 0;
SELECT '========================================' AS '';
