-- 全局设置表
CREATE TABLE IF NOT EXISTS `setting` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `field` VARCHAR(50) NOT NULL UNIQUE COMMENT '设置的字段名',
    `title` VARCHAR(100) NOT NULL COMMENT '设置的标题',
    `description` VARCHAR(500) COMMENT '设置的描述',
    `value` VARCHAR(1000) COMMENT '设置的值',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` TINYINT(1) DEFAULT 0 COMMENT '是否被删除(1:是 0:否)',
    INDEX idx_field (`field`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局设置表';

-- 插入默认系统设置
INSERT INTO `setting` (`field`, `title`, `description`, `value`) VALUES
('site_name', '站点名称', '网站的名称', '订单管理平台'),
('site_logo', '站点LOGO', '网站的LOGO URL', ''),
('register_enabled', '允许注册', '是否允许新用户注册', 'true'),
('max_upload_size', '最大上传大小', '单个文件最大上传大小(MB)', '10');
