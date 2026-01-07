-- ============================================================
-- 用户表 (t_user)
-- 说明:系统用户表,记录用户基本信息、账号信息、状态等
-- 关系:User N:M Role (通过t_user_role中间表关联)
-- 关系:User 1:N OperationLog (用户关联操作日志)
-- ============================================================

CREATE TABLE `t_user` (
  -- ========== 主键 ==========
  -- 用户ID,主键,自增。每条用户记录的唯一标识
  -- 注意:目前使用自增ID,若使用分布式架构应改为雪花ID
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',

  -- ========== 账号信息 ==========
  -- 用户名,登录账号,唯一标识。如:admin、zhangsan等
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  -- 密码,加密后的密码。使用BCrypt、Argon2等加密算法
  `password` VARCHAR(255) NOT NULL COMMENT '密码(加密)',
  -- 用户编号,业务唯一标识,如:USER001
  -- 默认空字符串表示未设置用户编号,避免使用NULL防止索引失效和简化查询逻辑
  `user_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '用户编号',
  -- 邮箱,可用于登录和找回密码
  -- 默认空字符串表示未设置邮箱,避免使用NULL防止索引失效和简化查询逻辑
  `email` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '邮箱',
  -- 手机号,可用于登录和找回密码。格式:可选+号开头,7-20位数字(如:13800138000、+8613800138000)
  -- 默认空字符串表示未设置手机号,避免使用NULL防止索引失效和简化查询逻辑
  `phone` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '手机号',
  -- 是否启用:0-禁用,1-启用
  -- 默认1表示启用,避免使用NULL防止索引失效和简化查询逻辑
  `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用:0-禁用,1-启用',
  -- 是否锁定:0-未锁定,1-已锁定(密码错误次数过多等)
  -- 默认0表示未锁定,避免使用NULL防止索引失效和简化查询逻辑
  `is_locked` TINYINT NOT NULL DEFAULT 0 COMMENT '是否锁定:0-未锁定,1-已锁定',
  -- 锁定时间(精确到毫秒),记录用户被锁定的时间
  -- 使用NULL表示未锁定,必须区分"未锁定"和"已锁定但时间为空"的业务含义,因此允许使用NULL
  `locked_time` DATETIME(3) NULL DEFAULT NULL COMMENT '锁定时间:NULL表示未锁定',
  -- 锁定原因,说明用户被锁定的原因
  `locked_reason` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '锁定原因',

  -- ========== 基本信息 ==========
  -- 真实姓名,用户的真实姓名
  `real_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '真实姓名',
  -- 昵称,用户的显示名称
  `nickname` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '昵称',
  -- 头像URL,用户头像的访问地址
  -- 使用NULL表示无头像,必须区分"无头像"和"已设置但URL为空"的业务含义,因此允许使用NULL
  `avatar_url` VARCHAR(500) NULL DEFAULT NULL COMMENT '头像URL:NULL表示无头像',
  -- 性别:UNKNOWN(未知)、MALE(男)、FEMALE(女)
  -- 默认空字符串表示未设置性别,避免使用NULL防止索引失效和简化查询逻辑
  `gender` VARCHAR(10) NOT NULL DEFAULT '' COMMENT '性别',
  -- 生日
  -- 使用NULL表示未设置生日,必须区分"未设置"和"已设置为空"的业务含义,因此允许使用NULL
  `birthday` DATE NULL DEFAULT NULL COMMENT '生日:NULL表示未设置生日',
  -- 身份证号,用于身份验证
  -- 默认空字符串表示未设置身份证号,避免使用NULL防止索引失效和简化查询逻辑
  `id_card` VARCHAR(18) NOT NULL DEFAULT '' COMMENT '身份证号',

  -- ========== 组织信息 ==========
  -- 部门ID,外键关联部门表(如果存在)。-1表示未分配部门,0及以上表示部门ID
  -- 默认-1表示未分配部门,避免使用NULL防止空指针问题和简化查询逻辑
  `department_id` BIGINT NOT NULL DEFAULT -1 COMMENT '部门ID:默认-1表示未分配部门',
  -- 部门名称(冗余字段,便于查询和展示)
  `department_name` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '部门名称(冗余字段)',
  -- 职位,用户的职位信息。示例:客户经理、采购专员、运营专员、数据管理员、系统管理员
  `position` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '职位',
  -- 工号,员工的工号
  `employee_no` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '工号',
  -- 业务角色类型,标识用户的主要业务角色。示例:CUSTOMER_MANAGER(客户经理)、PURCHASE_SPECIALIST(采购专员)、OPERATION_SPECIALIST(运营专员)、DATA_ADMIN(数据管理员)、SYSTEM_ADMIN(系统管理员)
  -- 默认空字符串表示未设置业务角色,避免使用NULL防止索引失效和简化查询逻辑
  `business_role_type` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '业务角色类型',

  -- ========== 登录信息 ==========
  -- 最后登录时间(精确到毫秒),记录用户最后一次登录的时间
  -- 使用NULL表示从未登录,必须区分"未登录"和"已登录但时间为空"的业务含义,因此允许使用NULL
  `last_login_time` DATETIME(3) NULL DEFAULT NULL COMMENT '最后登录时间:NULL表示从未登录',
  -- 最后登录IP,记录用户最后一次登录的IP地址
  `last_login_ip` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '最后登录IP',
  -- 登录次数,记录用户登录的总次数
  `login_count` INT NOT NULL DEFAULT 0 COMMENT '登录次数',
  -- 密码修改时间(精确到毫秒),记录用户最后一次修改密码的时间
  -- 使用NULL表示从未修改密码,必须区分"未修改"和"已修改但时间为空"的业务含义,因此允许使用NULL
  `password_changed_time` DATETIME(3) NULL DEFAULT NULL COMMENT '密码修改时间:NULL表示从未修改密码',
  -- 密码过期时间(精确到毫秒),记录密码的过期时间
  -- 使用NULL表示密码永不过期,必须区分"永不过期"和"已过期但时间为空"的业务含义,因此允许使用NULL
  `password_expire_time` DATETIME(3) NULL DEFAULT NULL COMMENT '密码过期时间:NULL表示密码永不过期',

  -- ========== 备注信息 ==========
  -- 用户备注
  `remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '用户备注',
  -- 内部备注(不对外展示)
  `internal_remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '内部备注',

  -- ========== 扩展信息 ==========
  -- 扩展信息,JSON格式存储扩展字段,便于扩展且无需修改表结构
  -- 注意:仅支持MySQL 5.7+版本
  -- 使用NULL表示无扩展信息,避免空JSON对象占用存储空间,且NULL可以明确区分"无扩展信息"和"空扩展信息"
  `extra_info` JSON DEFAULT NULL COMMENT '扩展信息:NULL表示无扩展信息',

  -- ========== 公共字段 ==========
  -- 创建时间(精确到毫秒),记录用户创建的时间
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  -- 创建人ID,记录用户的创建人,外键关联t_user表。-1表示系统自动创建,0及以上表示用户ID
  -- 默认-1表示系统创建,避免使用NULL防止空指针问题和简化查询逻辑
  `created_by` BIGINT NOT NULL DEFAULT -1 COMMENT '创建人ID:默认-1表示系统创建',
  -- 更新时间(精确到毫秒),记录用户信息最后更新的时间
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  -- 更新人ID,记录用户信息的最后更新人,外键关联t_user表。-1表示系统自动更新,0及以上表示用户ID
  -- 默认-1表示系统更新,避免使用NULL防止空指针问题和简化查询逻辑
  `updated_by` BIGINT NOT NULL DEFAULT -1 COMMENT '更新人ID:默认-1表示系统更新',
  -- 是否删除:0-未删除,1-已删除
  -- 默认0表示未删除,避免使用NULL防止索引失效和简化查询逻辑
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除,1-已删除',

  PRIMARY KEY (`id`),

  -- ========== 外键约束 ==========
  -- 注意:外键约束可选择性启用,取决于业务需求。启用外键可保证数据一致性,但可能影响性能
  -- 注意:部门表可能不存在,因此不设置外键约束

  -- ========== 约束设计 ==========
  -- 手机号格式约束(MySQL 8.0+),格式:可选+号开头,7-20位数字
  -- 允许空字符串或符合格式的字符串(如:13800138000、+8613800138000)
  -- 注意:MySQL 5.7及以下版本不支持CHECK约束,需在业务层使用正则校验(如:/^\+?[0-9]{7,20}$/)
  CONSTRAINT `ck_user_phone` CHECK (`phone` = '' OR `phone` REGEXP '^\\+?[0-9]{7,20}$'),

  -- ========== 索引设计 ==========
  -- 索引设计原则:只保留核心查询场景的索引,避免过度索引导致写入性能下降
  -- InnoDB联合索引遵循最左前缀原则,单字段索引被联合索引覆盖时需删除以避免冗余

  -- 用户名唯一索引(高频场景:用户登录验证)
  UNIQUE KEY `uk_user_username` (`username`, `is_deleted`) COMMENT '用户名唯一索引',

  -- 启用状态+锁定状态+软删除联合索引,用于查询可用的用户(高频场景:用户登录验证、用户列表查询)
  -- 覆盖场景:按启用状态查询、按启用状态+锁定状态查询
  KEY `idx_user_enabled_locked` (`is_enabled`, `is_locked`, `is_deleted`) COMMENT '启用锁定状态联合索引',

  -- 部门ID+启用状态+软删除联合索引,用于查询部门的用户(统计场景:查询部门的用户列表)
  KEY `idx_user_department_enabled` (`department_id`, `is_enabled`, `is_deleted`) COMMENT '部门启用状态联合索引',

  -- 创建时间+启用状态+软删除联合索引,用于时间排序和统计查询(覆盖创建时间单字段查询)
  KEY `idx_user_created_enabled` (`created_at`, `is_enabled`, `is_deleted`) COMMENT '创建时间启用状态联合索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='用户表:系统用户表,记录用户基本信息、账号信息、状态等。'
  '【关联关系】用户与角色通过t_user_role中间表实现N:M关联,一个用户可以有多个角色,一个角色可以分配给多个用户。'
  '【账号安全】密码使用加密算法存储,支持密码过期、账户锁定等安全机制。'
  '【登录统计】记录用户登录信息,包括最后登录时间、登录IP、登录次数等,便于安全审计。';

