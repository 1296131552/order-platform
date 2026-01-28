package com.company.user.model.vo.base;

import java.util.ArrayList;

import com.company.user.model.entity.base.BasePermission;

import lombok.Data;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class BasePermissionVO <T extends BasePermission, SELF extends BasePermissionVO<T, SELF>> {
    private T node;
    /**
     * 目标角色是否有该权限
     */
    private Boolean hasPermission = false;
    /**
     * 当前用户角色可以修改的权限（即拥有的权限）
     */
    private Boolean hasChange = false;
    private List<SELF> children = new ArrayList<>();

    /**
     * 构建id到权限VO的映射
     * @param permissionVOS 节点列表
     * @return id到权限VO的映射
     */
    public static <T extends BasePermission, V extends BasePermissionVO<T, V>> Map<Integer, V> buildMap(List<V> permissionVOS) {
        return permissionVOS.stream().collect(Collectors.toMap(vo -> vo.getNode().getId(), vo -> vo));
    }

    /**
     * 获取相同父节点的权限VO列表
     * @param permissionVOS 节点列表
     * @param parentId 父节点id
     * @return 相同父节点的权限VO列表
     */
    public static <T extends BasePermission, V extends BasePermissionVO<T, V>> List<V> getSameParentPermissionVO(Collection<V> permissionVOS, Integer parentId) {
        return permissionVOS.stream().filter(vo -> vo.getNode().getParentId().equals(parentId)).collect(Collectors.toList());
    }

    /**
     * 设置权限是否有权限
     * @param permittedIds 权限ID列表
     * @param permissions 权限列表
     */
    public static <T extends BasePermission, V extends BasePermissionVO<T, V>> void setHasPermission(List<Integer> permittedIds, List<V> permissions) {
        permissions.forEach(vo -> {
            if (permittedIds.contains(vo.getNode().getId())) {
                vo.setHasPermission(true);
            }
        });
    }

    /**
     * 设置权限是否有变更
     * @param permittedIds 权限ID列表
     * @param permissions 权限列表
     */
    public static <T extends BasePermission, V extends BasePermissionVO<T, V>> void setHasChange(List<Integer> permittedIds, List<V> permissions) {
        permissions.forEach(vo -> vo.setHasChange(permittedIds.contains(vo.getNode().getId())));
    }
}
