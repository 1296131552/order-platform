-- ============================================================
-- 用户角色关联表 (t_user_role)
-- 说明: 用户角色关联中间表,实现用户与角色的N:M关联
-- 关系: UserRole N:1 User (关联用户)
-- 关系: UserRole N:1 Role (关联角色)
-- 设计要点: 支持一个用户拥有多个角色,通过is_primary标识主角色用于数据权限判断
-- ============================================================

CREATE TABLE `t_user_role` (
  -- ========== 主键 ==========
  -- 关联ID,主键,自增
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',

  -- ========== 关联用户信息 ==========
  -- 关系: UserRole N:1 User (关联用户)
  -- 用户ID,外键关联t_user表
  `user_id` BIGINT NOT NULL COMMENT '用户ID',

  -- 用户名(冗余字段,便于查询和展示)
  -- 减少JOIN查询,提高查询性能
  `username` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '用户名(冗余字段)',

  -- ========== 关联角色信息 ==========
  -- 关系: UserRole N:1 Role (关联角色)
  -- 角色ID,外键关联t_role表
  `role_id` BIGINT NOT NULL COMMENT '角色ID',

  -- 角色代码(冗余字段,便于查询和展示)
  -- 减少JOIN查询,UserRoleService.getRoleCodesByUserId()直接查询此字段
  `role_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '角色代码(冗余字段)',

  -- ========== 主角色标识 ==========
  -- 是否主角色: 0-非主角色, 1-主角色
  -- 用户可能有多个角色,主角色用于数据权限判断
  -- 数据权限以主角色的data_scope_type为准
  -- 默认0表示非主角色,避免使用NULL防止索引失效和简化查询逻辑
  `is_primary` TINYINT NOT NULL DEFAULT 0 COMMENT '是否主角色:0-否,1-是',

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
  -- 用户ID+角色ID唯一约束,保证同一用户不会重复关联同一角色
  -- 注意: 考虑软删除,避免同一用户重复关联同一角色
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`, `is_deleted`) COMMENT '用户角色关联唯一约束',

  -- ========== 索引设计 ==========
  -- 索引设计原则: 只保留核心查询场景的索引,避免过度索引导致写入性能下降
  -- InnoDB联合索引遵循最左前缀原则,单字段索引被联合索引覆盖时需删除以避免冗余

  -- 用户ID+软删除联合索引,用于查询用户的所有角色(高频场景: 用户权限验证)
  -- 覆盖场景: 按用户ID查询角色、按用户ID+删除标记查询角色
  KEY `idx_user_role_user` (`user_id`, `is_deleted`) COMMENT '用户ID索引',

  -- 角色ID+软删除联合索引,用于查询角色的所有用户(统计场景: 查询某个角色的所有用户)
  KEY `idx_user_role_role` (`role_id`, `is_deleted`) COMMENT '角色ID索引',

  -- 用户ID+主角色标识+软删除联合索引,用于查询用户的主角色(高频场景: 数据权限判断)
  KEY `idx_user_role_primary` (`user_id`, `is_primary`, `is_deleted`) COMMENT '主角色标识索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='用户角色关联表: 用户角色关联中间表,实现用户与角色的N:M关联。'
  '【关联关系】每个关联记录关联一个用户和一个角色。一个用户可以有多个角色,一个角色可以分配给多个用户。'
  '【主角色】is_primary字段标识用户的主角色,用于数据权限判断。数据权限以主角色的data_scope_type为准。'
  '【冗余字段】用户名、角色代码作为冗余字段,便于查询和展示,避免频繁关联查询用户表和角色表。';

-- ============================================================
-- 索引使用说明
-- ============================================================

-- 1. 查询用户的所有角色
-- SELECT ur.*, r.role_name, r.data_scope_type
-- FROM t_user_role ur
-- LEFT JOIN t_role r ON ur.role_id = r.id AND r.is_deleted = 0
-- WHERE ur.user_id = ? AND ur.is_deleted = 0
-- ORDER BY ur.is_primary DESC, ur.id;

-- 2. 查询用户的角色代码列表(不JOIN,直接查冗余字段)
-- SELECT role_code FROM t_user_role WHERE user_id = ? AND is_deleted = 0;

-- 3. 查询用户的主角色
-- SELECT ur.*, r.role_name, r.data_scope_type
-- FROM t_user_role ur
-- LEFT JOIN t_role r ON ur.role_id = r.id AND r.is_deleted = 0
-- WHERE ur.user_id = ? AND ur.is_primary = 1 AND ur.is_deleted = 0;

-- 4. 查询角色下的所有用户
-- SELECT ur.*, u.username, u.real_name
-- FROM t_user_role ur
-- LEFT JOIN t_user u ON ur.user_id = u.id AND u.is_deleted = 0
-- WHERE ur.role_id = ? AND ur.is_deleted = 0;

-- 5. 为用户分配角色(设置主角色)
-- INSERT INTO t_user_role (user_id, username, role_id, role_code, is_primary, created_by, updated_by)
-- VALUES (?, ?, ?, ?, 1, ?, ?);

-- 6. 为用户追加角色(非主角色)
-- INSERT INTO t_user_role (user_id, username, role_id, role_code, is_primary, created_by, updated_by)
-- VALUES (?, ?, ?, ?, 0, ?, ?);

-- 7. 撤销用户角色(软删除)
-- UPDATE t_user_role SET is_deleted = 1, updated_by = ? WHERE user_id = ? AND role_id = ?;

-- 8. 切换用户主角色
-- UPDATE t_user_role SET is_primary = 0 WHERE user_id = ? AND is_deleted = 0;
-- UPDATE t_user_role SET is_primary = 1 WHERE user_id = ? AND role_id = ? AND is_deleted = 0;

-- ============================================================
-- 主角色说明
-- ============================================================

-- 主角色(is_primary=1)的作用:
-- 1. 数据权限判断: 用户的data_scope_type以主角色的data_scope_type为准
-- 2. 展示优先级: 用户列表展示时,优先展示主角色
-- 3. 业务语义: 主角色代表用户的主要职责,其他角色为辅助角色

-- 主角色的业务规则:
-- - 每个用户应该有且仅有一个主角色
-- - 分配第一个角色时,自动设置为主角色
-- - 可以手动切换主角色
-- - 撤销主角色时,需要先指定新的主角色

-- ============================================================
-- 冗余字段说明
-- ============================================================

-- username(用户名冗余字段):
-- - 作用: 便于查询和展示,避免JOIN t_user表
-- - 维护: 用户名修改时,需要同步更新此字段
-- - 查询: UserRoleService.getRoleCodesByUserId()可以直接查询role_code,无需JOIN

-- role_code(角色代码冗余字段):
-- - 作用: 便于查询和展示,避免JOIN t_role表
-- - 维护: 角色代码不应该修改(唯一标识),无需同步更新
-- - 查询: 可以直接获取角色代码列表,提高查询性能

-- 冗余字段的设计原则:
-- - 冗余不修改的值(如role_code): 非常安全,无需担心数据不一致
-- - 冗余可能修改的值(如username): 需要在修改时同步更新
-- - 权衡空间换时间,提高查询性能
