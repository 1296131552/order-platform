-- ============================================================
-- 用户表 (t_user)
-- 说明: 系统用户表,记录用户基本信息、账号信息、组织信息、安全状态等
-- 关系: User N:M Role (通过t_user_role中间表关联)
-- ============================================================

CREATE TABLE `t_user` (
  -- ========== 主键 ==========
  -- 用户ID,主键,自增。每条用户记录的唯一标识
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',

  -- ========== 账号信息 ==========
  -- 用户名,登录账号,唯一标识。如:admin、zhangsan等
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  -- 密码,加密后的密码。使用BCrypt加密算法
  `password` VARCHAR(255) NOT NULL COMMENT '密码(加密)',
  -- 用户编号,业务唯一标识,如:USER001。与username区分,用于业务对接
  `user_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '用户编号',

  -- ========== 基本信息 ==========
  -- 真实姓名,用户的真实姓名
  `real_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '真实姓名',
  -- 邮箱,可用于登录和找回密码
  `email` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '邮箱',
  -- 手机号,可用于登录和找回密码。格式:可选+号开头,7-20位数字(如:13800138000、+8613800138000)
  `phone` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '手机号',
  -- 头像URL,用户头像的访问地址
  -- 使用NULL表示无头像,必须区分"无头像"和"已设置但URL为空"的业务含义,因此允许使用NULL
  `avatar` VARCHAR(500) NULL DEFAULT NULL COMMENT '头像URL',

  -- ========== 状态控制 ==========
  -- 是否启用 (存储: TINYINT 0/1, 映射: Boolean)
  `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  -- 是否锁定 (存储: TINYINT 0/1, 映射: Boolean)
  `is_locked` TINYINT NOT NULL DEFAULT 0 COMMENT '是否锁定',
  -- 锁定时间(精确到毫秒),记录用户被锁定的时间
  -- 使用NULL表示未锁定,必须区分"未锁定"和"已锁定但时间为空"的业务含义,因此允许使用NULL
  `locked_time` DATETIME(3) NULL DEFAULT NULL COMMENT '锁定时间',
  -- 锁定原因,说明用户被锁定的原因。如:密码错误次数过多、管理员手动锁定
  `locked_reason` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '锁定原因',

  -- ========== 登录信息 ==========
  -- 最后登录时间(精确到毫秒),记录用户最后一次登录的时间
  -- 使用NULL表示从未登录,必须区分"未登录"和"已登录但时间为空"的业务含义,因此允许使用NULL
  `last_login_time` DATETIME(3) NULL DEFAULT NULL COMMENT '最后登录时间',
  -- 最后登录IP,记录用户最后一次登录的IP地址,用于安全审计
  `last_login_ip` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '最后登录IP',
  -- 登录次数,记录用户登录的总次数,用于用户行为分析、活跃用户统计
  `login_count` INT NOT NULL DEFAULT 0 COMMENT '登录次数',

  -- ========== 密码管理 ==========
  -- 密码修改时间(精确到毫秒),记录用户最后一次修改密码的时间,用于密码策略管理
  -- 使用NULL表示从未修改密码,必须区分"未修改"和"已修改但时间为空"的业务含义,因此允许使用NULL
  `password_changed_time` DATETIME(3) NULL DEFAULT NULL COMMENT '密码修改时间',
  -- 密码过期时间(精确到毫秒),记录密码的过期时间,用于强制定期修改密码策略
  -- 使用NULL表示密码永不过期,必须区分"永不过期"和"已过期但时间为空"的业务含义,因此允许使用NULL
  `password_expire_time` DATETIME(3) NULL DEFAULT NULL COMMENT '密码过期时间',

  -- ========== 组织信息 ==========
  -- 部门ID,外键关联部门表(如果存在)。NULL表示未分配部门
  `department_id` BIGINT NULL DEFAULT NULL COMMENT '部门ID',
  -- 职位,用户的职位信息。示例:客户经理、采购专员、运营专员、数据管理员、系统管理员
  `position` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '职位',
  -- 工号,员工的工号,企业系统必备
  `employee_no` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '工号',

  -- ========== 备注信息 ==========
  -- 用户备注,用于记录用户的补充信息
  `remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '用户备注',

  -- ========== 公共字段 ==========
  -- 创建时间(精确到毫秒),记录用户创建的时间
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  -- 创建人ID,记录用户的创建人,外键关联t_user表。NULL表示系统自动创建
  `created_by` BIGINT NULL DEFAULT NULL COMMENT '创建人ID',
  -- 更新时间(精确到毫秒),记录用户信息最后更新的时间
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  -- 更新人ID,记录用户信息的最后更新人,外键关联t_user表。NULL表示系统自动更新
  `updated_by` BIGINT NULL DEFAULT NULL COMMENT '更新人ID',
  -- 是否删除 (存储: TINYINT 0/1, 映射: Boolean)
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',

  PRIMARY KEY (`id`),

  -- ========== 唯一约束 ==========
  -- 用户名唯一索引,保证用户名唯一性(高频场景:用户登录验证)
  -- 软删除策略: 删除时需要修改 username 加后缀 (如: admin_deleted_1705334400)
  -- 这样可以释放 username 供新用户使用,同时保留已删除用户的数据用于审计
  UNIQUE KEY `uk_user_username` (`username`) COMMENT '用户名唯一索引',

  -- ========== 索引设计 ==========
  -- 启用状态+锁定状态+软删除联合索引,用于查询可用的用户(高频场景:用户登录验证、用户列表查询)
  KEY `idx_user_enabled_locked` (`is_enabled`, `is_locked`, `is_deleted`) COMMENT '启用锁定状态联合索引',

  -- 部门ID+启用状态+软删除联合索引,用于查询部门的用户(高频场景:数据权限隔离、部门用户列表)
  KEY `idx_user_department` (`department_id`, `is_enabled`, `is_deleted`) COMMENT '部门启用状态联合索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='用户表:系统用户表,记录用户基本信息、账号信息、组织信息、安全状态等。';
