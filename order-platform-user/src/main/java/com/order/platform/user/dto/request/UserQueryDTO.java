package com.order.platform.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户查询请求DTO
 *
 * 功能说明：
 * - 用户列表查询条件
 * - 支持分页
 * - 支持多条件组合查询
 *
 * 查询条件：
 * - username：用户名（模糊查询）
 * - realName：真实姓名（模糊查询）
 * - email：邮箱（模糊查询）
 * - phone：手机号（精确查询）
 * - departmentId：部门ID（精确查询）
 * - isEnabled：是否启用（精确查询）
 * - isLocked：是否锁定（精确查询）
 *
 * 分页参数：
 * - current：当前页码（从1开始）
 * - size：每页大小
 * - sortField：排序字段
 * - sortOrder：排序方向（ASC/DESC）
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户查询请求")
public class UserQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== 分页参数 ====================

    /**
     * 当前页码
     *
     * 说明：
     * - 从1开始
     * - 默认：1
     */
    @Schema(description = "当前页码", example = "1")
    private Long current = 1L;

    /**
     * 每页大小
     *
     * 说明：
     * - 默认：10
     * - 最大：100
     */
    @Schema(description = "每页大小", example = "10")
    private Long size = 10L;

    /**
     * 排序字段
     *
     * 示例：
     * - created_at：创建时间
     * - username：用户名
     * - login_count：登录次数
     */
    @Schema(description = "排序字段", example = "created_at")
    private String sortField;

    /**
     * 排序方向
     *
     * 说明：
     * - ASC：升序
     * - DESC：降序
     * - 默认：DESC
     */
    @Schema(description = "排序方向", example = "DESC")
    private String sortOrder;

    // ==================== 查询条件 ====================

    /**
     * 用户名（模糊查询）
     *
     * 说明：
     * - 支持模糊匹配
     * - 示例：zhang → 匹配 zhangsan, zhangsi 等
     */
    @Schema(description = "用户名（模糊查询）", example = "zhang")
    private String username;

    /**
     * 真实姓名（模糊查询）
     *
     * 说明：
     * - 支持模糊匹配
     * - 示例：张 → 匹配 张三, 张四 等
     */
    @Schema(description = "真实姓名（模糊查询）", example = "张")
    private String realName;

    /**
     * 邮箱（模糊查询）
     *
     * 说明：
     * - 支持模糊匹配
     * - 示例：example → 匹配 xxx@example.com
     */
    @Schema(description = "邮箱（模糊查询）", example = "example")
    private String email;

    /**
     * 手机号（精确查询）
     *
     * 说明：
     * - 精确匹配
     * - 示例：13800138000
     */
    @Schema(description = "手机号（精确查询）", example = "13800138000")
    private String phone;

    /**
     * 部门ID（精确查询）
     *
     * 说明：
     * - 查询指定部门的用户
     * - 示例：10
     */
    @Schema(description = "部门ID", example = "10")
    private Long departmentId;

    /**
     * 是否启用（精确查询）
     *
     * 说明：
     * - 0：禁用
     * - 1：启用
     * - null：全部
     */
    @Schema(description = "是否启用：0-禁用，1-启用", example = "1")
    private Integer isEnabled;

    /**
     * 是否锁定（精确查询）
     *
     * 说明：
     * - 0：未锁定
     * - 1：已锁定
     * - null：全部
     */
    @Schema(description = "是否锁定：0-未锁定，1-已锁定", example = "0")
    private Integer isLocked;
}
