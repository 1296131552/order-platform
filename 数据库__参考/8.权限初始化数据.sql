-- ============================================================
-- 权限初始化数据
-- 说明:根据业务文档定义的基础角色和权限数据
-- 注意:执行前请确保相关表已创建
-- ============================================================

-- ============================================================
-- 一、角色初始化数据
-- ============================================================

-- 系统管理员角色
INSERT INTO `t_role` (`role_code`, `role_name`, `role_description`, `role_type`, `role_level`, `sort_order`, `is_enabled`, `is_system`, `is_deletable`, `is_editable`, `created_by`, `updated_by`) VALUES
('SYSTEM_ADMIN', '系统管理员', '负责权限配置、数据维护与系统管理', 'SYSTEM', 100, 1, 1, 1, 0, 0, -1, -1);

-- 客户经理角色
INSERT INTO `t_role` (`role_code`, `role_name`, `role_description`, `role_type`, `role_level`, `sort_order`, `is_enabled`, `is_system`, `is_deletable`, `is_editable`, `created_by`, `updated_by`) VALUES
('CUSTOMER_MANAGER', '客户经理', '负责客户来单收集、订单创建与跟进', 'BUSINESS', 10, 2, 1, 1, 0, 0, -1, -1);

-- 采购专员角色
INSERT INTO `t_role` (`role_code`, `role_name`, `role_description`, `role_type`, `role_level`, `sort_order`, `is_enabled`, `is_system`, `is_deletable`, `is_editable`, `created_by`, `updated_by`) VALUES
('PURCHASE_SPECIALIST', '采购专员', '负责供应商选择、资质审核与合作确认', 'BUSINESS', 10, 3, 1, 1, 0, 0, -1, -1);

-- 运营专员角色
INSERT INTO `t_role` (`role_code`, `role_name`, `role_description`, `role_type`, `role_level`, `sort_order`, `is_enabled`, `is_system`, `is_deletable`, `is_editable`, `created_by`, `updated_by`) VALUES
('OPERATION_SPECIALIST', '运营专员', '负责发运计划制定、物流安排与在途跟踪', 'BUSINESS', 10, 4, 1, 1, 0, 0, -1, -1);

-- 数据管理员角色
INSERT INTO `t_role` (`role_code`, `role_name`, `role_description`, `role_type`, `role_level`, `sort_order`, `is_enabled`, `is_system`, `is_deletable`, `is_editable`, `created_by`, `updated_by`) VALUES
('DATA_ADMIN', '数据管理员', '负责数据查看、导出等数据管理操作', 'BUSINESS', 5, 5, 1, 1, 0, 0, -1, -1);

-- ============================================================
-- 二、权限初始化数据（按模块分类）
-- ============================================================

-- ========== 订单管理模块权限 ==========
-- 订单查看权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('order:view', '订单查看', '查看订单信息、订单行、状态等', 'MENU', 'order', 'view', 'ORDER', '/order/view', 1, 1, 1, 1, -1, -1);

-- 订单创建权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('order:create', '订单创建', '创建新订单', 'BUTTON', 'order', 'create', 'ORDER', '/order/create', 2, 1, 1, 1, -1, -1);

-- 订单编辑权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('order:update', '订单编辑', '编辑订单信息', 'BUTTON', 'order', 'update', 'ORDER', '/order/update', 3, 1, 1, 1, -1, -1);

-- 订单关闭权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('order:close', '订单关闭', '关闭订单', 'BUTTON', 'order', 'close', 'ORDER', '/order/close', 4, 1, 1, 1, -1, -1);

-- 订单归档权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('order:archive', '订单归档', '归档订单', 'BUTTON', 'order', 'archive', 'ORDER', '/order/archive', 5, 1, 1, 1, -1, -1);

-- 订单行维护权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('order_line:manage', '订单行维护', '维护订单行信息', 'BUTTON', 'order_line', 'manage', 'ORDER', '/order/line/manage', 6, 1, 1, 1, -1, -1);

-- ========== 合作方管理模块权限 ==========
-- 供应商查看权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('supplier:view', '供应商查看', '查看供应商信息', 'MENU', 'supplier', 'view', 'PARTNER', '/partner/supplier/view', 10, 1, 1, 1, -1, -1);

-- 供应商管理权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('supplier:manage', '供应商管理', '创建、编辑供应商信息', 'BUTTON', 'supplier', 'manage', 'PARTNER', '/partner/supplier/manage', 11, 1, 1, 1, -1, -1);

