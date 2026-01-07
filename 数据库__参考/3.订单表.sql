-- ============================================================
-- 订单表 (t_order)
-- 说明:销售订单主表,包含订单基本信息、状态、客户关联等核心数据
-- ============================================================

CREATE TABLE `t_order` (
  -- ========== 主键 ==========
  -- 订单ID,主键,自增。每条订单记录的唯一标识
  -- 注意:目前使用自增ID,若使用分布式架构应改为雪花ID
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',

  -- ========== 订单基本信息 ==========
  -- 订单号,业务唯一标识,如:CUST001-20241217-0001
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
  -- 订单标题/名称
  `order_title` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '订单标题',
  -- 订单类型
  -- TODO:还需具体确定订单类型
  `order_type` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '订单类型',

  -- ========== 关联客户信息 ==========
  -- 客户ID,外键关联t_customer表
  `customer_id` BIGINT NOT NULL COMMENT '客户ID',
  -- 客户名称(冗余字段,便于查询和展示)
  `customer_name` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '客户名称',
  -- 客户编码(冗余字段)
  `customer_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '客户编码',

  -- ========== 订单金额信息 ==========
  -- 订单总金额(精确到分),所有订单行的金额总和
  `total_amount` DECIMAL(18, 2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
  -- 币种,如:CNY(人民币)、USD(美元)
  `currency` VARCHAR(10) NOT NULL DEFAULT 'CNY' COMMENT '币种',

  -- ========== 订单状态信息 ==========
  -- 【状态机管理设计】订单状态关联状态字典表,实现状态统一管理
  -- 关系:Order N:1 StatusDict (订单关联状态字典)
  -- 订单状态字典ID,外键关联t_status_dict表。必须关联状态字典,保证状态定义统一
  -- 注意:默认值需要在业务层初始化时设置,关联到状态字典表中status_code='DRAFT'且business_type='order'的记录
  `status_dict_id` BIGINT NOT NULL COMMENT '订单状态字典ID',
  -- 订单状态代码(冗余字段,便于查询和展示)。从StatusDict表中同步,避免频繁JOIN查询
  -- 注意:此字段应与status_dict_id关联的状态字典记录中的status_code保持一致
  -- 默认值:DRAFT(草稿),草稿订单仅录入基本信息,未最终确认下单,避免使用NULL防止索引失效和简化查询逻辑
  `status_code` VARCHAR(50) NOT NULL DEFAULT 'DRAFT' COMMENT '订单状态代码(冗余字段):默认DRAFT表示草稿状态',
  -- 订单状态名称(冗余字段,便于展示)。从StatusDict表中同步,避免频繁JOIN查询
  -- 注意:此字段应与status_dict_id关联的状态字典记录中的status_name保持一致
  -- 默认值:草稿,与status_code的默认值DRAFT对应
  `status_name` VARCHAR(50) NOT NULL DEFAULT '草稿' COMMENT '订单状态名称(冗余字段):默认草稿',

  -- ========== 时间信息 ==========
  -- 订单日期,客户下单的日期
  -- 字段规则:草稿订单(DRAFT)可为NULL,执行中及后续状态必须填写
  -- 业务层需校验:status_code != 'DRAFT'时,order_date必填
  -- 使用NULL表示草稿订单未确定订单日期,必须区分"未确定"和"已确定为空"的业务含义,因此允许使用NULL
  `order_date` DATE NULL DEFAULT NULL COMMENT '订单日期:NULL表示草稿订单未确定订单日期',
  -- 期望交货日期
  -- 使用NULL表示未设置期望交货日期,必须区分"未设置"和"已设置为空"的业务含义,因此允许使用NULL
  `expected_delivery_date` DATE NULL DEFAULT NULL COMMENT '期望交货日期:NULL表示未设置期望交货日期',
  -- 实际交货日期
  -- 使用NULL表示未完成交货,必须区分"未完成"和"已完成但日期为空"的业务含义,因此允许使用NULL
  `actual_delivery_date` DATE NULL DEFAULT NULL COMMENT '实际交货日期:NULL表示未完成交货',

  -- ========== 备注信息 ==========
  -- 订单备注
  `remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '订单备注',
  -- 内部备注(不对外展示)
  `internal_remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '内部备注',

  -- ========== 扩展信息 ==========
  -- 扩展信息,JSON格式存储扩展字段,便于扩展且无需修改表结构
  -- 注意:仅支持MySQL 5.7+版本
  -- 使用NULL表示无扩展信息,避免空JSON对象占用存储空间,且NULL可以明确区分"无扩展信息"和"空扩展信息"
  `extra_info` JSON DEFAULT NULL COMMENT '扩展信息:NULL表示无扩展信息',

  -- ========== 公共字段 ==========
  -- 创建时间(精确到毫秒),记录订单创建的时间
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  -- 创建人ID,记录订单的创建人,外键关联t_user表。-1表示系统自动操作,0及以上表示用户ID
  -- 注意:此为订单创建人,与状态日志表中的操作人不同
  -- 默认-1表示系统创建,避免使用NULL防止空指针问题和简化查询逻辑
  `created_by` BIGINT NOT NULL DEFAULT -1 COMMENT '创建人ID:默认-1表示系统创建',
  -- 更新时间(精确到毫秒),记录订单信息最后更新的时间
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  -- 更新人ID,记录订单信息的最后更新人,外键关联t_user表。-1表示系统自动操作,0及以上表示用户ID
  -- 注意:此为订单更新人,状态变更操作人记录在状态日志表中
  -- 默认-1表示系统更新,避免使用NULL防止空指针问题和简化查询逻辑
  `updated_by` BIGINT NOT NULL DEFAULT -1 COMMENT '更新人ID:默认-1表示系统更新',
  -- 是否删除:0-未删除,1-已删除
  -- 默认0表示未删除,避免使用NULL防止索引失效和简化查询逻辑
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除,1-已删除',

  PRIMARY KEY (`id`),

  -- ========== 外键约束 ==========
  -- 关联状态字典表,保证状态定义统一
  -- 注意:外键约束可选择性启用,取决于业务需求。启用外键可保证数据一致性,但可能影响性能
  -- CONSTRAINT `fk_order_status_dict` FOREIGN KEY (`status_dict_id`) REFERENCES `t_status_dict` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,

  -- ========== 约束设计 ==========

  -- ========== 索引设计 ==========
  -- 索引设计原则:只保留核心查询场景的索引,避免过度索引导致写入性能下降
  -- InnoDB联合索引遵循最左前缀原则,单字段索引被联合索引覆盖时需删除以避免冗余

  -- 订单号唯一索引
  UNIQUE KEY `uk_order_order_no` (`order_no`) COMMENT '订单号唯一索引',

  -- 客户+状态+软删除联合索引,用于查询客户的特定状态订单(覆盖客户ID和状态单字段查询)
  -- 注意:使用status_code冗余字段进行索引,避免JOIN查询状态字典表
  KEY `idx_order_customer_status` (`customer_id`, `status_code`, `is_deleted`) COMMENT '客户状态联合索引',

  -- 订单日期+状态+软删除联合索引,用于按日期和状态查询订单(高频场景:如"2024-12月的执行中订单")
  -- 覆盖场景:按订单日期查询、按订单日期+状态查询、按订单日期+状态+删除标记查询
  -- 注意:使用status_code冗余字段进行索引,避免JOIN查询状态字典表
  KEY `idx_order_order_date_status` (`order_date`, `status_code`, `is_deleted`) COMMENT '订单日期状态联合索引',

  -- 创建时间+状态+软删除联合索引,用于时间排序和统计查询(覆盖创建时间单字段查询)
  -- 注意:使用status_code冗余字段进行索引,避免JOIN查询状态字典表
  KEY `idx_order_created_status` (`created_at`, `status_code`, `is_deleted`) COMMENT '创建时间状态联合索引',
  
  -- 状态字典ID索引,用于状态机管理和状态统计查询
  -- 查询场景:查询某个状态的所有订单,用于状态流转分析和统计
  KEY `idx_order_status_dict_id` (`status_dict_id`, `is_deleted`) COMMENT '状态字典ID索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='订单表:销售订单主表,包含订单基本信息、状态、客户关联、金额等核心数据。'
  '【状态机管理】本表通过status_dict_id关联t_status_dict表,实现状态统一管理。'
  'status_code和status_name字段作为冗余字段便于查询和展示,应与状态字典表中的对应字段保持一致。';
