-- ============================================================
-- 测试账号初始化数据
-- 说明: 插入系统测试账号，用于开发和测试
-- ============================================================

-- 插入测试账号：admin
-- 用户名: admin
-- 密码: admin123 (BCrypt加密)
-- 角色: 系统管理员
INSERT INTO `t_user` (
    `username`,
    `password`,
    `user_code`,
    `real_name`,
    `email`,
    `phone`,
    `avatar`,
    `is_enabled`,
    `is_locked`,
    `login_count`,
    `position`,
    `employee_no`,
    `created_by`,
    `updated_by`
) VALUES (
    'admin',
    '$2a$10$8DQBA4gbc0J5BANaX5rBLOqmPEijINw.CEzqRT0JvZDN1A99gjjb6',  -- 密码: admin123
    'U001',
    'admin',
    'admin@example.com',
    '13800138000',
    NULL,
    1,
    0,
    0,
    '系统管理员',
    'E001',
    NULL,
    NULL
);

-- 为 admin 用户分配系统管理员角色
-- 假设用户ID为1（刚插入的用户），角色ID为1（SYSTEM_ADMIN）
INSERT INTO `t_user_role` (
    `user_id`,
    `role_id`,
    `is_primary`,
    `created_by`,
    `updated_by`
) VALUES (
    (SELECT id FROM t_user WHERE username = 'admin'),
    (SELECT id FROM t_role WHERE role_code = 'SYSTEM_ADMIN'),
    1,
    NULL,
    NULL
);

-- ============================================================
-- 账号信息
-- ============================================================
-- 用户名: admin
-- 密码: admin123
-- 角色: 系统管理员（全部数据权限）
-- ============================================================
