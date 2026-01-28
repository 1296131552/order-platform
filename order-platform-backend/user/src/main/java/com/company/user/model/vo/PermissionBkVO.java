package com.company.user.model.vo;

import com.company.user.enums.model.PermissionBkType;
import com.company.user.model.entity.PermissionBk;
import com.company.user.model.vo.base.BasePermissionVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class PermissionBkVO extends BasePermissionVO<PermissionBk, PermissionBkVO> {

    /**
     * 过滤出菜单树中含有 permittedPermissionIds 的权限
     * @param permittedPermissionIds 有权限的权限ID列表
     * @param menus 菜单列表
     */
    public static void filterHasPermission(List<Integer> permittedPermissionIds, List<PermissionBkVO> menus) {
        menus.removeIf(permission -> permission.getNode().getType() != PermissionBkType.PAGE && !permittedPermissionIds.contains(permission.getNode().getId()));
    }

    /**
     * 从菜单树中提取指定类型的权限
     * @param menus 菜单列表
     * @param type 权限类型
     * @return 指定类型的权限列表
     */
    public static List<PermissionBkVO> extractType(List<PermissionBkVO> menus, PermissionBkType type) {
        return menus.stream()
                .filter(permission -> permission.getNode().getType() == type)
                .toList();
    }
}