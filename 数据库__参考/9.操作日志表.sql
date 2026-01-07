-- ============================================================
-- 操作日志表 (t_operation_log)
-- 说明:操作日志表,记录用户对业务实体的操作,用于审计追溯
-- 关系:OperationLog N:1 User (操作日志关联用户)
-- 关系:OperationLog 多态关联业务实体 (通过business_type + business_id实现)
-- ============================================================

CREATE TABLE `t_operation_log` (
  -- ========== 主键 ==========
  -- 操作日志ID,主键,自增。每条操作日志记录的唯一标识
  -- 注意:目前使用自增ID,若使用分布式架构应改为雪花ID
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '操作日志ID',

  -- ========== 关联用户信息 ==========
  -- 关系:OperationLog N:1 User (操作日志关联用户)
  -- 操作人ID,外键关联t_user表。记录执行操作的用户。-1表示系统自动操作,0及以上表示用户ID
  -- 默认-1表示系统操作,避免使用NULL防止空指针问题和简化查询逻辑
  `operator_id` BIGINT NOT NULL DEFAULT -1 COMMENT '操作人ID:默认-1表示系统操作',
  -- 操作人姓名(冗余字段,便于查询和展示)
  `operator_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作人姓名(冗余字段)',
  -- 操作人角色(冗余字段,便于查询和展示)。示例:客户经理、采购专员、运营专员、系统管理员
  `operator_role` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作人角色(冗余字段)',
  -- 操作人部门(冗余字段,便于查询和展示)
  `operator_department` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '操作人部门(冗余字段)',

  -- ========== 多态关联业务实体 ==========
  -- 【多态关联设计】通过business_type + business_id实现多态关联,支持关联多种业务实体
  -- 业务类型,标识该操作关联的业务实体类型
  -- 可选值:order(订单)、order_line(订单行)、shipment(发运计划)、shipment_line(快递单)、supplier(供应商)、carrier(承运商)、customer(客户)、exception(异常记录)、attachment(附件)等
  `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型:order/order_line/shipment/shipment_line/supplier/carrier/customer/exception/attachment等',
  -- 业务ID,关联业务实体的主键ID
  -- 示例:business_type='order', business_id=1001 表示操作关联订单ID=1001(Order表的id字段)
  -- 示例:business_type='supplier', business_id=2001 表示操作关联供应商ID=2001(Supplier表的id字段)
  `business_id` BIGINT NOT NULL COMMENT '业务ID:关联业务实体的主键ID',

  -- ========== 业务实体冗余信息(便于查询和展示) ==========
  -- 业务实体编号(冗余字段,便于查询和展示)
  -- 示例:订单号、发运计划号、供应商编码、承运商编码、客户编码等
  `business_no` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '业务实体编号(冗余字段)',
  -- 业务实体名称(冗余字段,便于展示)
  -- 示例:订单标题、供应商名称、承运商名称、客户名称等
  `business_name` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '业务实体名称(冗余字段)',

  -- ========== 操作基本信息 ==========
  -- 操作类型,标识本次操作的类型。示例:CREATE(创建)、UPDATE(更新)、DELETE(删除)、VIEW(查看)、EXPORT(导出)、IMPORT(导入)、AUDIT(审核)、CONFIRM(确认)、APPROVE(审批)、CANCEL(取消)等
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型',
  -- 操作模块,标识操作所属的功能模块。示例:ORDER(订单管理)、PARTNER(合作方管理)、SHIPMENT(发运管理)、RECEIPT(签收管理)、ATTACHMENT(附件管理)、SYSTEM(系统管理)等
  `operation_module` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作模块',
  -- 操作动作,详细描述操作动作。示例:创建订单、更新订单状态、删除订单行、审核供应商资质等
  `operation_action` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '操作动作',
  -- 操作描述,详细说明本次操作的内容和结果
  `operation_description` TEXT NOT NULL COMMENT '操作描述',
  -- 操作结果:SUCCESS(成功)、FAILED(失败)、PARTIAL(部分成功)
  -- 默认SUCCESS表示成功,避免使用NULL防止索引失效和简化查询逻辑
  `operation_result` VARCHAR(50) NOT NULL DEFAULT 'SUCCESS' COMMENT '操作结果:默认SUCCESS表示成功',
  -- 操作结果描述,详细说明操作结果
  `result_description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '操作结果描述',

  -- ========== 操作前后数据快照 ==========
  -- 操作前数据快照,JSON格式存储操作前的数据状态,便于追溯和回滚
  -- 示例:{"order_status": "DRAFT", "total_amount": 10000.00}
  -- 注意:使用NULL表示无操作前数据,避免空JSON对象占用存储空间,且NULL可以明确区分"无数据"和"空数据"
  `before_data` JSON DEFAULT NULL COMMENT '操作前数据快照:NULL表示无操作前数据',
  -- 操作后数据快照,JSON格式存储操作后的数据状态,便于追溯
  -- 示例:{"order_status": "EXECUTING", "total_amount": 12000.00}
  -- 注意:使用NULL表示无操作后数据,避免空JSON对象占用存储空间,且NULL可以明确区分"无数据"和"空数据"
  `after_data` JSON DEFAULT NULL COMMENT '操作后数据快照:NULL表示无操作后数据',

  -- ========== 操作环境信息 ==========
  -- 操作IP地址,记录操作时的IP地址,便于安全审计
  `operation_ip` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作IP地址',
  -- 操作设备,记录操作设备信息。示例:PC、Mobile、Tablet等
  `operation_device` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作设备',
  -- 操作浏览器,记录操作浏览器信息。示例:Chrome、Firefox、Safari等
  `operation_browser` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '操作浏览器',
  -- 操作来源,记录操作来源。示例:WEB(网页)、MOBILE(移动端)、API(接口)、SYSTEM(系统自动)等
  `operation_source` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作来源',
  -- 请求路径,记录操作的API路径或页面路径
  `request_path` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '请求路径',
  -- 请求方法,记录HTTP请求方法。示例:GET、POST、PUT、DELETE等
  `request_method` VARCHAR(10) NOT NULL DEFAULT '' COMMENT '请求方法',

  -- ========== 操作时间信息 ==========
  -- 操作时间(精确到毫秒),记录操作发生的实际时间
  `operation_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '操作时间',
  -- 操作耗时(毫秒),记录操作的耗时,便于性能分析
  -- 使用NULL表示未计算操作耗时,必须区分"未计算"和"已计算为0"的业务含义,因此允许使用NULL
  `operation_duration` INT NULL DEFAULT NULL COMMENT '操作耗时(毫秒):NULL表示未计算操作耗时',

  -- ========== 扩展信息 ==========
  -- 扩展信息,JSON格式存储扩展字段,便于扩展且无需修改表结构
  -- 注意:仅支持MySQL 5.7+版本
  -- 使用NULL表示无扩展信息,避免空JSON对象占用存储空间,且NULL可以明确区分"无扩展信息"和"空扩展信息"
  `extra_info` JSON DEFAULT NULL COMMENT '扩展信息:NULL表示无扩展信息',

  -- ========== 公共字段 ==========
  -- 创建时间(精确到毫秒),记录操作日志创建的时间
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  -- 是否删除:0-未删除,1-已删除
  -- 默认0表示未删除,避免使用NULL防止索引失效和简化查询逻辑
  -- 注意:操作日志通常不进行软删除,保留完整的审计记录
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除,1-已删除',

  PRIMARY KEY (`id`),

  -- ========== 外键约束 ==========
  -- 注意:多态关联无法设置外键约束,业务层需保证business_id的有效性
  -- 注意:外键约束可选择性启用,取决于业务需求。启用外键可保证数据一致性,但可能影响性能

  -- ========== 约束设计 ==========

  -- ========== 索引设计 ==========
  -- 索引设计原则:只保留核心查询场景的索引,避免过度索引导致写入性能下降
  -- InnoDB联合索引遵循最左前缀原则,单字段索引被联合索引覆盖时需删除以避免冗余

  -- 操作人ID+操作时间+软删除联合索引,用于查询用户的操作历史(高频场景:用户操作历史查询)
  -- 覆盖场景:按操作人ID查询、按操作人ID+操作时间查询
  KEY `idx_operation_log_operator_time` (`operator_id`, `operation_time`, `is_deleted`) COMMENT '操作人时间联合索引',

  -- 业务类型+业务ID+操作时间+软删除联合索引,用于查询业务实体的操作历史(高频场景:订单操作历史查询)
  -- 覆盖场景:按业务类型+业务ID查询、按业务类型+业务ID+操作时间查询
  KEY `idx_operation_log_business_time` (`business_type`, `business_id`, `operation_time`, `is_deleted`) COMMENT '业务实体时间联合索引',

  -- 操作类型+操作模块+操作时间+软删除联合索引,用于按操作类型和模块查询(统计场景:操作统计分析)
  KEY `idx_operation_log_type_module` (`operation_type`, `operation_module`, `operation_time`, `is_deleted`) COMMENT '操作类型模块联合索引',

  -- 操作时间+操作结果+软删除联合索引,用于按时间查询和统计(统计场景:操作成功率统计)
  KEY `idx_operation_log_time_result` (`operation_time`, `operation_result`, `is_deleted`) COMMENT '操作时间结果联合索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='操作日志表:操作日志表,记录用户对业务实体的操作,用于审计追溯。'
  '【多态关联设计】本表通过business_type + business_id实现多态关联,支持关联订单、订单行、发运计划、快递单、供应商、承运商、客户、异常记录、附件等多种业务实体。'
  'business_type字段标识关联的业务实体类型,business_id字段存储关联业务实体的主键ID。'
  '【数据快照】操作前数据快照(before_data)和操作后数据快照(after_data)以JSON格式存储,便于追溯数据变更历史和回滚操作。'
  '【审计追溯】记录操作人、操作时间、操作IP、操作设备等完整信息,满足合规审计要求。'
  '【业务实体冗余信息】business_no、business_name字段作为冗余字段,便于查询和展示,避免频繁关联查询业务实体表。';

