package com.company.user.service.basic.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.common.exception.BusinessException;
import com.company.user.enums.result.UserResultCode;
import com.company.user.mapper.UserRoleMapper;
import com.company.user.model.entity.Role;
import com.company.user.model.entity.UserRole;
import com.company.user.service.basic.UserRoleService;
import com.company.common.utils.CollectionUtil;

import jakarta.annotation.Resource;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService{
    @Resource
    private UserRoleMapper userRoleMapper;

    /**
     * 根据用户ID查询角色名称列表
     * @param userId 用户ID
     * @return 角色名称列表
     */
    @Override
    public List<Role> getRolesByUserId(Integer userId) {
        List<Role> roles = userRoleMapper.selectRolesByUserId(userId);
        if (roles.isEmpty()) {
            throw new BusinessException(UserResultCode.USER_ROLE_NOT_FOUND);
        }
        return roles;
    }

    /**
     * 根据用户ID查询角色ID列表
     * @param userId 用户ID
     * @return 角色ID列表
     */
    @Override
    public List<Integer> getRoleIds(Integer userId) {
        if (userId == null) {
            return List.of();
        }
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        List<UserRole> userRoles = list(wrapper);
        return UserRole.extractRoleIds(userRoles);
    }
    
    /**
     * 根据用户ID列表查询角色ID列表
     * @param userIds 用户ID列表
     * @return 角色ID列表
     */
    @Override
    public List<Integer> getRoleIdsByUserIds(List<Integer> userIds) {
        return userRoleMapper.selectRoleIdsByUserIds(userIds);
    }

    /**
     * 根据用户ID查询角色ID列表
     * @param userId 用户ID
     * @return 角色ID列表
     */
    @Override
    public List<Integer> getRoleIdsByUserId(Integer userId) {
        return getRoleIdsByUserIds(List.of(userId));
    }

    /**
     * 为用户添加角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    @Override
    public void addUserRoles(Integer userId, List<Integer> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }
        userRoleMapper.addUserRoles(userId, roleIds);
    }

    /**
     * 删除用户角色
     * @param userId 用户ID
     */
    @Override
    public void deleteUserRoles(Integer userId) {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        remove(wrapper);
    }

    /**
     * 删除用户角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    public void deleteUserRoles(Integer userId, List<Integer> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        wrapper.in(UserRole::getRoleId, roleIds);
        remove(wrapper);
    }

    /**
     * 更新用户角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    @Override
    public void updateUserRoles(Integer userId, List<Integer> roleIds) {
        List<Integer> oldRoleIds = getRoleIdsByUserId(userId);
        List<Integer> added = CollectionUtil.AddedCompare(oldRoleIds, roleIds);
        List<Integer> removed = CollectionUtil.RemovedCompare(oldRoleIds, roleIds);
        addUserRoles(userId, added);
        deleteUserRoles(userId, removed);
    }

    /**
     * 根据角色ID删除用户角色
     * @param roleId 角色ID
     */
    @Override
    public void deleteByRoleId(Integer roleId) {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getRoleId, roleId);
        remove(wrapper);
    }

    /**
     * 根据角色删除用户角色
     * @param role 角色
     */
    @Override
    public void deleteByRoleId(Role role) {
        deleteByRoleId(role.getId());
    }
}
