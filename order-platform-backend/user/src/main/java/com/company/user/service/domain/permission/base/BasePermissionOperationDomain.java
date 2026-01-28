package com.company.user.service.domain.permission.base;

import com.company.user.model.dto.base.BasePermissionDTO;

public interface BasePermissionOperationDomain<DTO extends BasePermissionDTO> {
    /**
     * 添加权限
     * @param permissionDTO 权限DTO对象
     */
    void addPermission(DTO permissionDTO);

    /**
     * 修改权限
     * @param permissionDTO 权限DTO对象
     */
    void modifyPermission(DTO permissionDTO);

    /**
     * 删除权限
     * @param permissionId 权限ID
     */
    void deletePermission(Integer permissionId);

     /**
      * 移动权限
      * @param permissionId 权限ID
      * @param isUp 是否向上移动
      */
    void movePermission(Integer permissionId, Boolean isUp);
}

