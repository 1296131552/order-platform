-- ============================================================
-- 订单行表 (t_order_line)
-- 说明:订单明细表,包含订单行的产品信息、数量、金额、供应商关联、收货地址等核心数据
-- ============================================================

CREATE TABLE `t_order_line` (
  -- ========== 主键 ==========
  -- 订单行ID,主键,自增。每条订单行记录的唯一标识
  -- 注意:目前使用自增ID,若使用分布式架构应改为雪花ID
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单行ID',

  -- ========== 关联订单信息 ==========
  -- 订单ID,外键关联t_order表
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  -- 行号,订单内的行序号,用于排序和展示,从1开始递增
  -- 注意:同一订单内行号应唯一,业务层需保证行号的唯一性和连续性
  `line_no` INT NOT NULL COMMENT '行号',

  -- ========== 产品信息 ==========
  -- 产品编码,订单行直接录入的产品编码,无约束可自由填写
  `product_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '产品编码',
  -- 产品名称,订单行直接录入的产品名称,无约束可自由填写
  `product_name` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '产品名称',
  -- 规格型号,订单行直接录入的规格型号信息
  `specification` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '规格型号',
  -- 单位,如:吨、公斤、件、箱、包等,订单行直接录入
  `unit` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '单位',

  -- ========== 关联供应商信息 ==========
  -- 供应商ID,外键关联t_supplier表。每个订单行对应一个供应商(订单创建时确定)
  `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
  -- 供应商名称(冗余字段,便于查询和展示)
  `supplier_name` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '供应商名称(冗余字段)',
  -- 供应商编码(冗余字段,便于查询和展示)
  `supplier_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '供应商编码(冗余字段)',

  -- ========== 收货地址信息 ==========
  -- 收货地址(完整地址),订单行级别的收货地址,支持一个订单有多个收货地址
  `delivery_address` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '收货地址',
  -- 收货联系人
  `delivery_contact` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '收货联系人',
  -- 收货联系电话,格式:可选+号开头,7-20位数字(如:13800138000、+8613800138000)
  -- 注意:MySQL 8.0+支持CHECK约束,MySQL 5.7及以下版本需在业务层校验格式
  `delivery_phone` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '收货联系电话',

  -- ========== 数量金额信息 ==========
  -- 数量,订单行的产品数量
  -- 注意:数量必须大于0,业务层需校验
  `quantity` DECIMAL(18, 3) NOT NULL DEFAULT 0.000 COMMENT '数量',
  -- 单价(精确到分),订单行的产品单价
  `unit_price` DECIMAL(18, 2) NOT NULL DEFAULT 0.00 COMMENT '单价',
  -- 金额(精确到分),订单行的总金额=数量×单价
  -- 注意:金额应等于数量×单价,业务层需校验计算正确性
  `total_amount` DECIMAL(18, 2) NOT NULL DEFAULT 0.00 COMMENT '金额',
  -- 币种,如:CNY(人民币)、USD(美元)。通常与订单表的币种一致,冗余存储便于查询
  `currency` VARCHAR(10) NOT NULL DEFAULT 'CNY' COMMENT '币种',

  -- ========== 订单行状态信息 ==========
  -- 【状态机管理设计】订单行状态关联状态字典表,实现状态统一管理
  -- 关系:OrderLine N:1 StatusDict (订单行关联状态字典)
  -- 订单行状态字典ID,外键关联t_status_dict表。必须关联状态字典,保证状态定义统一
  -- 注意:默认值需要在业务层初始化时设置,关联到状态字典表中status_code='DRAFT'且business_type='order_line'的记录
  `status_dict_id` BIGINT NOT NULL COMMENT '订单行状态字典ID',
  -- 订单行状态代码(冗余字段,便于查询和展示)。从StatusDict表中同步,避免频繁JOIN查询
  -- 注意:此字段应与status_dict_id关联的状态字典记录中的status_code保持一致
  -- 默认值:DRAFT(草稿),草稿订单行仅录入基本信息,未最终确认,避免使用NULL防止索引失效和简化查询逻辑
  `status_code` VARCHAR(50) NOT NULL DEFAULT 'DRAFT' COMMENT '订单行状态代码(冗余字段):默认DRAFT表示草稿状态',
  -- 订单行状态名称(冗余字段,便于展示)。从StatusDict表中同步,避免频繁JOIN查询
  -- 注意:此字段应与status_dict_id关联的状态字典记录中的status_name保持一致
  -- 默认值:草稿,与status_code的默认值DRAFT对应
  `status_name` VARCHAR(50) NOT NULL DEFAULT '草稿' COMMENT '订单行状态名称(冗余字段):默认草稿',

  -- ========== 备注信息 ==========
  -- 订单行备注
  `remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
  -- 内部备注(不对外展示)
  `internal_remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '内部备注',

  -- ========== 扩展信息 ==========
  -- 扩展信息,JSON格式存储扩展字段,便于扩展且无需修改表结构
  -- 注意:仅支持MySQL 5.7+版本
  -- 使用NULL表示无扩展信息,避免空JSON对象占用存储空间,且NULL可以明确区分"无扩展信息"和"空扩展信息"
  `extra_info` JSON DEFAULT NULL COMMENT '扩展信息:NULL表示无扩展信息',

  -- ========== 公共字段 ==========
  -- 创建时间(精确到毫秒),记录订单行创建的时间
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  -- 创建人ID,记录订单行的创建人,外键关联t_user表。-1表示系统自动操作,0及以上表示用户ID
  -- 默认-1表示系统创建,避免使用NULL防止空指针问题和简化查询逻辑
  `created_by` BIGINT NOT NULL DEFAULT -1 COMMENT '创建人ID:默认-1表示系统创建',
  -- 更新时间(精确到毫秒),记录订单行信息最后更新的时间
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  -- 更新人ID,记录订单行信息的最后更新人,外键关联t_user表。-1表示系统自动操作,0及以上表示用户ID
  -- 默认-1表示系统更新,避免使用NULL防止空指针问题和简化查询逻辑
  `updated_by` BIGINT NOT NULL DEFAULT -1 COMMENT '更新人ID:默认-1表示系统更新',
  -- 是否删除:0-未删除,1-已删除
  -- 默认0表示未删除,避免使用NULL防止索引失效和简化查询逻辑
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除,1-已删除',

  PRIMARY KEY (`id`),

  -- ========== 外键约束 ==========
  -- 关联订单表、供应商表、状态字典表,保证数据一致性
  -- 注意:外键约束可选择性启用,取决于业务需求。启用外键可保证数据一致性,但可能影响性能
  -- 注意:状态字典表关联,实现状态统一管理
  -- CONSTRAINT `fk_order_line_order` FOREIGN KEY (`order_id`) REFERENCES `t_order` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  -- CONSTRAINT `fk_order_line_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `t_supplier` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  -- CONSTRAINT `fk_order_line_status_dict` FOREIGN KEY (`status_dict_id`) REFERENCES `t_status_dict` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,

  -- ========== 约束设计 ==========
  -- 收货联系电话格式约束(MySQL 8.0+),格式:可选+号开头,7-20位数字
  -- 允许空字符串或符合格式的字符串(如:13800138000、+8613800138000)
  -- 注意:MySQL 5.7及以下版本不支持CHECK约束,需在业务层使用正则校验(如:/^\+?[0-9]{7,20}$/)
  CONSTRAINT `ck_order_line_delivery_phone` CHECK (`delivery_phone` = '' OR `delivery_phone` REGEXP '^\\+?[0-9]{7,20}$'),

  -- 订单ID+行号唯一约束,保证同一订单内行号唯一
  -- 注意:即使订单行被软删除,行号也不应重复使用,以保证业务逻辑的清晰性
  UNIQUE KEY `uk_order_line_order_line_no` (`order_id`, `line_no`) COMMENT '订单行号唯一约束',

  -- ========== 索引设计 ==========
  -- 索引设计原则:只保留核心查询场景的索引,避免过度索引导致写入性能下降
  -- InnoDB联合索引遵循最左前缀原则,单字段索引被联合索引覆盖时需删除以避免冗余

  -- 订单ID+状态+软删除联合索引,用于查询订单的订单行并按状态筛选(高频场景:查询订单的订单行)
  -- 覆盖场景:按订单ID查询订单行、按订单ID+状态查询订单行、按订单ID+删除标记查询订单行
  -- 注意:使用status_code冗余字段进行索引,避免JOIN查询状态字典表
  KEY `idx_order_line_order_status` (`order_id`, `status_code`, `is_deleted`) COMMENT '订单状态联合索引',

  -- 供应商ID+软删除联合索引,用于按供应商查询订单行(统计场景:查询某个供应商的所有订单行)
  KEY `idx_order_line_supplier_id` (`supplier_id`, `is_deleted`) COMMENT '供应商ID索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='订单行表:订单明细表,包含订单行的产品信息、数量、金额、供应商关联、收货地址等核心数据。'
  '【关联关系】每个订单行关联一个订单、一个供应商。一个订单行可以对应多个发运明细(ShipmentLine),支持分批次运输,'
  '发运明细通过order_line_id字段关联订单行,保证所有发运明细的数量总和≤订单行数量。'
  '【地址信息】收货地址信息(delivery_address、delivery_contact、delivery_phone)存储在订单行级别,支持一个订单有多个收货地址。'
  '【产品信息】产品信息(product_code、product_name、specification、unit)直接录入,无约束可自由填写,不关联产品表。'
  '【状态机管理】本表通过status_dict_id关联t_status_dict表,实现状态统一管理。status_code和status_name字段作为冗余字段便于查询和展示,'
  '应与状态字典表中的对应字段保持一致。订单行可以关联多个状态日志(OrderLineStatusLog)记录状态变更历史。';

