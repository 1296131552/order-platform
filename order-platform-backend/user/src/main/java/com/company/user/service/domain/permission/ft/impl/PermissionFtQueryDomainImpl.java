package com.company.user.service.domain.permission.ft.impl;

import com.company.user.model.entity.PermissionFt;
import com.company.user.model.entity.RolePermissionFt;
import com.company.user.service.domain.permission.base.impl.BasePermissionQueryDomainImpl;
import com.company.user.service.domain.permission.ft.PermissionFtQueryDomain;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionFtQueryDomainImpl extends BasePermissionQueryDomainImpl<
        PermissionFt,
        RolePermissionFt
        >
        implements PermissionFtQueryDomain {

    @Override
    protected List<Integer> getAllPermissionIds() {
        return List.of(1, 2, 3);
    }
}
