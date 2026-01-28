package com.company.user.service.domain.permission.bk.impl;

import com.company.user.model.dto.PermissionBkDTO;
import com.company.user.model.entity.PermissionBk;
import com.company.user.model.entity.RolePermissionBk;
import com.company.user.service.domain.permission.base.impl.BasePermissionOperationDomainImpl;
import com.company.user.service.domain.permission.bk.PermissionBkOperationDomain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class PermissionBkOperationDomainImpl extends BasePermissionOperationDomainImpl<
        PermissionBk,
        RolePermissionBk,
        PermissionBkDTO
        > implements PermissionBkOperationDomain {
}
