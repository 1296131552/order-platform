-- ============================================================
-- 附件标签表 (t_attachment_tag)
-- 说明:附件标签表,定义附件标签信息,支持附件分类和标记
-- 关系:AttachmentTag N:M Attachment (通过attachment_tag_relation中间表关联)
-- ============================================================

CREATE TABLE `t_attachment_tag` (
  -- ========== 主键 ==========
  -- 附件标签ID,主键,自增
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '附件标签ID',
  
  -- ========== 标签基本信息 ==========
  -- 标签代码,唯一标识标签。示例:CONTRACT(合同)、QUOTATION(报价)、VOUCHER(凭证)、PHOTO(照片)等
  -- 注意:标签代码必须唯一
  `tag_code` VARCHAR(50) NOT NULL COMMENT '标签代码',
  -- 标签名称(中文),用于展示。示例:合同、报价、凭证、照片
  `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名称',
  -- 标签描述,详细说明该标签的含义和用途
  `tag_description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '标签描述',
  
  -- ========== 标签分类 ==========
  -- 标签分类,用于标签分组和展示。示例:TYPE(类型标签)、CATEGORY(分类标签)、STATUS(状态标签)、CUSTOM(自定义标签)等
  `tag_category` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '标签分类',
  -- 标签类型,用于标签细分。示例:ORDER(订单相关)、SHIPMENT(发运相关)、RECEIPT(签收相关)、PARTNER(合作方相关)等
  `tag_type` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '标签类型',
  -- 标签颜色(用于前端展示),十六进制颜色代码。示例:#1890ff(蓝色)、#52c41a(绿色)
  `tag_color` VARCHAR(20) NOT NULL DEFAULT '#1890ff' COMMENT '标签颜色',
  -- 标签图标(用于前端展示),图标代码或URL
  `tag_icon` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '标签图标',
  -- 标签排序,用于标签列表排序。数值越小越靠前
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '标签排序',
  
  -- ========== 标签配置 ==========
  -- 是否启用:0-禁用,1-启用
  -- 默认1表示启用,避免使用NULL防止索引失效和简化查询逻辑
  `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用:0-禁用,1-启用',
  -- 是否系统标签:0-用户自定义,1-系统内置
  -- 默认0表示用户自定义,避免使用NULL防止索引失效和简化查询逻辑
  `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统标签:0-用户自定义,1-系统内置',
  -- 是否必选:0-可选,1-必选(某些业务场景下必须使用该标签)
  -- 默认0表示可选,避免使用NULL防止索引失效和简化查询逻辑
  `is_required` TINYINT NOT NULL DEFAULT 0 COMMENT '是否必选:0-可选,1-必选',
  
  -- ========== 扩展信息 ==========
  -- 额外信息,JSON格式存储扩展字段
  -- 示例:{"allowed_business_types": ["order", "shipment"], "max_attachment_count": 10}
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
  -- 标签代码必须唯一
  UNIQUE KEY `uk_attachment_tag_code` (`tag_code`, `is_deleted`) COMMENT '标签代码唯一约束',
  
  -- ========== 索引设计 ==========
  -- 标签分类+标签类型+排序+启用状态+软删除联合索引,用于查询启用的标签列表并按分类排序(高频场景:标签下拉选择)
  -- 覆盖场景:按标签分类查询、按标签类型查询、按启用状态查询、按标签分类+启用状态查询
  KEY `idx_attachment_tag_category_enabled` (`tag_category`, `tag_type`, `sort_order`, `is_enabled`, `is_deleted`) COMMENT '标签分类启用状态联合索引'
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='附件标签表:附件标签表,定义附件标签信息,支持附件分类和标记。'
  '每个标签记录包含标签代码、名称、描述、分类、类型等完整信息,通过attachment_tag_relation中间表与附件实现N:M关联。';

