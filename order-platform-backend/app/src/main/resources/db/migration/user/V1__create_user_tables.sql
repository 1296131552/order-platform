-- 用户主表
CREATE TABLE IF NOT EXISTS `user` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '用户密码(BCrypt加密)',
    `is_valid` TINYINT(1) DEFAULT 1 COMMENT '是否有效(1:有效 0:无效)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` TINYINT(1) DEFAULT 0 COMMENT '是否被删除(1:是 0:否)',
    INDEX idx_username (`username`),
    INDEX idx_is_valid (`is_valid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户主表';

-- 用户详情表
CREATE TABLE IF NOT EXISTS `t_user_detail` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID(关联user表)',
    `name` VARCHAR(50) COMMENT '用户的名字',
    `avatar_url` VARCHAR(500) COMMENT '用户头像链接',
    `sex` TINYINT DEFAULT 3 COMMENT '性别(1:男,2:女,3:未知)',
    `signature` VARCHAR(200) COMMENT '签名',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` TINYINT(1) DEFAULT 0 COMMENT '是否被删除(1:是 0:否)',
    INDEX idx_name (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户详情表';

-- 角色表
CREATE TABLE IF NOT EXISTS `t_role` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '角色名',
    `description` VARCHAR(200) COMMENT '角色描述',
    `parent_node_id` INT DEFAULT 0 COMMENT '父角色的ID(0表示根节点)',
    `level` INT DEFAULT 0 COMMENT '层级(根节点为0)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` TINYINT(1) DEFAULT 0 COMMENT '是否被删除(1:是 0:否)',
    INDEX idx_parent_node_id (`parent_node_id`),
    INDEX idx_level (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `t_user_role` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `role_id` INT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` TINYINT(1) DEFAULT 0 COMMENT '是否被删除(1:是 0:否)',
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_role_id (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 插入默认角色
INSERT INTO `t_role` (`name`, `description`, `parent_node_id`, `level`) VALUES
('超级管理员', '拥有所有权限', 0, 0),
('普通用户', '普通用户角色', 0, 0);

-- 插入默认管理员用户 (用户名: admin, 密码: admin123, BCrypt加密后)
INSERT INTO `user` (`username`, `password`, `is_valid`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 1);

-- 插入默认用户详情
INSERT INTO `t_user_detail` (`id`, `name`, `sex`) VALUES
(1, '系统管理员', 3);

-- 为管理员分配超级管理员角色
INSERT INTO `t_user_role` (`user_id`, `role_id`) VALUES
(1, 1);
