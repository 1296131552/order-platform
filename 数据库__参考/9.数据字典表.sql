-- ============================================================
-- 数据字典表 (t_data_dict)
-- 说明:数据字典表,存储系统数据字典项,用于下拉选择、数据校验等场景
-- ============================================================

CREATE TABLE `t_data_dict` (
  -- ========== 主键 ==========
  -- 字典ID,主键,自增
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典ID',
  
  -- ========== 字典基本信息 ==========
  -- 字典类型,唯一标识字典类型。示例:ORDER_TYPE(订单类型)、PAYMENT_METHOD(支付方式)、SHIPMENT_STATUS(发运状态)等
  -- 注意:字典类型必须唯一
  `dict_type` VARCHAR(50) NOT NULL COMMENT '字典类型',
  -- 字典类型名称(中文),用于展示。示例:订单类型、支付方式、发运状态
  `dict_type_name` VARCHAR(100) NOT NULL COMMENT '字典类型名称',
  -- 字典类型描述,详细说明该字典类型的含义和用途
  `dict_type_description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '字典类型描述',
  
  -- ========== 字典项信息 ==========
  -- 字典项代码,唯一标识字典项。示例:STANDARD(标准订单)、URGENT(紧急订单)等
  -- 注意:同一字典类型下字典项代码必须唯一
  `dict_code` VARCHAR(50) NOT NULL COMMENT '字典项代码',
  -- 字典项名称(中文),用于展示。示例:标准订单、紧急订单
  `dict_name` VARCHAR(100) NOT NULL COMMENT '字典项名称',
  -- 字典项值,字典项的实际值。可以是字符串、数字等
  `dict_value` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '字典项值',
  -- 字典项描述,详细说明该字典项的含义和用途
  `dict_description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '字典项描述',
  
  -- ========== 字典项属性 ==========
  -- 字典项排序,用于字典项列表排序。数值越小越靠前
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '字典项排序',
  -- 字典项颜色(用于前端展示),十六进制颜色代码。示例:#1890ff(蓝色)、#52c41a(绿色)
  `dict_color` VARCHAR(20) NOT NULL DEFAULT '#1890ff' COMMENT '字典项颜色',
  -- 字典项图标(用于前端展示),图标代码或URL
  `dict_icon` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '字典项图标',
  -- 是否默认项:0-非默认,1-默认(下拉选择时的默认值)
  -- 默认0表示非默认,避免使用NULL防止索引失效和简化查询逻辑
  `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认项:0-非默认,1-默认',
  -- 是否启用:0-禁用,1-启用
  -- 默认1表示启用,避免使用NULL防止索引失效和简化查询逻辑
  `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用:0-禁用,1-启用',
  -- 是否系统字典:0-用户自定义,1-系统内置
  -- 默认0表示用户自定义,避免使用NULL防止索引失效和简化查询逻辑
  `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统字典:0-用户自定义,1-系统内置',
  
  -- ========== 扩展信息 ==========
  -- 额外信息,JSON格式存储扩展字段
  -- 示例:{"parent_code": "STANDARD", "level": 2, "custom_field": "value"}
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
  -- 同一字典类型下字典项代码必须唯一
  UNIQUE KEY `uk_data_dict_type_code` (`dict_type`, `dict_code`, `is_deleted`) COMMENT '字典类型代码唯一约束',
  
  -- ========== 索引设计 ==========
  -- 字典类型+排序+启用状态+软删除联合索引,用于查询启用的字典项列表并按类型排序(高频场景:下拉选择)
  -- 覆盖场景:按字典类型查询、按启用状态查询、按字典类型+启用状态查询
  KEY `idx_data_dict_type_enabled` (`dict_type`, `sort_order`, `is_enabled`, `is_deleted`) COMMENT '字典类型启用状态联合索引'
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='数据字典表:数据字典表,存储系统数据字典项,用于下拉选择、数据校验等场景。'
  '每个字典记录包含字典类型、字典项代码、字典项名称、字典项值等完整信息,支持字典的分组管理和排序。'
  '字典类型用于区分不同的字典分类,同一字典类型下可以有多个字典项。';

