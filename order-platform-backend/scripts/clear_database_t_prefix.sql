-- =============================================
-- 订单可视化数字化管理平台 - 清空数据库数据 (t_前缀表名)
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
-- 清空角色前台权限关联表
TRUNCATE TABLE `t_role_permission_ft`;
-- 清空角色后台权限关联表
TRUNCATE TABLE `t_role_permission_bk`;
-- 清空角色表
TRUNCATE TABLE `t_role`;
-- 清空用户详情表
TRUNCATE TABLE `t_user_detail`;
-- 清空用户表
TRUNCATE TABLE `t_user`;
-- 清空前台权限表
TRUNCATE TABLE `t_permission_ft`;
-- 清空后台权限表
TRUNCATE TABLE `t_permission_bk`;
-- 清空登录日志表
TRUNCATE TABLE `t_log_login`;
-- 清空操作日志表
TRUNCATE TABLE `t_log_operation`;
-- 清空用户偏好设置表
TRUNCATE TABLE `t_preference`;
-- 清空公告表
TRUNCATE TABLE `t_notice`;

-- =============================================
-- 清空 order-system 数据库
-- =============================================
USE `order-system`;

-- 清空系统设置表
TRUNCATE TABLE `t_setting`;

-- 重置自增ID (从1开始)
ALTER TABLE `t_user` AUTO_INCREMENT = 1;
ALTER TABLE `t_user_detail` AUTO_INCREMENT = 1;
ALTER TABLE `t_role` AUTO_INCREMENT = 1;
ALTER TABLE `t_user_role` AUTO_INCREMENT = 1;
ALTER TABLE `t_permission_bk` AUTO_INCREMENT = 1;
ALTER TABLE `t_permission_ft` AUTO_INCREMENT = 1;
ALTER TABLE `t_role_permission_bk` AUTO_INCREMENT = 1;
ALTER TABLE `t_role_permission_ft` AUTO_INCREMENT = 1;
ALTER TABLE `t_log_login` AUTO_INCREMENT = 1;
ALTER TABLE `t_log_operation` AUTO_INCREMENT = 1;
ALTER TABLE `t_preference` AUTO_INCREMENT = 1;
ALTER TABLE `t_notice` AUTO_INCREMENT = 1;

USE `order-system`;
ALTER TABLE `t_setting` AUTO_INCREMENT = 1;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

SELECT '数据库数据已清空，表结构保留' AS status;
