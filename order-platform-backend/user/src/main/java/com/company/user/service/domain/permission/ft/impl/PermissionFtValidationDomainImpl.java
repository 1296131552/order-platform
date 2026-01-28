package com.company.user.service.domain.permission.ft.impl;

import com.company.user.enums.model.PermissionFtType;
import com.company.user.model.entity.PermissionFt;
import com.company.user.service.basic.PermissionFtService;
import com.company.user.service.domain.permission.base.impl.BasePermissionValidationDomainImpl;
import com.company.user.service.domain.permission.ft.PermissionFtValidationDomain;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class PermissionFtValidationDomainImpl extends BasePermissionValidationDomainImpl<
        PermissionFt
        >
        implements PermissionFtValidationDomain {
    @Resource
    private PermissionFtService permissionFtService;

    /**
     * 校验类型是否符合，按钮类型不能有子节点
     * @param parentId 父节点ID
     * @param type 权限类型
     * @return 是否符合
     */
    @Override
    public boolean validateTypeComply(Integer parentId, PermissionFtType type) {
        PermissionFt parentPermissionFt = permissionFtService.getById(parentId);
        PermissionFtType parentType = parentPermissionFt == null ? PermissionFtType.PAGE : parentPermissionFt.getType();
        return parentType != PermissionFtType.BUTTON;
    }
}
