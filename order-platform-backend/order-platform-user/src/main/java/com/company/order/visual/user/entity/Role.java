package com.company.order.visual.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色实体
 * <p>
 * 系统角色表,定义角色信息,用于权限管理和数据权限控制
 * 关系: Role N:M User (通过t_user_role中间表关联)
 *
 * @author Order Platform Team
 */
@Data
@TableName("t_role")
public class Role {

    /**
     * 角色ID,主键,自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    // ==================== 角色基本信息 ====================

    /**
     * 角色代码,唯一标识角色
     * 示例: CUSTOMER_MANAGER、PURCHASE_SPECIALIST、OPERATION_SPECIALIST、DATA_ADMIN、SYSTEM_ADMIN
     */
    private String roleCode;

    /**
     * 角色名称,用于展示
     * 示例: 客户经理、采购专员、运营专员、数据管理员、系统管理员
     */
    private String roleName;

    /**
     * 角色类型,用于角色分组
     * BUSINESS(业务角色): 客户经理、采购专员、运营专员、数据管理员
     * SYSTEM(系统角色): 系统管理员
     */
    private String roleType;

    /**
     * 数据权限类型,标识角色可以访问的数据范围
     * 1-ALL(全部数据): 系统管理员、数据管理员
     * 2-DEPARTMENT(本部门数据): 部门经理
     * 3-SELF(本人数据): 客户经理、采购专员、运营专员
     * 4-CUSTOM(自定义范围): 预留扩展
     */
    private Integer dataScopeType;

    /**
     * 角色描述,详细说明该角色的职责和权限范围
     */
    private String description;

    /**
     * 角色排序,用于角色列表排序。数值越小越靠前
     */
    private Integer sortOrder;

    // ==================== 角色配置 ====================

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 是否系统角色（系统角色不可删除）
     */
    private Boolean isSystem;

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
