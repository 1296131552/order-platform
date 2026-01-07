package com.order.platform.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户角色关联实体
 *
 * 对应数据库表：t_user_role
 *
 * 功能说明：
 * - 用户与角色的多对多关联关系
 * - 支持一个用户拥有多个角色
 * - 支持角色分配和撤销
 * - 支持主角色标识，用于数据权限判断
 *
 * 设计要点：
 * - is_primary 标识主角色，数据权限以主角色的 data_scope_type 为准
 * - username 和 role_code 为冗余字段，减少 JOIN 查询
 * - 用户可能有多个角色，但只有一个主角色
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user_role")
public class UserRole {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名（冗余字段，便于查询和展示）
     *
     * 注意：这里冗余 username 是为了减少 JOIN 查询，
     * 用户名修改时需要同步更新此字段
     */
    private String username;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色代码（冗余字段，用于快速查询）
     *
     * 注意：这里冗余 role_code 是为了减少 JOIN 查询，
     * UserRoleService.getRoleCodesByUserId() 直接查询此字段
     */
    private String roleCode;

    /**
     * 是否主角色：0-否，1-是
     *
     * 用户可能有多个角色，主角色用于数据权限判断
     * 数据权限以主角色的 data_scope_type 为准
     */
    private Integer isPrimary;

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
    private Integer isDeleted;
}
