package com.company.order.visual.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户角色查询结果（用于批量查询，解决 N+1 问题）
 * <p>
 * 包含 userId 用于分组，前端不可见
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleResult {
    /**
     * 用户ID（用于分组，不返回给前端）
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 数据权限类型
     */
    private Integer dataScopeType;

    /**
     * 是否主角色
     */
    private Boolean isPrimary;

    /**
     * 转换为 RoleInfo（去除 userId）
     */
    public UserVO.RoleInfo toRoleInfo() {
        return UserVO.RoleInfo.builder()
                .roleId(roleId)
                .roleCode(roleCode)
                .roleName(roleName)
                .dataScopeType(dataScopeType)
                .isPrimary(isPrimary)
                .build();
    }
}
