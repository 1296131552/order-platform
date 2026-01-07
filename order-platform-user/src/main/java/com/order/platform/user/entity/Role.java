package com.order.platform.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色实体类
 *
 * 对应数据库表：t_role
 *
 * 功能说明：
 * - 角色基本信息（角色代码、角色名称、角色类型）
 * - 数据权限控制（data_scope_type：1-全部、2-本部门、3-本人、4-自定义）
 * - 角色配置（是否启用、是否系统角色）
 * - 角色管理（排序、描述）
 *
 * 字段说明：
 * - 13字段精简设计（相比参考设计减少41%冗余）
 * - role_code：角色唯一标识，如SYSTEM_ADMIN、CUSTOMER_MANAGER
 * - role_type：角色类型（BUSINESS业务角色、SYSTEM系统角色）
 * - data_scope_type：数据权限类型，核心字段，用于数据权限控制
 * - is_system：是否系统角色，系统角色不可删除
 * - is_enabled：是否启用，默认启用
 *
 * 预定义角色（5个）：
 * - SYSTEM_ADMIN（系统管理员）：全部数据权限
 * - DATA_ADMIN（数据管理员）：全部数据权限
 * - CUSTOMER_MANAGER（客户经理）：本人数据权限
 * - PURCHASE_SPECIALIST（采购专员）：本人数据权限
 * - OPERATION_SPECIALIST（运营专员）：本人数据权限
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_role")
public class Role {

    /**
     * 角色ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色代码（唯一标识）
     *
     * 示例：SYSTEM_ADMIN、CUSTOMER_MANAGER、PURCHASE_SPECIALIST、OPERATION_SPECIALIST、DATA_ADMIN
     */
    private String roleCode;

    /**
     * 角色名称
     *
     * 示例：系统管理员、客户经理、采购专员、运营专员、数据管理员
     */
    private String roleName;

    /**
     * 角色类型
     *
     * BUSINESS（业务角色）：客户经理、采购专员、运营专员、数据管理员
     * SYSTEM（系统角色）：系统管理员
     */
    private String roleType;

    /**
     * 数据权限类型（核心字段）
     *
     * 1 - ALL（全部数据）：系统管理员、数据管理员
     * 2 - DEPARTMENT（本部门数据）：部门经理
     * 3 - SELF（本人数据）：客户经理、采购专员、运营专员
     * 4 - CUSTOM（自定义范围）：预留扩展
     */
    private Integer dataScopeType;

    /**
     * 角色描述
     *
     * 详细说明该角色的职责和权限范围
     */
    private String description;

    /**
     * 排序号
     *
     * 用于角色列表排序，数值越小越靠前
     */
    private Integer sortOrder;

    /**
     * 是否启用
     *
     * 0 - 禁用
     * 1 - 启用（默认）
     */
    private Integer isEnabled;

    /**
     * 是否系统角色
     *
     * 0 - 用户自定义角色（可删除）
     * 1 - 系统内置角色（不可删除）
     */
    private Integer isSystem;

    /**
     * 创建时间（精确到毫秒）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 创建人ID
     *
     * 默认-1表示系统创建
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 更新时间（精确到毫秒）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 更新人ID
     *
     * 默认-1表示系统更新
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    /**
     * 是否删除（逻辑删除）
     *
     * 0 - 未删除
     * 1 - 已删除
     */
    @TableLogic
    private Integer isDeleted;

    // ==================== 常量定义 ====================

    /**
     * 角色类型常量
     */
    public static final class RoleType {
        /** 业务角色 */
        public static final String BUSINESS = "BUSINESS";

        /** 系统角色 */
        public static final String SYSTEM = "SYSTEM";
    }

    /**
     * 数据权限类型常量
     */
    public static final class DataScope {
        /** 全部数据 */
        public static final int ALL = 1;

        /** 本部门数据 */
        public static final int DEPARTMENT = 2;

        /** 本人数据 */
        public static final int SELF = 3;

        /** 自定义范围 */
        public static final int CUSTOM = 4;
    }

    /**
     * 启用状态常量
     */
    public static final class Enabled {
        /** 禁用 */
        public static final int DISABLED = 0;

        /** 启用 */
        public static final int ENABLED = 1;
    }

    /**
     * 系统角色常量
     */
    public static final class SystemRole {
        /** 用户自定义角色 */
        public static final int CUSTOM = 0;

        /** 系统内置角色 */
        public static final int SYSTEM = 1;
    }

    // ==================== 预定义角色代码 ====================

    /**
     * 预定义角色代码常量
     */
    public static final class RoleCode {
        /** 系统管理员 */
        public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";

        /** 数据管理员 */
        public static final String DATA_ADMIN = "DATA_ADMIN";

        /** 客户经理 */
        public static final String CUSTOMER_MANAGER = "CUSTOMER_MANAGER";

        /** 采购专员 */
        public static final String PURCHASE_SPECIALIST = "PURCHASE_SPECIALIST";

        /** 运营专员 */
        public static final String OPERATION_SPECIALIST = "OPERATION_SPECIALIST";
    }

    // ==================== 辅助方法 ====================

    /**
     * 判断是否为系统角色
     *
     * @return true-系统角色，false-用户自定义角色
     */
    public boolean isSystemRole() {
        return SystemRole.SYSTEM == this.isSystem;
    }

    /**
     * 判断是否启用
     *
     * @return true-启用，false-禁用
     */
    public boolean isEnabled() {
        return Enabled.ENABLED == this.isEnabled;
    }

    /**
     * 获取数据权限类型枚举
     *
     * @return 数据权限类型（1-4）
     */
    public int getDataScope() {
        return this.dataScopeType != null ? this.dataScopeType : DataScope.SELF;
    }

    /**
     * 判断是否为全部数据权限
     *
     * @return true-全部数据权限，false-其他权限
     */
    public boolean isAllDataScope() {
        return DataScope.ALL == getDataScope();
    }

    /**
     * 判断是否为本人数据权限
     *
     * @return true-本人数据权限，false-其他权限
     */
    public boolean isSelfDataScope() {
        return DataScope.SELF == getDataScope();
    }

    /**
     * 判断是否为部门数据权限
     *
     * @return true-部门数据权限，false-其他权限
     */
    public boolean isDepartmentDataScope() {
        return DataScope.DEPARTMENT == getDataScope();
    }
}
