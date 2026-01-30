-- 全局设置表
CREATE TABLE IF NOT EXISTS `t_setting` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '设置ID',
    `field` VARCHAR(256) NOT NULL COMMENT '设置的字段名',
    `title` VARCHAR(1024) NOT NULL DEFAULT '无命名' COMMENT '设置字段的中文名',
    `description` VARCHAR(1024) DEFAULT NULL COMMENT '设置的描述',
    `value` TEXT NOT NULL COMMENT '设置的值',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '是否被删除(1:是 0:否)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_field` (`field`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全局设置表';

-- 插入默认系统设置
INSERT INTO `t_setting` (`field`, `title`, `description`, `value`) VALUES
('permission_display', '权限显示', '是否仅显示有权限操作的数据', '0'),
('project_title', '项目名称', '整个项目的项目名', 'order-platform'),
('user_role_number_limit', '用户角色数量限制', '用户能拥有的最高角色数', '3'),
('default_roles', '默认用户角色', '新建和注册用户的默认角色', '3');
