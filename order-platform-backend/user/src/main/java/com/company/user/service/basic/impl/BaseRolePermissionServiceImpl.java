package com.company.user.service.basic.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.user.model.entity.base.BaseRolePermission;
import com.company.user.service.basic.BaseRolePermissionService;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

public class BaseRolePermissionServiceImpl<M extends BaseMapper<T>, T extends BaseRolePermission> extends ServiceImpl<M, T>
    implements BaseRolePermissionService<T> {

    /**
     * 获取角色权限ID列表
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @Override
    public List<Integer> getPermissionIds(Integer roleId) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        return BaseRolePermission.extractPermissionIds(list(queryWrapper));
    }

    /**
     * 获取角色权限ID列表
     * @param roleIds 角色ID列表
     * @return 权限ID列表
     */
    @Override
    public List<Integer> getPermissionIds(List<Integer> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("role_id", roleIds);
        return BaseRolePermission.extractPermissionIds(list(queryWrapper));
    }

    /**
     * 根据角色删除角色权限
     * @param roleId 角色ID
     */
    @Override
    public void deleteByRoleId(Integer roleId) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        remove(queryWrapper);
    }

    /**
     * 根据权限删除角色权限
     * @param permissionId 权限ID
     */
    @Override
    public void deleteByPermissionId(Integer permissionId) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("permission_id", permissionId);
        remove(queryWrapper);
    }

    /**
     * 获取存在的角色权限ID列表，例如
     * <pre>
     * {@code
     * roleIds = [1, 2, 3]
     * permissionIds = [100, 200, 300]
     * 且只有100，200这两个权限存在
     * 则返回 ["1_100", "1_200", "2_100", "2_200", "3_100", "3_200"]
     * </pre>
     * }
     * @param roleIds 角色ID列表
     * @param permissionIds 权限ID列表
     * @return 存在的角色权限ID列表
     */
    @Override
    public List<String> getExists(List<Integer> roleIds, List<Integer> permissionIds) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("role_id", roleIds).in("permission_id", permissionIds);
        return BaseRolePermission.spliceRolePermissionIds(list(queryWrapper));
    }

     /**
     * 添加角色权限
     * @param roleId 角色id
     * @param permissionIds 权限id列表
     */
     @Override
     public void addRolePermissions(Integer roleId, List<Integer> permissionIds) {
        List<T> rolePermissions = BaseRolePermission.createList(getEntityClass(), roleId, permissionIds);
        saveBatch(rolePermissions);
     }

     /**
     * 添加角色权限
     * @param roleIds 角色id列表
     * @param permissionIds 权限id列表
     */
     @Override
     public void addRolePermissions(List<Integer> roleIds, List<Integer> permissionIds) {
         if (CollectionUtils.isEmpty(roleIds) || CollectionUtils.isEmpty(permissionIds)) {
             return;
         }
         List<T> rolePermissions = BaseRolePermission.createList(getEntityClass(), roleIds, permissionIds);
         List<String> exists = getExists(roleIds, permissionIds);
         rolePermissions.removeIf(rv -> exists.contains(rv.getRoleId() + "_" + rv.getPermissionId()));

         saveBatch(rolePermissions);
     }

     /**
     * 删除角色权限
     * @param roleId 角色id
     * @param permissionIds 权限id列表
     */
     @Override
     public void deleteRolePermissions(Integer roleId, List<Integer> permissionIds) {
         if (roleId == null || CollectionUtils.isEmpty(permissionIds)) {
             return;
         }
         QueryWrapper<T> queryWrapper = new QueryWrapper<>();
         queryWrapper.eq("role_id", roleId).in("permission_id", permissionIds);
         remove(queryWrapper);
     }

     /**
     * 删除角色权限
     * @param roleIds 角色id列表
     * @param permissionIds 权限id列表
     */
     @Override
     public void deleteRolePermissions(List<Integer> roleIds, List<Integer> permissionIds) {
         if (CollectionUtils.isEmpty(roleIds) || CollectionUtils.isEmpty(permissionIds)) {
             return;
         }
         QueryWrapper<T> queryWrapper = new QueryWrapper<>();
         queryWrapper.in("role_id", roleIds).in("permission_id", permissionIds);
         remove(queryWrapper);
     }
}
