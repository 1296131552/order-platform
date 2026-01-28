package com.company.user.service.domain.permission.base.impl;

import com.company.user.converter.PermissionGenericConverter;
import com.company.user.model.dto.base.BasePermissionDTO;
import com.company.user.model.entity.base.BasePermission;
import com.company.user.model.entity.base.BaseRolePermission;
import com.company.user.service.basic.BasePermissionService;
import com.company.user.service.basic.BaseRolePermissionService;
import com.company.user.service.basic.factory.PermissionServiceFactory;
import com.company.user.service.basic.factory.RolePermissionServiceFactory;
import com.company.user.service.domain.permission.base.BasePermissionOperationDomain;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class)
public class BasePermissionOperationDomainImpl<
        T extends BasePermission,
        RP extends BaseRolePermission,
        DTO extends BasePermissionDTO
        >
        implements BasePermissionOperationDomain<DTO> {

    @Resource
    private PermissionGenericConverter permissionGenericConverter;

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
     * 添加权限
     * @param permissionDTO 权限DTO对象
     */
    @Override
    public void addPermission(DTO permissionDTO) {
        T permission = permissionGenericConverter.toPermission(permissionDTO);
        permissionService.tailInsert(permission);    }

    /**
     * 修改权限
     * @param permissionDTO 权限DTO对象
     */
    @Override
    public void modifyPermission(DTO permissionDTO) {
        T permission = permissionGenericConverter.toPermission(permissionDTO);
        permissionService.modifyPermission(permission);
    }

    /**
     * 删除权限
     * @param permissionId 权限ID
     */
    @Override
    public void deletePermission(Integer permissionId) {
        permissionService.deletePermission(permissionId);
        rolePermissionService.deleteByPermissionId(permissionId);
    }

    /**
     * 移动权限
     * @param permissionId 权限ID
     * @param isUp 是否向上移动
     */
    @Override
    public void movePermission(Integer permissionId, Boolean isUp) {
        if (isUp) {
            permissionService.moveUp(permissionId);
        } else {
            permissionService.moveDown(permissionId);
        }
    }
}
