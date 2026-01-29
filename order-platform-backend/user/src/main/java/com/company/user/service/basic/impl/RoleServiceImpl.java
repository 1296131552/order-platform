package com.company.user.service.basic.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.user.constant.RoleConstant;
import com.company.user.mapper.RoleMapper;
import com.company.user.model.entity.Role;
import com.company.user.service.basic.RoleService;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService{
    /**
     * 获取最高级角色
     * @param roleIds 角色id列表
     * @return 最高级角色
     */
    @Override
    public Integer getHighestLevel(List<Integer> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return null;
        }
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Role::getId, roleIds)
                .ge(Role::getLevel, 0)
                .orderByAsc(Role::getLevel)
                .last("limit 1");
        Role role = getOne(queryWrapper);
        if (role == null) {
            return RoleConstant.GLOBAL_ROLE_LEVEL;
        }
        return role.getLevel();
    }

    /**
     * 获取子角色列表
     * @param roleId 角色id
     * @return 子角色列表
     */
    @Override
    public List<Role> getChildRoles(Integer roleId) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getParentNodeId, roleId);
        return list(queryWrapper);
    }

    /**
     * 获取子角色id列表
     * @param roleId 角色id
     * @return 子角色id列表
     */
    @Override
    public List<Integer> getChildRoleIds(Integer roleId) {
        return Role.extractIds(getChildRoles(roleId));
    }

    /**
     * 获取用户列表
     * @param roleIds 角色id列表
     * @return 用户列表
     */
    @Override
    public List<Role> getRoles(List<Integer> roleIds) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Role::getId, roleIds);
        return list(queryWrapper);
    }

    /**
     * 获取非全局角色列表
     * @return 非全局角色列表
     */
    @Override
    public List<Role> getNotGlobalRoles() {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(Role::getLevel, RoleConstant.GLOBAL_ROLE_LEVEL);
        return list(queryWrapper);
    }

 /**
     * 获取非全局角色id列表
     * @return 非全局角色id列表
     */
    @Override
    public List<Integer> getNotGlobalRoleIds() {
        return Role.extractIds(getNotGlobalRoles());
    }

    /**
     * 添加角色
     * @param role 角色
     */
    @Override
    public void addRole(Role role) {
        Integer level = getLevel(role.getParentNodeId());
        role.setLevel(level);
        save(role);
    }

    /**
     * 添加全局角色
     * @param role 角色
     */
    @Override
    public void addGlobalRole(Role role) {
        role.setParentNodeId(RoleConstant.GLOBAL_ROLE_PARENT_ID);
        role.setLevel(RoleConstant.GLOBAL_ROLE_LEVEL);
        save(role);
    }

    /**
     * 删除角色
     * @param roleId 角色ID
     */
    @Override
    public void deleteRole(Integer roleId) {
        List<Integer> descendantRoleIds = getDescendantRoleIds(roleId);
        descendantRoleIds.add(roleId);
        removeByIds(descendantRoleIds);
    }

    /**
     * 获取子孙角色列表
     * @param roleIds 角色id列表
     * @return 子孙角色列表
     */
    @Override
    public List<Role> getDescendantRoles(List<Integer> roleIds) {
        List<Role> allRoles = list();

        // 建立角色关系映射，key为父角色ID，value为子角色列表
        Map<Integer, List<Role>> parentChildMap = Role.buildParentChildMap(allRoles);

        // 递归收集所有子孙角色ID
        Set<Integer> childRoleIds = new HashSet<>();
        addChildRoleIds(roleIds, parentChildMap, childRoleIds);

        // 过滤出实际存在的子孙角色
        return allRoles.stream()
                .filter(role -> childRoleIds.contains(role.getId()))
                .collect(Collectors.toList());
    }

    /**
     * 获取子角色列表
     * @param roleId 角色id
     * @return 子角色列表
     */
    @Override
    public List<Role> getDescendantRoles(Integer roleId) {
        return getDescendantRoles(List.of(roleId));
    }

    /**
     * 获取子角色id列表
     * @param roleIds 角色id列表
     * @return 子角色id列表
     */
    @Override
    public List<Integer> getDescendantRoleIds(List<Integer> roleIds) {
        return Role.extractIds(getDescendantRoles(roleIds));
    }

    /**
     * 获取子角色id列表
     * @param roleId 角色id
     * @return 子角色id列表
     */
    @Override
    public List<Integer> getDescendantRoleIds(Integer roleId) {
        return Role.extractIds(getDescendantRoles(roleId));
    }

    /**
     * 获取祖先角色id列表
     * @param roleId 角色id
     * @return 祖先角色id列表
     */
    @Override
    public List<Integer> getAncestorRoleIds(Integer roleId) {
        List<Integer> ancestorRoleIds = new ArrayList<>();
        List<Role> roles = list();

        Map<Integer, Role> roleMap = Role.buildMap(roles);

        Role currentRole = roleMap.get(roleId);
        while (!Objects.equals(currentRole.getParentNodeId(), RoleConstant.ROOT_ROLE_PARENT_ID)) {
            ancestorRoleIds.add(currentRole.getParentNodeId());
            currentRole = roleMap.get(currentRole.getParentNodeId());
        }

        return ancestorRoleIds;
    }

    /**
     * 更新角色
     * @param role 角色
     */
    @Override
    public void updateRole(Role role) {
        Integer level = getLevel(role.getParentNodeId());
        role.setLevel(level);
        updateById(role);
    }

    /**
     * 获取角色等级
     * @param parentNodeId 父节点id
     * @return 角色等级
     */
    private Integer getLevel(Integer parentNodeId) {
        Role parentRole = getById(parentNodeId);
        return parentRole.getLevel() + 1;
    }

    /**
     * 递归收集子角色ID
     * @param parentIds 父角色ID集合
     * @param parentChildMap 父子关系映射
     * @param resultIds 结果集合
     */
    private void addChildRoleIds(List<Integer> parentIds,
                                 Map<Integer, List<Role>> parentChildMap,
                                 Set<Integer> resultIds) {
        List<Integer> nextLevelIds = new ArrayList<>();

        for (Integer parentId : parentIds) {
            List<Role> children = parentChildMap.get(parentId);
            if (children != null) {
                for (Role child : children) {
                    if (!resultIds.contains(child.getId())) {
                        resultIds.add(child.getId());
                        nextLevelIds.add(child.getId());
                    }
                }
            }
        }

        // 如果还有下一层子角色，继续递归
        if (!nextLevelIds.isEmpty()) {
            addChildRoleIds(nextLevelIds, parentChildMap, resultIds);
        }
    }

}
