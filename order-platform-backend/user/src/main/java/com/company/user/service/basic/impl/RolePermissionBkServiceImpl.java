package com.company.user.service.basic.impl;

import com.company.user.mapper.RolePermissionBkMapper;
import com.company.user.model.entity.RolePermissionBk;
import com.company.user.service.basic.RolePermissionBkService;
import org.springframework.stereotype.Service;

/**
 * 角色后台权限关联服务实现类
 */
@Service
public class RolePermissionBkServiceImpl
        extends BaseRolePermissionServiceImpl<RolePermissionBkMapper, RolePermissionBk>
        implements RolePermissionBkService {
}
