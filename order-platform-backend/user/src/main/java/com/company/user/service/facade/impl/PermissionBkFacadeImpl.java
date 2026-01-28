package com.company.user.service.facade.impl;
import com.company.common.exception.BusinessException;
import com.company.user.constant.PermissionConstant;
import com.company.user.enums.model.PermissionBkType;
import com.company.user.enums.model.RoleType;
import com.company.user.enums.result.PermissionResultCode;
import com.company.user.model.dto.PermissionBkDTO;
import com.company.user.model.vo.PermissionBkVO;
import com.company.user.service.domain.permission.bk.PermissionBkOperationDomain;
import com.company.user.service.domain.permission.bk.PermissionBkQueryDomain;
import com.company.user.service.domain.permission.bk.PermissionBkValidationDomain;
import com.company.user.service.domain.permission.bk.builder.PermissionsBkBuilderFactory;
import com.company.user.service.domain.role.RoleQueryDomain;
import com.company.user.service.domain.role.builder.RolesBuilderFactory;
import com.company.user.service.facade.PermissionBkFacade;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
@Service
public class PermissionBkFacadeImpl implements PermissionBkFacade {
    @Resource
    private PermissionBkQueryDomain permissionBkQueryDomain;
    @Resource
    private PermissionBkValidationDomain permissionBkValidationDomain;
    @Resource
    private PermissionBkOperationDomain permissionBkOperationDomain;
    @Resource
    private RoleQueryDomain roleQueryDomain;

    @Resource
    private RolesBuilderFactory rolesBuilderFactory;
    @Resource
    private PermissionsBkBuilderFactory permissionsBkBuilderFactory;

    /**
     * 获取权限树
     * @param userId 用户ID
     * @return 权限树
     */
    @Override
    public List<PermissionBkVO> getViewTree(Integer userId) {
        List<Integer> currentRoleIds = roleQueryDomain.getRoleIds(userId);
        return permissionsBkBuilderFactory.create(currentRoleIds)
                .withPermissionTypes(List.of(PermissionBkType.DIRECTORY, PermissionBkType.MENU, PermissionBkType.PAGE))
                .withPermissionFlag()
                .buildTree();
    }

    /**
     * 获取菜单树
     * @param userId 用户ID
     * @return 菜单树
     */
    @Override
    public List<PermissionBkVO> getMenuTree(Integer userId) {
        List<Integer> currentRoleIds = roleQueryDomain.getRoleIds(userId);
        return permissionsBkBuilderFactory.create(currentRoleIds)
                .withPermissionTypes(List.of(PermissionBkType.DIRECTORY, PermissionBkType.MENU))
                .filterByPermission()
                .buildTree();
    }

    /**
     * 获取菜单和按钮树
     * @param userId 用户ID
     * @return 菜单和按钮树
     */
    @Override
    public List<PermissionBkVO> getMenuAndButtonTree(Integer userId, Integer targetRoleId) {
        List<Integer> currentRoleIds = roleQueryDomain.getRoleIds(userId);

        return permissionsBkBuilderFactory.create(currentRoleIds, targetRoleId)
                .withPermissionTypes(List.of(PermissionBkType.DIRECTORY, PermissionBkType.MENU, PermissionBkType.BUTTON))
                .withChangeFlag()
                .withPermissionFlag()
                .buildTree();
    }
    /**
     * 获取目录树
     * @param userId 用户ID
     * @return 目录树
     */
    @Override
    public List<PermissionBkVO> getDirectoryTree(Integer userId) {
        List<Integer> currentRoleIds = roleQueryDomain.getRoleIds(userId);
        return permissionsBkBuilderFactory.create(currentRoleIds)
                .withPermissionTypes(List.of(PermissionBkType.DIRECTORY))
                .filterByPermission()
                .buildTree();
    }

    /**
     * 获取权限列表
     * @param keyword 权限名称关键词
     * @return 权限列表
     */
    @Override
    public List<PermissionBkVO> getViewVOS(Integer userId, String keyword) {
        List<Integer> currentRoleIds = roleQueryDomain.getRoleIds(userId);
        return permissionsBkBuilderFactory.create(currentRoleIds)
                .withPermissionTypes(List.of(PermissionBkType.DIRECTORY, PermissionBkType.MENU, PermissionBkType.PAGE))
                .withKeyword(keyword)
                .filterByPermission()
                .build();
    }

