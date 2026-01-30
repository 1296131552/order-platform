package com.company.user.service.basic.impl;

import com.company.user.mapper.RolePermissionFtMapper;
import com.company.user.model.entity.RolePermissionFt;
import com.company.user.service.basic.RolePermissionFtService;
import org.springframework.stereotype.Service;

/**
 * 角色前台权限关联服务实现类
 */
@Service
public class RolePermissionFtServiceImpl
        extends BaseRolePermissionServiceImpl<RolePermissionFtMapper, RolePermissionFt>
        implements RolePermissionFtService {
}
