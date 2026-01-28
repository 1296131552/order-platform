package com.company.user.service.domain.permission.bk.impl;

import com.company.user.enums.model.PermissionBkType;
import com.company.user.model.entity.PermissionBk;
import com.company.user.model.entity.RolePermissionBk;
import com.company.user.service.basic.PermissionBkService;
import com.company.user.service.domain.permission.base.impl.BasePermissionQueryDomainImpl;
import com.company.user.service.domain.permission.bk.PermissionBkQueryDomain;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionBkQueryDomainImpl extends BasePermissionQueryDomainImpl<PermissionBk, RolePermissionBk>
        implements PermissionBkQueryDomain {

    private final PermissionBkService permissionBkService;

    public PermissionBkQueryDomainImpl(PermissionBkService permissionBkService) {
        this.permissionBkService = permissionBkService;
    }

    @Override
    protected List<Integer> getAllPermissionIds() {
        return PermissionBk.extractPermissionIds(permissionBkService.getPermissionByTypes(List.of(
                PermissionBkType.DIRECTORY,
                PermissionBkType.MENU,
                PermissionBkType.BUTTON
        )));
    }
}
