-- ============================================================
-- 异常处理记录表 (t_exception_handling)
-- 说明:异常处理过程记录表,记录异常处理的详细过程和历史
-- 关系:ExceptionHandling N:1 Exception (异常处理记录关联异常记录)
-- ============================================================

CREATE TABLE `t_exception_handling` (
  -- ========== 主键 ==========
  -- 异常处理记录ID,主键,自增。每条异常处理记录的唯一标识
  -- 注意:目前使用自增ID,若使用分布式架构应改为雪花ID
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '异常处理记录ID',

  -- ========== 关联异常记录信息 ==========
  -- 关系:ExceptionHandling N:1 Exception (异常处理记录关联异常记录)
  -- 异常ID,外键关联t_exception表。每条异常处理记录必须关联一个异常记录
  `exception_id` BIGINT NOT NULL COMMENT '异常ID',
  -- 异常类型代码(冗余字段,便于查询和展示)
  `exception_type_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '异常类型代码(冗余字段)',
  -- 异常类型名称(冗余字段,便于展示)
  `exception_type_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '异常类型名称(冗余字段)',
  -- 业务类型(冗余字段,便于查询和展示)
  `business_type` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '业务类型(冗余字段)',
  -- 业务ID(冗余字段,便于查询和展示)
  `business_id` BIGINT NOT NULL DEFAULT 0 COMMENT '业务ID(冗余字段)',

  -- ========== 异常处理基本信息 ==========
  -- 处理步骤序号,记录异常处理的步骤顺序,从1开始递增
  -- 注意:同一异常的处理步骤序号应唯一且连续,业务层需保证步骤序号的唯一性和连续性
  `handling_step` INT NOT NULL COMMENT '处理步骤序号',
  -- 处理操作类型,标识本次处理的操作类型
  -- 示例:ASSIGN(分配)、ACCEPT(接受)、PROCESS(处理)、ESCALATE(升级)、RESOLVE(解决)、CLOSE(关闭)、CANCEL(取消)
  `handling_action` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '处理操作类型',
  -- 处理操作描述,详细说明本次处理操作的内容
  `handling_description` TEXT NOT NULL COMMENT '处理操作描述',
  -- 处理前异常状态,记录处理前的异常状态
  -- 示例:PENDING(待处理)、PROCESSING(处理中)、RESOLVED(已解决)
  `before_status` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '处理前异常状态',
  -- 处理后异常状态,记录处理后的异常状态
  -- 示例:PROCESSING(处理中)、RESOLVED(已解决)、CLOSED(已关闭)
  `after_status` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '处理后异常状态',
  -- 处理结果:SUCCESS(成功)、FAILED(失败)、PARTIAL(部分解决)、CANCELLED(已取消)
  -- 默认空字符串表示未设置处理结果,避免使用NULL防止索引失效和简化查询逻辑
  `handling_result` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '处理结果',
  -- 处理结果说明,详细说明处理结果
  `result_description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '处理结果说明',

  -- ========== 异常处理人信息 ==========
  -- 处理人ID,外键关联t_user表。记录执行本次处理操作的用户。-1表示系统自动操作,0及以上表示用户ID
  -- 默认-1表示系统处理,避免使用NULL防止空指针问题和简化查询逻辑
  `handler_id` BIGINT NOT NULL DEFAULT -1 COMMENT '处理人ID:默认-1表示系统处理',
  -- 处理人姓名(冗余字段,便于查询和展示)
  `handler_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '处理人姓名(冗余字段)',
  -- 处理人部门(冗余字段,便于查询和展示)
  `handler_department` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '处理人部门(冗余字段)',
  -- 处理时间(精确到毫秒),记录本次处理操作的时间
  `handling_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '处理时间',
  -- 处理耗时(秒),记录本次处理操作的耗时,便于统计分析
  -- 使用NULL表示未计算处理耗时,必须区分"未计算"和"已计算为0"的业务含义,因此允许使用NULL
  `handling_duration` INT NULL DEFAULT NULL COMMENT '处理耗时(秒):NULL表示未计算处理耗时',

  -- ========== 异常处理关联信息 ==========
  -- 关联附件ID列表,JSON格式存储本次处理操作关联的附件ID列表,便于关联处理相关的凭证和照片
  -- 示例:[1001, 1002, 1003]
  -- 注意:使用NULL表示无关联附件,避免空JSON数组占用存储空间,且NULL可以明确区分"无关联附件"和"空附件列表"
  `related_attachment_ids` JSON DEFAULT NULL COMMENT '关联附件ID列表:NULL表示无关联附件',
  -- 关联操作日志ID,关联操作日志表,便于追溯处理操作的详细日志
  -- 使用NULL表示无关联操作日志,必须区分"无关联"和"已关联但ID为空"的业务含义,因此允许使用NULL
  `related_operation_log_id` BIGINT NULL DEFAULT NULL COMMENT '关联操作日志ID:NULL表示无关联操作日志',

  -- ========== 异常处理备注信息 ==========
  -- 处理备注
  `remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '处理备注',
  -- 内部备注(不对外展示)
  `internal_remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '内部备注',

  -- ========== 扩展信息 ==========
  -- 扩展信息,JSON格式存储扩展字段,便于扩展且无需修改表结构
  -- 注意:仅支持MySQL 5.7+版本
  -- 使用NULL表示无扩展信息,避免空JSON对象占用存储空间,且NULL可以明确区分"无扩展信息"和"空扩展信息"
  `extra_info` JSON DEFAULT NULL COMMENT '扩展信息:NULL表示无扩展信息',

  -- ========== 公共字段 ==========
  -- 创建时间(精确到毫秒),记录异常处理记录创建的时间
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  -- 创建人ID,记录异常处理记录的创建人,外键关联t_user表。-1表示系统自动操作,0及以上表示用户ID
  -- 默认-1表示系统创建,避免使用NULL防止空指针问题和简化查询逻辑
  `created_by` BIGINT NOT NULL DEFAULT -1 COMMENT '创建人ID:默认-1表示系统创建',
  -- 更新时间(精确到毫秒),记录异常处理记录信息最后更新的时间
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  -- 更新人ID,记录异常处理记录信息的最后更新人,外键关联t_user表。-1表示系统自动操作,0及以上表示用户ID
  -- 默认-1表示系统更新,避免使用NULL防止空指针问题和简化查询逻辑
  `updated_by` BIGINT NOT NULL DEFAULT -1 COMMENT '更新人ID:默认-1表示系统更新',
  -- 是否删除:0-未删除,1-已删除
  -- 默认0表示未删除,避免使用NULL防止索引失效和简化查询逻辑
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除,1-已删除',

  PRIMARY KEY (`id`),

  -- ========== 外键约束 ==========
  -- 关联异常记录表,保证数据一致性
  -- 注意:外键约束可选择性启用,取决于业务需求。启用外键可保证数据一致性,但可能影响性能
  -- CONSTRAINT `fk_exception_handling_exception` FOREIGN KEY (`exception_id`) REFERENCES `t_exception` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,

  -- ========== 约束设计 ==========
  -- 异常ID+处理步骤序号唯一约束,保证同一异常的处理步骤序号唯一
  -- 注意:即使异常处理记录被软删除,处理步骤序号也不应重复使用,以保证业务逻辑的清晰性
  UNIQUE KEY `uk_exception_handling_exception_step` (`exception_id`, `handling_step`) COMMENT '异常处理步骤唯一约束',

  -- ========== 索引设计 ==========
  -- 索引设计原则:只保留核心查询场景的索引,避免过度索引导致写入性能下降
  -- InnoDB联合索引遵循最左前缀原则,单字段索引被联合索引覆盖时需删除以避免冗余

  -- 异常ID+处理步骤序号+软删除联合索引,用于查询异常的所有处理记录并按步骤排序(高频场景:查看异常处理历史)
  -- 覆盖场景:按异常ID查询处理记录、按异常ID+处理步骤查询处理记录
  KEY `idx_exception_handling_exception_step` (`exception_id`, `handling_step`, `is_deleted`) COMMENT '异常处理步骤索引',

  -- 处理人ID+处理时间+软删除联合索引,用于查询处理人的处理记录(统计场景:查询处理人的处理历史)
  -- 覆盖场景:按处理人ID查询、按处理人ID+处理时间查询
  KEY `idx_exception_handling_handler_time` (`handler_id`, `handling_time`, `is_deleted`) COMMENT '处理人时间索引',

  -- 业务类型+业务ID+软删除联合索引,用于查询业务实体的所有异常处理记录(统计场景:查询业务实体的异常处理历史)
  -- 注意:业务类型和业务ID为冗余字段,便于查询和统计,不设置外键约束
  KEY `idx_exception_handling_business` (`business_type`, `business_id`, `is_deleted`) COMMENT '业务实体关联索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='异常处理记录表:异常处理过程记录表,记录异常处理的详细过程和历史。'
  '【关联关系】每个异常处理记录关联一个异常记录。一个异常可以有多条处理记录,记录异常处理的完整过程。'
  '【处理步骤】处理步骤序号(handling_step)从1开始递增,保证同一异常的处理步骤序号唯一且连续,便于按时间顺序查看异常处理历史。'
  '【处理操作】处理操作类型(handling_action)标识本次处理的操作类型,如分配、接受、处理、解决、关闭等,便于统计和分析。'
  '【状态变更】处理前异常状态(before_status)和处理后异常状态(after_status)记录异常状态的变更,便于追溯异常状态流转历史。'
  '【冗余字段】异常类型代码、异常类型名称、业务类型、业务ID等字段作为冗余字段,便于查询和展示,避免频繁关联查询异常记录表和业务实体表。';

