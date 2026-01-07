-- ============================================================
-- 发运计划表 (t_shipment)
-- 说明:发运计划主表,运营人员设计的发运计划,审核通过后便可以发货
-- 保留此表便于审查和记录查看,记录发运计划级别的信息
-- 注意:本表记录的是发运计划,不包含具体的承运商、收货方、物流单号等信息
-- 这些信息属于快递单级别,记录在发运明细表(t_shipment_line)中
-- ============================================================

CREATE TABLE `t_shipment` (
  -- ========== 主键 ==========
  -- 发运计划ID,主键,自增。每条发运计划记录的唯一标识
  -- 注意:目前使用自增ID,若使用分布式架构应改为雪花ID
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '发运计划ID',

  -- ========== 发运计划基本信息 ==========
  -- 发运计划号,业务唯一标识,如:SHIP-PLAN-20241217-0001
  -- 用于标识运营人员制定的发运计划,便于审查和记录查看
  `shipment_no` VARCHAR(50) NOT NULL COMMENT '发运计划号',

  -- ========== 关联订单信息 ==========
  -- 订单ID,外键关联t_order表。一个订单可以有多个发运计划
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  -- 订单号(冗余字段,便于查询和展示)
  `order_no` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '订单号(冗余字段)',

  -- ========== 发运计划状态信息 ==========
  -- 【状态机管理设计】发运计划状态关联状态字典表,实现状态统一管理
  -- 关系:Shipment N:1 StatusDict (发运计划关联状态字典)
  -- 发运计划状态字典ID,外键关联t_status_dict表。必须关联状态字典,保证状态定义统一
  -- 注意:默认值需要在业务层初始化时设置,关联到状态字典表中status_code='DRAFT'且business_type='shipment'的记录
  -- 状态含义:草稿(待审核) -> 已审核 -> 已执行 -> 已完成
  `status_dict_id` BIGINT NOT NULL COMMENT '发运计划状态字典ID',
  -- 发运计划状态代码(冗余字段,便于查询和展示)。从StatusDict表中同步,避免频繁JOIN查询
  -- 注意:此字段应与status_dict_id关联的状态字典记录中的status_code保持一致
  -- 默认值:DRAFT(草稿),发运计划已创建但未审核,避免使用NULL防止索引失效和简化查询逻辑
  `status_code` VARCHAR(50) NOT NULL DEFAULT 'DRAFT' COMMENT '发运计划状态代码(冗余字段):默认DRAFT表示草稿状态',

  -- ========== 备注信息 ==========
  -- 发运计划备注
  `remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '发运计划备注',
  -- 内部备注(不对外展示)
  `internal_remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '内部备注',

  -- ========== 扩展信息 ==========
  -- 扩展信息,JSON格式存储扩展字段,便于扩展且无需修改表结构
  -- 注意:仅支持MySQL 5.7+版本
  -- 使用NULL表示无扩展信息,避免空JSON对象占用存储空间,且NULL可以明确区分"无扩展信息"和"空扩展信息"
  `extra_info` JSON DEFAULT NULL COMMENT '扩展信息:NULL表示无扩展信息',

  -- ========== 公共字段 ==========
  -- 创建时间(精确到毫秒),记录发运计划创建的时间
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  -- 创建人ID,记录发运计划的创建人,外键关联t_user表。-1表示系统自动操作,0及以上表示用户ID
  -- 默认-1表示系统创建,避免使用NULL防止空指针问题和简化查询逻辑
  `created_by` BIGINT NOT NULL DEFAULT -1 COMMENT '创建人ID:默认-1表示系统创建',
  -- 更新时间(精确到毫秒),记录发运计划信息最后更新的时间
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  -- 更新人ID,记录发运计划信息的最后更新人,外键关联t_user表。-1表示系统自动操作,0及以上表示用户ID
  -- 默认-1表示系统更新,避免使用NULL防止空指针问题和简化查询逻辑
  `updated_by` BIGINT NOT NULL DEFAULT -1 COMMENT '更新人ID:默认-1表示系统更新',
  -- 是否删除:0-未删除,1-已删除
  -- 默认0表示未删除,避免使用NULL防止索引失效和简化查询逻辑
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除,1-已删除',

  PRIMARY KEY (`id`),

  -- ========== 外键约束 ==========
  -- 关联订单表、状态字典表,保证数据一致性
  -- 注意:外键约束可选择性启用,取决于业务需求。启用外键可保证数据一致性,但可能影响性能
  -- CONSTRAINT `fk_shipment_order` FOREIGN KEY (`order_id`) REFERENCES `t_order` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  -- CONSTRAINT `fk_shipment_status_dict` FOREIGN KEY (`status_dict_id`) REFERENCES `t_status_dict` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,

  -- ========== 约束设计 ==========

  -- ========== 索引设计 ==========
  -- 索引设计原则:只保留核心高频查询场景的索引,避免过度索引导致写入性能下降
  -- InnoDB联合索引遵循最左前缀原则,单字段索引被联合索引覆盖时需删除以避免冗余

  -- 发运计划号唯一索引
  UNIQUE KEY `uk_shipment_shipment_no` (`shipment_no`) COMMENT '发运计划号唯一索引',

  -- 订单ID+状态+软删除联合索引,用于查询订单的发运计划(高频场景:查询订单的发运计划)
  -- 注意:使用status_code冗余字段进行索引,避免JOIN查询状态字典表
  KEY `idx_shipment_order_status` (`order_id`, `status_code`, `is_deleted`) COMMENT '订单状态联合索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='发运计划表:发运计划主表,运营人员设计的发运计划,审核通过后便可以发货。'
  '【业务说明】本表记录的是发运计划级别的信息,用于运营人员制定发运方案、审核和记录查看。'
  '发运计划审核通过后,会创建具体的快递单(发运明细表t_shipment_line),每个快递单包含承运商、收货方、物流单号等具体信息。'
  '【关联关系】每个发运计划关联一个订单。发运计划可以被多个快递单(ShipmentLine)关联,'
  '支持一个发运计划包含多个快递单。发运计划可以关联多个状态日志(ShipmentStatusLog)记录状态变更历史。'
  '【状态机管理】本表通过status_dict_id关联t_status_dict表,实现状态统一管理。status_code字段作为冗余字段便于查询,'
  '应与状态字典表中的status_code保持一致。状态流转:草稿(待审核) -> 已审核 -> 已执行 -> 已完成。'
  '【多态关联】发运计划可以通过business_type=''shipment''+business_id关联附件(Attachment)和异常记录(Exception)。';

