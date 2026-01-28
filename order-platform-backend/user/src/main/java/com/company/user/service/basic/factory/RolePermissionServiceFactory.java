package com.company.user.service.basic.factory;

import com.company.common.factory.BaseServiceFactory;
import com.company.user.model.entity.RolePermissionBk;
import com.company.user.model.entity.RolePermissionFt;
import com.company.user.model.entity.base.BaseRolePermission;
import com.company.user.service.basic.BaseRolePermissionService;
import com.company.user.service.basic.RolePermissionBkService;
import com.company.user.service.basic.RolePermissionFtService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class RolePermissionServiceFactory extends BaseServiceFactory<BaseRolePermissionService<?>, BaseRolePermission> {

    @Resource
    private RolePermissionBkService rolePermissionBkService;

    @Resource
    private RolePermissionFtService rolePermissionFtService;

    @PostConstruct
    public void init() {
        registerService(RolePermissionBk.class, rolePermissionBkService);
        registerService(RolePermissionFt.class, rolePermissionFtService);
    }
}
