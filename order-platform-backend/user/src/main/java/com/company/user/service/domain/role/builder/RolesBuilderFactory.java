package com.company.user.service.domain.role.builder;

import org.springframework.stereotype.Service;

import com.company.user.converter.RoleConverter;
import com.company.user.service.basic.RoleService;
import com.company.user.service.basic.UserRoleService;

import jakarta.annotation.Resource;

@Service
public class RolesBuilderFactory {
    @Resource
    private RoleService roleService;
    @Resource
    private UserRoleService userRoleService;
    @Resource
    private RoleConverter roleConverter;
    /**
     * 创建新的角色列表构建器实例
     */
    public RolesBuilder create() {
        return new RolesBuilder(roleService, userRoleService,roleConverter);
    }

    /**
     * 创建新的角色列表构建器实例（指定用户）
     */
    public RolesBuilder create(Integer userId) {
        return create()
                .forUser(userId);
    }
}
