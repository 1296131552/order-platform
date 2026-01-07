-- ============================================================
-- 权限表 (t_permission)
-- 说明:系统权限表,定义权限信息,用于权限控制
-- 关系:Permission N:M Role (通过t_role_permission中间表关联)
-- ============================================================

CREATE TABLE `t_permission` (
  -- ========== 主键 ==========
  -- 权限ID,主键,自增
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  
  -- ========== 权限基本信息 ==========
  -- 权限代码,唯一标识权限。示例:order:view(订单查看)、order:create(订单创建)、order:update(订单更新)等
  -- 注意:权限代码必须唯一
  `permission_code` VARCHAR(100) NOT NULL COMMENT '权限代码',
  -- 权限名称(中文),用于展示。示例:订单查看、订单创建、订单更新
  `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
  -- 权限描述,详细说明该权限的含义和用途
  `permission_description` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '权限描述',
  
  -- ========== 权限分类 ==========
  -- 权限类型,用于权限分组。示例:MENU(菜单权限)、BUTTON(按钮权限)、DATA(数据权限)、API(接口权限)等
  `permission_type` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '权限类型',
  -- 权限资源,标识权限对应的资源。示例:order(订单管理)、order_line(订单行)、partner(合作方管理)、supplier(供应商)、carrier(承运商)、shipment(发运管理)、receipt(签收管理)、attachment(附件管理)、dashboard(大屏看板)、map(业务地图)、timeline(时间线)、import_export(数据导入导出)、system(系统管理)等
  `resource` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '权限资源',
  -- 权限操作,标识权限对应的操作。示例:view(查看)、create(创建)、update(更新)、delete(删除)、export(导出)、import(导入)、audit(审核)、confirm(确认)、approve(审批)等
  `action` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '权限操作',
  -- 权限模块,标识权限所属的功能模块。示例:ORDER(订单管理)、PARTNER(合作方管理)、SHIPMENT(发运管理)、RECEIPT(签收管理)、ATTACHMENT(附件管理)、DASHBOARD(大屏看板)、SYSTEM(系统管理)等
  -- 默认空字符串表示未设置权限模块,避免使用NULL防止索引失效和简化查询逻辑
  `permission_module` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '权限模块',
  -- 权限路径,权限对应的资源路径或API路径。示例:/api/order/list、/order/view等
  `permission_path` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '权限路径',
  -- 权限图标(用于前端展示),图标代码或URL
  `permission_icon` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '权限图标',
  -- 权限排序,用于权限列表排序。数值越小越靠前
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '权限排序',
  
  -- ========== 权限层级 ==========
  -- 父权限ID,用于构建权限树结构。0表示顶级权限
  -- 默认0表示顶级权限,避免使用NULL防止空指针问题和简化查询逻辑
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父权限ID:默认0表示顶级权限',
  -- 权限层级,标识权限在树结构中的层级。1表示顶级,2表示二级,以此类推
  -- 默认1表示顶级权限,避免使用NULL防止索引失效和简化查询逻辑
  `permission_level` INT NOT NULL DEFAULT 1 COMMENT '权限层级:1表示顶级',
  -- 权限路径(树路径),用于快速查询子权限。示例:0/1/2表示ID为2的权限,其父权限为1,顶级为0
  `permission_tree_path` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '权限路径(树路径)',
  
  -- ========== 权限配置 ==========
  -- 是否启用:0-禁用,1-启用
  -- 默认1表示启用,避免使用NULL防止索引失效和简化查询逻辑
  `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用:0-禁用,1-启用',
  -- 是否系统权限:0-用户自定义,1-系统内置
  -- 默认0表示用户自定义,避免使用NULL防止索引失效和简化查询逻辑
  `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统权限:0-用户自定义,1-系统内置',
  -- 是否可见:0-不可见,1-可见(用于前端菜单显示控制)
  -- 默认1表示可见,避免使用NULL防止索引失效和简化查询逻辑
  `is_visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见:0-不可见,1-可见',
  
  -- ========== 扩展信息 ==========
  -- 额外信息,JSON格式存储扩展字段
  -- 示例:{"method": "GET", "required_params": ["order_id"], "data_scope": "self"}
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
  
  -- ========== 外键约束 ==========
  -- 关联父权限,保证权限树结构的一致性
  -- 注意:外键约束可选择性启用,取决于业务需求。启用外键可保证数据一致性,但可能影响性能
  -- CONSTRAINT `fk_permission_parent` FOREIGN KEY (`parent_id`) REFERENCES `t_permission` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  
  -- ========== 唯一约束 ==========
  -- 权限代码必须唯一
  UNIQUE KEY `uk_permission_code` (`permission_code`, `is_deleted`) COMMENT '权限代码唯一约束',
  
  -- ========== 索引设计 ==========
  -- 父权限ID+权限层级+排序+启用状态+软删除联合索引,用于查询权限树结构(高频场景:构建权限菜单树)
  -- 覆盖场景:按父权限ID查询子权限、按权限层级查询、按启用状态查询
  KEY `idx_permission_parent_enabled` (`parent_id`, `permission_level`, `sort_order`, `is_enabled`, `is_deleted`) COMMENT '父权限启用状态联合索引',

  -- 权限模块+权限资源+权限操作+启用状态+软删除联合索引,用于按模块和资源查询权限(高频场景:权限验证)
  -- 覆盖场景:按权限模块查询、按资源查询、按权限模块+资源查询
  KEY `idx_permission_module_resource` (`permission_module`, `resource`, `action`, `is_enabled`, `is_deleted`) COMMENT '权限模块资源联合索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='权限表:系统权限表,定义权限信息,用于权限控制。'
  '每个权限记录包含权限代码、名称、描述、类型、资源、操作、模块等完整信息,支持权限树结构,'
  '通过t_role_permission中间表与角色实现N:M关联。'
  '【权限模块】权限模块包括:ORDER(订单管理)、PARTNER(合作方管理)、SHIPMENT(发运管理)、'
  'RECEIPT(签收管理)、ATTACHMENT(附件管理)、DASHBOARD(大屏看板)、SYSTEM(系统管理)等。'
  '【权限资源】权限资源包括:order(订单)、order_line(订单行)、supplier(供应商)、carrier(承运商)、'
  'shipment(发运批次)、receipt(签收)、attachment(附件)、dashboard(看板)、map(地图)、timeline(时间线)等。'
  '【权限操作】权限操作包括:view(查看)、create(创建)、update(更新)、delete(删除)、export(导出)、'
  'import(导入)、audit(审核)、confirm(确认)、approve(审批)等。';

