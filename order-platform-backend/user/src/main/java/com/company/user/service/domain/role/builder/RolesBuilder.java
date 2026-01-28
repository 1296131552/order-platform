package com.company.user.service.domain.role.builder;

import com.company.common.utils.TreeBuilder;
import com.company.user.constant.RoleConstant;
import com.company.user.converter.RoleConverter;
import com.company.user.enums.model.RoleType;
import com.company.user.model.entity.Role;
import com.company.user.model.vo.RoleVO;
import com.company.user.service.basic.RoleService;
import com.company.user.service.basic.UserRoleService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE) // 所有字段默认私有
public class RolesBuilder {

    final RoleService roleService;
    final UserRoleService userRoleService;
    final RoleConverter roleConverter;

    public RolesBuilder(RoleService roleService, UserRoleService userRoleService, RoleConverter roleConverter) {
        this.roleService = roleService;
        this.userRoleService = userRoleService;
        this.roleConverter = roleConverter;
    }

    /**
     * 查询信息
     */
    Integer userId;
    List<RoleType> roleTypes;

    /**
     * 为用户查询
     */
    public RolesBuilder forUser(Integer userId) {
        this.userId = userId;
        return this;
    }

    /**
     * 角色类型列表查询
     */
    public RolesBuilder forRoleTypes(List<RoleType> roleTypes) {
        this.roleTypes = roleTypes;
        return this;
    }

    /**
     * 角色类型查询
     */
    public RolesBuilder forRoleType(RoleType roleType) {
        this.roleTypes = List.of(roleType);
        return this;
    }

    /**
     * 构建角色列表
     */
    public List<Role> build() {
        Set<Role> resultRoles = new HashSet<>();

        roleTypes.forEach(rolesType -> {
            switch (rolesType) {
                case ALL:
                    resultRoles.addAll(roleService.list());
                    break;
                case CHILD:
                    List<Integer> roleIds = userRoleService.getRoleIds(userId);
                    resultRoles.addAll(roleService.getDescendantRoles(roleIds));
                    break;
                case GLOBAL:
                    resultRoles.addAll(roleService.getChildRoles(RoleConstant.GLOBAL_ROLE_PARENT_ID));
                    break;
                case SELF:
                    resultRoles.addAll(userRoleService.getRolesByUserId(userId));
                    break;
                case NOT_GLOBAL:
                    resultRoles.addAll(roleService.getNotGlobalRoles());
                    break;
            }
        });

        return Role.sortByCreateTime(resultRoles);
    }

    /**
     * 构建角色ID列表
     */
    public List<Integer> buildIds() {
        return Role.extractIds(build());
    }

    /**
     * 构建角色树
     */
    public List<RoleVO> buildTree() {
        // 当前构建的角色ID列表作为基础角色列表
        List<Role> roles = build();
        List<RoleVO> roleVOS = roleConverter.toRoleVOS(roles);
        return buildRoleTree(roleVOS);
    }

    /**
     * 构建角色树（根据是否显示权限过滤角色）
     * @param hasPermissionDisplay 是否显示权限
     * @param permittedRoleIds 权限角色ID列表
     * @return 角色树
     */
    public List<RoleVO> buildTree(boolean hasPermissionDisplay, List<Integer> permittedRoleIds) {
        // 当前构建的角色ID列表作为基础角色列表
        List<Role> roles = build();
        List<RoleVO> roleVOS = roleConverter.toRoleVOS(roles);

        // 过滤出角色树中有权限的角色
        if (hasPermissionDisplay) {
            RoleVO.filterHasPermission(permittedRoleIds, roleVOS);
        }

        // 设置角色是否有权限
        RoleVO.setHasPermission(permittedRoleIds, roleVOS);

        // 构建角色树
        return buildRoleTree(roleVOS);
    }

    /**
     * 具体的角色树构建方法，根据角色VO列表构建角色树
     * @param roleVOS 角色VO列表
     * @return 角色树
     */
    private List<RoleVO> buildRoleTree(List<RoleVO> roleVOS) {
        // 从角色VO列表中提取全局角色，同时从角色VO列表中移除全局角色
        List<RoleVO> globalRoleVOS = RoleVO.extractGlobalRoles(roleVOS);

        TreeBuilder<RoleVO, Integer> treeBuilder = new TreeBuilder<>();

        // 构建角色树
        List<RoleVO> roleTree = treeBuilder.buildTree(
                roleVOS,
                roleVO -> roleVO.getNode().getId(),
                roleVO -> roleVO.getNode().getParentNodeId(),
                RoleVO::getChildren,
                RoleConstant.ROOT_ROLE_PARENT_ID,
                null
        );

        // 全局角色添加到角色树
        roleTree.addAll(globalRoleVOS);
        return roleTree;
    }
}
