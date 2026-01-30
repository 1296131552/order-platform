package com.company.user.service.basic.impl;

import com.company.user.mapper.PermissionBkMapper;
import com.company.user.model.entity.PermissionBk;
import com.company.user.service.basic.PermissionBkService;
import org.springframework.stereotype.Service;

/**
 * 后台权限服务实现类
 */
@Service
public class PermissionBkServiceImpl
        extends BasePermissionServiceImpl<PermissionBkMapper, PermissionBk>
        implements PermissionBkService {
}
