package com.order.platform.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户审核状态枚举
 *
 * 功能说明：
 * - 定义用户审核流程中的各种状态
 * - 用于注册审核、用户状态管理
 *
 * 状态说明：
 * - NONE：无需审核（管理员创建的用户）
 * - PENDING：待审核（自主注册的用户，等待管理员审核）
 * - APPROVED：已通过（审核通过，用户可正常使用）
 * - REJECTED：已拒绝（审核拒绝，用户无法使用）
 *
 * 状态流转：
 * NONE → 无需流转（管理员创建的直接可用）
 * PENDING → APPROVED（审核通过）
 * PENDING → REJECTED（审核拒绝）
 *
 * 使用场景：
 * - 用户注册时设置初始状态
 * - 管理员审核时更新状态
 * - 查询用户列表时过滤状态
 *
 * @since 1.0.1
 */
@Getter
@AllArgsConstructor
public enum UserAuditStatus {

    /**
     * 无需审核
     *
     * 适用场景：
     * - 管理员直接创建的用户
     * - 系统初始化的默认用户
     */
    NONE("无需审核"),

    /**
     * 待审核
     *
     * 适用场景：
     * - 用户自主注册
     * - 邀请码注册（需要审核）
     */
    PENDING("待审核"),

    /**
     * 已通过
     *
     * 适用场景：
     * - 审核通过
     * - 用户可正常使用系统
     */
    APPROVED("已通过"),

    /**
     * 已拒绝
     *
     * 适用场景：
     * - 审核拒绝
     * - 用户无法使用系统
     * - 可重新注册申请
     */
    REJECTED("已拒绝");

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 判断是否需要审核
     *
     * @return true-需要审核，false-无需审核
     */
    public boolean needsAudit() {
        return this == PENDING;
    }

    /**
     * 判断是否已审核通过
     *
     * @return true-已通过，false-未通过
     */
    public boolean isApproved() {
        return this == APPROVED || this == NONE;
    }

    /**
     * 判断是否被拒绝
     *
     * @return true-已拒绝，false-未拒绝
     */
    public boolean isRejected() {
        return this == REJECTED;
    }
}
