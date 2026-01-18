package com.company.order.visual.user.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {
    private Long id;
    private String username;
    private String userCode;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private Boolean isEnabled;
    private Boolean isLocked;
    private String position;
    private String employeeNo;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private Integer loginCount;
    private List<RoleInfo> roles;
    private LocalDateTime createdAt;

    /**
     * 角色信息（从 LoginResponse 移过来，统一管理）
     */
    @Data
    @Builder
    @NoArgsConstructor 
    @AllArgsConstructor
    public static class RoleInfo {
        private Long roleId;
        private String roleCode;
        private String roleName;
        private Integer dataScopeType;
        private Boolean isPrimary;
    }    
}
