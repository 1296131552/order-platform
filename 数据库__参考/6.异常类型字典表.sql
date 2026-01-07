-- ============================================================
-- 异常类型字典表 (t_exception_type)
-- 说明:统一管理所有异常类型定义,支持异常分类管理
-- 关系:ExceptionType 1:N Exception (异常类型关联异常记录)
-- ============================================================

CREATE TABLE `t_exception_type` (
  -- ========== 主键 ==========
  -- 异常类型ID,主键,自增
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '异常类型ID',
  
  -- ========== 异常类型基本信息 ==========
  -- 异常类型代码,唯一标识异常类型。示例:TRANSPORT_DELAY(运输延迟)、ARRIVAL_DIFFERENCE(到货差异)、CERTIFICATE_MISSING(凭证缺失)等
  -- 注意:异常类型代码必须唯一
  `exception_type_code` VARCHAR(50) NOT NULL COMMENT '异常类型代码',
  -- 异常类型名称(中文),用于展示。示例:运输延迟、到货差异、凭证缺失
  `exception_type_name` VARCHAR(50) NOT NULL COMMENT '异常类型名称',
  -- 异常类型描述,详细说明该异常类型的含义和用途
  `exception_type_description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '异常类型描述',
  
  -- ========== 异常类型分类 ==========
  -- 异常分类,用于异常分组和展示。示例:TRANSPORT(运输异常)、DELIVERY(交付异常)、DOCUMENT(凭证异常)、QUALITY(质量异常)、OTHER(其他)
  `exception_category` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '异常分类',
  -- 异常级别:CRITICAL(严重)、HIGH(高)、MEDIUM(中)、LOW(低)
  -- 默认空字符串表示未设置异常级别,避免使用NULL防止索引失效和简化查询逻辑
  `exception_level` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '异常级别',
  -- 异常类型排序,用于异常类型列表排序。数值越小越靠前
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '异常类型排序',
  
  -- ========== 异常类型配置 ==========
  -- 异常类型颜色(用于前端展示),十六进制颜色代码。示例:#ff4d4f(红色)、#faad14(橙色)
  `exception_color` VARCHAR(20) NOT NULL DEFAULT '#ff4d4f' COMMENT '异常类型颜色',
  -- 异常类型图标(用于前端展示),图标代码或URL
  `exception_icon` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '异常类型图标',
  -- 是否启用:0-禁用,1-启用
  -- 默认1表示启用,避免使用NULL防止索引失效和简化查询逻辑
  `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用:0-禁用,1-启用',
  
  -- ========== 扩展信息 ==========
  -- 额外信息,JSON格式存储扩展字段
  -- 示例:{"required_fields": ["exception_time", "exception_description"], "auto_assign_rule": "by_business_type"}
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
  -- 异常类型代码必须唯一
  UNIQUE KEY `uk_exception_type_code` (`exception_type_code`, `is_deleted`) COMMENT '异常类型代码唯一约束',
  
  -- ========== 索引设计 ==========
  -- 异常分类+排序+启用状态+软删除联合索引,用于查询启用的异常类型列表并按分类排序(高频场景:异常类型下拉选择)
  -- 覆盖场景:按异常分类查询、按启用状态查询、按异常分类+启用状态查询
  KEY `idx_exception_type_category_enabled` (`exception_category`, `sort_order`, `is_enabled`, `is_deleted`) COMMENT '异常分类启用状态联合索引'
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='异常类型字典表:统一管理所有异常类型定义,支持异常分类管理。'
  '每个异常类型记录包含异常类型代码、名称、描述、分类、级别等完整信息,被异常记录表引用以保证异常类型定义统一。';

