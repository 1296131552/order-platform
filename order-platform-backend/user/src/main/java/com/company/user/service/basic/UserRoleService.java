package com.company.user.service.basic;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.user.model.entity.Role;
import com.company.user.model.entity.UserRole;

public interface UserRoleService extends IService<UserRole>{
    /**
     * 根据用户ID查询角色名称列表
     * @param userId 用户ID
     * @return 角色名称列表
     */
    List<Role> getRolesByUserId(Integer userId);

    /**
     * 根据用户ID查询角色ID列表
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Integer> getRoleIds(Integer userId);

    /**
     * 根据用户ID列表查询角色ID列表
     * @param userIds 用户ID列表
     * @return 角色ID列表
     */
    List<Integer> getRoleIdsByUserIds(List<Integer> userIds);

    /**
     * 根据用户ID查询角色ID列表
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Integer> getRoleIdsByUserId(Integer userId);

    /**
     * 为用户添加角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    void addUserRoles(Integer userId, List<Integer> roleIds);

    /**
     * 删除用户角色
     * @param userId 用户ID
     */
    void deleteUserRoles(Integer userId);

    /**
     * 更新用户角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    void updateUserRoles(Integer userId, List<Integer> roleIds);

    /**
     * 根据角色ID删除用户角色
     * @param roleId 角色ID
     */
    void deleteByRoleId(Integer roleId);

    /**
     * 根据角色删除用户角色
     * @param role 角色
     */
    void deleteByRoleId(Role role);
}
