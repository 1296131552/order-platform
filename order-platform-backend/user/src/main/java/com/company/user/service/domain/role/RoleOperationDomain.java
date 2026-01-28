package com.company.user.service.domain.role;

import com.company.user.model.dto.RoleDTO;

import java.util.List;

public interface RoleOperationDomain {
    /**
     * 添加角色
     * @param roleDTO 角色dto
     */
    void addRole(RoleDTO roleDTO);

    /**
     * 添加全局角色
     * @param roleDTO 角色dto
     */
    void addGlobalRole(RoleDTO roleDTO);

    /**
     * 更新角色
     * @param roleDTO 角色dto
     */
    void updateRole(RoleDTO roleDTO);

    /**
     * 更新全局角色
     * @param roleDTO 角色dto
     */
    void updateGlobalRole(RoleDTO roleDTO);

    /**
     * 删除角色
     * @param roleId 角色ID
     */
    void deleteRole(Integer roleId);

    /**
     * 分配权限权限
     * @param roleId 角色ID
     * @param oldPermissionIds 旧权限ID列表
     * @param newPermissionIds 新权限ID列表
     */
    void assignNormalPermission(Integer roleId, List<Integer> oldPermissionIds, List<Integer> newPermissionIds);

    /**
     * 分配全局权限权限
     * @param roleId 角色ID
     * @param oldPermissionIds 旧权限ID列表
     * @param newPermissionIds 新权限ID列表
     */
    void assignGlobalPermission(Integer roleId, List<Integer> oldPermissionIds, List<Integer> newPermissionIds);

    /**
     * 获取角色权限ID列表
     * @param parentRoleId 父角色ID
     * @return 权限ID列表
     */
    List<Integer> getPermissionIdsByParentRoleId(Integer parentRoleId);
}
