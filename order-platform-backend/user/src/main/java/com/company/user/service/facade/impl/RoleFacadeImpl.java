package com.company.user.service.facade.impl;

import com.company.common.exception.BusinessException;
import com.company.system.api.SettingApi;
import com.company.user.enums.model.RoleType;
import com.company.user.enums.model.RolesType;
import com.company.user.enums.result.PermissionResultCode;
import com.company.user.enums.result.RoleResultCode;
import com.company.user.model.dto.AssignPermissionDTO;
import com.company.user.model.dto.RoleDTO;
import com.company.user.model.entity.Role;
import com.company.user.model.vo.RoleVO;
import com.company.user.service.domain.permission.bk.PermissionBkQueryDomain;
import com.company.user.service.domain.permission.bk.PermissionBkValidationDomain;
import com.company.user.service.domain.role.RoleOperationDomain;
import com.company.user.service.domain.role.builder.RoleValidationBuilderFactory;
import com.company.user.service.domain.role.builder.RolesBuilderFactory;
import com.company.user.service.facade.RoleFacade;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleFacadeImpl implements RoleFacade {

    @Resource
    private RoleOperationDomain roleOperationDomain;
    @Resource
    private PermissionBkQueryDomain permissionBkQueryDomain;
    @Resource
    private PermissionBkValidationDomain permissionBkValidationDomain;

    @Resource
    private RolesBuilderFactory rolesBuilderFactory;
    @Resource
    private RoleValidationBuilderFactory roleValidationBuilderFactory;

    @Resource
    private SettingApi settingApi;

    /**
     * 获取角色树
     * @param currentUserId 当前用户ID
     * @param type 类型
     * @return 角色树
     */
    @Override
    public List<RoleVO> getRoleTree(Integer currentUserId, RolesType type) {
        List<RoleType> roleTypes = type.toRoleTypes();
        List<RoleVO> roleVOS;
        if (type == RolesType.ALL) {
            // 当前用户的子角色和全局角色是有权限的角色
            List<Integer> permittedRoleIds = rolesBuilderFactory
                    .create(currentUserId)
                    .forRoleTypes(List.of(RoleType.CHILD, RoleType.GLOBAL))
                    .buildIds();
            // 获取非全局的所有角色，并标注好hasPermission（是否有权限）
            roleVOS = rolesBuilderFactory.create().forRoleType(RoleType.ALL)
                    .buildTree(settingApi.hasPermissionDisplay(), permittedRoleIds);
        } else {
            roleVOS = rolesBuilderFactory.create(currentUserId).forRoleTypes(roleTypes).buildTree();
        }
        return roleVOS;
    }

    /**
     * 获取角色列表
     * @param currentUserId 当前用户ID
     * @param type 类型
     * @return 角色列表
     */
    @Override
    public List<Role> getRoles(Integer currentUserId, RolesType type) {
        List<RoleType> roleTypes = type.toRoleTypes();
        return rolesBuilderFactory.create(currentUserId).forRoleTypes(roleTypes).build();
    }

    /**
     * 添加角色
     * @param roleDTO 角色dto
     */
    @Override
    public void addRole(RoleDTO roleDTO, Integer userId) {
        // 如果要添加的角色的父角色不是当前角色的子角色或者本身，则不能添加
        if (!roleValidationBuilderFactory.create(userId)
                .forRoleTypes(RolesType.CHILD_AND_SELF.toRoleTypes())
                .validateRole(roleDTO.getParentNodeId())
        ) {
            throw new BusinessException(RoleResultCode.ROLE_NOT_AUTHORIZED_ADD);
        }
        roleOperationDomain.addRole(roleDTO);
    }

    /**
     * 添加全局角色
     * @param roleDTO 角色dto
     */
    @Override
    public void addGlobalRole(RoleDTO roleDTO) {
        roleOperationDomain.addGlobalRole(roleDTO);
    }

    /**
     * 修改角色
     * @param roleDTO 角色dto
     * @param userId 用户ID
     */
    @Override
    public void updateRole(RoleDTO roleDTO, Integer userId) {
        // 如果要修改的角色不是当前角色的子角色，则不能修改
        if (!roleValidationBuilderFactory.create(userId)
                .forRoleType(RoleType.CHILD)
                .validateRole(roleDTO.getId())
        ) {
            throw new BusinessException(RoleResultCode.ROLE_NOT_AUTHORIZED_UPDATE);
        }
        // 如果要修改角色的父角色不是当前角色的子角色或者本身，则不能修改
        if (!roleValidationBuilderFactory.create(userId)
                .forRoleTypes(RolesType.CHILD_AND_SELF.toRoleTypes())
                .validateRole(roleDTO.getParentNodeId())
        ) {
            throw new BusinessException(RoleResultCode.ROLE_NOT_AUTHORIZED_UPDATE);
        }
        roleOperationDomain.updateRole(roleDTO);
    }

    /**
     * 更新全局角色
     * @param roleDTO 角色dto
     */
    @Override
    public void updateGlobalRole(RoleDTO roleDTO) {
        // 如果当前要修改的角色不是全局角色，则不能修改
        if (!roleValidationBuilderFactory.create()
                .forRoleType(RoleType.GLOBAL)
                .validateRole(roleDTO.getId())
        ) {
            throw new BusinessException(RoleResultCode.ROLE_NOT_GLOBAL);
        }
        roleOperationDomain.updateGlobalRole(roleDTO);
    }

    /**
     * 删除角色
     * @param roleId 角色ID
     * @param userId 用户ID
     */
    @Override
    public void deleteRole(Integer roleId, Integer userId) {
        // 如果要删除的角色不是当前角色的子角色，则不能删除
        if (!roleValidationBuilderFactory.create(userId)
                .forRoleType(RoleType.CHILD)
                .validateRole(roleId)
        ) {
            throw new BusinessException(RoleResultCode.ROLE_NOT_AUTHORIZED_DELETE);
        }
        roleOperationDomain.deleteRole(roleId);
    }

    /**
     * 删除全局角色
     * @param roleId 角色ID
     */
    @Override
    public void deleteGlobalRole(Integer roleId) {
        // 如果当前要删除的角色不是全局角色，则不能删除
        if (!roleValidationBuilderFactory.create()
                .forRoleType(RoleType.GLOBAL)
                .validateRole(roleId)
        ) {
            throw new BusinessException(RoleResultCode.ROLE_NOT_GLOBAL);
        }
        roleOperationDomain.deleteRole(roleId);
    }

    /**
     * 分配权限权限
     * @param userId 当前操作用户ID
     * @param dto 分配权限dto
     */
    @Override
    public void assignPermission(Integer userId, AssignPermissionDTO dto) {
        Integer targetRoleId = dto.getRoleId();
        List<Integer> newPermissionIds = dto.getViewIds();

        // 如果要分配权限的角色不是当前角色的子角色，则不能分配
        if (!roleValidationBuilderFactory.create(userId)
                .forRoleType(RoleType.CHILD)
                .validateRole(targetRoleId)
        ) {
            throw new BusinessException(RoleResultCode.ROLE_NOT_AUTHORIZED_ASSIGN);
        }
        // 如果要分配的权限是当前角色没有的，则不能分配
        if (!permissionBkValidationDomain.validateUserHavePermissions(userId, newPermissionIds)) {
            throw new BusinessException(PermissionResultCode.PERMISSION_NOT_AUTHORIZED_ASSIGN);
        }

        List<Integer> oldViewIds = permissionBkQueryDomain.getPermissionId(targetRoleId);
        roleOperationDomain.assignNormalPermission(targetRoleId, oldViewIds, newPermissionIds);
    }

    /**
     * 分配全局权限权限
     * @param dto 分配权限dto
     */
    @Override
    public void assignGlobalPermission(AssignPermissionDTO dto) {
        Integer targetRoleId = dto.getRoleId();
        List<Integer> newPermissionIds = dto.getViewIds();
        List<Integer> oldPermissionIds = permissionBkQueryDomain.getPermissionId(targetRoleId);
        roleOperationDomain.assignGlobalPermission(targetRoleId, oldPermissionIds, newPermissionIds);
    }
}
