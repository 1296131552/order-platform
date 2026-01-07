-- ============================================================
-- 状态字典表 (t_status_dict)
-- 说明:统一管理所有业务实体的状态定义,支持状态机管理
-- 关系:StatusDict 1:N OrderStatusLog/ShipmentStatusLog/ReceiptStatusLog
-- ============================================================

CREATE TABLE `t_status_dict` (
  -- ========== 主键 ==========
  -- 状态字典ID,主键,自增
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '状态字典ID',
  
  -- ========== 状态基本信息 ==========
  -- 状态代码,唯一标识状态。示例:DRAFT、EXECUTING、COMPLETED等
  -- 注意:同一业务类型下状态代码必须唯一
  `status_code` VARCHAR(50) NOT NULL COMMENT '状态代码',
  -- 状态名称(中文),用于展示。示例:草稿、执行中、已完成
  `status_name` VARCHAR(50) NOT NULL COMMENT '状态名称',
  -- 状态描述,详细说明该状态的含义和用途
  `status_description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '状态描述',
  
  -- ========== 业务类型分类 ==========
  -- 业务类型,标识该状态属于哪个业务实体
  -- 注意:同一状态代码可以在不同业务类型中存在,但含义可能不同
  `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型',
  
  -- ========== 状态属性 ==========
  -- 状态分类,用于状态分组和展示。示例:init(初始状态)、processing(处理中)、final(终态)
  `status_category` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '状态分类',
  -- 是否终态:0-非终态(可继续流转),1-终态(不可继续流转)
  -- 终态示例:CLOSED(已关闭)、ARCHIVED(已归档)
  `is_final` TINYINT NOT NULL DEFAULT 0 COMMENT '是否终态:0-否,1-是',
  -- 状态排序,用于状态列表排序。数值越小越靠前
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '状态排序',
  
  -- ========== 状态流转配置 ==========
  -- 状态颜色(用于前端展示),十六进制颜色代码。示例:#1890ff(蓝色)、#52c41a(绿色)
  `status_color` VARCHAR(20) NOT NULL DEFAULT '#1890ff' COMMENT '状态颜色',
  -- 状态图标(用于前端展示),图标代码或URL
  `status_icon` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '状态图标',
  
  -- ========== 扩展信息 ==========
  -- 额外信息,JSON格式存储扩展字段
  -- 示例:{"required_permissions": ["order:update"], "allowed_operations": ["cancel", "edit"]}
  -- 注意:使用NULL表示无扩展信息,避免空JSON对象占用存储空间,且NULL可以明确区分"无扩展信息"和"空扩展信息"
  `extra_info` JSON DEFAULT NULL COMMENT '扩展信息',
  
  -- ========== 时间戳 ==========
  -- 创建时间
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  -- 更新时间
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  -- 创建人ID
  -- 默认-1表示系统创建,避免使用NULL防止空指针问题和简化查询逻辑
  `created_by` BIGINT NOT NULL DEFAULT -1 COMMENT '创建人ID:默认-1表示系统创建',
  -- 更新人ID
  -- 默认-1表示系统更新,避免使用NULL防止空指针问题和简化查询逻辑
  `updated_by` BIGINT NOT NULL DEFAULT -1 COMMENT '更新人ID:默认-1表示系统更新',
  
  -- ========== 软删除 ==========
  -- 是否删除:0-未删除,1-已删除
  -- 默认0表示未删除,避免使用NULL防止索引失效和简化查询逻辑
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除:0-未删除,1-已删除',
  -- 删除时间
  -- 注意:使用NULL表示未删除,必须区分"未删除"和"已删除但删除时间为空"的业务含义,因此允许使用NULL
  `deleted_at` DATETIME(3) NULL DEFAULT NULL COMMENT '删除时间',
  
  PRIMARY KEY (`id`),
  
  -- ========== 唯一约束 ==========
  -- 同一业务类型下状态代码必须唯一
  UNIQUE KEY `uk_status_code_business_type` (`status_code`, `business_type`, `is_deleted`) COMMENT '状态代码+业务类型唯一约束',
  
  -- ========== 索引设计 ==========
  -- 按业务类型查询状态列表
  KEY `idx_status_dict_business_type` (`business_type`, `sort_order`, `is_deleted`) COMMENT '业务类型查询索引',
  -- 按状态分类查询
  KEY `idx_status_dict_category` (`business_type`, `status_category`, `is_deleted`) COMMENT '状态分类查询索引'
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='状态字典表:统一管理所有业务实体的状态定义,支持状态机管理。'
  '每个状态记录包含状态代码、名称、描述、业务类型等完整信息,被状态日志表引用以保证状态定义统一。';

