package com.company.user.service.facade;

import com.company.user.enums.model.RolesType;
import com.company.user.model.dto.AssignPermissionDTO;
import com.company.user.model.dto.RoleDTO;
import com.company.user.model.entity.Role;
import com.company.user.model.vo.RoleVO;

import java.util.List;

public interface RoleFacade {


    /**
     * 获取角色树
     * @param currentUserId 当前用户ID
     * @param type 类型
     * @return 角色树
     */
    List<RoleVO> getRoleTree(Integer currentUserId, RolesType type);

    /**
     * 获取角色列表
     * @param currentUserId 当前用户ID
     * @param type 类型
     * @return 角色列表
     */
    List<Role> getRoles(Integer currentUserId, RolesType type);

    /**
     * 添加角色
     * @param roleDTO 角色dto
     */
    void addRole(RoleDTO roleDTO, Integer userId);

    /**
     * 添加全局角色
     * @param roleDTO 角色dto
     */
    void addGlobalRole(RoleDTO roleDTO);

    /**
     * 更新角色
     * @param roleDTO 角色dto
     * @param userId 用户ID
     */
    void updateRole(RoleDTO roleDTO, Integer userId);

    /**
     * 更新全局角色
     * @param roleDTO 角色dto
     */
    void updateGlobalRole(RoleDTO roleDTO);

    /**
     * 删除角色
     * @param roleId 角色ID
     * @param userId 用户ID
     */
    void deleteRole(Integer roleId, Integer userId);

    /**
     * 删除全局角色
     * @param roleId 角色ID
     */
    void deleteGlobalRole(Integer roleId);

    /**
     * 分配权限权限
     * @param userId 当前操作用户ID
     * @param dto 分配权限dto
     */
    void assignPermission(Integer userId, AssignPermissionDTO dto);

    /**
     * 分配全局权限权限
     * @param dto 分配权限dto
     */
    void assignGlobalPermission(AssignPermissionDTO dto);
}
