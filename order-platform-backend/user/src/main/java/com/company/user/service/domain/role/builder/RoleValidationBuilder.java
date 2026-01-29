package com.company.user.service.domain.role.builder;

import java.util.HashSet;
import java.util.List;

import com.company.user.enums.model.RoleType;
import com.company.user.service.basic.UserRoleService;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * 角色验证构建器 - 用于灵活配置验证条件
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleValidationBuilder {
    final UserRoleService userRoleService;
    final RolesBuilderFactory rolesBuilderFactory;

    /**
     * 验证信息
     */
    Integer userId;
    List<RoleType> roleTypes;// 默认包含全局角色

    /**
     * 构造函数 - 通过工厂创建
     */
    public RoleValidationBuilder(UserRoleService userRoleService, RolesBuilderFactory rolesBuilderFactory) {
        this.userRoleService = userRoleService;
        this.rolesBuilderFactory = rolesBuilderFactory;
    }

    /**
     * 设置当前用户ID
     */
    public RoleValidationBuilder forUser(Integer currentUserId) {
        this.userId = currentUserId;
        return this;
    }

    /**
     * 设置角色类型列表
     */
    public RoleValidationBuilder forRoleTypes(List<RoleType> roleTypes) {
        this.roleTypes = roleTypes;
        return this;
    }

    /**
     * 角色类型查询
     */
    public RoleValidationBuilder forRoleType(RoleType roleType) {
        this.roleTypes = List.of(roleType);
        return this;
    }
        
    /**
     * 验证角色列表是否都在当前用户的权限范围内
     * @param targetRoleIds 目标角色ID列表
     * @return 是否全部通过验证
     */
    public boolean validateRoles(List<Integer> targetRoleIds) {
        if (targetRoleIds == null || targetRoleIds.isEmpty()) {
            return true; // 空列表默认验证通过
        }
        
        // 构建当前用户的权限范围
        List<Integer> permittedRoleIds = rolesBuilderFactory.create(userId)
                .forRoleTypes(roleTypes)
                .buildIds();
        
        // 检查所有目标角色是否都在权限范围内
        return new HashSet<>(permittedRoleIds).containsAll(targetRoleIds);
    }
    
    /**
     * 验证单个角色是否在权限范围内
     * @param targetRoleId 目标角色ID
     * @return 是否通过验证
     */
    public boolean validateRole(Integer targetRoleId) {
        if (targetRoleId == null) {
            return true; // null值默认验证通过
        }
        return validateRoles(List.of(targetRoleId));
    }
        
    /**
     * 验证用户列表是否都在当前用户的权限范围内
     * @param targetUserIds 目标用户ID列表
     * @return 是否全部通过验证
     */
    public boolean validateUsers(List<Integer> targetUserIds) {
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            return true; // 空列表默认验证通过
        }
        
        // 获取目标用户的角色ID列表
        List<Integer> roleIds = userRoleService.getRoleIdsByUserIds(targetUserIds);
        
        // 验证这些角色是否都在权限范围内
        return validateRoles(roleIds);
    }
    
    /**
     * 验证单个用户是否在权限范围内
     * @param targetUserId 目标用户ID
     * @return 是否通过验证
     */
    public boolean validateUser(Integer targetUserId) {
        if (targetUserId == null) {
            return true; // null值默认验证通过
        }
        return validateUsers(List.of(targetUserId));
    }
}
