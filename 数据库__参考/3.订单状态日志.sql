-- ============================================================
-- 订单状态日志表 (t_order_status_log)
-- 说明:记录订单全生命周期的状态变更历史,支持业务时间线和审计追溯
-- ============================================================

CREATE TABLE `t_order_status_log` (
  -- ========== 主键 ==========
  -- 日志记录ID,主键,自增。每条状态变更记录的唯一标识
  -- 注意:目前使用自增ID,若使用分布式架构应改为雪花ID
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志记录ID',
  
  -- ========== 关联订单信息 ==========
  -- 关系:订单(1) -> 状态日志(N)
  -- 订单ID,外键关联t_order表
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  -- 订单号(冗余字段,便于查询,示例:CUST001-20241217-0001)
  `order_no` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '订单号',
  
  -- ========== 状态变更信息 ==========
  -- 【状态机管理设计】状态日志表引用 StatusDict 中的状态码,保证状态统一管理
  -- 关系:OrderStatusLog N:1 StatusDict (状态日志引用状态字典)
  -- 变更前的订单状态字典ID。外键关联t_status_dict表。初始创建时from_status_dict_id为NULL(表示无前序状态)
  -- 使用NULL表示无前序状态(初始创建),必须区分"从特定状态变更"和"从无状态变更"的业务含义,因此允许使用NULL
  `from_status_dict_id` BIGINT NULL DEFAULT NULL COMMENT '变更前状态字典ID:NULL表示无前序状态(初始创建)',
  -- 变更后的订单状态字典ID。外键关联t_status_dict表。必须关联状态字典,保证状态定义统一
  `to_status_dict_id` BIGINT NOT NULL COMMENT '变更后状态字典ID',
  -- 变更前的订单状态代码(冗余字段,便于查询和展示)。从StatusDict表中同步,避免频繁JOIN查询
  -- 注意:此字段应与from_status_dict_id关联的状态字典记录中的status_code保持一致
  -- 使用NULL表示无前序状态(初始创建),与from_status_dict_id保持一致
  `from_status` VARCHAR(50) NULL DEFAULT NULL COMMENT '变更前状态代码(冗余字段):NULL表示无前序状态(初始创建)',
  -- 变更后的订单状态代码(冗余字段,便于查询和展示)。从StatusDict表中同步,避免频繁JOIN查询
  -- 注意:此字段应与to_status_dict_id关联的状态字典记录中的status_code保持一致
  `to_status` VARCHAR(50) NOT NULL COMMENT '变更后状态代码(冗余字段)',
  
  -- ========== 业务节点信息 ==========
  -- 业务节点代码,用于业务时间线展示。命名规范示例:order_created(订单创建)、supplier_confirmed(供应商确认)、
  -- shipment_started(发运开始)、receipt_completed(签收完成)。空字符串表示无具体业务节点
  -- TODO: 业务节点名称还需后续确认
  -- 注意:节点中文名称通过枚举类或字典表映射,不在此表冗余存储以避免数据不一致风险
  `business_node` VARCHAR(30) NOT NULL DEFAULT '' COMMENT '业务节点代码',
  -- 节点分类:normal(正常)、exception(异常)。用于时间线节点分类展示和筛选
  `business_node_category` ENUM('normal', 'exception') NOT NULL DEFAULT 'normal' COMMENT '节点分类',
  -- 业务阶段枚举:ORDER_INIT(订单初始化)、COOPERATION(合作确认)、SHIPMENT(发运)、RECEIPT(签收)、SETTLEMENT(结算)
  -- 标识订单生命周期的大阶段,用于按阶段查询和统计
  `business_stage` VARCHAR(30) NOT NULL DEFAULT '' COMMENT '业务阶段',
  
  -- ========== 操作人信息 ==========
  -- 操作人ID,外键关联t_user表。-1表示系统自动操作,0及以上表示用户ID
  -- 注意:与订单表的created_by/updated_by保持一致,使用-1作为系统操作的默认值
  -- 默认-1表示系统操作,避免使用NULL防止空指针问题和简化查询逻辑
  `operator_id` BIGINT NOT NULL DEFAULT -1 COMMENT '操作人ID:默认-1表示系统操作',
  -- 操作人姓名(快照字段),用于审计和展示。记录操作时的操作人信息,即使后续修改也保持不变,保证历史记录准确性。空字符串表示系统操作
  -- 默认空字符串表示系统操作,避免使用NULL防止索引失效和简化查询逻辑
  `operator_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作人姓名:默认空字符串表示系统操作',
  -- 操作人角色,如:客户经理、采购专员、运营专员、系统管理员。用于统计分析和权限审计
  -- 默认空字符串表示无角色,避免使用NULL防止索引失效和简化查询逻辑
  `operator_role` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '操作人角色:默认空字符串表示无角色',
  -- 操作类型:manual(手动)、auto(自动,如批次签收完成后自动变更状态)、system(系统任务触发)
  `operator_type` ENUM('manual', 'auto', 'system') NOT NULL DEFAULT 'manual' COMMENT '操作类型',
  
  -- ========== 变更原因和备注 ==========
  -- 状态变更原因/操作说明,用于业务追溯和问题排查。示例:"客户要求取消订单"、"所有发运批次已完成签收"
  `change_reason` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '变更原因',
  -- 备注信息,存储操作相关的补充说明
  `remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
  
  -- ========== 关联业务对象 ==========
  -- 关联业务类型,与related_business_id配合使用,用于追溯和查询
  -- 示例:shipment(发运批次)、receipt(签收记录)、exception(异常记录)、supplier(供应商)、carrier(承运商)
  `related_business_type` VARCHAR(30) NOT NULL DEFAULT '' COMMENT '关联业务类型',
  -- 关联业务对象ID,如:发运批次ID、签收记录ID等。0表示无关联业务对象
  `related_business_id` BIGINT NOT NULL DEFAULT 0 COMMENT '关联业务对象ID',
  -- 关联业务对象编号(冗余字段),如:发运批次号、签收单号。便于查询和展示,避免关联查询
  `related_business_no` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '关联业务对象编号',
  
  -- ========== 操作时间 ==========
  -- 业务操作时间(精确到毫秒),记录状态变更发生的实际业务时间。示例:2024-12-17 14:30:25.123
  -- 注意:operate_time(业务操作时间)与created_at(入库时间)可能不同,存在延迟入库的情况
  `operate_time` DATETIME(3) NOT NULL COMMENT '业务操作时间',
  
  -- ========== 扩展信息 ==========
  -- 额外信息,JSON格式存储扩展字段,便于扩展且无需修改表结构
  -- 存储业务自定义字段、临时数据等。示例:{"custom_field1": "value1", "custom_field2": 123}
  -- 注意:仅支持MySQL 5.7+版本
  -- 使用NULL表示无扩展信息,避免空JSON对象占用存储空间,且NULL可以明确区分"无扩展信息"和"空扩展信息"
  `extra_info` JSON DEFAULT NULL COMMENT '扩展信息:NULL表示无扩展信息',
  
  -- ========== 时间戳 ==========
  -- 日志入库时间(精确到毫秒),记录数据写入数据库的时间。默认值为当前时间
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '入库时间',
  
  -- ========== 软删除 ==========
  -- 是否删除:0-未删除,1-已删除(软删除)
  -- 注意:审计日志表应使用软删除,不可物理删除,以保留审计痕迹。即使误删操作也应通过软删除标记,保留数据记录
  -- 默认0表示未删除,避免使用NULL防止索引失效和简化查询逻辑
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除,1-已删除',
  -- 删除时间(精确到毫秒),记录软删除操作的时间
  -- 注意:使用NULL表示未删除,必须区分"未删除"和"已删除但删除时间为空"的业务含义,因此允许使用NULL
  `deleted_at` DATETIME(3) NULL DEFAULT NULL COMMENT '删除时间:NULL表示未删除',
  
  PRIMARY KEY (`id`),
  
  -- ========== 外键约束 ==========
  -- 关联状态字典表,保证状态定义统一
  -- 注意:外键约束可选择性启用,取决于业务需求。启用外键可保证数据一致性,但可能影响性能
  -- CONSTRAINT `fk_order_status_log_from_status_dict` FOREIGN KEY (`from_status_dict_id`) REFERENCES `t_status_dict` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  -- CONSTRAINT `fk_order_status_log_to_status_dict` FOREIGN KEY (`to_status_dict_id`) REFERENCES `t_status_dict` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  
  -- ========== 索引设计说明 ==========
  -- 索引设计原则:只保留核心查询场景的索引,避免过度索引导致写入性能下降
  -- InnoDB联合索引遵循最左前缀原则,单字段索引被联合索引覆盖时需删除以避免冗余
  
  -- 核心查询1:订单时间线查询(最常用,高频查询)
  -- 查询场景:查询某个订单的所有状态变更记录,按时间顺序展示(业务时间线)
  -- 支持场景:1)按订单ID查询全部历史 2)按订单ID+业务阶段查询 3)按订单ID+业务节点查询
  -- 索引字段:order_id(定位订单)+ operate_time(时间排序)+ is_deleted(软删除筛选)
  -- 查询实例:SELECT * FROM t_order_status_log WHERE order_id = 123 AND is_deleted = 0 ORDER BY operate_time
  KEY `idx_order_status_log_order_time` (`order_id`, `is_deleted`,`operate_time`) COMMENT '订单时间线查询索引',
  
  -- 核心查询2:状态字典查询(用于状态机管理和统计分析)
  -- 查询场景:查询某个状态的所有状态变更记录,用于状态流转分析和统计
  -- 索引字段:to_status_dict_id(状态字典ID)+ operate_time(时间排序)+ is_deleted(软删除筛选)
  -- 查询实例:查询今天所有被关闭的订单状态变更记录,用于统计关闭原因
  KEY `idx_order_status_log_status_dict` (`to_status_dict_id`, `is_deleted`, `operate_time`) COMMENT '状态字典查询索引',

  -- 核心查询3:操作人查询(审计场景,重要但频率较低)
  -- 查询场景1:查询某个操作人在某个时间段内的所有操作记录(基础审计)
  -- 查询场景2:查询某个操作人对特定订单的操作记录(操作人+订单组合查询)
  -- 索引字段:operator_id(定位操作人)+ order_id(订单ID,可选)+ operate_time(时间排序)+ is_deleted(软删除筛选)
  KEY `idx_order_status_log_operator` (`operator_id`, `order_id`, `is_deleted`, `operate_time`) COMMENT '操作人查询索引(支持操作人单独查询和操作人+订单组合查询)',

  -- 核心查询4:关联业务对象查询(低频查询,按需保留)
  -- 查询场景:查询与某个业务对象(如发运批次、签收记录)相关的所有状态变更记录
  -- 索引字段:related_business_type(业务类型筛选)+ related_business_id(业务对象ID定位)+ is_deleted(软删除筛选)
  -- 支持按关联业务类型和业务对象ID查询相关的状态变更记录,用于业务对象的状态追溯,过滤已删除记录
  -- 注意:查询无关联业务对象时,使用related_business_type为空字符串或related_business_id为0
  -- 优化建议:如果此查询场景频率很低,可考虑删除此索引,通过应用层分页或限制查询范围来优化
  KEY `idx_order_status_log_related_business` (`related_business_type`, `related_business_id`, `is_deleted`) COMMENT '关联业务对象查询索引'
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='订单状态日志表:记录订单全生命周期的状态变更历史,支持业务时间线展示、操作审计、状态追溯等功能。'
  '每条记录代表一次订单状态的变更操作,包含变更前后的状态信息、操作人信息、业务节点信息、关联业务对象等完整信息。'
  '【状态机管理】本表通过from_status_dict_id和to_status_dict_id关联t_status_dict表,实现状态统一管理。'
  '状态码字段(from_status/to_status)作为冗余字段便于查询,应与状态字典表中的status_code保持一致。';