-- 插入无权限默认角色
INSERT INTO `t_role` (`name`, `description`, `parent_node_id`, `level`) VALUES
('默认用户', '新注册用户的默认角色，无任何特殊权限', 0, 0);
