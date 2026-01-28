package com.company.user.service.basic;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.user.model.entity.base.BaseRolePermission;

import java.util.List;

public interface BaseRolePermissionService <T extends BaseRolePermission> extends IService<T> {

    /**
     * 获取角色的权限id列表
     * @param roleId 角色id
     * @return 权限id列表
     */
    List<Integer> getPermissionIds(Integer roleId);

    /**
     * 获取角色列表的权限id列表
     * @param roleIds 角色id列表
     * @return 权限id列表
     */
    List<Integer> getPermissionIds(List<Integer> roleIds);

    /**
     * 删除角色的所有权限
     * @param roleId 角色id
     */
    void deleteByRoleId(Integer roleId);

     /**
      * 删除权限的所有角色
      * @param permissionId 权限id
      */
    void deleteByPermissionId(Integer permissionId);

    /**
     * 获取角色列表和权限列表中存在的角色权限关系
     * @param roleIds 角色id列表
     * @param permissionIds 权限id列表
     * @return 角色权限关系列表
     */
    List<String> getExists(List<Integer> roleIds, List<Integer> permissionIds);

     /**
      * 添加角色的权限
      * @param roleId 角色id
      * @param permissionIds 权限id列表
      */
    void addRolePermissions(Integer roleId, List<Integer> permissionIds);

     /**
      * 添加角色列表的权限
      * @param roleIds 角色id列表
      * @param permissionIds 权限id列表
      */
    void addRolePermissions(List<Integer> roleIds, List<Integer> permissionIds);

     /**
      * 删除角色的权限
      * @param roleId 角色id
      * @param permissionIds 权限id列表
      */
    void deleteRolePermissions(Integer roleId, List<Integer> permissionIds);

     /**
      * 删除角色列表的权限
      * @param roleIds 角色id列表
      * @param permissionIds 权限id列表
      */
    void deleteRolePermissions(List<Integer> roleIds, List<Integer> permissionIds);
}
