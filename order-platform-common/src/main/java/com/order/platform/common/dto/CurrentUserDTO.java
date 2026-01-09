package com.order.platform.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 当前登录用户信息DTO
 *
 * 用于在请求上下文中传递用户信息
 *
 * 字段说明：
 * - 基础字段（id, username, realName）：用于认证和显示
 * - 联系方式（email, phone, avatar）：用于个人资料
 * - 权限字段（roles, departmentId, departmentName）：用于权限控制
 * - 业务字段（userCode, employeeNo, position）：用于业务和审计
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserDTO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名（登录账号）
     */
    private String username;

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
     * 角色代码列表（如 ["CUSTOMER_MANAGER", "DATA_ADMIN"]）
     */
    private List<String> roles;

    /**
     * 部门ID（-1表示未分配部门）
     */
    private Long departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 用户编号（业务唯一标识，如 USER001）
     *
     * 与 username 的区别：
     * - username：登录账号，用户可修改
     * - userCode：业务编号，系统生成且不可变
     */
    private String userCode;

    /**
     * 工号（企业内部员工编号）
     */
    private String employeeNo;

    /**
     * 职位（如：客户经理、采购专员、运营专员、数据管理员、系统管理员）
     */
    private String position;
}
