-- ============================================================
-- 异常记录表 (t_exception)
-- 说明:异常主表,记录异常信息,通过多态关联支持关联多种业务实体
-- 关系:Exception N:1 ExceptionType (异常记录关联异常类型)
-- 关系:Exception 1:N ExceptionHandling (异常记录关联异常处理记录)
-- ============================================================

CREATE TABLE `t_exception` (
  -- ========== 主键 ==========
  -- 异常ID,主键,自增。每条异常记录的唯一标识
  -- 注意:目前使用自增ID,若使用分布式架构应改为雪花ID
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '异常ID',

  -- ========== 关联异常类型信息 ==========
  -- 关系:Exception N:1 ExceptionType (异常记录关联异常类型)
  -- 异常类型ID,外键关联t_exception_type表。必须关联异常类型字典,保证异常类型定义统一
  `exception_type_id` BIGINT NOT NULL COMMENT '异常类型ID',
  -- 异常类型代码(冗余字段,便于查询和展示)。从ExceptionType表中同步,避免频繁JOIN查询
  -- 注意:此字段应与exception_type_id关联的异常类型字典记录中的exception_type_code保持一致
  `exception_type_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '异常类型代码(冗余字段)',
  -- 异常类型名称(冗余字段,便于展示)。从ExceptionType表中同步,避免频繁JOIN查询
  -- 注意:此字段应与exception_type_id关联的异常类型字典记录中的exception_type_name保持一致
  `exception_type_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '异常类型名称(冗余字段)',
  -- 异常分类(冗余字段,便于查询和展示)
  `exception_category` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '异常分类(冗余字段)',
  -- 异常级别(冗余字段,便于查询和展示)
  `exception_level` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '异常级别(冗余字段)',

  -- ========== 多态关联业务实体 ==========
  -- 【多态关联设计】通过business_type + business_id实现多态关联,支持关联多种业务实体
  -- 业务类型,标识该异常关联的业务实体类型
  -- 可选值:order(订单)、order_line(订单行)、shipment(发运计划)、shipment_line(快递单)、receipt_detail(签收明细)
  `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型:order/order_line/shipment/shipment_line/receipt_detail',
  -- 业务ID,关联业务实体的主键ID
  -- 示例:business_type='order', business_id=1001 表示异常关联订单ID=1001(Order表的id字段)
  -- 示例:business_type='order_line', business_id=2001 表示异常关联订单行ID=2001(OrderLine表的id字段)
  -- 示例:business_type='shipment_line', business_id=3001 表示异常关联快递单ID=3001(ShipmentLine表的id字段)
  `business_id` BIGINT NOT NULL COMMENT '业务ID:关联业务实体的主键ID',

  -- ========== 业务实体冗余信息(便于查询和展示) ==========
  -- 业务实体编号(冗余字段,便于查询和展示)
  -- 示例:订单号、订单行号、发运计划号、物流单号等
  `business_no` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '业务实体编号(冗余字段)',
  -- 业务实体名称(冗余字段,便于展示)
  -- 示例:订单标题、产品名称等
  `business_name` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '业务实体名称(冗余字段)',

  -- ========== 异常基本信息 ==========
  -- 异常标题,简要描述异常情况
  `exception_title` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '异常标题',
  -- 异常描述,详细说明异常情况
  `exception_description` TEXT NOT NULL COMMENT '异常描述',
  -- 异常发生时间(精确到毫秒),记录异常实际发生的时间
  -- 使用NULL表示未确定异常发生时间,必须区分"未确定"和"已确定为空"的业务含义,因此允许使用NULL
  `exception_time` DATETIME(3) NULL DEFAULT NULL COMMENT '异常发生时间:NULL表示未确定异常发生时间',
  -- 异常发现时间(精确到毫秒),记录异常被发现的时间
  -- 使用NULL表示未确定异常发现时间,必须区分"未确定"和"已确定为空"的业务含义,因此允许使用NULL
  `exception_discovered_time` DATETIME(3) NULL DEFAULT NULL COMMENT '异常发现时间:NULL表示未确定异常发现时间',
  -- 异常发现人ID,外键关联t_user表。记录发现异常的用户。-1表示系统自动发现,0及以上表示用户ID
  -- 默认-1表示系统发现,避免使用NULL防止空指针问题和简化查询逻辑
  `discovered_by` BIGINT NOT NULL DEFAULT -1 COMMENT '异常发现人ID:默认-1表示系统发现',

  -- ========== 异常状态信息 ==========
  -- 异常状态:PENDING(待处理)、PROCESSING(处理中)、RESOLVED(已解决)、CLOSED(已关闭)、IGNORED(已忽略)
  -- 默认PENDING表示待处理,避免使用NULL防止索引失效和简化查询逻辑
  `exception_status` VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT '异常状态:默认PENDING表示待处理',
  -- 异常状态描述,说明当前状态的含义
  `exception_status_description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '异常状态描述',
  -- 异常优先级:URGENT(紧急)、HIGH(高)、MEDIUM(中)、LOW(低)
  -- 默认空字符串表示未设置优先级,避免使用NULL防止索引失效和简化查询逻辑
  `priority` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '异常优先级',
  -- 是否影响业务:0-不影响,1-影响
  -- 默认0表示不影响,避免使用NULL防止索引失效和简化查询逻辑
  `is_business_impact` TINYINT NOT NULL DEFAULT 0 COMMENT '是否影响业务:0-不影响,1-影响',
  -- 影响业务描述,说明异常对业务的影响
  `business_impact_description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '影响业务描述',

  -- ========== 异常处理信息 ==========
  -- 异常处理人ID,外键关联t_user表。记录负责处理异常的用户。-1表示未分配,0及以上表示用户ID
  -- 默认-1表示未分配,避免使用NULL防止空指针问题和简化查询逻辑
  `handler_id` BIGINT NOT NULL DEFAULT -1 COMMENT '异常处理人ID:默认-1表示未分配',
  -- 异常处理人姓名(冗余字段,便于查询和展示)
  `handler_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '异常处理人姓名(冗余字段)',
  -- 异常处理开始时间(精确到毫秒),记录异常处理开始的时间
  -- 使用NULL表示未开始处理,必须区分"未开始"和"已开始但时间为空"的业务含义,因此允许使用NULL
  `handling_start_time` DATETIME(3) NULL DEFAULT NULL COMMENT '异常处理开始时间:NULL表示未开始处理',
  -- 异常处理完成时间(精确到毫秒),记录异常处理完成的时间
  -- 使用NULL表示未完成处理,必须区分"未完成"和"已完成但时间为空"的业务含义,因此允许使用NULL
  `handling_end_time` DATETIME(3) NULL DEFAULT NULL COMMENT '异常处理完成时间:NULL表示未完成处理',
  -- 异常处理结果:SUCCESS(成功)、FAILED(失败)、PARTIAL(部分解决)、CANCELLED(已取消)
  -- 默认空字符串表示未设置处理结果,避免使用NULL防止索引失效和简化查询逻辑
  `handling_result` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '异常处理结果',
  -- 异常处理说明,详细说明异常处理过程和结果
  `handling_description` TEXT NOT NULL COMMENT '异常处理说明',

  -- ========== 异常关联信息 ==========
  -- 关联异常ID,关联其他异常记录。用于异常关联分析,如:一个异常可能引发其他异常
  -- 使用NULL表示无关联异常,必须区分"无关联"和"已关联但ID为空"的业务含义,因此允许使用NULL
  `related_exception_id` BIGINT NULL DEFAULT NULL COMMENT '关联异常ID:NULL表示无关联异常',
  -- 关联附件ID列表,JSON格式存储关联的附件ID列表,便于关联异常相关的凭证和照片
  -- 示例:[1001, 1002, 1003]
  -- 注意:使用NULL表示无关联附件,避免空JSON数组占用存储空间,且NULL可以明确区分"无关联附件"和"空附件列表"
  `related_attachment_ids` JSON DEFAULT NULL COMMENT '关联附件ID列表:NULL表示无关联附件',

  -- ========== 备注信息 ==========
  -- 异常备注
  `remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '异常备注',
  -- 内部备注(不对外展示)
  `internal_remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '内部备注',

  -- ========== 扩展信息 ==========
  -- 扩展信息,JSON格式存储扩展字段,便于扩展且无需修改表结构
  -- 注意:仅支持MySQL 5.7+版本
  -- 使用NULL表示无扩展信息,避免空JSON对象占用存储空间,且NULL可以明确区分"无扩展信息"和"空扩展信息"
  `extra_info` JSON DEFAULT NULL COMMENT '扩展信息:NULL表示无扩展信息',

  -- ========== 公共字段 ==========
  -- 创建时间(精确到毫秒),记录异常创建的时间
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  -- 创建人ID,记录异常的创建人,外键关联t_user表。-1表示系统自动操作,0及以上表示用户ID
  -- 默认-1表示系统创建,避免使用NULL防止空指针问题和简化查询逻辑
  `created_by` BIGINT NOT NULL DEFAULT -1 COMMENT '创建人ID:默认-1表示系统创建',
  -- 更新时间(精确到毫秒),记录异常信息最后更新的时间
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  -- 更新人ID,记录异常信息的最后更新人,外键关联t_user表。-1表示系统自动操作,0及以上表示用户ID
  -- 默认-1表示系统更新,避免使用NULL防止空指针问题和简化查询逻辑
  `updated_by` BIGINT NOT NULL DEFAULT -1 COMMENT '更新人ID:默认-1表示系统更新',
  -- 是否删除:0-未删除,1-已删除
  -- 默认0表示未删除,避免使用NULL防止索引失效和简化查询逻辑
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除,1-已删除',

  PRIMARY KEY (`id`),

  -- ========== 外键约束 ==========
  -- 关联异常类型字典表,保证异常类型定义统一
  -- 注意:外键约束可选择性启用,取决于业务需求。启用外键可保证数据一致性,但可能影响性能
  -- CONSTRAINT `fk_exception_exception_type` FOREIGN KEY (`exception_type_id`) REFERENCES `t_exception_type` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,

  -- ========== 约束设计 ==========

  -- ========== 索引设计 ==========
  -- 索引设计原则:只保留核心查询场景的索引,避免过度索引导致写入性能下降
  -- InnoDB联合索引遵循最左前缀原则,单字段索引被联合索引覆盖时需删除以避免冗余

  -- 业务类型+业务ID+软删除联合索引,用于查询业务实体的所有异常(高频场景:查询订单的所有异常)
  -- 覆盖场景:按业务类型+业务ID查询异常、按业务类型+业务ID+删除标记查询异常
  KEY `idx_exception_business` (`business_type`, `business_id`, `is_deleted`) COMMENT '业务实体关联索引',

  -- 异常状态+处理人ID+优先级+软删除联合索引,用于异常处理工作台查询(高频场景:待处理异常列表、我的异常)
  -- 覆盖场景:按异常状态查询、按处理人ID+异常状态查询、按异常状态+优先级排序
  KEY `idx_exception_status_handler` (`exception_status`, `handler_id`, `priority`, `is_deleted`) COMMENT '异常状态处理人联合索引',

  -- 异常类型ID+异常状态+软删除联合索引,用于按异常类型和状态查询异常(统计场景:异常类型统计)
  -- 覆盖场景:按异常类型查询、按异常类型+异常状态查询
  KEY `idx_exception_type_status` (`exception_type_id`, `exception_status`, `is_deleted`) COMMENT '异常类型状态联合索引',

  -- 创建时间+异常状态+软删除联合索引,用于时间排序和统计查询(覆盖创建时间单字段查询)
  KEY `idx_exception_created_status` (`created_at`, `exception_status`, `is_deleted`) COMMENT '创建时间状态联合索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='异常记录表:异常主表,记录异常信息,通过多态关联支持关联多种业务实体。'
  '【多态关联设计】本表通过business_type + business_id实现多态关联,支持关联订单、订单行、发运计划、快递单、签收明细等多种业务实体。'
  'business_type字段标识关联的业务实体类型,business_id字段存储关联业务实体的主键ID。'
  '【异常类型管理】本表通过exception_type_id关联t_exception_type表,实现异常类型统一管理。'
  'exception_type_code、exception_type_name、exception_category、exception_level字段作为冗余字段便于查询和展示,'
  '应与异常类型字典表中的对应字段保持一致。'
  '【异常处理】异常处理过程记录在ExceptionHandling表中,本表记录异常的基本处理信息(处理人、处理时间、处理结果等)。'
  '【业务实体冗余信息】business_no、business_name字段作为冗余字段,便于查询和展示,避免频繁关联查询业务实体表。';

