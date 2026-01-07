-- ============================================================
-- 供应商表 (t_supplier)
-- 说明:合作方-供应商信息表,记录供应商基本信息、联系方式、地址等
-- ============================================================

CREATE TABLE `t_supplier` (
  -- ========== 主键 ==========
  -- 供应商ID,主键,自增。每条供应商记录的唯一标识
  -- 注意:目前使用自增ID,若使用分布式架构应改为雪花ID
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '供应商ID',
  
  -- ========== 基本信息 ==========
  -- 供应商编码,业务唯一标识,如:SUP001
  `supplier_code` VARCHAR(50) NOT NULL COMMENT '供应商编码',
  -- 供应商名称,企业全称
  `supplier_name` VARCHAR(200) NOT NULL COMMENT '供应商名称',
  -- 供应商简称,用于展示
  `supplier_short_name` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '供应商简称',
  
  -- ========== 企业信息(后续根据实际修改) ==========
  -- 统一社会信用代码,18位,用于企业身份识别
  -- 默认空字符串表示未填写,避免使用NULL防止索引失效和简化查询逻辑
  `unified_social_credit_code` VARCHAR(18) NOT NULL DEFAULT '' COMMENT '统一社会信用代码:默认空字符串表示未填写',
  -- 营业执照号(冗余字段)
  -- 默认空字符串表示未填写,避免使用NULL防止索引失效和简化查询逻辑
  `business_license_no` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '营业执照号:默认空字符串表示未填写',
  -- 企业类型,如:有限责任公司、股份有限公司、个体工商户等
  -- 默认空字符串表示未填写,避免使用NULL防止索引失效和简化查询逻辑
  `enterprise_type` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '企业类型:默认空字符串表示未填写',
  -- 注册资本(单位:元)
  -- 使用NULL表示未填写注册资本,必须区分"未填写"和"已填写为0"的业务含义,因此允许使用NULL
  `registered_capital` DECIMAL(18, 2) NULL DEFAULT NULL COMMENT '注册资本:NULL表示未填写注册资本',
  -- 成立日期
  -- 使用NULL表示未填写成立日期,必须区分"未填写"和"已填写为空"的业务含义,因此允许使用NULL
  `establishment_date` DATE NULL DEFAULT NULL COMMENT '成立日期:NULL表示未填写成立日期',
  -- 经营范围
  `business_scope` VARCHAR(1000) NOT NULL DEFAULT '' COMMENT '经营范围',
  
  -- ========== 联系方式 ==========
  -- 联系人姓名
  `contact_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '联系人姓名',
  -- 联系人电话,格式:可选+号开头,7-20位数字(如:13800138000、+8613800138000)
  -- 注意:MySQL 8.0+支持CHECK约束,MySQL 5.7及以下版本需在业务层校验格式
  `contact_phone` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '联系人电话',
  -- 联系人邮箱
  `contact_email` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '联系人邮箱',
  -- 联系电话(企业电话)
  `phone` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '联系电话',
  
  -- ========== 地址信息 ==========
  -- 省份,如:北京市、上海市、广东省
  `address_province` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '省份',
  -- 城市,如:北京市、上海市、广州市
  `address_city` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '城市',
  -- 区县,如:朝阳区、浦东新区、天河区
  `address_district` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '区县',
  -- 详细地址,街道、门牌号等
  `address_detail` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '详细地址',
  -- 邮政编码
  `postal_code` VARCHAR(10) NOT NULL DEFAULT '' COMMENT '邮政编码',
  
  -- ========== 状态信息 ==========
  -- 供应商状态:ACTIVE(启用)、INACTIVE(停用)、SUSPENDED(暂停)
  -- 用于控制供应商是否可用,停用后不能关联新订单
  -- 默认ACTIVE表示启用状态,避免使用NULL防止索引失效和简化查询逻辑
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '供应商状态:默认ACTIVE表示启用状态',
  
  -- ========== 评级信息 ==========
  -- 供应商等级,如:A级、B级、C级,用于供应商分类管理
  `supplier_level` VARCHAR(10) NOT NULL DEFAULT '' COMMENT '供应商等级',

  
  -- ========== 备注信息 ==========
  -- 备注信息,存储供应商相关的补充说明
  `remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备注',
  -- 内部备注(不对外展示)
  `internal_remark` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '内部备注',
  
  -- ========== 扩展信息 ==========
  -- 扩展信息,JSON格式存储扩展字段,便于扩展且无需修改表结构
  -- 存储业务自定义字段、临时数据等。示例:{"custom_field1": "value1", "custom_field2": 123}
  -- 注意:仅支持MySQL 5.7+版本
  -- 使用NULL表示无扩展信息,避免空JSON对象占用存储空间,且NULL可以明确区分"无扩展信息"和"空扩展信息"
  `extra_info` JSON DEFAULT NULL COMMENT '扩展信息:NULL表示无扩展信息',
  
  -- ========== 公共字段 ==========
  -- 创建时间(精确到毫秒),记录供应商创建的时间
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  -- 创建人ID,记录供应商的创建人,外键关联t_user表。-1表示系统自动操作,0及以上表示用户ID
  -- 默认-1表示系统创建,避免使用NULL防止空指针问题和简化查询逻辑
  `created_by` BIGINT NOT NULL DEFAULT -1 COMMENT '创建人ID:默认-1表示系统创建',
  -- 更新时间(精确到毫秒),记录供应商信息最后更新的时间
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  -- 更新人ID,记录供应商信息的最后更新人,外键关联t_user表。-1表示系统自动操作,0及以上表示用户ID
  -- 默认-1表示系统更新,避免使用NULL防止空指针问题和简化查询逻辑
  `updated_by` BIGINT NOT NULL DEFAULT -1 COMMENT '更新人ID:默认-1表示系统更新',
  -- 是否删除:0-未删除,1-已删除(软删除)
  -- 默认0表示未删除,避免使用NULL防止索引失效和简化查询逻辑
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除,1-已删除',
  
  PRIMARY KEY (`id`),
  
  -- ========== 约束设计 ==========
  -- 联系电话格式约束(MySQL 8.0+),格式:可选+号开头,7-20位数字
  -- 允许空字符串或符合格式的字符串(如:13800138000、+8613800138000)
  -- 注意:MySQL 5.7及以下版本不支持CHECK约束,需在业务层使用正则校验(如:/^\+?[0-9]{7,20}$/)
  CONSTRAINT `ck_supplier_contact_phone` CHECK (`contact_phone` = '' OR `contact_phone` REGEXP '^\\+?[0-9]{7,20}$'),
  CONSTRAINT `ck_supplier_phone` CHECK (`phone` = '' OR `phone` REGEXP '^\\+?[0-9]{7,20}$'),
  
  -- ========== 索引设计 ==========
  -- 索引设计原则:只保留核心查询场景的索引,避免过度索引导致写入性能下降
  -- InnoDB联合索引遵循最左前缀原则,单字段索引被联合索引覆盖时需删除以避免冗余
  
  -- 供应商编码唯一索引
  UNIQUE KEY `uk_supplier_code` (`supplier_code`) COMMENT '供应商编码唯一索引',
  
  -- 统一社会信用代码唯一索引(用于企业身份识别)
  UNIQUE KEY `uk_supplier_unified_social_credit_code` (`unified_social_credit_code`) COMMENT '统一社会信用代码唯一索引',
  
  -- 供应商名称索引(用于模糊查询)
  KEY `idx_supplier_name` (`supplier_name`) COMMENT '供应商名称索引',
  
  -- 状态+软删除联合索引,用于查询启用的供应商
  KEY `idx_supplier_status` (`status`, `is_deleted`) COMMENT '状态软删除联合索引',
  
  -- 供应商等级+状态+软删除联合索引,用于按等级查询供应商
  KEY `idx_supplier_level_status` (`supplier_level`, `status`, `is_deleted`) COMMENT '供应商等级状态联合索引',
  
  -- 创建时间+软删除联合索引,用于时间排序和统计查询
  KEY `idx_supplier_created` (`created_at`, `is_deleted`) COMMENT '创建时间软删除联合索引'
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='供应商表:合作方-供应商信息表,记录供应商基本信息、企业信息、联系方式、地址、状态等。'
  '供应商通过订单行关联订单,可关联附件(资质文件)和履约统计。';

