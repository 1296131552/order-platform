-- ============================================================
-- 合作方绩效明细表 (t_partner_performance_detail)
-- 说明:记录每次订单/发运的绩效明细,用于实时统计和追溯
-- ============================================================

CREATE TABLE `t_partner_performance_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '绩效明细ID',
  
  -- ========== 关联合作方信息 ==========
  `partner_type` ENUM('SUPPLIER', 'CARRIER') NOT NULL COMMENT '合作方类型',
  `partner_id` BIGINT NOT NULL COMMENT '合作方ID',
  `partner_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '合作方编码',
  `partner_name` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '合作方名称',
  
  -- ========== 关联业务对象 ==========
  -- 关联业务类型:ORDER(订单)、SHIPMENT(发运批次)
  `related_business_type` ENUM('ORDER', 'SHIPMENT') NOT NULL COMMENT '关联业务类型',
  `related_business_id` BIGINT NOT NULL COMMENT '关联业务对象ID',
  `related_business_no` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '关联业务对象编号',
  
  -- ========== 时间信息 ==========
  -- 订单创建时间(或发运开始时间)
  `business_start_time` DATETIME(3) NOT NULL COMMENT '业务开始时间',
  -- 订单完成时间(或签收时间)
  -- 使用NULL表示业务未完成,必须区分"未完成"和"已完成但时间为空"的业务含义,因此允许使用NULL
  `business_end_time` DATETIME(3) NULL DEFAULT NULL COMMENT '业务结束时间:NULL表示业务未完成',
  -- 期望完成时间(期望交货日期)
  -- 使用NULL表示未设置期望完成时间,必须区分"未设置"和"已设置为空"的业务含义,因此允许使用NULL
  `expected_end_time` DATETIME(3) NULL DEFAULT NULL COMMENT '期望完成时间:NULL表示未设置期望完成时间',
  
  -- ========== 绩效指标 ==========
  -- 是否按时完成:1-按时,0-未按时
  -- 按时指:business_end_time <= expected_end_time
  `is_on_time` TINYINT NOT NULL DEFAULT 0 COMMENT '是否按时完成:1-按时,0-未按时',
  -- 是否有异常:1-有异常,0-无异常
  `has_exception` TINYINT NOT NULL DEFAULT 0 COMMENT '是否有异常:1-有异常,0-无异常',
  -- 异常类型,如:DELAY(运输延迟)、DIFFERENCE(到货差异)、DOCUMENT_MISSING(凭证缺失)
  `exception_type` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '异常类型',
  
  -- ========== 公共字段 ==========
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  -- 默认0表示未删除,避免使用NULL防止索引失效和简化查询逻辑
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除,1-已删除',
  
  PRIMARY KEY (`id`),
  
  -- ========== 索引设计 ==========
  -- 合作方+业务对象唯一索引
  UNIQUE KEY `uk_partner_performance_detail_business` 
    (`partner_type`, `partner_id`, `related_business_type`, `related_business_id`) 
    COMMENT '合作方业务对象唯一索引',
  
  -- 合作方+时间+软删除联合索引,用于查询合作方的绩效历史
  KEY `idx_partner_performance_detail_partner_time` 
    (`partner_type`, `partner_id`, `business_end_time`, `is_deleted`) 
    COMMENT '合作方时间联合索引',
  
  -- 业务对象查询索引
  KEY `idx_partner_performance_detail_business` 
    (`related_business_type`, `related_business_id`, `is_deleted`) 
    COMMENT '业务对象查询索引'
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='合作方绩效明细表:记录每次订单/发运的绩效明细,用于实时统计和追溯。';

-- ============================================================
-- 合作方绩效统计表 (t_partner_performance)
-- 说明:统计供应商和承运商的绩效指标,用于看板展示和排行榜查询
-- ============================================================