-- 供应商资质审核权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('supplier:audit', '供应商资质审核', '审核供应商资质', 'BUTTON', 'supplier', 'audit', 'PARTNER', '/partner/supplier/audit', 12, 1, 1, 1, -1, -1);

-- 承运商查看权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('carrier:view', '承运商查看', '查看承运商信息', 'MENU', 'carrier', 'view', 'PARTNER', '/partner/carrier/view', 13, 1, 1, 1, -1, -1);

-- 承运商管理权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('carrier:manage', '承运商管理', '创建、编辑承运商信息', 'BUTTON', 'carrier', 'manage', 'PARTNER', '/partner/carrier/manage', 14, 1, 1, 1, -1, -1);

-- 承运商资质审核权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('carrier:audit', '承运商资质审核', '审核承运商资质', 'BUTTON', 'carrier', 'audit', 'PARTNER', '/partner/carrier/audit', 15, 1, 1, 1, -1, -1);

-- ========== 发运管理模块权限 ==========
-- 发运批次查看权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('shipment:view', '发运批次查看', '查看发运批次信息', 'MENU', 'shipment', 'view', 'SHIPMENT', '/shipment/view', 20, 1, 1, 1, -1, -1);

-- 发运批次创建权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('shipment:create', '发运批次创建', '创建发运批次', 'BUTTON', 'shipment', 'create', 'SHIPMENT', '/shipment/create', 21, 1, 1, 1, -1, -1);

-- 发运批次编辑权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('shipment:update', '发运批次编辑', '编辑发运批次信息', 'BUTTON', 'shipment', 'update', 'SHIPMENT', '/shipment/update', 22, 1, 1, 1, -1, -1);

-- 发运批次确认权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('shipment:confirm', '发运批次确认', '确认发运计划', 'BUTTON', 'shipment', 'confirm', 'SHIPMENT', '/shipment/confirm', 23, 1, 1, 1, -1, -1);

-- ========== 签收管理模块权限 ==========
-- 签收查看权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('receipt:view', '签收查看', '查看签收信息', 'MENU', 'receipt', 'view', 'RECEIPT', '/receipt/view', 30, 1, 1, 1, -1, -1);

-- 签收确认权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('receipt:confirm', '签收确认', '确认签收', 'BUTTON', 'receipt', 'confirm', 'RECEIPT', '/receipt/confirm', 31, 1, 1, 1, -1, -1);

-- 差异处理权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('receipt:difference', '差异处理', '处理签收差异', 'BUTTON', 'receipt', 'difference', 'RECEIPT', '/receipt/difference', 32, 1, 1, 1, -1, -1);

-- ========== 附件管理模块权限 ==========
-- 附件查看权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('attachment:view', '附件查看', '查看附件信息', 'MENU', 'attachment', 'view', 'ATTACHMENT', '/attachment/view', 40, 1, 1, 1, -1, -1);

-- 附件上传权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('attachment:upload', '附件上传', '上传附件', 'BUTTON', 'attachment', 'upload', 'ATTACHMENT', '/attachment/upload', 41, 1, 1, 1, -1, -1);

-- 附件下载权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('attachment:download', '附件下载', '下载附件', 'BUTTON', 'attachment', 'download', 'ATTACHMENT', '/attachment/download', 42, 1, 1, 1, -1, -1);

-- 附件标签管理权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('attachment:tag', '附件标签管理', '管理附件标签', 'BUTTON', 'attachment', 'tag', 'ATTACHMENT', '/attachment/tag', 43, 1, 1, 1, -1, -1);

-- ========== 大屏看板模块权限 ==========
-- 看板查看权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('dashboard:view', '大屏看板查看', '查看大屏看板', 'MENU', 'dashboard', 'view', 'DASHBOARD', '/dashboard/view', 50, 1, 1, 1, -1, -1);

-- 看板筛选权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('dashboard:filter', '看板筛选', '筛选看板数据', 'BUTTON', 'dashboard', 'filter', 'DASHBOARD', '/dashboard/filter', 51, 1, 1, 1, -1, -1);

-- 看板钻取权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('dashboard:drill', '看板钻取', '钻取至订单明细', 'BUTTON', 'dashboard', 'drill', 'DASHBOARD', '/dashboard/drill', 52, 1, 1, 1, -1, -1);

-- ========== 业务地图与时间线模块权限 ==========
-- 业务地图查看权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('map:view', '业务地图查看', '查看业务地图', 'MENU', 'map', 'view', 'DASHBOARD', '/map/view', 60, 1, 1, 1, -1, -1);

