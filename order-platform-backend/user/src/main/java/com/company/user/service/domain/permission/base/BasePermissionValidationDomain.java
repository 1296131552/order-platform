package com.company.user.service.domain.permission.base;

import com.company.user.enums.model.PermissionBkType;
import com.company.user.enums.model.PermissionFtType;

import java.util.List;

public interface BasePermissionValidationDomain {
    /**
     * 校验用户是否有指定权限列表
     * @param userId 用户ID
     * @param permissionIds 权限ID列表
     * @return 是否有权限权限
     */
    boolean validateUserHavePermissions(Integer userId, List<Integer> permissionIds);

    /**
     * 校验用户是否有指定权限
     * @param userId 用户ID
     * @param permissionId 权限ID
     * @return 是否有指定权限
     */
    boolean validateUserHavePermission(Integer userId, Integer permissionId);

     /**
      * 校验权限类型是否符合要求，由子类实现
      * @param parentId 父权限ID
      * @param type 权限类型
      * @return 是否符合要求
      */
    boolean validateTypeComply(Integer parentId, PermissionBkType type);

    /**
     * 校验权限类型是否符合要求，由子类实现
     * @param parentId 父权限ID
     * @param type 权限类型
     * @return 是否符合要求
     */
    boolean validateTypeComply(Integer parentId, PermissionFtType type);

     /**
      * 校验父权限ID是否等于自身ID或其子权限ID
      * @param parentId 父权限ID
      * @param permissionId 权限ID
      * @return 是否符合要求
      */
    boolean validateParentIdEqualsSelfAndDescendants(Integer parentId, Integer permissionId);

     /**
      * 校验是否可以上移
      * @param permissionId 权限ID
      * @return 是否可以上移
      */
    boolean validateMoveUp(Integer permissionId);

     /**
      * 校验是否可以下移
      * @param permissionId 权限ID
      * @return 是否可以下移
      */
    boolean validateMoveDown(Integer permissionId);

     /**
      * 校验是否可以修改权限有效性
      * @param useId 用户ID
      * @param permissionId 权限ID
      * @param isValid 是否有效
      * @return 是否可以修改权限有效性
      */
    boolean validateModifyValid(Integer useId, Integer permissionId, Boolean isValid);
}
