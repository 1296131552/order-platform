-- ============================================================
-- 角色表 (t_role)
-- 说明: 系统角色表,定义角色信息,用于权限管理和数据权限控制
-- 关系: Role N:M User (通过t_user_role中间表关联)
-- 关系: Role N:M Permission (通过t_role_permission中间表关联)
-- ============================================================

CREATE TABLE `t_role` (
  -- ========== 主键 ==========
  -- 角色ID,主键,自增
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',

  -- ========== 角色基本信息 ==========
  -- 角色代码,唯一标识角色
  -- 示例: CUSTOMER_MANAGER(客户经理)、PURCHASE_SPECIALIST(采购专员)、OPERATION_SPECIALIST(运营专员)、DATA_ADMIN(数据管理员)、SYSTEM_ADMIN(系统管理员)
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色代码',

  -- 角色名称,用于展示
  -- 示例: 客户经理、采购专员、运营专员、数据管理员、系统管理员
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',

  -- 角色类型,用于角色分组
  -- BUSINESS(业务角色): 客户经理、采购专员、运营专员、数据管理员
  -- SYSTEM(系统角色): 系统管理员
  `role_type` VARCHAR(30) NOT NULL DEFAULT 'BUSINESS' COMMENT '角色类型:BUSINESS/SYSTEM',

  -- 数据权限类型,标识角色可以访问的数据范围
  -- 1-ALL(全部数据): 系统管理员、数据管理员
  -- 2-DEPARTMENT(本部门数据): 部门经理
  -- 3-SELF(本人数据): 客户经理、采购专员、运营专员
  -- 4-CUSTOM(自定义范围): 预留扩展
  `data_scope_type` TINYINT NOT NULL DEFAULT 3 COMMENT '数据权限:1-全部,2-本部门,3-本人,4-自定义',

  -- 角色描述,详细说明该角色的职责和权限范围
  `description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '角色描述',

  -- 角色排序,用于角色列表排序。数值越小越靠前
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',

  -- ========== 角色配置 ==========
  -- 是否启用: 0-禁用, 1-启用
  -- 默认1表示启用,避免使用NULL防止索引失效和简化查询逻辑
  `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用:0-禁用,1-启用',

  -- 是否系统角色: 0-用户自定义, 1-系统内置
  -- 系统角色不可删除,避免误删导致权限系统崩溃
  -- 默认0表示用户自定义,避免使用NULL防止索引失效和简化查询逻辑
  `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统角色:0-用户自定义,1-系统内置',

  -- ========== 公共字段 ==========
  -- 创建时间(精确到毫秒)
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

  -- 创建人ID,记录角色的创建人
  -- 默认-1表示系统创建,避免使用NULL防止空指针问题和简化查询逻辑
  `created_by` BIGINT NOT NULL DEFAULT -1 COMMENT '创建人ID:默认-1表示系统创建',

  -- 更新时间(精确到毫秒)
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

  -- 更新人ID,记录角色信息的最后更新人
  -- 默认-1表示系统更新,避免使用NULL防止空指针问题和简化查询逻辑
  `updated_by` BIGINT NOT NULL DEFAULT -1 COMMENT '更新人ID:默认-1表示系统更新',

  -- 是否删除: 0-未删除, 1-已删除
  -- 默认0表示未删除,避免使用NULL防止索引失效和简化查询逻辑
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除,1-已删除',

  PRIMARY KEY (`id`),

  -- ========== 唯一约束 ==========
  -- 角色代码必须唯一(考虑软删除)
  UNIQUE KEY `uk_role_code` (`role_code`, `is_deleted`) COMMENT '角色代码唯一约束',

  -- ========== 索引设计 ==========
  -- 索引设计原则: 只保留核心查询场景的索引,避免过度索引导致写入性能下降
  -- InnoDB联合索引遵循最左前缀原则,单字段索引被联合索引覆盖时需删除以避免冗余

  -- 角色类型+启用状态+软删除联合索引,用于查询启用的角色列表(高频场景: 角色下拉选择)
  -- 覆盖场景: 按角色类型查询、按启用状态查询、按角色类型+启用状态查询
  KEY `idx_role_type_enabled` (`role_type`, `is_enabled`, `is_deleted`) COMMENT '角色类型启用状态联合索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='角色表: 系统角色表,定义角色信息,用于权限管理和数据权限控制。'
  '【数据权限】data_scope_type字段是实现数据权限控制的核心,定义角色可以访问的数据范围。'
  '【角色类型】BUSINESS(业务角色)包括客户经理、采购专员、运营专员、数据管理员;SYSTEM(系统角色)包括系统管理员。'
  '【关联关系】角色与用户通过t_user_role中间表实现N:M关联,角色与权限通过t_role_permission中间表实现N:M关联。';

-- ============================================================
-- 预定义角色初始化数据
-- 说明: 根据甲方需求文档定义的5个标准角色
-- ============================================================

-- 系统管理员角色
-- 职责: 负责权限配置、数据维护与系统管理
-- 数据权限: 全部数据
INSERT INTO `t_role` (`role_code`, `role_name`, `role_type`, `data_scope_type`, `description`, `sort_order`, `is_enabled`, `is_system`, `created_by`, `updated_by`) VALUES
('SYSTEM_ADMIN', '系统管理员', 'SYSTEM', 1, '负责权限配置、数据维护与系统管理', 1, 1, 1, -1, -1);

-- 数据管理员角色
-- 职责: 负责数据查看、导出等数据管理操作
-- 数据权限: 全部数据
INSERT INTO `t_role` (`role_code`, `role_name`, `role_type`, `data_scope_type`, `description`, `sort_order`, `is_enabled`, `is_system`, `created_by`, `updated_by`) VALUES
('DATA_ADMIN', '数据管理员', 'SYSTEM', 1, '负责数据查看、导出等数据管理操作', 2, 1, 1, -1, -1);

-- 客户经理角色
-- 职责: 负责客户来单收集、订单创建与跟进
-- 数据权限: 本人数据
INSERT INTO `t_role` (`role_code`, `role_name`, `role_type`, `data_scope_type`, `description`, `sort_order`, `is_enabled`, `is_system`, `created_by`, `updated_by`) VALUES
('CUSTOMER_MANAGER', '客户经理', 'BUSINESS', 3, '负责客户来单收集、订单创建与跟进', 3, 1, 1, -1, -1);

-- 采购专员角色
-- 职责: 负责供应商选择、资质审核与合作确认
-- 数据权限: 本人数据
INSERT INTO `t_role` (`role_code`, `role_name`, `role_type`, `data_scope_type`, `description`, `sort_order`, `is_enabled`, `is_system`, `created_by`, `updated_by`) VALUES
('PURCHASE_SPECIALIST', '采购专员', 'BUSINESS', 3, '负责供应商选择、资质审核与合作确认', 4, 1, 1, -1, -1);

-- 运营专员角色
-- 职责: 负责发运计划制定、物流安排与在途跟踪
-- 数据权限: 本人数据
INSERT INTO `t_role` (`role_code`, `role_name`, `role_type`, `data_scope_type`, `description`, `sort_order`, `is_enabled`, `is_system`, `created_by`, `updated_by`) VALUES
('OPERATION_SPECIALIST', '运营专员', 'BUSINESS', 3, '负责发运计划制定、物流安排与在途跟踪', 5, 1, 1, -1, -1);

-- ============================================================
-- 索引使用说明
-- ============================================================

-- 1. 查询所有启用的角色
-- SELECT * FROM t_role WHERE is_enabled = 1 AND is_deleted = 0 ORDER BY sort_order;

-- 2. 查询所有系统角色
-- SELECT * FROM t_role WHERE role_type = 'SYSTEM' AND is_deleted = 0;

-- 3. 查询所有业务角色
-- SELECT * FROM t_role WHERE role_type = 'BUSINESS' AND is_enabled = 1 AND is_deleted = 0;

-- 4. 根据角色代码查询角色
-- SELECT * FROM t_role WHERE role_code = 'SYSTEM_ADMIN' AND is_deleted = 0;

-- 5. 查询所有可删除的角色(非系统角色)
-- SELECT * FROM t_role WHERE is_system = 0 AND is_deleted = 0;

-- ============================================================
-- 数据权限类型说明
-- ============================================================

-- data_scope_type 字段值说明:
-- 1-ALL(全部数据): 系统管理员、数据管理员,可以查看和操作全部数据
-- 2-DEPARTMENT(本部门数据): 部门经理,只能查看本部门的数据
-- 3-SELF(本人数据): 客户经理、采购专员、运营专员,只能查看自己创建的数据
-- 4-CUSTOM(自定义范围): 预留扩展,支持更复杂的数据权限控制(如按客户、供应商等)

-- ============================================================
-- 角色类型说明
-- ============================================================

-- role_type 字段值说明:
-- BUSINESS(业务角色): 客户经理、采购专员、运营专员、数据管理员
-- SYSTEM(系统角色): 系统管理员

-- ============================================================
-- 系统角色 vs 用户自定义角色
-- ============================================================

-- 系统角色(is_system=1):
-- - 系统内置角色,不可删除
-- - 通常是业务核心角色
-- - 示例: 系统管理员、客户经理、采购专员、运营专员、数据管理员

-- 用户自定义角色(is_system=0):
-- - 用户自定义角色,可以删除
-- - 用于满足特殊业务需求
-- - 示例: 临时项目角色、特殊权限角色
