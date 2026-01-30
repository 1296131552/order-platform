-- 添加邮箱和手机号字段到用户表
-- V3__add_email_phone.sql

-- 添加邮箱字段
ALTER TABLE `t_user` ADD COLUMN `email` VARCHAR(255) DEFAULT NULL COMMENT '邮箱' AFTER `password`;

-- 添加手机号字段
ALTER TABLE `t_user` ADD COLUMN `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号' AFTER `email`;

-- 添加邮箱唯一索引
ALTER TABLE `t_user` ADD UNIQUE KEY `uk_email` (`email`);

-- 添加手机号唯一索引
ALTER TABLE `t_user` ADD UNIQUE KEY `uk_phone` (`phone`);
