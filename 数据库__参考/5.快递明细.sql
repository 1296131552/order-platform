-- ============================================================
-- 快递单表 (t_shipment_line)
-- 说明:快递单表,每个记录代表一个快递单中的一个订单行产品
-- 快递单 = 一个承运商 + 一个收货方 + 一个物流单号
-- 一个快递单可以包含多个订单行的产品,通过快递单号(logistics_no)字段关联
-- 注意:本表记录的是实际的快递单信息,包含承运商、收货方、物流单号等具体信息
-- ============================================================

CREATE TABLE `t_shipment_line` (
  -- ========== 主键 ==========
  -- 快递单明细ID,主键,自增。每条快递单明细记录的唯一标识
  -- 注意:目前使用自增ID,若使用分布式架构应改为雪花ID
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '快递单明细ID',

  -- ========== 关联发运计划信息 ==========
  -- 发运计划ID,外键关联t_shipment表
  `shipment_id` BIGINT NOT NULL COMMENT '发运计划ID',
  -- 发运计划号(冗余字段,便于查询和展示)
  `shipment_no` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '发运计划号(冗余字段)',

  -- ========== 快递单基本信息 ==========
  -- 物流单号,承运商提供的物流单号,用于标识快递单
  -- 注意:同一个快递单的多个记录(包含多个订单行产品)共享相同的物流单号
  -- 业务层需保证:相同物流单号的记录具有相同的承运商、收货方等快递单信息
  `logistics_no` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '物流单号(快递单标识)',
  -- 明细行号,快递单内的明细行序号,用于排序和展示,从1开始递增
  -- 注意:同一快递单(相同logistics_no)内行号应唯一,业务层需保证行号的唯一性和连续性
  `line_no` INT NOT NULL COMMENT '明细行号',

  -- ========== 关联订单行信息 ==========
  -- 订单行ID,外键关联t_order_line表。一个快递单可以包含多个订单行的产品
  `order_line_id` BIGINT NOT NULL COMMENT '订单行ID',
  -- 订单ID(冗余字段,便于查询和展示)
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  -- 订单号(冗余字段,便于查询和展示)
  `order_no` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '订单号(冗余字段)',

  -- ========== 关联承运商信息 ==========
  -- 承运商ID,外键关联t_carrier表。快递单的承运商
  -- 注意:同一快递单(相同logistics_no)的所有记录应具有相同的承运商信息
  `carrier_id` BIGINT NOT NULL COMMENT '承运商ID',
  -- 承运商名称(冗余字段,便于查询和展示)
  `carrier_name` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '承运商名称(冗余字段)',
  -- 承运商编码(冗余字段,便于查询和展示)
  `carrier_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '承运商编码(冗余字段)',

  -- ========== 收货方信息 ==========
  -- 收货方名称,直接存储在快递单中,不关联独立实体
  -- 注意:同一快递单(相同logistics_no)的所有记录应具有相同的收货方信息
  `receiver_name` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '收货方名称',
  -- 收货方编码,直接存储在快递单中,不关联独立实体
  `receiver_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '收货方编码',

  -- ========== 产品信息(冗余字段,快照) ==========
  -- 产品编码(冗余字段,快照发运时的产品编码,避免产品信息变更影响历史发运记录)
  `product_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '产品编码(冗余字段)',
  -- 产品名称(冗余字段,快照发运时的产品名称,避免产品信息变更影响历史发运记录)
  `product_name` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '产品名称(冗余字段)',
  -- 规格型号(冗余字段,快照发运时的规格型号)
  `specification` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '规格型号(冗余字段)',
  -- 单位(冗余字段,快照发运时的单位)
  `unit` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '单位(冗余字段)',

  -- ========== 数量信息 ==========
  -- 发运数量,本次快递单中该订单行产品的发运数量
  -- 注意:发运数量必须大于0,且发运数量≤订单行数量,业务层需校验
  `quantity` DECIMAL(18, 3) NOT NULL DEFAULT 0.000 COMMENT '发运数量',

  -- ========== 提货信息 ==========
  -- 提货地址(完整地址)
  -- 注意:同一快递单(相同logistics_no)的所有记录应具有相同的提货信息
  `pickup_address` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '提货地址',
  -- 提货联系人
  `pickup_contact` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '提货联系人',
  -- 提货联系电话,格式:可选+号开头,7-20位数字(如:13800138000、+8613800138000)
  -- 注意:MySQL 8.0+支持CHECK约束,MySQL 5.7及以下版本需在业务层校验格式
  `pickup_phone` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '提货联系电话',
  -- 计划提货时间
  -- 使用NULL表示未设置计划提货时间,必须区分"未设置"和"已设置为空"的业务含义,因此允许使用NULL
  `planned_pickup_time` DATETIME(3) NULL DEFAULT NULL COMMENT '计划提货时间:NULL表示未设置计划提货时间',
  -- 实际提货时间
  -- 使用NULL表示未完成提货,必须区分"未完成"和"已完成但时间为空"的业务含义,因此允许使用NULL
  `actual_pickup_time` DATETIME(3) NULL DEFAULT NULL COMMENT '实际提货时间:NULL表示未完成提货',

  -- ========== 到货信息 ==========
  -- 到货地址(完整地址),通常与收货方地址一致,但可能不同
  -- 注意:同一快递单(相同logistics_no)的所有记录应具有相同的到货信息
  `delivery_address` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '到货地址',
  -- 到货联系人
  `delivery_contact` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '到货联系人',
  -- 到货联系电话,格式:可选+号开头,7-20位数字(如:13800138000、+8613800138000)
  -- 注意:MySQL 8.0+支持CHECK约束,MySQL 5.7及以下版本需在业务层校验格式
  `delivery_phone` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '到货联系电话',
  -- 计划到货时间
  -- 使用NULL表示未设置计划到货时间,必须区分"未设置"和"已设置为空"的业务含义,因此允许使用NULL
  `planned_delivery_time` DATETIME(3) NULL DEFAULT NULL COMMENT '计划到货时间:NULL表示未设置计划到货时间',
  -- 实际到货时间
  -- 使用NULL表示未完成到货,必须区分"未完成"和"已完成但时间为空"的业务含义,因此允许使用NULL
  `actual_delivery_time` DATETIME(3) NULL DEFAULT NULL COMMENT '实际到货时间:NULL表示未完成到货',

  -- ========== 发运路线信息 ==========
  -- 起运地省份
  `origin_province` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '起运地省份',
  -- 起运地城市
  `origin_city` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '起运地城市',
  -- 目的地省份
  `destination_province` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '目的地省份',
  -- 目的地城市
  `destination_city` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '目的地城市',
  -- 运输距离(单位:公里)
  -- 使用NULL表示未计算运输距离,必须区分"未计算"和"已计算为0"的业务含义,因此允许使用NULL
  `distance` DECIMAL(10, 2) NULL DEFAULT NULL COMMENT '运输距离(公里):NULL表示未计算运输距离',
  -- 运输方式,如:公路运输、铁路运输、航空运输等
  `transport_mode` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '运输方式',

  -- ========== 快递单状态信息 ==========
  -- 【状态机管理设计】快递单状态关联状态字典表,实现状态统一管理
  -- 关系:ShipmentLine N:1 StatusDict (快递单关联状态字典)
  -- 快递单状态字典ID,外键关联t_status_dict表。必须关联状态字典,保证状态定义统一
  -- 注意:默认值需要在业务层初始化时设置,关联到状态字典表中status_code='PENDING'且business_type='shipment_line'的记录
  -- 注意:同一快递单(相同logistics_no)的所有记录应具有相同的状态
  `status_dict_id` BIGINT NOT NULL COMMENT '快递单状态字典ID',
  -- 快递单状态代码(冗余字段,便于查询和展示)。从StatusDict表中同步,避免频繁JOIN查询
  -- 注意:此字段应与status_dict_id关联的状态字典记录中的status_code保持一致
  -- 默认值:PENDING(待发运),待发运快递单已创建但未开始发运,避免使用NULL防止索引失效和简化查询逻辑
  `status_code` VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT '快递单状态代码(冗余字段):默认PENDING表示待发运状态',
  -- 快递单状态名称(冗余字段,便于展示)。从StatusDict表中同步,避免频繁JOIN查询
  -- 注意:此字段应与status_dict_id关联的状态字典记录中的status_name保持一致
  -- 默认值:待发运,与status_code的默认值PENDING对应
  `status_name` VARCHAR(50) NOT NULL DEFAULT '待发运' COMMENT '快递单状态名称(冗余字段):默认待发运',

  -- ========== 备注信息 ==========
  -- 快递单备注
  `remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',

  -- ========== 扩展信息 ==========
  -- 扩展信息,JSON格式存储扩展字段,便于扩展且无需修改表结构
  -- 注意:仅支持MySQL 5.7+版本
  -- 使用NULL表示无扩展信息,避免空JSON对象占用存储空间,且NULL可以明确区分"无扩展信息"和"空扩展信息"
  `extra_info` JSON DEFAULT NULL COMMENT '扩展信息:NULL表示无扩展信息',

  -- ========== 公共字段 ==========
  -- 创建时间(精确到毫秒),记录快递单明细创建的时间
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  -- 创建人ID,记录快递单明细的创建人,外键关联t_user表。-1表示系统自动操作,0及以上表示用户ID
  -- 默认-1表示系统创建,避免使用NULL防止空指针问题和简化查询逻辑
  `created_by` BIGINT NOT NULL DEFAULT -1 COMMENT '创建人ID:默认-1表示系统创建',
  -- 更新时间(精确到毫秒),记录快递单明细信息最后更新的时间
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  -- 更新人ID,记录快递单明细信息的最后更新人,外键关联t_user表。-1表示系统自动操作,0及以上表示用户ID
  -- 默认-1表示系统更新,避免使用NULL防止空指针问题和简化查询逻辑
  `updated_by` BIGINT NOT NULL DEFAULT -1 COMMENT '更新人ID:默认-1表示系统更新',
  -- 是否删除:0-未删除,1-已删除
  -- 默认0表示未删除,避免使用NULL防止索引失效和简化查询逻辑
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除,1-已删除',

  PRIMARY KEY (`id`),

  -- ========== 外键约束 ==========
  -- 关联发运计划表、订单行表、承运商表、状态字典表,保证数据一致性
  -- 注意:外键约束可选择性启用,取决于业务需求。启用外键可保证数据一致性,但可能影响性能
  -- 注意:收货方信息直接存储在表中,不再关联独立的收货方表
  -- CONSTRAINT `fk_shipment_line_shipment` FOREIGN KEY (`shipment_id`) REFERENCES `t_shipment` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  -- CONSTRAINT `fk_shipment_line_order_line` FOREIGN KEY (`order_line_id`) REFERENCES `t_order_line` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  -- CONSTRAINT `fk_shipment_line_carrier` FOREIGN KEY (`carrier_id`) REFERENCES `t_carrier` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  -- CONSTRAINT `fk_shipment_line_status_dict` FOREIGN KEY (`status_dict_id`) REFERENCES `t_status_dict` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,

  -- ========== 约束设计 ==========
  -- 联系电话格式约束(MySQL 8.0+),格式:可选+号开头,7-20位数字
  -- 允许空字符串或符合格式的字符串(如:13800138000、+8613800138000)
  -- 注意:MySQL 5.7及以下版本不支持CHECK约束,需在业务层使用正则校验(如:/^\+?[0-9]{7,20}$/)
  CONSTRAINT `ck_shipment_line_pickup_phone` CHECK (`pickup_phone` = '' OR `pickup_phone` REGEXP '^\\+?[0-9]{7,20}$'),
  CONSTRAINT `ck_shipment_line_delivery_phone` CHECK (`delivery_phone` = '' OR `delivery_phone` REGEXP '^\\+?[0-9]{7,20}$'),

  -- 物流单号+明细行号唯一约束,保证同一快递单内明细行号唯一
  -- 注意:即使快递单明细被软删除,行号也不应重复使用,以保证业务逻辑的清晰性
  UNIQUE KEY `uk_shipment_line_logistics_line_no` (`logistics_no`, `line_no`, `is_deleted`) COMMENT '快递单明细行号唯一约束',

  -- ========== 索引设计 ==========
  -- 索引设计原则:只保留核心高频查询场景的索引,避免过度索引导致写入性能下降
  -- InnoDB联合索引遵循最左前缀原则,单字段索引被联合索引覆盖时需删除以避免冗余

  -- 发运计划ID+软删除联合索引,用于查询发运计划的所有快递单(高频场景:查询发运计划的快递单)
  KEY `idx_shipment_line_shipment_id` (`shipment_id`, `is_deleted`) COMMENT '发运计划ID索引',

  -- 物流单号+软删除联合索引,用于查询快递单的所有明细(高频场景:查询快递单详情)
  KEY `idx_shipment_line_logistics_no` (`logistics_no`, `is_deleted`) COMMENT '物流单号索引',

  -- 订单行ID+软删除联合索引,用于查询订单行的所有快递单(高频场景:查询订单行的快递单)
  KEY `idx_shipment_line_order_line_id` (`order_line_id`, `is_deleted`) COMMENT '订单行ID索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='快递单表:快递单表,每个记录代表一个快递单中的一个订单行产品。'
  '【业务说明】本表记录的是实际的快递单信息,包含承运商、收货方、物流单号等具体信息。'
  '快递单 = 一个承运商 + 一个收货方 + 一个物流单号。一个快递单可以包含多个订单行的产品,'
  '通过物流单号(logistics_no)字段关联。同一快递单(相同logistics_no)的所有记录应具有相同的承运商、收货方、状态等信息。'
  '【关联关系】每个快递单明细关联一个发运计划、一个订单行、一个承运商。'
  '收货方信息(receiver_name、receiver_code)直接存储在表中,不再关联独立的收货方实体。'
  '快递单明细通过order_line_id字段关联订单行,保证发运数量≤订单行数量。'
  '一个订单行可以对应多个快递单,支持分批次运输。'
  '【产品信息】产品信息(product_code、product_name、specification、unit)作为冗余字段保存发运时的快照,'
  '避免产品信息变更影响历史发运记录。'
  '【状态机管理】本表通过status_dict_id关联t_status_dict表,实现状态统一管理。status_code和status_name字段作为冗余字段便于查询和展示,'
  '应与状态字典表中的对应字段保持一致。快递单可以关联多个状态日志(ShipmentLineStatusLog)记录状态变更历史。';

