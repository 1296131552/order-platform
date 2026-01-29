package com.company.user.service.basic;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.user.model.entity.Role;

public interface RoleService extends IService<Role>{
    /**
     * 获取最高级角色
     * @param roleIds 角色id列表
     * @return 最高级角色
     */
    Integer getHighestLevel(List<Integer> roleIds);

    /**
     * 获取子角色列表
     * @param roleId 角色id
     * @return 子角色列表
     */
    List<Role> getChildRoles(Integer roleId);

    /**
     * 获取子角色id列表
     * @param roleId 角色id
     * @return 子角色id列表
     */
    List<Integer> getChildRoleIds(Integer roleId);

    /**
     * 获取用户列表
     * @param roleIds 角色id列表
     * @return 用户列表
     */
    List<Role> getRoles(List<Integer> roleIds);

    /**
     * 获取非全局角色列表
     * @return 非全局角色列表
     */
    List<Role> getNotGlobalRoles();

    /**
     * 获取非全局角色id列表
     * @return 非全局角色id列表
     */
    List<Integer> getNotGlobalRoleIds();

    /**
     * 添加角色
     * @param role 角色
     */
    void addRole(Role role);

    /**
     * 添加全局角色
     * @param role 角色
     */
    void addGlobalRole(Role role);

    /**
     * 删除角色
     * @param roleId 角色ID
     */
    void deleteRole(Integer roleId);

    /**
     * 获取子孙角色列表
     * @param roleIds 角色id列表
     * @return 子孙角色列表
     */
    List<Role> getDescendantRoles(List<Integer> roleIds);

    /**
     * 获取子角色列表
     * @param roleId 角色id
     * @return 子角色列表
     */
    List<Role> getDescendantRoles(Integer roleId);

    /**
     * 获取子角色id列表
     * @param roleIds 角色id列表
     * @return 子角色id列表
     */
    List<Integer> getDescendantRoleIds(List<Integer> roleIds);

    /**
     * 获取子角色id列表
     * @param roleId 角色id
     * @return 子角色id列表
     */
    List<Integer> getDescendantRoleIds(Integer roleId);

    /**
     * 获取祖先角色id列表
     * @param roleId 角色id
     * @return 祖先角色id列表
     */
    List<Integer> getAncestorRoleIds(Integer roleId);

    /**
     * 更新角色
     * @param role 角色
     */
    void updateRole(Role role);
}
