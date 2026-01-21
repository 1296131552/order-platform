package com.company.order.visual.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户查询请求
 */
@Data
@Schema(description = "用户分页查询请求")
public class UserQueryRequest {

    // ==================== 账号信息（模糊查询）====================

    @Schema(description = "用户名（模糊查询）", example = "admin")
    private String username;

    @Schema(description = "用户编码（模糊查询）", example = "U001")
    private String userCode;

    @Schema(description = "邮箱（模糊查询）", example = "admin@example.com")
    private String email;

    @Schema(description = "手机号（模糊查询）", example = "138")
    private String phone;

    // ==================== 基本信息 ====================

    @Schema(description = "真实姓名（模糊查询）", example = "张三")
    private String realName;

    @Schema(description = "职位（模糊查询）", example = "系统管理员")
    private String position;

    @Schema(description = "工号（模糊查询）", example = "E001")
    private String employeeNo;

    // ==================== 状态筛选 ====================

    @Schema(description = "是否启用", example = "true")
    private Boolean isEnabled;

    @Schema(description = "是否锁定", example = "false")
    private Boolean isLocked;

    // ==================== 组织与权限 ====================

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "角色ID", example = "1")
    private Long roleId;

    // ==================== 时间范围 ====================

    @Schema(description = "创建时间-开始（格式：yyyy-MM-dd HH:mm:ss）", example = "2024-01-01T00:00:00")
    private LocalDateTime createdAtStart;

    @Schema(description = "创建时间-结束（格式：yyyy-MM-dd HH:mm:ss）", example = "2024-12-31T23:59:59")
    private LocalDateTime createdAtEnd;

    @Schema(description = "最后登录时间-开始", example = "2024-01-01T00:00:00")
    private LocalDateTime lastLoginTimeStart;

    @Schema(description = "最后登录时间-结束", example = "2024-12-31T23:59:59")
    private LocalDateTime lastLoginTimeEnd;

    // ==================== 分页 ====================

    @Schema(description = "页码（从1开始）", example = "1", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10", defaultValue = "10")
    private Integer pageSize = 10;
}
