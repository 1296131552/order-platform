package com.company.order.visual.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.order.visual.user.dto.UserRoleResult;
import com.company.order.visual.user.dto.UserVO.RoleInfo;
import com.company.order.visual.user.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 查询单个用户的角色（JOIN 查询，一次查询完成）
     */
    @Select("""
            SELECT r.id as roleId, r.role_code as roleCode, r.role_name as roleName,
                   r.data_scope_type as dataScopeType, ur.is_primary as isPrimary
            FROM t_user_role ur
            JOIN t_role r ON ur.role_id = r.id
            WHERE ur.user_id = #{userId} AND ur.is_deleted = false AND r.is_enabled = true
            """)
    List<RoleInfo> selectRolesByUserId(@Param("userId") Long userId);

    /**
     * 批量查询多个用户的角色（解决 N+1 问题）
     *
     * @return 包含 userId 的角色列表，使用方需按 userId 分组
     */
    @Select("""
            <script>
            SELECT ur.user_id as userId, r.id as roleId, r.role_code as roleCode,
                   r.role_name as roleName, r.data_scope_type as dataScopeType,
                   ur.is_primary as isPrimary
            FROM t_user_role ur
            JOIN t_role r ON ur.role_id = r.id
            WHERE ur.user_id IN
            <foreach item='id' collection='userIds' open='(' separator=',' close=')'>
                #{id}
            </foreach>
            AND ur.is_deleted = false AND r.is_enabled = true
            ORDER BY ur.user_id, ur.is_primary DESC
            </script>
            """)
    List<UserRoleResult> selectRolesByUserIds(@Param("userIds") List<Long> userIds);
}