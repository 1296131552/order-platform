-- ============================================================
-- 操作日志表 (t_operation_log)
-- 说明: 操作日志表,记录用户对业务实体的操作,用于审计追溯
-- 设计: 混合存储方案 - 核心信息存 MySQL, 详细快照存对象存储
-- 关系: OperationLog N:1 User (操作日志关联用户)
-- 关系: OperationLog 多态关联业务实体 (通过 business_type + business_id 实现)
-- ============================================================

CREATE TABLE `t_operation_log` (
  -- ========== 主键 ==========
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '操作日志ID',

  -- ========== 操作人信息 ==========
  -- 操作人ID,-1表示系统自动操作,0及以上表示用户ID
  -- 默认-1表示系统操作,避免使用NULL防止空指针问题和简化查询逻辑
  `operator_id` BIGINT NOT NULL DEFAULT -1 COMMENT '操作人ID:默认-1表示系统操作',

  -- 操作人姓名(冗余字段,便于查询和展示)
  `operator_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作人姓名(冗余字段)',

  -- 操作人用户编号(冗余字段,便于查询和展示)
  -- user_code与username区别:username是登录账号可修改,user_code是业务编号不可变
  `operator_user_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作人用户编号(冗余字段)',

  -- 操作人工号(冗余字段,便于企业审计和统计)
  `operator_employee_no` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作人工号(冗余字段)',

  -- 操作人部门ID(冗余字段,便于按部门统计和权限审计)
  -- 默认-1表示未分配部门,避免使用NULL防止空指针问题和简化查询逻辑
  `operator_department_id` BIGINT NOT NULL DEFAULT -1 COMMENT '操作人部门ID:-1表示未分配部门',

  -- 操作人部门名称(冗余字段,便于查询和展示)
  `operator_department_name` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '操作人部门名称(冗余字段)',

  -- 操作人职位(冗余字段,便于职责审计)
  `operator_position` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作人职位(冗余字段)',

  -- ========== 多态关联业务实体 ==========
  -- 【多态关联设计】通过 business_type + business_id 实现多态关联
  -- 业务类型,标识该操作关联的业务实体类型
  -- 可选值: order、order_line、shipment、shipment_line、supplier、carrier、customer、exception、attachment、user、role 等
  `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型:order/order_line/shipment/supplier/carrier/customer/exception/attachment/user/role等',

  -- 业务ID,关联业务实体的主键ID
  -- 示例: business_type='order', business_id=1001 表示操作关联订单ID=1001
  `business_id` BIGINT NOT NULL COMMENT '业务ID:关联业务实体的主键ID',

  -- ========== 业务实体冗余信息 ==========
  -- 业务实体编号(冗余字段,便于查询和展示)
  -- 示例: 订单号、发运计划号、供应商编码等
  `business_no` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '业务实体编号(冗余字段)',

  -- ========== 操作基本信息 ==========
  -- 操作类型,标识本次操作的类型
  -- 可选值: CREATE(创建)、UPDATE(更新)、DELETE(删除)、VIEW(查看)、EXPORT(导出)、IMPORT(导入)、AUDIT(审核)、CONFIRM(确认)、APPROVE(审批)、CANCEL(取消)等
  `operation_type` VARCHAR(30) NOT NULL COMMENT '操作类型:CREATE/UPDATE/DELETE/VIEW/EXPORT/IMPORT/AUDIT/CONFIRM/APPROVE/CANCEL/LOGIN/LOGOUT',

  -- 操作模块,标识操作所属的功能模块
  -- 可选值: ORDER(订单管理)、PARTNER(合作方管理)、SHIPMENT(发运管理)、RECEIPT(签收管理)、ATTACHMENT(附件管理)、EXCEPTION(异常管理)、VISUALIZATION(可视化)、DASHBOARD(看板)、SYSTEM(系统管理)、USER(用户管理)等
  `operation_module` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作模块:ORDER/PARTNER/SHIPMENT/RECEIPT/ATTACHMENT/EXCEPTION/VISUALIZATION/DASHBOARD/SYSTEM/USER',

  -- 操作描述,详细描述本次操作的内容
  `operation_desc` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '操作描述',

  -- 操作结果: SUCCESS(成功)、FAILED(失败)、PARTIAL(部分成功)
  -- 默认SUCCESS表示成功,避免使用NULL防止索引失效和简化查询逻辑
  `operation_result` VARCHAR(30) NOT NULL DEFAULT 'SUCCESS' COMMENT '操作结果:SUCCESS/FAILED/PARTIAL',

  -- 操作结果描述,详细说明操作结果或失败原因
  `result_desc` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '操作结果描述',

  -- ========== 操作环境信息 ==========
  -- 操作IP地址,记录操作时的IP地址,便于安全审计
  `operation_ip` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作IP地址',

  -- 请求路径,记录操作的API路径
  `request_path` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '请求路径',

  -- 请求方法,记录HTTP请求方法
  -- 可选值: GET、POST、PUT、DELETE 等
  `request_method` VARCHAR(10) NOT NULL DEFAULT '' COMMENT '请求方法:GET/POST/PUT/DELETE',

  -- ========== 操作时间信息 ==========
  -- 操作时间(精确到毫秒),记录操作发生的实际时间
  `operation_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '操作时间',

  -- 操作耗时(毫秒),记录操作的耗时,便于性能分析
  -- 使用NULL表示未计算操作耗时,必须区分"未计算"和"已计算为0"的业务含义,因此允许使用NULL
  `operation_duration` INT NULL DEFAULT NULL COMMENT '操作耗时(毫秒):NULL表示未计算操作耗时',

  -- ========== 数据快照信息 ==========
  -- 数据快照文件Key,存储在对象存储(OSS/MinIO)中的文件路径
  -- 文件路径格式: logs/operation/{year}/{month}/{logId}.json
  -- 文件内容格式: {"before":{...}, "after":{...}}
  -- 使用NULL表示无数据快照或快照存储在 extra_info 字段
  `snapshot_key` VARCHAR(500) NULL DEFAULT NULL COMMENT '数据快照文件Key:NULL表示无快照或快照在extra_info中',

  -- ========== 扩展信息 ==========
  -- 扩展信息,JSON格式存储扩展字段,便于扩展且无需修改表结构
  -- 注意: 仅支持 MySQL 5.7+ 版本
  -- 使用场景: 存储操作前后的数据快照、设备信息、浏览器信息等
  -- 使用NULL表示无扩展信息,避免空JSON对象占用存储空间
  `extra_info` JSON DEFAULT NULL COMMENT '扩展信息:NULL表示无扩展信息',

  -- ========== 公共字段 ==========
  -- 是否删除: 0-未删除, 1-已删除
  -- 默认0表示未删除,避免使用NULL防止索引失效和简化查询逻辑
  -- 注意: 操作日志通常不进行软删除,保留完整的审计记录
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除,1-已删除',

  PRIMARY KEY (`id`),

  -- ========== 索引设计 ==========
  -- 索引设计原则: 只保留核心查询场景的索引,避免过度索引导致写入性能下降
  -- InnoDB联合索引遵循最左前缀原则,单字段索引被联合索引覆盖时需删除以避免冗余

  -- 操作人ID+操作时间+软删除联合索引,用于查询用户的操作历史(高频场景: 用户操作历史查询)
  -- 覆盖场景: 按操作人ID查询、按操作人ID+操作时间查询
  KEY `idx_operation_log_operator_time` (`operator_id`, `operation_time`, `is_deleted`) COMMENT '操作人时间联合索引',

  -- 业务类型+业务ID+操作时间+软删除联合索引,用于查询业务实体的操作历史(高频场景: 订单操作历史查询)
  -- 覆盖场景: 按业务类型+业务ID查询、按业务类型+业务ID+操作时间查询
  KEY `idx_operation_log_business_time` (`business_type`, `business_id`, `operation_time`, `is_deleted`) COMMENT '业务实体时间联合索引',

  -- 操作类型+操作模块+操作时间+软删除联合索引,用于按操作类型和模块查询(统计场景: 操作统计分析)
  KEY `idx_operation_log_type_module` (`operation_type`, `operation_module`, `operation_time`, `is_deleted`) COMMENT '操作类型模块联合索引',

  -- 操作时间+操作结果+软删除联合索引,用于按时间查询和统计(统计场景: 操作成功率统计)
  KEY `idx_operation_log_time_result` (`operation_time`, `operation_result`, `is_deleted`) COMMENT '操作时间结果联合索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='操作日志表: 操作日志表,记录用户对业务实体的操作,用于审计追溯'
  '【多态关联设计】本表通过 business_type + business_id 实现多态关联,支持关联订单、订单行、发运计划、快递单、供应商、承运商、客户、异常记录、附件、用户、角色等多种业务实体。'
  'business_type 字段标识关联的业务实体类型,business_id 字段存储关联业务实体的主键ID。'
  '【混合存储方案】核心信息存储在 MySQL 主表中,详细数据快照(before/after)存储在对象存储(OSS/MinIO)中,通过 snapshot_key 字段关联。'
  '【审计追溯】记录操作人、操作时间、操作IP、操作结果等完整信息,满足合规审计要求。'
  '【业务实体冗余信息】business_no 字段作为冗余字段,便于查询和展示,避免频繁关联查询业务实体表。'
  '【异步保存】操作日志通过独立线程池异步保存,不影响业务性能。';

-- ============================================================
-- 索引使用说明
-- ============================================================

-- 1. 查询某用户的操作历史
-- SELECT * FROM t_operation_log WHERE operator_id = ? AND is_deleted = 0 ORDER BY operation_time DESC;

-- 2. 查询某订单的操作历史
-- SELECT * FROM t_operation_log WHERE business_type = 'order' AND business_id = ? AND is_deleted = 0 ORDER BY operation_time DESC;

-- 3. 统计某时间段内各操作类型的数量
-- SELECT operation_type, COUNT(*) as count FROM t_operation_log WHERE operation_time >= ? AND operation_time < ? AND is_deleted = 0 GROUP BY operation_type;

-- 4. 查询某模块的失败操作
-- SELECT * FROM t_operation_log WHERE operation_module = 'ORDER' AND operation_result = 'FAILED' AND is_deleted = 0 ORDER BY operation_time DESC;

-- 5. 统计操作成功率
-- SELECT
--   operation_result,
--   COUNT(*) as count,
--   SUM(COUNT(*)) OVER() as total
-- FROM t_operation_log
-- WHERE operation_time >= ? AND operation_time < ? AND is_deleted = 0
-- GROUP BY operation_result;

-- ============================================================
-- 数据快照格式说明
-- ============================================================

-- 快照文件路径: logs/operation/2026/01/12345.json
-- 快照文件内容:
-- {
--   "before": {
--     "order_status": "DRAFT",
--     "total_amount": 10000.00
--   },
--   "after": {
--     "order_status": "EXECUTING",
--     "total_amount": 12000.00
--   }
-- }

-- ============================================================
-- 操作类型枚举值
-- ============================================================

-- CREATE: 创建
-- UPDATE: 更新
-- DELETE: 删除
-- VIEW: 查看
-- EXPORT: 导出
-- IMPORT: 导入
-- AUDIT: 审核
-- CONFIRM: 确认
-- APPROVE: 审批
-- CANCEL: 取消
-- LOGIN: 登录
-- LOGOUT: 登出
-- OTHER: 其他

-- ============================================================
-- 操作模块枚举值
-- ============================================================

-- ORDER: 订单管理
-- PARTNER: 合作方管理
-- SHIPMENT: 发运管理
-- RECEIPT: 签收管理
-- ATTACHMENT: 附件管理
-- EXCEPTION: 异常管理
-- VISUALIZATION: 可视化
-- DASHBOARD: 看板
-- SYSTEM: 系统管理
-- USER: 用户管理

-- ============================================================
-- 业务类型枚举值
-- ============================================================

-- order: 订单
-- order_line: 订单行
-- shipment: 发运批次
-- shipment_line: 快递单
-- receipt: 签收明细
-- customer: 客户
-- supplier: 供应商
-- carrier: 承运商
-- exception: 异常记录
-- attachment: 附件
-- user: 用户
-- role: 角色

-- ============================================================
-- 操作结果枚举值
-- ============================================================

-- SUCCESS: 成功
-- FAILED: 失败
-- PARTIAL: 部分成功
