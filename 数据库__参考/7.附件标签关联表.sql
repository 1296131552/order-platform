-- ============================================================
-- 附件标签关联表 (t_attachment_tag_relation)
-- 说明:附件标签关联中间表,实现附件与附件标签的N:M关联
-- 关系:AttachmentTagRelation N:1 Attachment (关联附件)
-- 关系:AttachmentTagRelation N:1 AttachmentTag (关联附件标签)
-- ============================================================

CREATE TABLE `t_attachment_tag_relation` (
  -- ========== 主键 ==========
  -- 关联ID,主键,自增。每条关联记录的唯一标识
  -- 注意:目前使用自增ID,若使用分布式架构应改为雪花ID
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',

  -- ========== 关联附件信息 ==========
  -- 关系:AttachmentTagRelation N:1 Attachment (关联附件)
  -- 附件ID,外键关联t_attachment表。每条关联记录必须关联一个附件
  `attachment_id` BIGINT NOT NULL COMMENT '附件ID',
  -- 附件名称(冗余字段,便于查询和展示)
  `attachment_name` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '附件名称(冗余字段)',
  -- 业务类型(冗余字段,便于查询和展示)
  `business_type` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '业务类型(冗余字段)',
  -- 业务ID(冗余字段,便于查询和展示)
  `business_id` BIGINT NOT NULL DEFAULT 0 COMMENT '业务ID(冗余字段)',

  -- ========== 关联附件标签信息 ==========
  -- 关系:AttachmentTagRelation N:1 AttachmentTag (关联附件标签)
  -- 附件标签ID,外键关联t_attachment_tag表。每条关联记录必须关联一个附件标签
  `attachment_tag_id` BIGINT NOT NULL COMMENT '附件标签ID',
  -- 附件标签代码(冗余字段,便于查询和展示)
  `tag_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '附件标签代码(冗余字段)',
  -- 附件标签名称(冗余字段,便于展示)
  `tag_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '附件标签名称(冗余字段)',

  -- ========== 关联信息 ==========
  -- 关联时间(精确到毫秒),记录附件与标签关联的时间
  `relation_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '关联时间',
  -- 关联人ID,外键关联t_user表。记录创建关联的用户。-1表示系统自动关联,0及以上表示用户ID
  -- 默认-1表示系统关联,避免使用NULL防止空指针问题和简化查询逻辑
  `relation_by` BIGINT NOT NULL DEFAULT -1 COMMENT '关联人ID:默认-1表示系统关联',
  -- 关联人姓名(冗余字段,便于查询和展示)
  `relation_by_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '关联人姓名(冗余字段)',
  -- 关联说明,说明关联的原因或用途
  `relation_description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '关联说明',

  -- ========== 扩展信息 ==========
  -- 扩展信息,JSON格式存储扩展字段,便于扩展且无需修改表结构
  -- 注意:仅支持MySQL 5.7+版本
  -- 使用NULL表示无扩展信息,避免空JSON对象占用存储空间,且NULL可以明确区分"无扩展信息"和"空扩展信息"
  `extra_info` JSON DEFAULT NULL COMMENT '扩展信息:NULL表示无扩展信息',

  -- ========== 公共字段 ==========
  -- 创建时间(精确到毫秒),记录关联记录创建的时间
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  -- 创建人ID,记录关联记录的创建人,外键关联t_user表。-1表示系统自动操作,0及以上表示用户ID
  -- 默认-1表示系统创建,避免使用NULL防止空指针问题和简化查询逻辑
  `created_by` BIGINT NOT NULL DEFAULT -1 COMMENT '创建人ID:默认-1表示系统创建',
  -- 更新时间(精确到毫秒),记录关联记录信息最后更新的时间
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  -- 更新人ID,记录关联记录信息的最后更新人,外键关联t_user表。-1表示系统自动操作,0及以上表示用户ID
  -- 默认-1表示系统更新,避免使用NULL防止空指针问题和简化查询逻辑
  `updated_by` BIGINT NOT NULL DEFAULT -1 COMMENT '更新人ID:默认-1表示系统更新',
  -- 是否删除:0-未删除,1-已删除
  -- 默认0表示未删除,避免使用NULL防止索引失效和简化查询逻辑
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除,1-已删除',

  PRIMARY KEY (`id`),

  -- ========== 外键约束 ==========
  -- 关联附件表和附件标签表,保证数据一致性
  -- 注意:外键约束可选择性启用,取决于业务需求。启用外键可保证数据一致性,但可能影响性能
  -- CONSTRAINT `fk_attachment_tag_relation_attachment` FOREIGN KEY (`attachment_id`) REFERENCES `t_attachment` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  -- CONSTRAINT `fk_attachment_tag_relation_tag` FOREIGN KEY (`attachment_tag_id`) REFERENCES `t_attachment_tag` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,

  -- ========== 约束设计 ==========
  -- 附件ID+附件标签ID唯一约束,保证同一附件不会重复关联同一标签
  -- 注意:即使关联记录被软删除,也不应重复使用同一个附件ID+标签ID组合
  UNIQUE KEY `uk_attachment_tag_relation` (`attachment_id`, `attachment_tag_id`, `is_deleted`) COMMENT '附件标签关联唯一约束',

  -- ========== 索引设计 ==========
  -- 索引设计原则:只保留核心查询场景的索引,避免过度索引导致写入性能下降
  -- InnoDB联合索引遵循最左前缀原则,单字段索引被联合索引覆盖时需删除以避免冗余

  -- 附件ID+软删除联合索引,用于查询附件的所有标签(高频场景:查看附件的标签)
  -- 覆盖场景:按附件ID查询标签、按附件ID+删除标记查询标签
  KEY `idx_attachment_tag_relation_attachment` (`attachment_id`, `is_deleted`) COMMENT '附件ID索引',

  -- 附件标签ID+软删除联合索引,用于查询标签的所有附件(统计场景:查询某个标签的所有附件)
  KEY `idx_attachment_tag_relation_tag` (`attachment_tag_id`, `is_deleted`) COMMENT '附件标签ID索引',

  -- 业务类型+业务ID+软删除联合索引,用于查询业务实体的所有附件标签关联(统计场景:查询业务实体的标签)
  -- 注意:业务类型和业务ID为冗余字段,便于查询和统计,不设置外键约束
  KEY `idx_attachment_tag_relation_business` (`business_type`, `business_id`, `is_deleted`) COMMENT '业务实体关联索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='附件标签关联表:附件标签关联中间表,实现附件与附件标签的N:M关联。'
  '【关联关系】每个关联记录关联一个附件和一个附件标签。一个附件可以关联多个标签,一个标签可以关联多个附件。'
  '【唯一约束】附件ID+附件标签ID唯一约束,保证同一附件不会重复关联同一标签。'
  '【冗余字段】附件名称、业务类型、业务ID、标签代码、标签名称等字段作为冗余字段,便于查询和展示,避免频繁关联查询附件表和标签表。';

