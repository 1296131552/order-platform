package com.company.user.service.domain.permission.bk.impl;

import com.company.user.enums.model.PermissionBkType;
import com.company.user.model.entity.PermissionBk;
import com.company.user.service.basic.PermissionBkService;
import com.company.user.service.domain.permission.base.impl.BasePermissionValidationDomainImpl;
import com.company.user.service.domain.permission.bk.PermissionBkValidationDomain;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class PermissionBkValidationDomainImpl extends BasePermissionValidationDomainImpl<
        PermissionBk
        >
        implements PermissionBkValidationDomain {

    @Resource
    private PermissionBkService permissionBkService;

    /**
     * 校验类型是否符合，要求如下
     * <li>目录类型只能有目录和菜单类型的子节点</li>
     * <li>菜单类型只能有按钮类型的子节点</li>
     * @param parentId 父节点ID
     * @param type 权限类型
     * @return 是否符合
     */
    @Override
    public boolean validateTypeComply(Integer parentId, PermissionBkType type) {
        PermissionBk parentPermissionBk = permissionBkService.getById(parentId);
        PermissionBkType parentType = parentPermissionBk == null ? PermissionBkType.DIRECTORY : parentPermissionBk.getType();

        if (type == PermissionBkType.DIRECTORY || type == PermissionBkType.MENU) {
            return parentType == PermissionBkType.DIRECTORY;
        } else {
            return parentType == PermissionBkType.MENU;
        }
    }
}