    /**
     * 获取权限码列表
     * @param userId 用户ID
     * @return 权限码列表
     */
    @Override
    public List<String> getPermissionCode(Integer userId) {
        List<Integer> currentAndChildRoleIds = rolesBuilderFactory.create(userId).forRoleTypes(List.of(
                RoleType.SELF, RoleType.CHILD
        )).buildIds();
        return permissionBkQueryDomain.getPermissionCode(currentAndChildRoleIds);
    }

    /**
     * 添加权限
     * @param userId 用户ID
     * @param permissionBkDTO 权限DTO
     */
    @Override
    public void addPermission(Integer userId, PermissionBkDTO permissionBkDTO) {
        if (!permissionBkValidationDomain.validateTypeComply(permissionBkDTO.getParentId(), permissionBkDTO.getType())) {
            throw new BusinessException(PermissionResultCode.PERMISSION_TYPE_NOT_COMPLY);
        }
        if (!Objects.equals(permissionBkDTO.getParentId(), PermissionConstant.ROOT_PERMISSION_PARENT_ID)
                && !permissionBkValidationDomain.validateUserHavePermission(userId, permissionBkDTO.getParentId())) {
            throw new BusinessException(PermissionResultCode.PERMISSION_NOT_AUTHORIZED_ADD);
        }
        permissionBkOperationDomain.addPermission(permissionBkDTO);
    }

    /**
     * 修改权限
     * @param userId 用户ID
     * @param permissionBkDTO 权限DTO
     */
    @Override
    public void modifyPermission(Integer userId, PermissionBkDTO permissionBkDTO) {
        // 父ID不能等于当前ID或者当前ID的子ID
        if (permissionBkValidationDomain.validateParentIdEqualsSelfAndDescendants(permissionBkDTO.getId(), permissionBkDTO.getParentId())) {
            throw new BusinessException(PermissionResultCode.PERMISSION_CANNOT_BE_PARENT);
        }
        // 父权限有规定的子权限类型，比如 DIRECTORY 的子权限不能是 BUTTON
        if (!permissionBkValidationDomain.validateTypeComply(permissionBkDTO.getParentId(), permissionBkDTO.getType())) {
            throw new BusinessException(PermissionResultCode.PERMISSION_TYPE_NOT_COMPLY);
        }
        if (!permissionBkValidationDomain.validateModifyValid(userId, permissionBkDTO.getId(), permissionBkDTO.getIsValid())) {
            throw new BusinessException(PermissionResultCode.PERMISSION_CANNOT_MODIFY_VALID);
        }
        if (!permissionBkValidationDomain.validateUserHavePermission(userId, permissionBkDTO.getId())) {
            throw new BusinessException(PermissionResultCode.PERMISSION_NOT_AUTHORIZED_MODIFY);
        }
        permissionBkOperationDomain.modifyPermission(permissionBkDTO);
    }

    /**
     * 删除权限
     * @param userId 用户ID
     * @param viewId 权限ID
     */
    @Override
    public void deletePermission(Integer userId, Integer viewId) {
        if (!permissionBkValidationDomain.validateUserHavePermission(userId, viewId)) {
            throw new BusinessException(PermissionResultCode.PERMISSION_NOT_AUTHORIZED_DELETE);
        }
        permissionBkOperationDomain.deletePermission(viewId);
    }
    /**
     * 移动权限
     * @param viewId 权限ID
     * @param isUp 是否上移
     */
    @Override
    public void movePermission(Integer viewId, Boolean isUp) {
        if (isUp && !permissionBkValidationDomain.validateMoveUp(viewId)) {
            throw new BusinessException(PermissionResultCode.PERMISSION_CANNOT_MOVE_UP);
        }
        if (!isUp && !permissionBkValidationDomain.validateMoveDown(viewId)) {
            throw new BusinessException(PermissionResultCode.PERMISSION_CANNOT_MOVE_DOWN);
        }
        permissionBkOperationDomain.movePermission(viewId, isUp);
    }

}
