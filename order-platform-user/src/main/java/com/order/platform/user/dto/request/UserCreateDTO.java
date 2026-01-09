package com.order.platform.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 创建用户请求DTO
 *
 * 功能说明：
 * - 用户创建时的数据传输对象
 * - 包含用户基本信息、组织信息、初始密码
 * - 密码自动加密存储
 *
 * 字段说明：
 * - username：用户名（登录账号，唯一）
 * - password：初始密码（BCrypt加密存储）
 * - realName：真实姓名
 * - email：邮箱（唯一）
 * - phone：手机号（唯一）
 * - departmentId：所属部门ID
 * - position：职位
 * - employeeNo：工号
 * - remark：备注
 *
 * 验证规则：
 * - 用户名：2-20字符，字母开头，只含字母数字下划线
 * - 密码：6-20字符
 * - 邮箱：标准邮箱格式
 * - 手机号：11位数字，1开头
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建用户请求")
public class UserCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名（登录账号）
     *
     * 验证规则：
     * - 非空
     * - 2-20字符
     * - 字母开头
     * - 只包含字母、数字、下划线
     * - 全局唯一
     */
    @Schema(description = "用户名", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度为2-20字符")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{1,19}$", message = "用户名必须以字母开头，只能包含字母、数字、下划线")
    private String username;

    /**
     * 初始密码
     *
     * 验证规则：
     * - 非空
     * - 6-20字符
     * - 后端使用BCrypt加密存储
     *
     * 安全说明：
     * - 默认密码：123456
     * - 首次登录后建议修改密码
     */
    @Schema(description = "初始密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20字符")
    private String password;

    /**
     * 真实姓名
     *
     * 验证规则：
     * - 非空
     * - 2-20字符
     */
    @Schema(description = "真实姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 20, message = "真实姓名长度为2-20字符")
    private String realName;

    /**
     * 邮箱
     *
     * 验证规则：
     * - 标准邮箱格式
     * - 全局唯一
     * - 可选
     */
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     *
     * 验证规则：
     * - 11位数字
     * - 1开头
     * - 全局唯一
     * - 可选
     */
    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 部门ID
     *
     * 说明：
     * - -1表示未分配部门
     * - 用于数据权限隔离
     */
    @Schema(description = "部门ID", example = "10")
    private Long departmentId;

    /**
     * 部门名称
     *
     * 说明：
     * - 冗余字段，便于展示
     * - 创建时自动填充
     */
    @Schema(description = "部门名称", example = "华东大区")
    private String departmentName;

    /**
     * 职位
     *
     * 示例：
     * - 客户经理
     * - 采购专员
     * - 运营专员
     */
    @Schema(description = "职位", example = "客户经理")
    private String position;

    /**
     * 工号
     *
     * 说明：
     * - 企业内部员工编号
     * - 可选
     */
    @Schema(description = "工号", example = "EMP001")
    private String employeeNo;

    /**
     * 用户备注
     *
     * 说明：
     * - 可选
     * - 最多500字符
     */
    @Schema(description = "用户备注", example = "负责华东区域客户管理")
    private String remark;
}
