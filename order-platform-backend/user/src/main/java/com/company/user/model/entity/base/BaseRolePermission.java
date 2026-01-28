package com.company.user.model.entity.base;

import java.util.List;
import java.util.stream.Collectors;
import com.company.common.model.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class BaseRolePermission extends BaseEntity {
    /**
     * 角色ID（关联role表）
     */
    private Integer roleId;

     /**
      * 权限ID（关联permission表）
      */
    private Integer permissionId;

    /**
     * 构造函数，受保护，用于子类继承
     * @param roleId 角色id
     * @param permissionId 权限id
     */
    protected BaseRolePermission(Integer roleId, Integer permissionId) {
        this.roleId = roleId;
        this.permissionId = permissionId;
    }

    /**
     * 从角色权限列表中提取权限ID列表（去重）
     * @param rolePermissions 角色权限列表
     * @return 权限ID列表（去重）
     */
    public static <T extends BaseRolePermission> List<Integer> extractPermissionIds(List<T> rolePermissions) {
        return rolePermissions.stream().map(BaseRolePermission::getPermissionId).distinct().collect(Collectors.toList());
    }

    /**
     * 拼接角色权限id列表（返回格式roleId_viewId）
     * @param rolePermissions 角色权限列表
     * @return 角色权限id列表（去重）
     */
    public static <T extends BaseRolePermission> List<String> spliceRolePermissionIds(List<T> rolePermissions) {
        return rolePermissions.stream().map(rv -> rv.getRoleId() + "_" + rv.getPermissionId()).distinct().collect(Collectors.toList());
    }

    /**
     * 批量创建角色权限实例
     * @param entityClass 实体类
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @param <T> 角色权限类型
     * @return 角色权限实例列表
     */
    public static <T extends BaseRolePermission> List<T> createList(Class<T> entityClass, Integer roleId, List<Integer> permissionIds) {
        return permissionIds.stream()
                .map(permissionId -> {
                    try {
                        return entityClass.getDeclaredConstructor(Integer.class, Integer.class)
                                .newInstance(roleId, permissionId);
                    } catch (Exception e) {
                        throw new RuntimeException("创建角色权限实例失败", e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 批量创建角色权限实例
     * @param entityClass 实体类
     * @param roleIds 角色ID列表
     * @param permissionIds 权限ID列表
     * @param <T> 角色权限类型
     * @return 角色权限实例列表
     */
    public static <T extends BaseRolePermission> List<T> createList(Class<T> entityClass, List<Integer> roleIds, List<Integer> permissionIds) {
        return roleIds.stream()
                .flatMap(roleId -> createList(entityClass, roleId, permissionIds).stream())
                .collect(Collectors.toList());
    }
}
