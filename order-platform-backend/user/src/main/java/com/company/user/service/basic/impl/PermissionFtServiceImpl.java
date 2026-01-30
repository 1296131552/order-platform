package com.company.user.service.basic.impl;

import com.company.user.mapper.PermissionFtMapper;
import com.company.user.model.entity.PermissionFt;
import com.company.user.service.basic.PermissionFtService;
import org.springframework.stereotype.Service;

/**
 * 前台权限服务实现类
 */
@Service
public class PermissionFtServiceImpl
        extends BasePermissionServiceImpl<PermissionFtMapper, PermissionFt>
        implements PermissionFtService {
}
