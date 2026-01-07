package com.order.platform.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.order.platform.user.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关联 Mapper
 *
 * 功能说明：
 * - 查询用户的角色列表
 * - 查询角色下的用户列表
 * - 用户角色关联管理
 *
 * @since 1.0.0
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 查询用户的角色代码列表
     *
     * 查询条件：
     * - 用户ID匹配
     * - 未删除（is_deleted = 0）
     *
     * 注意：不查询 is_enabled 字段，因为角色表（t_role）才有启用状态
     * 角色启用状态过滤在 RoleService 中处理，通过 JOIN t_role 表实现
     *
     * @param userId 用户ID
     * @return 角色代码列表
     */
    @Select("SELECT role_code FROM t_user_role " +
            "WHERE user_id = #{userId} " +
            "AND is_deleted = 0")
    List<String> selectRoleCodesByUserId(Long userId);
}
