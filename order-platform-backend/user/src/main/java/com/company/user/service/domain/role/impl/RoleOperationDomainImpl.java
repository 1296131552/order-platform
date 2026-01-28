package com.company.user.service.domain.role.impl;

import com.company.common.utils.CollectionUtil;
import com.company.user.constant.RoleConstant;
import com.company.user.converter.RoleConverter;
import com.company.user.model.dto.RoleDTO;
import com.company.user.model.entity.Role;
import com.company.user.service.basic.RoleService;
import com.company.user.service.basic.RolePermissionBkService;
import com.company.user.service.basic.UserRoleService;
import com.company.user.service.basic.PermissionBkService;
import com.company.user.service.domain.role.RoleOperationDomain;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class RoleOperationDomainImpl implements RoleOperationDomain {

    @Resource
    private RoleService roleService;
    @Resource
    private UserRoleService userRoleService;
    @Resource
    private RolePermissionBkService rolePermissionBkService;
    @Resource
    private PermissionBkService permissionBkService;
    @Resource
    private RoleConverter roleConverter;

    /**
     * 添加角色
     * @param roleDTO 角色dto
     */
    @Override
    public void addRole(RoleDTO roleDTO) {
        Role role = roleConverter.toRole(roleDTO);
        roleService.addRole(role);
        // 获取全局角色有权限的页面
        List<Integer> globalPermissionIds = getPermissionIdsByParentRoleId(RoleConstant.GLOBAL_ROLE_PARENT_ID);
        List<Integer> parentPermissionIds = rolePermissionBkService.getPermissionIds(roleDTO.getParentNodeId());

        // 取公共部分
        List<Integer> commonPermissionIds = CollectionUtil.CommonCompare(globalPermissionIds, parentPermissionIds);

        // 分配权限权限
        rolePermissionBkService.addRolePermissions(role.getId(), commonPermissionIds);
    }

    /**
     * 添加全局角色
     * @param roleDTO 角色dto
     */
    @Override
    public void addGlobalRole(RoleDTO roleDTO) {
        Role role = roleConverter.toRole(roleDTO);
        roleService.addGlobalRole(role);
    }

    /**
     * 更新角色
     * @param roleDTO 角色dto
     */
    @Override
    public void updateRole(RoleDTO roleDTO) {
        Role role = roleConverter.toRole(roleDTO);
        roleService.updateRole(role);
    }

    /**
     * 更新全局角色
     * @param roleDTO 角色dto
     */
    @Override
    public void updateGlobalRole(RoleDTO roleDTO) {
        Role role = roleConverter.toRole(roleDTO);
        roleService.updateById(role);
    }

    /**
     * 删除角色
     * @param roleId 角色ID
     */
    @Override
    public void deleteRole(Integer roleId) {
        roleService.deleteRole(roleId);
        userRoleService.deleteByRoleId(roleId);
        rolePermissionBkService.deleteByRoleId(roleId);
    }

    /**
     * 分配权限权限
     * @param roleId 角色ID
     * @param oldPermissionIds 旧权限ID列表
     * @param newPermissionIds 新权限ID列表
     */
    @Override
    public void assignNormalPermission(Integer roleId, List<Integer> oldPermissionIds, List<Integer> newPermissionIds) {
        // 当前添加的权限以及当前添加的权限的祖宗权限
        List<Integer> addedAndAncestorViewIds = getAddedAndAncestorViewIds(oldPermissionIds, newPermissionIds);
        // 当前删除的权限以及当前删除的权限的子孙权限
        List<Integer> removedAndDescendantViewIds = getRemovedAndDescendantViewIds(oldPermissionIds, newPermissionIds);

        // 修改当前角色权限
        rolePermissionBkService.addRolePermissions(roleId, addedAndAncestorViewIds);
        rolePermissionBkService.deleteRolePermissions(roleId, removedAndDescendantViewIds);

        // 修改当前角色的所有子孙角色权限
        List<Integer> childRoleIds = roleService.getDescendantRoleIds(roleId);
        rolePermissionBkService.deleteRolePermissions(childRoleIds, removedAndDescendantViewIds);

        // 修改当前角色的所有祖先角色权限
        List<Integer> ancestorRoleIds = roleService.getAncestorRoleIds(roleId);
        rolePermissionBkService.addRolePermissions(ancestorRoleIds, addedAndAncestorViewIds);
    }

    /**
     * 分配全局权限权限
     * @param roleId 角色ID
     * @param oldPermissionIds 旧权限ID列表
     * @param newPermissionIds 新权限ID列表
     */
    @Override
    public void assignGlobalPermission(Integer roleId, List<Integer> oldPermissionIds, List<Integer> newPermissionIds) {
        // 当前添加的权限以及当前添加的权限的祖宗权限
        List<Integer> addedAndAncestorViewIds = getAddedAndAncestorViewIds(oldPermissionIds, newPermissionIds);
        // 当前删除的权限以及当前删除的权限的子孙权限
        List<Integer> removedAndDescendantViewIds = getRemovedAndDescendantViewIds(oldPermissionIds, newPermissionIds);

        // 修改当前角色权限
        rolePermissionBkService.addRolePermissions(roleId, addedAndAncestorViewIds);
        rolePermissionBkService.deleteRolePermissions(roleId, removedAndDescendantViewIds);

        // 修改当前所有普通角色的权限
        List<Integer> notGlobalRoleIds = roleService.getNotGlobalRoleIds();
        rolePermissionBkService.addRolePermissions(notGlobalRoleIds, addedAndAncestorViewIds);
    }

    /**
     * 获取角色权限ID列表
     * @param parentRoleId 父角色ID
     * @return 权限ID列表
     */
    @Override
    public List<Integer> getPermissionIdsByParentRoleId(Integer parentRoleId) {
        List<Integer> childRoleIds = roleService.getChildRoleIds(parentRoleId);
        return rolePermissionBkService.getPermissionIds(childRoleIds);
    }

    /**
     * 获取新增权限和祖先权限ID列表
     * @param oldViewIds 旧权限ID列表
     * @param newViewIds 新权限ID列表
     * @return 新增权限和祖先权限ID列表
     */
    private List<Integer> getAddedAndAncestorViewIds(List<Integer> oldViewIds, List<Integer> newViewIds) {
        List<Integer> addedViewIds = CollectionUtil.AddedCompare(oldViewIds, newViewIds);
        List<Integer> ancestorIds = permissionBkService.getAncestorIds(addedViewIds);

        // 流式去重
        return Stream.concat(addedViewIds.stream(), ancestorIds.stream()).distinct().collect(Collectors.toList());
    }

    /**
     * 获取减少权限和后代权限ID列表
     * @param oldViewIds 旧权限ID列表
     * @param newViewIds 新权限ID列表
     * @return 减少权限和后代权限ID列表
     */
    private List<Integer> getRemovedAndDescendantViewIds(List<Integer> oldViewIds, List<Integer> newViewIds) {
        List<Integer> removedViewIds = CollectionUtil.RemovedCompare(oldViewIds, newViewIds);
        List<Integer> descendantIds = permissionBkService.getDescendantIds(removedViewIds);

        // 流式去重
        return Stream.concat(removedViewIds.stream(), descendantIds.stream()).distinct().collect(Collectors.toList());
    }
}