-- 时间线查看权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('timeline:view', '时间线查看', '查看流程时间线', 'MENU', 'timeline', 'view', 'DASHBOARD', '/timeline/view', 61, 1, 1, 1, -1, -1);

-- ========== 数据导入导出模块权限 ==========
-- 数据导出权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('data:export', '数据导出', '导出业务数据', 'BUTTON', 'data', 'export', 'IMPORT_EXPORT', '/data/export', 70, 1, 1, 1, -1, -1);

-- 数据导入权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('data:import', '数据导入', '导入历史数据', 'BUTTON', 'data', 'import', 'IMPORT_EXPORT', '/data/import', 71, 1, 1, 1, -1, -1);

-- ========== 系统管理模块权限 ==========
-- 用户管理权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('system:user', '用户管理', '管理系统用户', 'MENU', 'user', 'manage', 'SYSTEM', '/system/user', 80, 1, 1, 1, -1, -1);

-- 角色管理权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('system:role', '角色管理', '管理系统角色', 'MENU', 'role', 'manage', 'SYSTEM', '/system/role', 81, 1, 1, 1, -1, -1);

-- 权限管理权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('system:permission', '权限管理', '管理系统权限', 'MENU', 'permission', 'manage', 'SYSTEM', '/system/permission', 82, 1, 1, 1, -1, -1);

-- ========== 异常管理模块权限 ==========
-- 异常查看权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('exception:view', '异常查看', '查看异常信息', 'MENU', 'exception', 'view', 'EXCEPTION', '/exception/view', 90, 1, 1, 1, -1, -1);

-- 异常上报权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('exception:report', '异常上报', '上报异常', 'BUTTON', 'exception', 'report', 'EXCEPTION', '/exception/report', 91, 1, 1, 1, -1, -1);

-- 异常处理权限
INSERT INTO `t_permission` (`permission_code`, `permission_name`, `permission_description`, `permission_type`, `resource`, `action`, `permission_module`, `permission_path`, `sort_order`, `is_enabled`, `is_system`, `is_visible`, `created_by`, `updated_by`) VALUES
('exception:handle', '异常处理', '处理异常', 'BUTTON', 'exception', 'handle', 'EXCEPTION', '/exception/handle', 92, 1, 1, 1, -1, -1);

-- ============================================================
-- 三、角色权限关联初始化数据（根据业务文档定义）
-- ============================================================

-- 注意:以下关联关系需要根据实际业务需求调整
-- 这里仅提供示例,实际使用时需要根据角色ID和权限ID进行关联

-- 系统管理员拥有所有权限（示例，需要根据实际权限ID调整）
-- INSERT INTO `t_role_permission` (`role_id`, `permission_id`, `relation_by`, `created_by`, `updated_by`) 
-- SELECT r.id, p.id, -1, -1, -1 FROM t_role r, t_permission p WHERE r.role_code = 'SYSTEM_ADMIN';

-- 客户经理权限（订单管理相关）
-- INSERT INTO `t_role_permission` (`role_id`, `permission_id`, `relation_by`, `created_by`, `updated_by`)
-- SELECT r.id, p.id, -1, -1, -1 FROM t_role r, t_permission p 
-- WHERE r.role_code = 'CUSTOMER_MANAGER' AND p.permission_module = 'ORDER';

-- 采购专员权限（合作方管理相关）
-- INSERT INTO `t_role_permission` (`role_id`, `permission_id`, `relation_by`, `created_by`, `updated_by`)
-- SELECT r.id, p.id, -1, -1, -1 FROM t_role r, t_permission p 
-- WHERE r.role_code = 'PURCHASE_SPECIALIST' AND p.permission_module = 'PARTNER';

-- 运营专员权限（发运管理、签收管理相关）
-- INSERT INTO `t_role_permission` (`role_id`, `permission_id`, `relation_by`, `created_by`, `updated_by`)
-- SELECT r.id, p.id, -1, -1, -1 FROM t_role r, t_permission p 
-- WHERE r.role_code = 'OPERATION_SPECIALIST' AND (p.permission_module = 'SHIPMENT' OR p.permission_module = 'RECEIPT');

-- 数据管理员权限（查看、导出相关）
-- INSERT INTO `t_role_permission` (`role_id`, `permission_id`, `relation_by`, `created_by`, `updated_by`)
-- SELECT r.id, p.id, -1, -1, -1 FROM t_role r, t_permission p 
-- WHERE r.role_code = 'DATA_ADMIN' AND (p.action = 'view' OR p.action = 'export');

