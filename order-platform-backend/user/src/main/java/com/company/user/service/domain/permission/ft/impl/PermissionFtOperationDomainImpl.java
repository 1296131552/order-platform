package com.company.user.service.domain.permission.ft.impl;

import com.company.user.model.dto.PermissionFtDTO;
import com.company.user.model.entity.PermissionFt;
import com.company.user.model.entity.RolePermissionFt;
import com.company.user.service.domain.permission.base.impl.BasePermissionOperationDomainImpl;
import com.company.user.service.domain.permission.ft.PermissionFtOperationDomain;

public class PermissionFtOperationDomainImpl extends BasePermissionOperationDomainImpl<
        PermissionFt,
        RolePermissionFt,
        PermissionFtDTO
        >
        implements PermissionFtOperationDomain {
}
