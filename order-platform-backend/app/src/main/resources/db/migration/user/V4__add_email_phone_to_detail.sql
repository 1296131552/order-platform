-- 添加邮箱和手机号字段到用户详情表
-- V4__add_email_phone_to_detail.sql

-- 添加邮箱字段
ALTER TABLE `t_user_detail` ADD COLUMN `email` VARCHAR(255) DEFAULT NULL COMMENT '邮箱' AFTER `name`;

-- 添加手机号字段
ALTER TABLE `t_user_detail` ADD COLUMN `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号' AFTER `email`;
