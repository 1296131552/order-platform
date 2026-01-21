package com.company.order.visual.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户视图对象
 * <p>
 * userCode 动态计算：USER + 10位数字ID（左侧补零）
 * 这样就不需要在数据库存储冗余字段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息视图对象")
public class UserVO {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名（登录账号）", example = "admin")
    private String username;

    /**
     * 用户编码（业务编号，动态计算）
     * 格式：USER + 10位数字ID（左侧补零）
     * 示例：USER0000000001
     * <p>
     * 不在数据库存储，通过 id 动态计算，消除冗余
     */
    @Schema(description = "用户编码（业务编号）", example = "USER0000000001")
    public String getUserCode() {
        return id != null ? String.format("USER%010d", id) : null;
    }

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "邮箱地址", example = "admin@example.com")
    private String email;

    @Schema(description = "手机号码", example = "13800138000")
    private String phone;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "是否启用（true=启用，false=禁用）", example = "true")
    private Boolean isEnabled;

    @Schema(description = "是否锁定（true=锁定，false=正常）", example = "false")
    private Boolean isLocked;

    @Schema(description = "职位", example = "系统管理员")
    private String position;

    @Schema(description = "工号", example = "E001")
    private String employeeNo;

    @Schema(description = "最后登录时间", example = "2024-01-20T10:30:00")
    private LocalDateTime lastLoginTime;

    @Schema(description = "最后登录IP", example = "192.168.1.100")
    private String lastLoginIp;

    @Schema(description = "登录次数", example = "42")
    private Integer loginCount;

    @Schema(description = "用户角色列表")
    private List<RoleInfo> roles;

    @Schema(description = "创建时间", example = "2024-01-01T09:00:00")
    private LocalDateTime createdAt;

    /**
     * 角色信息（从 LoginResponse 移过来，统一管理）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户角色信息")
    public static class RoleInfo {

        @Schema(description = "角色ID", example = "1")
        private Long roleId;

        @Schema(description = "角色编码", example = "ROLE_ADMIN")
        private String roleCode;

        @Schema(description = "角色名称", example = "系统管理员")
        private String roleName;

        @Schema(description = "数据权限类型（1=全部，2=本部门，3=本人）", example = "1")
        private Integer dataScopeType;

        @Schema(description = "是否为主角色", example = "true")
        private Boolean isPrimary;
    }
}
