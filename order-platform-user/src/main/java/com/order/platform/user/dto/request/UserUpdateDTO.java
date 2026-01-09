package com.order.platform.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 更新用户请求DTO
 *
 * 功能说明：
 * - 用户更新时的数据传输对象
 * - 只包含可修改的字段（username不可修改）
 * - 密码单独接口修改
 *
 * 字段说明：
 * - realName：真实姓名
 * - email：邮箱
 * - phone：手机号
 * - departmentId：所属部门ID
 * - position：职位
 * - employeeNo：工号
 * - remark：备注
 * - isEnabled：是否启用
 *
 * 验证规则：
 * - 邮箱：标准邮箱格式（修改后需检查唯一性）
 * - 手机号：11位数字，1开头（修改后需检查唯一性）
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新用户请求")
public class UserUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 真实姓名
     *
     * 验证规则：
     * - 2-20字符
     */
    @Schema(description = "真实姓名", example = "张三")
    @Size(min = 2, max = 20, message = "真实姓名长度为2-20字符")
    private String realName;

    /**
     * 邮箱
     *
     * 验证规则：
     * - 标准邮箱格式
     * - 修改后需检查唯一性
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
     * - 修改后需检查唯一性
     */
    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 部门ID
     *
     * 说明：
     * - -1表示未分配部门
     */
    @Schema(description = "部门ID", example = "10")
    private Long departmentId;

    /**
     * 部门名称
     *
     * 说明：
     * - 冗余字段，便于展示
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

    /**
     * 用户备注
     *
     * 说明：
     * - 最多500字符
     */
    @Schema(description = "用户备注", example = "负责华东区域客户管理")
    private String remark;

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
     * 头像URL
     *
     * 说明：
     * - 用户头像图片地址
     */
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
}
