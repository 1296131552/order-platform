package com.company.order.visual.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户角色关联实体
 * <p>
 * 用户角色关联中间表,实现用户与角色的N:M关联
 * 关系: UserRole N:1 User (关联用户)
 * 关系: UserRole N:1 Role (关联角色)
 * <p>
 * 权限计算: 用户数据权限取所有角色中的"最宽松"权限（dataScopeType 最小值）
 * 1=全部 > 2=部门 > 3=本人, 即 MIN(dataScopeType)
 *
 * @author Order Platform Team
 */
@Data
@TableName("t_user_role")
public class UserRole {

    /**
     * 关联ID,主键,自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    // ==================== 关联用户信息 ====================

    /**
     * 用户ID,外键关联t_user表
     */
    private Long userId;

    // ==================== 关联角色信息 ====================

    /**
     * 角色ID,外键关联t_role表
     */
    private Long roleId;

    // ==================== 主角色标识 ====================

    /**
     * 是否主角色（仅用于界面展示，权限计算取所有角色最宽松值）
     */
    private Boolean isPrimary;

    // ==================== 公共字段 ====================

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 创建人ID（NULL表示系统创建）
     */
    private Long createdBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 更新人ID（NULL表示系统更新）
     */
    private Long updatedBy;

    /**
     * 是否删除
     */
    private Boolean isDeleted;
}
