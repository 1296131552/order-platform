package com.company.user.service.domain.permission.base.impl;

import com.company.user.constant.RoleConstant;
import com.company.user.model.entity.base.BasePermission;
import com.company.user.model.entity.base.BaseRolePermission;
import com.company.user.service.basic.BasePermissionService;
import com.company.user.service.basic.BaseRolePermissionService;
import com.company.user.service.basic.RoleService;
import com.company.user.service.basic.factory.PermissionServiceFactory;
import com.company.user.service.basic.factory.RolePermissionServiceFactory;
import com.company.user.service.domain.permission.base.BasePermissionQueryDomain;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import java.util.List;
import java.util.Objects;

public abstract class BasePermissionQueryDomainImpl<
        T extends BasePermission,
        RP extends BaseRolePermission
        >
        implements BasePermissionQueryDomain {

    @Resource
    private RoleService roleService;

    @Resource
    private PermissionServiceFactory permissionServiceFactory;

    @Resource
    private RolePermissionServiceFactory rolePermissionServiceFactory;

    protected BasePermissionService<T> permissionService;
    protected BaseRolePermissionService<RP> rolePermissionService;

    @PostConstruct
    public void init() {
        // 通过工厂的反射方法获取服务，指定泛型参数位置
        this.permissionService = permissionServiceFactory.getServiceByGeneric(this, 0); // 第一个泛型参数
        this.rolePermissionService = rolePermissionServiceFactory.getServiceByGeneric(this, 1); // 第二个泛型参数
    }

    /**
     * 获取权限id列表（单角色）
     * @param roleId 角色id
     * @return 权限id列表
     */
    @Override
    public List<Integer> getPermissionId(Integer roleId) {
        return getPermissionId(List.of(roleId));
    }

    /**
     * 获取权限id列表
     * @param roleIds 角色id列表
     * @return 权限id列表
     */
    @Override
    public List<Integer> getPermissionId(List<Integer> roleIds) {
        // 获取最高级角色
        Integer highestLevel = roleService.getHighestLevel(roleIds);
        if (Objects.equals(highestLevel, RoleConstant.ROLE_HIGHEST_LEVEL)) {
            // 返回所有权限
            return getAllPermissionIds();
        }
        // 否则返回角色权限列表
        return rolePermissionService.getPermissionIds(roleIds);
    }

    /**
     * 获取权限码列表
     * @param roleIds 角色id列表
     * @return 权限码列表
     */
    @Override
    public List<String> getPermissionCode(List<Integer> roleIds) {
        List<Integer> permissionId = getPermissionId(roleIds);
        return permissionService.getPermissionCodes(permissionId);
    }

    /**
     * 获取所有权限id列表（抽象方法，子类实现）
     * @return 所有权限id列表
     */
    protected abstract List<Integer> getAllPermissionIds();
}