CREATE TABLE `t_partner_performance` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '绩效统计ID',
  
  -- ========== 关联合作方信息 ==========
  `partner_type` ENUM('SUPPLIER', 'CARRIER') NOT NULL COMMENT '合作方类型',
  `partner_id` BIGINT NOT NULL COMMENT '合作方ID',
  `partner_code` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '合作方编码',
  `partner_name` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '合作方名称',
  
  -- ========== 统计周期信息 ==========
  -- 统计日期(按日统计)
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  -- 统计周期:DAY(日)、WEEK(周)、MONTH(月)、YEAR(年)
  `stat_period` ENUM('DAY', 'WEEK', 'MONTH', 'YEAR') NOT NULL DEFAULT 'DAY' COMMENT '统计周期',
  
  -- ========== 订单统计指标 ==========
  -- 订单总数(或发运批次总数)
  `total_count` INT NOT NULL DEFAULT 0 COMMENT '订单总数(或发运批次总数)',
  -- 完成订单数(已签收)
  `completed_count` INT NOT NULL DEFAULT 0 COMMENT '完成订单数',
  
  -- ========== 绩效指标(核心指标) ==========
  -- 准时率:按时签收订单数/总完成订单数×100%
  -- 计算公式:on_time_count / completed_count × 100
  -- 使用NULL表示未计算或无可统计数据(如completed_count=0),必须区分"未计算"和"已计算为0%"的业务含义,因此允许使用NULL
  `on_time_rate` DECIMAL(5, 2) NULL DEFAULT NULL COMMENT '准时率(%):NULL表示未计算或无可统计数据',
  -- 按时签收订单数
  -- 默认0表示无按时签收订单,避免使用NULL防止索引失效和简化查询逻辑
  `on_time_count` INT NOT NULL DEFAULT 0 COMMENT '按时签收订单数:默认0表示无按时签收订单',
  
  -- 异常率:异常订单数/订单总数×100%
  -- 计算公式:exception_count / total_count × 100
  -- 使用NULL表示未计算或无可统计数据(如total_count=0),必须区分"未计算"和"已计算为0%"的业务含义,因此允许使用NULL
  `exception_rate` DECIMAL(5, 2) NULL DEFAULT NULL COMMENT '异常率(%):NULL表示未计算或无可统计数据',
  -- 异常订单数(存在到货差异或运输异常的订单数)
  -- 默认0表示无异常订单,避免使用NULL防止索引失效和简化查询逻辑
  `exception_count` INT NOT NULL DEFAULT 0 COMMENT '异常订单数:默认0表示无异常订单',
  
  -- ========== 版本号(乐观锁) ==========
  -- 默认0表示初始版本,避免使用NULL防止索引失效和简化查询逻辑
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号,用于乐观锁控制并发:默认0表示初始版本',
  
  -- ========== 公共字段 ==========
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  -- 默认0表示未删除,避免使用NULL防止索引失效和简化查询逻辑
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除,1-已删除',
  
  PRIMARY KEY (`id`),
  
  -- ========== 索引设计 ==========
  -- 合作方+统计周期唯一索引
  UNIQUE KEY `uk_partner_performance_partner_period` 
    (`partner_type`, `partner_id`, `stat_date`, `stat_period`) 
    COMMENT '合作方统计周期唯一索引',
  
  -- 准时率排行榜索引(承运商准时率排行榜)
  KEY `idx_partner_performance_on_time_rate` 
    (`partner_type`, `stat_date`, `stat_period`, `on_time_rate` DESC, `is_deleted`) 
    COMMENT '准时率排行榜索引',
  
  -- 异常率排行榜索引(供应商异常率排行榜)
  KEY `idx_partner_performance_exception_rate` 
    (`partner_type`, `stat_date`, `stat_period`, `exception_rate` ASC, `is_deleted`) 
    COMMENT '异常率排行榜索引',
  
  -- 合作方查询索引
  KEY `idx_partner_performance_partner` 
    (`partner_type`, `partner_id`, `stat_date`, `stat_period`, `is_deleted`) 
    COMMENT '合作方查询索引'
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='合作方绩效统计表:统计供应商和承运商的绩效指标(准时率、异常率),用于看板展示和排行榜查询。'
  '支持按日/周/月/年统计。';