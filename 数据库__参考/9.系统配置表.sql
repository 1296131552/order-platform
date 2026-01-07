-- ============================================================
-- 系统配置表 (t_system_config)
-- 说明:系统参数配置表,存储系统配置项和参数值
-- ============================================================

CREATE TABLE `t_system_config` (
  -- ========== 主键 ==========
  -- 配置ID,主键,自增
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  
  -- ========== 配置基本信息 ==========
  -- 配置键,唯一标识配置项。示例:system.name(系统名称)、order.auto_close_days(订单自动关闭天数)、file.max_size(文件最大大小)等
  -- 注意:配置键必须唯一
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  -- 配置值,配置项的值。可以是字符串、数字、JSON等格式
  `config_value` TEXT NOT NULL COMMENT '配置值',
  -- 配置名称(中文),用于展示。示例:系统名称、订单自动关闭天数、文件最大大小
  `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
  -- 配置描述,详细说明该配置项的含义和用途
  `config_description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '配置描述',
  
  -- ========== 配置分类 ==========
  -- 配置分组,用于配置分组管理。示例:SYSTEM(系统配置)、ORDER(订单配置)、FILE(文件配置)、SECURITY(安全配置)、NOTIFICATION(通知配置)等
  `config_group` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '配置分组',
  -- 配置类型,标识配置值的数据类型。示例:STRING(字符串)、NUMBER(数字)、BOOLEAN(布尔值)、JSON(JSON对象)、ARRAY(数组)等
  `config_type` VARCHAR(50) NOT NULL DEFAULT 'STRING' COMMENT '配置类型:默认STRING',
  -- 配置排序,用于配置列表排序。数值越小越靠前
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '配置排序',
  
  -- ========== 配置约束 ==========
  -- 默认值,配置项的默认值
  `default_value` TEXT NOT NULL COMMENT '默认值',
  -- 配置约束,JSON格式存储配置的约束条件。示例:{"min": 0, "max": 100, "pattern": "^[0-9]+$"}
  -- 注意:使用NULL表示无约束,避免空JSON对象占用存储空间,且NULL可以明确区分"无约束"和"空约束"
  `config_constraint` JSON DEFAULT NULL COMMENT '配置约束:NULL表示无约束',
  -- 是否必填:0-可选,1-必填
  -- 默认0表示可选,避免使用NULL防止索引失效和简化查询逻辑
  `is_required` TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填:0-可选,1-必填',
  -- 是否可编辑:0-不可编辑(系统内置),1-可编辑
  -- 默认1表示可编辑,避免使用NULL防止索引失效和简化查询逻辑
  `is_editable` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可编辑:0-不可编辑,1-可编辑',
  -- 是否系统配置:0-用户自定义,1-系统内置
  -- 默认0表示用户自定义,避免使用NULL防止索引失效和简化查询逻辑
  `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统配置:0-用户自定义,1-系统内置',
  
  -- ========== 配置状态 ==========
  -- 是否启用:0-禁用,1-启用
  -- 默认1表示启用,避免使用NULL防止索引失效和简化查询逻辑
  `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用:0-禁用,1-启用',
  
  -- ========== 扩展信息 ==========
  -- 额外信息,JSON格式存储扩展字段
  -- 示例:{"validation_rule": "email", "help_text": "请输入有效的邮箱地址"}
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
  -- 配置键必须唯一
  UNIQUE KEY `uk_system_config_key` (`config_key`, `is_deleted`) COMMENT '配置键唯一约束',
  
  -- ========== 索引设计 ==========
  -- 配置分组+配置类型+排序+启用状态+软删除联合索引,用于查询启用的配置列表并按分组排序(高频场景:配置管理页面)
  -- 覆盖场景:按配置分组查询、按配置类型查询、按启用状态查询、按配置分组+启用状态查询
  KEY `idx_system_config_group_enabled` (`config_group`, `config_type`, `sort_order`, `is_enabled`, `is_deleted`) COMMENT '配置分组启用状态联合索引'
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='系统配置表:系统参数配置表,存储系统配置项和参数值。'
  '每个配置记录包含配置键、配置值、配置名称、描述、分组、类型等完整信息,支持配置的分组管理和类型约束。';

