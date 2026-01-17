-- ============================================================
-- 用户角色关联表 (t_user_role)
-- 说明: 用户角色关联中间表,实现用户与角色的N:M关联
-- 关系: UserRole N:1 User (关联用户)
-- 关系: UserRole N:1 Role (关联角色)
-- 权限计算: 用户数据权限取所有角色中的"最宽松"权限（data_scope_type 最小值）
--          1=全部 > 2=部门 > 3=本人, 即 MIN(data_scope_type)
-- ============================================================

CREATE TABLE `t_user_role` (
  -- ========== 主键 ==========
  -- 关联ID,主键,自增
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',

  -- ========== 关联用户信息 ==========
  -- 关系: UserRole N:1 User (关联用户)
  -- 用户ID,外键关联t_user表
  `user_id` BIGINT NOT NULL COMMENT '用户ID',

  -- ========== 关联角色信息 ==========
  -- 关系: UserRole N:1 Role (关联角色)
  -- 角色ID,外键关联t_role表
  `role_id` BIGINT NOT NULL COMMENT '角色ID',

  -- ========== 主角色标识 ==========
  -- 是否主角色（存储: TINYINT 0/1, 映射: Boolean）
  -- 注意: 数据权限计算已改为取所有角色的最宽松权限，此字段保留用于界面展示
  `is_primary` TINYINT NOT NULL DEFAULT 0 COMMENT '是否主角色',

  -- ========== 公共字段 ==========
  -- 创建时间(精确到毫秒)
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

  -- 创建人ID,记录关联关系的创建人。NULL表示系统创建
  `created_by` BIGINT NULL DEFAULT NULL COMMENT '创建人ID',

  -- 更新时间(精确到毫秒)
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

  -- 更新人ID,记录关联关系的最后更新人。NULL表示系统更新
  `updated_by` BIGINT NULL DEFAULT NULL COMMENT '更新人ID',

  -- 是否删除 (存储: TINYINT 0/1, 映射: Boolean)
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',

  PRIMARY KEY (`id`),

  -- ========== 唯一约束 ==========
  -- 用户ID+角色ID唯一约束,保证同一用户不会重复关联同一角色
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`) COMMENT '用户角色关联唯一约束',

  -- ========== 索引设计 ==========
  -- 用户ID+软删除联合索引,用于查询用户的所有角色(高频场景: 用户权限验证)
  KEY `idx_user_role_user` (`user_id`, `is_deleted`) COMMENT '用户ID索引',

  -- 角色ID+软删除联合索引,用于查询角色的所有用户(统计场景: 查询某个角色的所有用户)
  KEY `idx_user_role_role` (`role_id`, `is_deleted`) COMMENT '角色ID索引',

  -- 用户ID+主角色标识+软删除联合索引,用于查询用户的主角色(高频场景: 数据权限判断)
  KEY `idx_user_role_primary` (`user_id`, `is_primary`, `is_deleted`) COMMENT '主角色标识索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='用户角色关联表: 用户角色关联中间表,实现用户与角色的N:M关联。';
