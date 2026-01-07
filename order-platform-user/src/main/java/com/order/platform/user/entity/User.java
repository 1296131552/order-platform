package com.order.platform.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * 对应数据库表：t_user
 *
 * 功能说明：
 * - 用户基本信息（用户名、密码、真实姓名等）
 * - 用户状态（启用、锁定、删除等）
 * - 登录信息（最后登录时间、登录次数等）
 * - 密码管理（密码修改时间、密码过期时间等）
 * - 组织信息（部门、职位、工号等）
 *
 * 字段说明：
 * - 25字段完整设计，满足实际项目长期维护需求
 * - 账号安全：支持账户锁定、密码过期
 * - 登录统计：记录登录次数和最后登录信息
 * - 组织信息：支持基于部门的数据权限隔离
 * - 业务标识：userCode用户编号，与username区分
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user")
public class User {

    /**
     * 用户ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（登录账号）
     */
    private String username;

    /**
     * 密码（BCrypt加密）
     */
    private String password;

    /**
     * 用户编号（业务唯一标识，如USER001）
     */
    private String userCode;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 是否启用：0-禁用，1-启用
     */
    private Integer isEnabled;

    /**
     * 是否锁定：0-未锁定，1-已锁定
     */
    private Integer isLocked;

    /**
     * 锁定时间
     */
    private LocalDateTime lockedTime;

    /**
     * 锁定原因
     */
    private String lockedReason;

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

    /**
     * 密码修改时间
     */
    private LocalDateTime passwordChangedTime;

    /**
     * 密码过期时间
     */
    private LocalDateTime passwordExpireTime;

    /**
     * 部门ID（-1表示未分配部门）
     */
    private Long departmentId;

    /**
     * 部门名称（冗余字段）
     */
    private String departmentName;

    /**
     * 职位
     */
    private String position;

    /**
     * 工号
     */
    private String employeeNo;

    /**
     * 用户备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 更新人ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer isDeleted;
}
