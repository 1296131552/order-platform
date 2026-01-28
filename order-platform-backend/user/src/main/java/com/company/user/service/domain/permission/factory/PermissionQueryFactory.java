package com.company.user.service.domain.permission.factory;

import com.company.common.factory.BaseServiceFactory;
import com.company.user.model.entity.PermissionBk;
import com.company.user.model.entity.PermissionFt;
import com.company.user.model.entity.base.BasePermission;
import com.company.user.service.domain.permission.base.BasePermissionQueryDomain;
import com.company.user.service.domain.permission.bk.PermissionBkQueryDomain;
import com.company.user.service.domain.permission.ft.PermissionFtQueryDomain;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class PermissionQueryFactory extends BaseServiceFactory<BasePermissionQueryDomain, BasePermission> {
    @Resource
    private PermissionBkQueryDomain permissionBkQueryDomain;

    @Resource
    private PermissionFtQueryDomain permissionFtQueryDomain;

    @PostConstruct
    public void init() {
        registerService(PermissionBk.class, permissionBkQueryDomain);
        registerService(PermissionFt.class, permissionFtQueryDomain);
    }
}
