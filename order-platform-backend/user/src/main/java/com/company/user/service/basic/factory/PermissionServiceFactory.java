package com.company.user.service.basic.factory;
import com.company.common.factory.BaseServiceFactory;
import com.company.user.model.entity.PermissionBk;
import com.company.user.model.entity.PermissionFt;
import com.company.user.model.entity.base.BasePermission;
import com.company.user.service.basic.BasePermissionService;
import com.company.user.service.basic.PermissionBkService;
import com.company.user.service.basic.PermissionFtService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class PermissionServiceFactory extends BaseServiceFactory<BasePermissionService<?>, BasePermission> {
    @Resource
    private PermissionBkService permissionBkService;

    @Resource
    private PermissionFtService permissionFtService;

    @PostConstruct
    public void init() {
        registerService(PermissionBk.class, permissionBkService);
        registerService(PermissionFt.class, permissionFtService);
    }

}