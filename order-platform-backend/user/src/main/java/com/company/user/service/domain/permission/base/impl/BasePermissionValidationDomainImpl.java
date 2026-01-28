package com.company.user.service.domain.permission.base.impl;

import com.company.user.constant.PermissionConstant;
import com.company.user.enums.model.PermissionBkType;
import com.company.user.enums.model.PermissionFtType;
import com.company.user.model.entity.base.BasePermission;
import com.company.user.service.basic.BasePermissionService;
import com.company.user.service.basic.UserRoleService;
import com.company.user.service.basic.factory.PermissionServiceFactory;
import com.company.user.service.domain.permission.base.BasePermissionQueryDomain;
import com.company.user.service.domain.permission.base.BasePermissionValidationDomain;
import com.company.user.service.domain.permission.factory.PermissionQueryFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class BasePermissionValidationDomainImpl<
        T extends BasePermission
        >
        implements BasePermissionValidationDomain {

    private BasePermissionService<T> permissionService;
    private BasePermissionQueryDomain permissionQueryDomain;

    @Resource
    private PermissionServiceFactory permissionServiceFactory;

    @Resource
    private PermissionQueryFactory permissionQueryFactory;

    @Resource
    private UserRoleService userRoleService;

    @PostConstruct
    public void init() {
        permissionService = permissionServiceFactory.getServiceByGeneric(this, 0);
        permissionQueryDomain = permissionQueryFactory.getServiceByGeneric(this, 0);
    }

    /**
     * 校验用户是否有权限权限
     * @param userId 用户ID
     * @param permissionIds 权限ID列表
     * @return 是否有权限权限
     */
    @Override
    public boolean validateUserHavePermissions(Integer userId, List<Integer> permissionIds) {
        List<Integer> roleIds = userRoleService.getRoleIds(userId);
        List<Integer> currentPermissionIds = permissionQueryDomain.getPermissionId(roleIds);
        return new HashSet<>(currentPermissionIds).containsAll(permissionIds);
    }

    /**
     * 校验用户是否有指定权限
     * @param userId 用户ID
     * @param permissionId 权限ID
     * @return 是否有指定权限
     */
    @Override
    public boolean validateUserHavePermission(Integer userId, Integer permissionId) {
        return validateUserHavePermissions(userId, List.of(permissionId));
    }

    /**
     * 校验权限类型是否符合要求，由子类实现
     * @param parentId 父权限ID
     * @param type 权限类型
     * @return 是否符合要求
     */
    @Override
    public boolean validateTypeComply(Integer parentId, PermissionBkType type) {
        throw new UnsupportedOperationException("方法未实现");
    }

    /**
     * 校验权限类型是否符合要求，由子类实现
     * @param parentId 父权限ID
     * @param type 权限类型
     * @return 是否符合要求
     */
    @Override
    public boolean validateTypeComply(Integer parentId, PermissionFtType type) {
        throw new UnsupportedOperationException("方法未实现");
    }

    /**
     * 校验父权限ID是否等于自身或其子权限ID
     * @param parentId 父权限ID
     * @param permissionId 权限ID
     * @return 是否符合要求
     */
    @Override
    public boolean validateParentIdEqualsSelfAndDescendants(Integer parentId, Integer permissionId) {
        if (Objects.equals(parentId, permissionId)) {
            return true;
        }
        List<Integer> descendantIds = permissionService.getDescendantIds(parentId);
        return descendantIds.contains(permissionId);
    }

     /**
      * 校验是否可以上移，不能上移的情况为：
      * <p>
      * 权限为头权限
      * @param permissionId 权限ID
      * @return 是否可以上移
      */
     @Override
     public boolean validateMoveUp(Integer permissionId) {
        T permission = permissionService.getById(permissionId);
        return !Objects.equals(permission.getOrder(), PermissionConstant.HEAD_PERMISSION_ORDER);
    }

     /**
      * 校验是否可以下移，不能下移的情况为：
      * <p>
      * 权限为尾权限
      * @param permissionId 权限ID
      * @return 是否可以下移
      */
     @Override
     public boolean validateMoveDown(Integer permissionId) {
         T permission = permissionService.getById(permissionId);
         return !permissionService.isTailPermission(permission);
    }

    /**
     * 校验是否可以修改权限的启用状态，不能修改的情况为：
     * <p>
     * 并没有该权限所有后代权限的权限
     * @param useId 用户ID
     * @param permissionId 权限ID
     * @param isValid 权限状态
     * @return 是否可以修改权限的启用状态
     */
    @Override
    public boolean validateModifyValid(Integer useId, Integer permissionId, Boolean isValid) {
         T permission = permissionService.getById(permissionId);
        // 验证是否修改了权限的启用状态
        if (permission.getIsValid() == isValid) {
            return true;
        }
        // 如果对该权限的后代权限都有权限，就可以修改启用状态
        List<Integer> descendantIds = permissionService.getDescendantIds(permissionId);
        return validateUserHavePermissions(useId, descendantIds);
    }
}

