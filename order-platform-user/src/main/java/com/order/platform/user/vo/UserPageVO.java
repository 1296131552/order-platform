package com.order.platform.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户分页响应VO
 *
 * 功能说明：
 * - 用户列表页展示数据
 * - 包含用户基本信息、组织信息、登录信息
 * - 不包含敏感信息（如密码）
 *
 * 字段说明：
 * - 基本信息：id、username、realName、email、phone、avatar
 * - 组织信息：departmentId、departmentName、position、employeeNo
 * - 状态信息：isEnabled、isLocked、lockedTime
 * - 登录信息：lastLoginTime、lastLoginIp、loginCount
 * - 角色信息：roles（用户角色列表）
 * - 系统信息：createdAt、createdBy
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户分页响应")
public class UserPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== 基本信息 ====================

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    /**
     * 用户编号
     *
     * 说明：
     * - 业务唯一标识
     * - 如：USER001
     */
    @Schema(description = "用户编号", example = "USER001")
    private String userCode;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    // ==================== 组织信息 ====================

    /**
     * 部门ID
     */
    @Schema(description = "部门ID", example = "10")
    private Long departmentId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "华东大区")
    private String departmentName;

    /**
     * 职位
     */
    @Schema(description = "职位", example = "客户经理")
    private String position;

    /**
     * 工号
     */
    @Schema(description = "工号", example = "EMP001")
    private String employeeNo;

    // ==================== 状态信息 ====================

    /**
     * 是否启用
     *
     * 说明：
     * - 0：禁用
     * - 1：启用
     */
    @Schema(description = "是否启用：0-禁用，1-启用", example = "1")
    private Integer isEnabled;

    /**
     * 是否锁定
     *
     * 说明：
     * - 0：未锁定
     * - 1：已锁定
     */
    @Schema(description = "是否锁定：0-未锁定，1-已锁定", example = "0")
    private Integer isLocked;

    /**
     * 锁定时间
     *
     * 说明：
     * - isLocked=1时有值
     */
    @Schema(description = "锁定时间", example = "2024-01-01T12:00:00")
    private LocalDateTime lockedTime;

    /**
     * 锁定原因
     *
     * 说明：
     * - isLocked=1时有值
     */
    @Schema(description = "锁定原因", example = "密码错误次数过多")
    private String lockedReason;

    // ==================== 登录信息 ====================

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间", example = "2024-01-01T12:00:00")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @Schema(description = "最后登录IP", example = "192.168.1.100")
    private String lastLoginIp;

    /**
     * 登录次数
     */
    @Schema(description = "登录次数", example = "100")
    private Integer loginCount;

    // ==================== 角色信息 ====================

    /**
     * 用户角色列表
     *
     * 说明：
     * - 角色代码列表
     * - 示例：["CUSTOMER_MANAGER", "ORDER_MANAGER"]
     */
    @Schema(description = "用户角色列表", example = "[\"CUSTOMER_MANAGER\"]")
    private List<String> roles;

    /**
     * 用户角色名称列表
     *
     * 说明：
     * - 角色名称列表（便于展示）
     * - 示例：["客户经理", "订单管理员"]
     */
    @Schema(description = "用户角色名称列表", example = "[\"客户经理\"]")
    private List<String> roleNames;

    // ==================== 系统信息 ====================

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-01-01T12:00:00")
    private LocalDateTime createdAt;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID", example = "1")
    private Long createdBy;

    /**
     * 创建人用户名
     *
     * 说明：
     * - 冗余字段，便于展示
     */
    @Schema(description = "创建人用户名", example = "admin")
    private String createdByUsername;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2024-01-01T12:00:00")
    private LocalDateTime updatedAt;

    /**
     * 用户备注
     */
    @Schema(description = "用户备注", example = "负责华东区域客户管理")
    private String remark;

    // ==================== 辅助方法 ====================

    /**
     * 判断用户是否已启用
     *
     * @return true-已启用，false-已禁用
     */
    public boolean isEnabled() {
        return Integer.valueOf(1).equals(isEnabled);
    }

    /**
     * 判断用户是否已锁定
     *
     * @return true-已锁定，false-未锁定
     */
    public boolean isLockedStatus() {
        return Integer.valueOf(1).equals(isLocked);
    }
}
