-- ============================================================
-- 用户表升级脚本 - 添加用户注册审核相关字段
-- 版本: v1.0.1
-- 日期: 2026-01-10
-- 说明: 添加用户首次登录标记和审核状态相关字段
-- ============================================================

-- 添加 is_first_login 字段
ALTER TABLE `t_user`
ADD COLUMN `is_first_login` TINYINT NOT NULL DEFAULT 1
COMMENT '是否首次登录:0-否,1-是'
AFTER `login_count`;

-- 添加 audit_status 字段
ALTER TABLE `t_user`
ADD COLUMN `audit_status` VARCHAR(30) NOT NULL DEFAULT 'NONE'
COMMENT '审核状态:NONE-无需审核,PENDING-待审核,APPROVED-已通过,REJECTED-已拒绝'
AFTER `is_first_login`;

-- 添加 audit_remark 字段
ALTER TABLE `t_user`
ADD COLUMN `audit_remark` VARCHAR(500) NOT NULL DEFAULT ''
COMMENT '审核备注:记录审核未通过的原因或其他审核信息'
AFTER `audit_status`;

-- 添加 audit_by 字段
ALTER TABLE `t_user`
ADD COLUMN `audit_by` BIGINT NOT NULL DEFAULT -1
COMMENT '审核人ID:-1表示未审核,0及以上表示审核人ID'
AFTER `audit_remark`;

-- 添加 audit_time 字段
ALTER TABLE `t_user`
ADD COLUMN `audit_time` DATETIME(3) NULL DEFAULT NULL
COMMENT '审核时间:NULL表示未审核'
AFTER `audit_by`;

-- ============================================================
-- 索引优化
-- ============================================================

-- 添加审核状态索引（用于查询待审核用户）
-- 如果索引已存在会报错，可以忽略
-- ALTER TABLE `t_user`
-- ADD INDEX `idx_audit_status` (`audit_status`, `is_deleted`)
-- COMMENT '审核状态索引';

-- ============================================================
-- 数据更新
-- ============================================================

-- 将现有系统管理员用户的审核状态设置为 APPROVED（无需审核）
UPDATE `t_user`
SET `audit_status` = 'APPROVED',
    `is_first_login` = 0
WHERE `username` IN ('admin', 'system') AND `is_deleted` = 0;

-- ============================================================
-- 验证脚本
-- ============================================================

-- 验证字段是否添加成功
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 't_user'
  AND COLUMN_NAME IN ('is_first_login', 'audit_status', 'audit_remark', 'audit_by', 'audit_time')
ORDER BY ORDINAL_POSITION;

-- 验证数据更新
SELECT
    id,
    username,
    is_first_login,
    audit_status,
    audit_by,
    audit_time
FROM `t_user`
WHERE `is_deleted` = 0
LIMIT 10;
