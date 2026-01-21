-- ============================================================
-- 用户名唯一索引优化
-- 说明: 将 username 唯一索引改为 (username, is_deleted) 联合索引
-- 目的: 支持逻辑删除场景，同一 username 可有多条历史记录，但只能有一条有效记录
-- ============================================================

-- 删除原有的单字段唯一索引
DROP INDEX `uk_user_username` ON `t_user`;

-- 创建新的联合唯一索引 (username, is_deleted)
-- 这样同一个 username 只能有一条 is_deleted=0 的记录
-- 但可以有多条 is_deleted=1 的历史记录
CREATE UNIQUE INDEX `uk_username_deleted` ON `t_user`(`username`, `is_deleted`) COMMENT '用户名唯一索引(含软删除)';
