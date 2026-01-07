-- ============================================================
-- 签收明细表 (t_receipt_detail)
-- 说明:签收明细表,记录具体签收数量,直接关联快递单,包含签收差异信息
-- 关系:ReceiptDetail 1:1 ShipmentLine (签收明细与快递单一对一关系)
-- ============================================================

CREATE TABLE `t_receipt_detail` (
  -- ========== 主键 ==========
  -- 签收明细ID,主键,自增。每条签收明细记录的唯一标识
  -- 注意:目前使用自增ID,若使用分布式架构应改为雪花ID
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '签收明细ID',

  -- ========== 关联快递单信息 ==========
  -- 关系:签收明细(1) -> 快递单(1)
  -- 快递单ID,外键关联t_shipment_line表。签收明细与快递单一对一关系
  `shipment_line_id` BIGINT NOT NULL COMMENT '快递单ID',
  -- 物流单号(冗余字段,便于查询和展示)
  `logistics_no` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '物流单号(冗余字段)',

  -- ========== 关联订单行信息(冗余字段,便于查询) ==========
  -- 订单行ID(冗余字段,通过快递单关联订单行,便于查询和展示)
  `order_line_id` BIGINT NOT NULL COMMENT '订单行ID(冗余字段)',
  -- 订单ID(冗余字段,便于查询和展示)
  `order_id` BIGINT NOT NULL COMMENT '订单ID(冗余字段)',
  -- 订单号(冗余字段,便于查询和展示)
  `order_no` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '订单号(冗余字段)',

  -- ========== 签收数量信息 ==========
  `receipt_quantity` DECIMAL(18, 3) NOT NULL DEFAULT 0.000 COMMENT '签收数量',
  -- 快递单发运数量(冗余字段,快照快递单发运数量,便于计算差异)
  `shipment_quantity` DECIMAL(18, 3) NOT NULL DEFAULT 0.000 COMMENT '快递单发运数量(冗余字段)',

  -- ========== 签收时间信息 ==========
  -- 签收时间(精确到毫秒),记录实际签收时间
  -- 使用NULL表示未签收,必须区分"未签收"和"已签收但时间为空"的业务含义,因此允许使用NULL
  `receipt_time` DATETIME(3) NULL DEFAULT NULL COMMENT '签收时间:NULL表示未签收',
  -- 签收日期,签收时间的日期部分,便于按日期查询和统计
  -- 使用NULL表示未签收,与receipt_time保持一致
  `receipt_date` DATE NULL DEFAULT NULL COMMENT '签收日期:NULL表示未签收',

  -- ========== 签收人信息 ==========
  -- 签收人姓名,实际签收人的姓名
  `receipt_person` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '签收人姓名',
  -- 签收人联系电话,格式:可选+号开头,7-20位数字(如:13800138000、+8613800138000)
  -- 注意:MySQL 8.0+支持CHECK约束,MySQL 5.7及以下版本需在业务层校验格式
  `receipt_phone` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '签收人联系电话',
  -- 签收人身份证号(可选字段,用于身份验证)
  `receipt_id_card` VARCHAR(18) NOT NULL DEFAULT '' COMMENT '签收人身份证号',

  -- ========== 签收地址信息 ==========
  -- 签收地址(完整地址),实际签收地址,通常与快递单到货地址一致,但可能不同
  `receipt_address` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '签收地址',
  -- 签收地址联系人
  `receipt_contact` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '签收地址联系人',
  -- 签收地址联系电话,格式:可选+号开头,7-20位数字(如:13800138000、+8613800138000)
  -- 注意:MySQL 8.0+支持CHECK约束,MySQL 5.7及以下版本需在业务层校验格式
  `receipt_contact_phone` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '签收地址联系电话',

  -- ========== 产品信息(冗余字段,快照) ==========
  -- 产品编码(冗余字段,快照签收时的产品编码,避免产品信息变更影响历史签收记录)
  `product_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '产品编码(冗余字段)',
  -- 产品名称(冗余字段,快照签收时的产品名称,避免产品信息变更影响历史签收记录)
  `product_name` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '产品名称(冗余字段)',
  -- 规格型号(冗余字段,快照签收时的规格型号)
  `specification` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '规格型号(冗余字段)',
  -- 单位(冗余字段,快照签收时的单位)
  `unit` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '单位(冗余字段)',

  -- ========== 签收差异信息 ==========
  -- 差异数量,签收数量与发运数量的差值。正数表示多收,负数表示少收,0表示无差异
  -- 计算公式:receipt_quantity - shipment_quantity
  -- 使用NULL表示未计算差异,必须区分"未计算"和"已计算为0"的业务含义,因此允许使用NULL
  `difference_quantity` DECIMAL(18, 3) NULL DEFAULT NULL COMMENT '差异数量:NULL表示未计算差异',
  -- 差异类型:NO_DIFFERENCE(无差异)、MORE(多收)、LESS(少收)、DAMAGED(损坏)、OTHER(其他)
  -- 默认空字符串表示未设置差异类型,避免使用NULL防止索引失效和简化查询逻辑
  `difference_type` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '差异类型',
  -- 是否有差异:0-无差异,1-有差异
  -- 默认0表示无差异,避免使用NULL防止索引失效和简化查询逻辑
  `has_difference` TINYINT NOT NULL DEFAULT 0 COMMENT '是否有差异:0-无差异,1-有差异',
  -- 差异原因描述
  `difference_reason` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '差异原因',
  -- 差异处理状态:PENDING(待处理)、PROCESSING(处理中)、RESOLVED(已解决)、IGNORED(已忽略)
  -- 默认空字符串表示未设置处理状态,避免使用NULL防止索引失效和简化查询逻辑
  `difference_status` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '差异处理状态',
  -- 差异处理说明
  `difference_handling` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '差异处理说明',
  -- 差异处理人ID,外键关联t_user表。-1表示未处理,0及以上表示用户ID
  -- 默认-1表示未处理,避免使用NULL防止空指针问题和简化查询逻辑
  `difference_handler_id` BIGINT NOT NULL DEFAULT -1 COMMENT '差异处理人ID:默认-1表示未处理',
  -- 差异处理时间(精确到毫秒),记录差异处理的时间
  -- 使用NULL表示未处理,必须区分"未处理"和"已处理但时间为空"的业务含义,因此允许使用NULL
  `difference_handled_time` DATETIME(3) NULL DEFAULT NULL COMMENT '差异处理时间:NULL表示未处理',

  -- ========== 备注信息 ==========
  -- 签收备注
  `remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '签收备注',
  -- 内部备注(不对外展示)
  `internal_remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '内部备注',

  -- ========== 扩展信息 ==========
  -- 扩展信息,JSON格式存储扩展字段,便于扩展且无需修改表结构
  -- 注意:仅支持MySQL 5.7+版本
  -- 使用NULL表示无扩展信息,避免空JSON对象占用存储空间,且NULL可以明确区分"无扩展信息"和"空扩展信息"
  `extra_info` JSON DEFAULT NULL COMMENT '扩展信息:NULL表示无扩展信息',

  -- ========== 公共字段 ==========
  -- 创建时间(精确到毫秒),记录签收明细创建的时间
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  -- 创建人ID,记录签收明细的创建人,外键关联t_user表。-1表示系统自动操作,0及以上表示用户ID
  -- 默认-1表示系统创建,避免使用NULL防止空指针问题和简化查询逻辑
  `created_by` BIGINT NOT NULL DEFAULT -1 COMMENT '创建人ID:默认-1表示系统创建',
  -- 更新时间(精确到毫秒),记录签收明细信息最后更新的时间
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  -- 更新人ID,记录签收明细信息的最后更新人,外键关联t_user表。-1表示系统自动操作,0及以上表示用户ID
  -- 默认-1表示系统更新,避免使用NULL防止空指针问题和简化查询逻辑
  `updated_by` BIGINT NOT NULL DEFAULT -1 COMMENT '更新人ID:默认-1表示系统更新',
  -- 是否删除:0-未删除,1-已删除
  -- 默认0表示未删除,避免使用NULL防止索引失效和简化查询逻辑
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除,1-已删除',

  PRIMARY KEY (`id`),

  -- ========== 外键约束 ==========
  -- 关联快递单表,保证数据一致性
  -- 注意:外键约束可选择性启用,取决于业务需求。启用外键可保证数据一致性,但可能影响性能
  -- 注意:订单行信息(order_line_id)为冗余字段,通过快递单关联,不设置外键约束
  -- CONSTRAINT `fk_receipt_detail_shipment_line` FOREIGN KEY (`shipment_line_id`) REFERENCES `t_shipment_line` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,

  -- ========== 约束设计 ==========
  -- 联系电话格式约束(MySQL 8.0+),格式:可选+号开头,7-20位数字
  -- 允许空字符串或符合格式的字符串(如:13800138000、+8613800138000)
  -- 注意:MySQL 5.7及以下版本不支持CHECK约束,需在业务层使用正则校验(如:/^\+?[0-9]{7,20}$/)
  CONSTRAINT `ck_receipt_detail_receipt_phone` CHECK (`receipt_phone` = '' OR `receipt_phone` REGEXP '^\\+?[0-9]{7,20}$'),
  CONSTRAINT `ck_receipt_detail_receipt_contact_phone` CHECK (`receipt_contact_phone` = '' OR `receipt_contact_phone` REGEXP '^\\+?[0-9]{7,20}$'),

  -- 快递单ID唯一约束,保证签收明细与快递单一对一关系
  -- 注意:即使签收明细被软删除,也不应重复使用同一个快递单ID
  UNIQUE KEY `uk_receipt_detail_shipment_line_id` (`shipment_line_id`, `is_deleted`) COMMENT '快递单ID唯一约束',

  -- ========== 索引设计 ==========
  -- 索引设计原则:只保留核心高频查询场景的索引,避免过度索引导致写入性能下降
  -- InnoDB联合索引遵循最左前缀原则,单字段索引被联合索引覆盖时需删除以避免冗余

  -- 订单行ID+软删除联合索引,用于查询订单行的所有签收明细(统计场景:查询订单行的签收记录)
  -- 注意:订单行ID为冗余字段,通过快递单关联,用于统计和查询优化
  KEY `idx_receipt_detail_order_line_id` (`order_line_id`, `is_deleted`) COMMENT '订单行ID索引',

  -- 签收日期+软删除联合索引,用于按日期查询和统计签收记录(统计场景:按日期统计签收数量)
  KEY `idx_receipt_detail_receipt_date` (`receipt_date`, `is_deleted`) COMMENT '签收日期索引',

  -- 订单ID+签收日期+软删除联合索引,用于查询订单的签收记录并按日期筛选(高频场景:查询订单的签收历史)
  KEY `idx_receipt_detail_order_receipt_date` (`order_id`, `receipt_date`, `is_deleted`) COMMENT '订单签收日期联合索引',

  -- 差异类型+差异状态+软删除联合索引,用于查询有差异的签收记录(统计场景:查询待处理的差异记录)
  KEY `idx_receipt_detail_difference` (`has_difference`, `difference_status`, `is_deleted`) COMMENT '差异查询索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='签收明细表:签收明细表,记录具体签收数量,直接关联快递单,包含签收差异信息。'
  '【业务说明】本表记录的是实际的签收信息,包含签收数量、签收时间、签收人等具体信息。'
  '签收明细直接关联快递单,与快递单是一对一关系,通过shipment_line_id字段关联,保证每个快递单只有一个签收明细。'
  '【关联关系】每个签收明细关联一个快递单。订单行信息(order_line_id、order_id、order_no)为冗余字段,'
  '通过快递单关联订单行,便于查询和统计,不设置外键约束。'
  '签收数量应≤快递单发运数量,业务层需校验。'
  '【产品信息】产品信息(product_code、product_name、specification、unit)作为冗余字段保存签收时的快照,'
  '避免产品信息变更影响历史签收记录。'
  '【数量信息】快递单发运数量(shipment_quantity)作为冗余字段,便于计算签收差异,避免关联查询快递单表。'
  '【差异信息】差异信息(difference_quantity、difference_type、difference_reason等)直接存储在签收明细表中,'
  '便于查询和统计,无需单独的签收差异表。差异数量=签收数量-发运数量,业务层需计算并更新。';

