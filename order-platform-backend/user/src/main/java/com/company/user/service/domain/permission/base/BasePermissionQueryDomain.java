package com.company.user.service.domain.permission.base;

import java.util.List;

public interface BasePermissionQueryDomain {
    /**
     * 获取权限id列表
     * @param roleId 角色id
     * @return 权限id列表
     */
    List<Integer> getPermissionId(Integer roleId);

    /**
     * 获取权限id列表
     * @param roleIds 角色id列表
     * @return 权限id列表
     */
    List<Integer> getPermissionId(List<Integer> roleIds);

    /**
     * 获取权限码列表
     * @param roleIds 角色id列表
     * @return 权限码列表
     */
    List<String> getPermissionCode(List<Integer> roleIds);
}