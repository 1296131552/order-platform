package com.company.user.mapper;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.user.model.entity.Role;
import com.company.user.model.entity.UserRole;

import io.lettuce.core.dynamic.annotation.Param;

public interface UserRoleMapper extends BaseMapper<UserRole>{

    /**
     * 根据用户ID查询角色名称列表
     * @param userId 用户ID
     * @return 角色名称列表
     */
    List<Role> selectRolesByUserId(@Param("userId") Integer userId);


    /**
     * 根据用户ID列表查询角色ID列表
     * @param userIds 用户ID列表
     * @return 角色ID列表
     */
    List<Integer> selectRoleIdsByUserIds(@Param("userIds") List<Integer> userIds);

    /**
     * 为用户添加角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    void addUserRoles(@Param("userId") Integer userId, @Param("roleIds") List<Integer> roleIds);

}
