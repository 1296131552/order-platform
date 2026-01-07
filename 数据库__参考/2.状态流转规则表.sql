-- ============================================================
-- 状态流转规则表 (t_status_transition_rule)
-- 说明:定义状态流转规则,控制哪些状态可以流转到哪些状态(状态机核心)
-- 关系:通过business_type字段绑定不同业务实体类型
-- ============================================================

CREATE TABLE `t_status_transition_rule` (
  -- ========== 主键 ==========
  -- 规则ID,主键,自增
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  
  -- ========== 业务类型 ==========
  -- 业务类型,标识该规则适用于哪个业务实体
  -- 可选值:order(订单)、shipment(发运批次)、receipt(签收记录)
  `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型:order/shipment/receipt',
  
  -- ========== 状态流转定义 ==========
  -- 源状态字典ID,外键关联t_status_dict表。NULL表示允许从任意状态流转(初始状态)
  -- 注意:建议使用状态字典ID而非状态代码,保证数据一致性
  -- 使用NULL表示无前序状态(初始状态),必须区分"从特定状态流转"和"从任意状态流转"的业务含义,因此允许使用NULL
  `from_status_dict_id` BIGINT NULL DEFAULT NULL COMMENT '源状态字典ID:NULL表示允许从任意状态流转(初始状态)',
  -- 源状态代码(冗余字段,便于查询和展示)
  -- 使用NULL表示无前序状态(初始状态),与from_status_dict_id保持一致
  `from_status_code` VARCHAR(50) NULL DEFAULT NULL COMMENT '源状态代码(冗余字段):NULL表示无前序状态(初始状态)',
  -- 目标状态字典ID,外键关联t_status_dict表。必须指定目标状态
  `to_status_dict_id` BIGINT NOT NULL COMMENT              '目标状态字典ID',
  -- 目标状态代码(冗余字段,便于查询和展示)
  `to_status_code` VARCHAR(50) NOT NULL COMMENT            '目标状态代码(冗余字段)',
  
  -- ========== 流转规则配置 ==========
  -- 是否启用:0-禁用,1-启用。禁用后该流转规则将不允许执行
  `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT          '是否启用:0-禁用,1-启用',
  -- 规则优先级,数值越大优先级越高。当存在多条规则时,按优先级执行
  `priority` INT NOT NULL DEFAULT 0 COMMENT                '规则优先级',
  -- 规则描述,说明该流转规则的业务含义和适用场景
  `description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT   '规则描述',
  
  -- ========== 流转条件配置 ==========
  -- 流转条件(JSON格式),定义流转的前置条件
  -- 示例:{"required_fields": ["supplier_id"], "min_amount": 1000, "required_permissions": ["order:update"]}
  -- 注意:条件校验逻辑在应用层实现,此处仅存储条件配置
  -- 使用NULL表示无流转条件,避免空JSON对象占用存储空间,且NULL可以明确区分"无条件"和"空条件"
  `transition_conditions` JSON DEFAULT NULL COMMENT '流转条件(JSON格式):NULL表示无流转条件',
  -- 流转后动作(JSON格式),定义状态流转后需要执行的动作
  -- 示例:{"notify_users": ["operator"], "trigger_events": ["order_status_changed"], "update_fields": ["updated_at"]}
  -- 使用NULL表示无流转后动作,避免空JSON对象占用存储空间,且NULL可以明确区分"无动作"和"空动作"
  `transition_actions` JSON DEFAULT NULL COMMENT '流转后动作(JSON格式):NULL表示无流转后动作',
  
  -- ========== 权限和角色控制 ==========
  -- 允许执行该流转的角色列表(JSON格式),空数组表示所有角色都可执行
  -- 示例:["客户经理", "运营专员"]
  -- 使用NULL表示无角色限制,避免空JSON数组占用存储空间,且NULL可以明确区分"无限制"和"空限制"
  `allowed_roles` JSON DEFAULT NULL COMMENT '允许执行的角色列表:NULL表示无角色限制',
  -- 允许执行该流转的权限列表(JSON格式),空数组表示无需特殊权限
  -- 示例:["order:update", "order:status:change"]
  -- 使用NULL表示无权限要求,避免空JSON数组占用存储空间,且NULL可以明确区分"无要求"和"空要求"
  `required_permissions` JSON DEFAULT NULL COMMENT '需要的权限列表:NULL表示无权限要求',
  
  -- ========== 扩展信息 ==========
  -- 额外信息,JSON格式存储扩展字段
  -- 使用NULL表示无扩展信息,避免空JSON对象占用存储空间,且NULL可以明确区分"无扩展信息"和"空扩展信息"
  `extra_info` JSON DEFAULT NULL COMMENT '扩展信息:NULL表示无扩展信息',
  
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
  `deleted_at` DATETIME(3) NULL DEFAULT NULL COMMENT '删除时间:NULL表示未删除',
  
  PRIMARY KEY (`id`),
  
  -- ========== 唯一约束 ==========
  -- 同一业务类型下,相同的源状态和目标状态组合只能有一条规则
  UNIQUE KEY `uk_status_transition_rule_business_from_to` (`business_type`, `from_status_dict_id`, `to_status_dict_id`, `is_deleted`) COMMENT '状态流转规则唯一约束',
  
  -- ========== 外键约束 ==========
  -- 关联状态字典表,保证状态定义统一
  -- 注意:外键约束可选择性启用,取决于业务需求
  -- CONSTRAINT `fk_transition_rule_from_status` FOREIGN KEY (`from_status_dict_id`) REFERENCES `t_status_dict` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  -- CONSTRAINT `fk_transition_rule_to_status` FOREIGN KEY (`to_status_dict_id`) REFERENCES `t_status_dict` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  
  -- ========== 索引设计 ==========
  -- 按业务类型和源状态查询可流转的目标状态
  KEY `idx_transition_rule_from_status` (`business_type`, `from_status_dict_id`, `is_enabled`, `is_deleted`) COMMENT '源状态查询索引',
  -- 按业务类型和目标状态查询可流转的源状态
  KEY `idx_transition_rule_to_status` (`business_type`, `to_status_dict_id`, `is_enabled`, `is_deleted`) COMMENT '目标状态查询索引',
  -- 按业务类型查询所有规则(用于规则管理)
  KEY `idx_transition_rule_business_type` (`business_type`, `priority`, `is_deleted`) COMMENT '业务类型查询索引'
  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='状态流转规则表:定义状态流转规则,控制哪些状态可以流转到哪些状态,是状态机管理的核心表。'
  '通过business_type字段区分不同业务实体,支持灵活的流转条件、权限控制和动作配置。';

