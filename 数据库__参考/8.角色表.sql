-- ============================================================
-- 角色表 (t_role)
-- 说明:系统角色表,定义角色信息,用于权限管理
-- 关系:Role N:M User (通过t_user_role中间表关联)
-- 关系:Role N:M Permission (通过t_role_permission中间表关联)
-- ============================================================

CREATE TABLE `t_role` (
  -- ========== 主键 ==========
  -- 角色ID,主键,自增
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  
  -- ========== 角色基本信息 ==========
  -- 角色代码,唯一标识角色。示例:CUSTOMER_MANAGER(客户经理)、PURCHASE_SPECIALIST(采购专员)、OPERATION_SPECIALIST(运营专员)、DATA_ADMIN(数据管理员)、SYSTEM_ADMIN(系统管理员)等
  -- 注意:角色代码必须唯一
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色代码',
  -- 角色名称(中文),用于展示。示例:客户经理、采购专员、运营专员、数据管理员、系统管理员
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  -- 角色描述,详细说明该角色的职责和权限范围
  -- 示例:客户经理-负责客户来单收集、订单创建与跟进;采购专员-负责供应商选择、资质审核与合作确认;运营专员-负责发运计划制定、物流安排与在途跟踪
  `role_description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '角色描述',
  
  -- ========== 角色分类 ==========
  -- 角色类型,用于角色分组。示例:SYSTEM(系统角色)、BUSINESS(业务角色)、CUSTOM(自定义角色)等
  -- 业务角色包括:客户经理、采购专员、运营专员、数据管理员
  `role_type` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '角色类型',
  -- 角色级别,用于权限层级控制。数值越大权限越高,如:1(普通用户)、10(管理员)、100(超级管理员)
  -- 默认0表示未设置角色级别,避免使用NULL防止索引失效和简化查询逻辑
  `role_level` INT NOT NULL DEFAULT 0 COMMENT '角色级别:数值越大权限越高',
  -- 角色排序,用于角色列表排序。数值越小越靠前
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '角色排序',
  
  -- ========== 角色配置 ==========
  -- 是否启用:0-禁用,1-启用
  -- 默认1表示启用,避免使用NULL防止索引失效和简化查询逻辑
  `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用:0-禁用,1-启用',
  -- 是否系统角色:0-用户自定义,1-系统内置
  -- 默认0表示用户自定义,避免使用NULL防止索引失效和简化查询逻辑
  `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统角色:0-用户自定义,1-系统内置',
  -- 是否可删除:0-可删除,1-不可删除(系统角色通常不可删除)
  -- 默认0表示可删除,避免使用NULL防止索引失效和简化查询逻辑
  `is_deletable` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可删除:0-可删除,1-不可删除',
  -- 是否可编辑:0-可编辑,1-不可编辑(系统角色通常不可编辑)
  -- 默认1表示可编辑,避免使用NULL防止索引失效和简化查询逻辑
  `is_editable` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可编辑:0-可编辑,1-不可编辑',
  
  -- ========== 扩展信息 ==========
  -- 额外信息,JSON格式存储扩展字段
  -- 示例:{"data_scope": "all", "menu_ids": [1, 2, 3], "dept_ids": [10, 20]}
  -- 注意:使用NULL表示无扩展信息,避免空JSON对象占用存储空间,且NULL可以明确区分"无扩展信息"和"空扩展信息"
  `extra_info` JSON DEFAULT NULL COMMENT '扩展信息',
  
  -- ========== 时间戳 ==========
  -- 创建时间
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  -- 更新时间
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  -- 创建人ID
  -- 默认-1表示系统创建,避免使用NULL防止空指针问题和简化查询逻辑
  `created_by` BIGINT NOT NULL DEFAULT -1 COMMENT '创建人ID:默认-1表示系统创建',
  -- 更新人ID
  -- 默认-1表示系统更新,避免使用NULL防止空指针问题和简化查询逻辑
  `updated_by` BIGINT NOT NULL DEFAULT -1 COMMENT '更新人ID:默认-1表示系统更新',
  
  -- ========== 软删除 ==========
  -- 是否删除:0-未删除,1-已删除
  -- 默认0表示未删除,避免使用NULL防止索引失效和简化查询逻辑
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除,1-已删除',
  -- 删除时间
  -- 注意:使用NULL表示未删除,必须区分"未删除"和"已删除但删除时间为空"的业务含义,因此允许使用NULL
  `deleted_at` DATETIME(3) NULL DEFAULT NULL COMMENT '删除时间',
  
  PRIMARY KEY (`id`),
  
  -- ========== 唯一约束 ==========
  -- 角色代码必须唯一
  UNIQUE KEY `uk_role_code` (`role_code`, `is_deleted`) COMMENT '角色代码唯一约束',
  
  -- ========== 索引设计 ==========
  -- 角色类型+角色级别+排序+启用状态+软删除联合索引,用于查询启用的角色列表并按类型和级别排序(高频场景:角色下拉选择)
  -- 覆盖场景:按角色类型查询、按启用状态查询、按角色类型+启用状态查询
  KEY `idx_role_type_enabled` (`role_type`, `role_level`, `sort_order`, `is_enabled`, `is_deleted`) COMMENT '角色类型启用状态联合索引'
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='角色表:系统角色表,定义角色信息,用于权限管理。'
  '每个角色记录包含角色代码、名称、描述、类型、级别等完整信息,通过t_user_role中间表与用户实现N:M关联,'
  '通过t_role_permission中间表与权限实现N:M关联。';

