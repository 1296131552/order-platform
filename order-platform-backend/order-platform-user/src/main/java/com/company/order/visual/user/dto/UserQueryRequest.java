package com.company.order.visual.user.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户查询请求
 */
@Data
public class UserQueryRequest {

    // ==================== 账号信息（模糊查询）====================

    /** 用户名（模糊） */
    private String username;

    /** 用户编码（模糊）- 企业场景常用 */
    private String userCode;

    /** 邮箱（模糊）- 管理员常用 */
    private String email;

    /** 手机号（模糊）- 管理员常用 */
    private String phone;

    // ==================== 基本信息 ====================

    /** 真实姓名（模糊） */
    private String realName;

    /** 职位（模糊） */
    private String position;

    /** 工号（模糊） */
    private String employeeNo;

    // ==================== 状态筛选 ====================

    /** 是否启用 */
    private Boolean isEnabled;

    /** 是否锁定 */
    private Boolean isLocked;

    // ==================== 组织与权限 ====================

    /** 部门ID */
    private Long departmentId;

    /** 角色ID */
    private Long roleId;

    // ==================== 时间范围 ====================

    /** 创建时间-开始 */
    private LocalDateTime createdAtStart;

    /** 创建时间-结束 */
    private LocalDateTime createdAtEnd;

    /** 最后登录时间-开始（活跃用户分析） */
    private LocalDateTime lastLoginTimeStart;

    /** 最后登录时间-结束 */
    private LocalDateTime lastLoginTimeEnd;

    // ==================== 分页 ====================

    /** 页码，默认1 */
    private Integer pageNum = 1;

    /** 每页大小，默认10 */
    private Integer pageSize = 10;
}
