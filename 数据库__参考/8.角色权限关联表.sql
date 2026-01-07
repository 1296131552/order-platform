-- ============================================================
-- 角色权限关联表 (t_role_permission)
-- 说明:角色权限关联中间表,实现角色与权限的N:M关联
-- 关系:RolePermission N:1 Role (关联角色)
-- 关系:RolePermission N:1 Permission (关联权限)
-- ============================================================

CREATE TABLE `t_role_permission` (
  -- ========== 主键 ==========
  -- 关联ID,主键,自增。每条关联记录的唯一标识
  -- 注意:目前使用自增ID,若使用分布式架构应改为雪花ID
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',

  -- ========== 关联角色信息 ==========
  -- 关系:RolePermission N:1 Role (关联角色)
  -- 角色ID,外键关联t_role表。每条关联记录必须关联一个角色
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  -- 角色代码(冗余字段,便于查询和展示)
  `role_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '角色代码(冗余字段)',
  -- 角色名称(冗余字段,便于展示)
  `role_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '角色名称(冗余字段)',

  -- ========== 关联权限信息 ==========
  -- 关系:RolePermission N:1 Permission (关联权限)
  -- 权限ID,外键关联t_permission表。每条关联记录必须关联一个权限
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  -- 权限代码(冗余字段,便于查询和展示)
  `permission_code` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '权限代码(冗余字段)',
  -- 权限名称(冗余字段,便于展示)
  `permission_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '权限名称(冗余字段)',

  -- ========== 关联信息 ==========
  -- 关联时间(精确到毫秒),记录角色与权限关联的时间
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
  -- 关联角色表和权限表,保证数据一致性
  -- 注意:外键约束可选择性启用,取决于业务需求。启用外键可保证数据一致性,但可能影响性能
  -- CONSTRAINT `fk_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `t_role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  -- CONSTRAINT `fk_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `t_permission` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,

  -- ========== 约束设计 ==========
  -- 角色ID+权限ID唯一约束,保证同一角色不会重复关联同一权限
  -- 注意:即使关联记录被软删除,也不应重复使用同一个角色ID+权限ID组合
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`, `is_deleted`) COMMENT '角色权限关联唯一约束',

  -- ========== 索引设计 ==========
  -- 索引设计原则:只保留核心查询场景的索引,避免过度索引导致写入性能下降
  -- InnoDB联合索引遵循最左前缀原则,单字段索引被联合索引覆盖时需删除以避免冗余

  -- 角色ID+软删除联合索引,用于查询角色的所有权限(高频场景:角色权限验证)
  -- 覆盖场景:按角色ID查询权限、按角色ID+删除标记查询权限
  KEY `idx_role_permission_role` (`role_id`, `is_deleted`) COMMENT '角色ID索引',

  -- 权限ID+软删除联合索引,用于查询权限的所有角色(统计场景:查询某个权限的所有角色)
  KEY `idx_role_permission_permission` (`permission_id`, `is_deleted`) COMMENT '权限ID索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='角色权限关联表:角色权限关联中间表,实现角色与权限的N:M关联。'
  '【关联关系】每个关联记录关联一个角色和一个权限。一个角色可以有多个权限,一个权限可以分配给多个角色。'
  '【唯一约束】角色ID+权限ID唯一约束,保证同一角色不会重复关联同一权限。'
  '【冗余字段】角色代码、角色名称、权限代码、权限名称等字段作为冗余字段,便于查询和展示,避免频繁关联查询角色表和权限表。';

