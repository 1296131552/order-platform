-- =============================================
-- 订单可视化数字化管理平台 - 清空数据库数据 (适配旧表结构)
-- 此脚本将清空所有表的数据，但保留表结构
-- 数据库: order-user, order-system
-- =============================================

-- 设置外键检查为0
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 清空 order-user 数据库
-- =============================================
USE `order-user`;

-- 清空用户角色关联表
TRUNCATE TABLE `t_user_role`;
-- 清空角色表
TRUNCATE TABLE `t_role`;
-- 清空用户详情表
TRUNCATE TABLE `t_user_detail`;
-- 清空用户表
TRUNCATE TABLE `user`;

-- =============================================
-- 清空 order-system 数据库
-- =============================================
USE `order-system`;

-- 清空系统设置表
TRUNCATE TABLE `setting`;

-- =============================================
-- 重置自增ID
-- =============================================
USE `order-user`;
ALTER TABLE `user` AUTO_INCREMENT = 1;
ALTER TABLE `t_user_detail` AUTO_INCREMENT = 1;
ALTER TABLE `t_role` AUTO_INCREMENT = 1;
ALTER TABLE `t_user_role` AUTO_INCREMENT = 1;

USE `order-system`;
ALTER TABLE `setting` AUTO_INCREMENT = 1;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

SELECT '数据库数据已清空' AS status;
