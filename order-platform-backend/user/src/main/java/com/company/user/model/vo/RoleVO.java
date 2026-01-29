package com.company.user.model.vo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.company.user.constant.RoleConstant;
import com.company.user.model.entity.Role;

import lombok.Data;

@Data
public class RoleVO {
    private Role node;
    private Boolean hasPermission = false;
    private List<RoleVO> children = new ArrayList<>();

    /**
     * 构建角色VO映射
     */
    public static Map<Integer, RoleVO> buildMap(List<RoleVO> roleVOS) {
        return roleVOS.stream().collect(Collectors.toMap(
                roleVO -> roleVO.getNode().getId(),
                roleVO -> roleVO,
                (first, second) -> first, // 冲突时保留先出现的值
                LinkedHashMap::new
        ));
    }

    /**
     * 从角色VO列表中提取全局角色，同时从角色VO列表中移除全局角色
     *
     * @param roleVOS 角色VO列表
     * @return 全局角色VO列表
     */
    public static List<RoleVO> extractGlobalRoles(List<RoleVO> roleVOS) {
        List<RoleVO> globalRoles = roleVOS.stream()
                .filter(roleVO -> roleVO.getNode().getParentNodeId().equals(RoleConstant.GLOBAL_ROLE_PARENT_ID))
                .toList();

        // 从角色VO列表中移除全局角色
        roleVOS.removeAll(globalRoles);
        return globalRoles;
    }

    /**
     * 设置角色是否有权限
     * @param permittedRoleIds 权限角色ID列表
     * @param roleVOS 角色VO列表
     */
    public static void setHasPermission(List<Integer> permittedRoleIds, List<RoleVO> roleVOS) {
        roleVOS.forEach(roleVO -> {
            if (permittedRoleIds.contains(roleVO.getNode().getId())) {
                roleVO.setHasPermission(true);
            }
        });
    }

    /**
     * 过滤出角色树中有权限的角色
     * @param permittedRoleIds 权限角色ID列表
     * @param roleVOS 角色VO列表
     */
    public static void filterHasPermission(List<Integer> permittedRoleIds, List<RoleVO> roleVOS) {
        roleVOS.removeIf(roleVO -> !permittedRoleIds.contains(roleVO.getNode().getId()));
    }
}
