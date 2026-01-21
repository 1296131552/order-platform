package com.company.order.visual.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 * <p>
 * 系统用户表,记录用户基本信息、账号信息、组织信息、安全状态等
 * 关系: User N:M Role (通过t_user_role中间表关联)
 * <p>
 * 审计字段自动填充：createdAt, createdBy, updatedAt, updatedBy, isDeleted 由 MetaObjectHandler 自动处理
 *
 * @author Order Platform Team
 */
@Data
@TableName("t_user")
public class User {

    /**
     * 用户ID,主键,自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    // ==================== 账号信息 ====================

    /**
     * 用户名,登录账号,唯一标识
     */
    private String username;

    /**
     * 密码,加密后的密码（BCrypt加密算法）
     */
    private String password;

    /**
     * 用户编号,业务唯一标识,与username区分
     */
    private String userCode;

    // ==================== 基本信息 ====================

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱,可用于登录和找回密码
     */
    private String email;

    /**
     * 手机号,可用于登录和找回密码
     */
    private String phone;

    /**
     * 头像URL
     */
    private String avatar;

    // ==================== 状态控制 ====================

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 是否锁定
     */
    private Boolean isLocked;

    /**
     * 锁定时间
     */
    private LocalDateTime lockedTime;

    /**
     * 锁定原因
     */
    private String lockedReason;

    // ==================== 登录信息 ====================

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 登录次数
     */
    private Integer loginCount;

    // ==================== 密码管理 ====================

    /**
     * 密码修改时间 NULL表示从未修改
     */
    private LocalDateTime passwordChangedTime;

    /**
     * 密码过期时间 NULL表示永不过期
     */
    private LocalDateTime passwordExpireTime;

    // ==================== 组织信息 ====================

    /**
     * 部门ID（NULL表示未分配部门）
     */
    private Long departmentId;

    /**
     * 职位
     */
    private String position;

    /**
     * 工号
     */
    private String employeeNo;

    // ==================== 备注信息 ====================

    /**
     * 用户备注
     */
    private String remark;

    // ==================== 公共字段 ====================

    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 创建人ID（自动填充，NULL表示系统创建）
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 更新时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 更新人ID（自动填充，NULL表示系统更新）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    /**
     * 是否删除（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private Boolean isDeleted;
}
