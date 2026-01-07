package com.order.platform.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色权限关联实体类
 *
 * 对应数据库表：t_role_permission
 *
 * 功能说明：
 * - 角色权限关联中间表，实现角色与权限的N:M关联
 * - 不使用t_permission表，权限是代码层面的硬编码
 * - 权限代码格式：{模块}:{操作}
 * - 支持一个角色拥有多个权限，一个权限分配给多个角色
 *
 * 字段说明：
 * - 10字段精简设计（相比参考设计减少44%冗余）
 * - role_id：关联的角色ID（外键）
 * - role_code：角色代码（冗余字段，便于查询和展示）
 * - permission_code：权限代码（格式：{模块}:{操作}）
 *
 * 权限代码示例：
 * - ORDER:*（订单所有权限）
 * - ORDER:VIEW（订单查看权限）
 * - SHIPMENT:CREATE（发运创建权限）
 * - ATTACHMENT:UPLOAD（附件上传权限）
 * - DATA:EXPORT（数据导出权限）
 *
 * 设计要点：
 * - 权限是代码层面的硬编码，修改权限需要修改代码
 * - 适度冗余role_code字段，减少JOIN查询
 * - 唯一约束：(role_id + permission_code + is_deleted)
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_role_permission")
public class RolePermission {

    /**
     * 关联ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色ID（外键，关联t_role表）
     */
    private Long roleId;

    /**
     * 角色代码（冗余字段，便于查询和展示）
     *
     * 示例：SYSTEM_ADMIN、CUSTOMER_MANAGER
     */
    private String roleCode;

    /**
     * 权限代码（格式：{模块}:{操作}）
     *
     * 权限模块：
     * - USER（用户管理）
     * - ROLE（角色管理）
     * - ORDER（订单管理）
     * - PARTNER（合作方管理）
     * - SHIPMENT（发运管理）
     * - ATTACHMENT（附件管理）
     * - EXCEPTION（异常管理）
     * - DASHBOARD（看板管理）
     * - DATA（数据管理）
     *
     * 权限操作：
     * - *（所有权限）
     * - VIEW（查看）
     * - CREATE（创建）
     * - UPDATE（更新）
     * - DELETE（删除）
     * - AUDIT（审核）
     * - UPLOAD（上传）
     * - DOWNLOAD（下载）
     * - EXPORT（导出）
     * - IMPORT（导入）
     *
     * 示例：
     * - ORDER:*（订单所有权限）
     * - ORDER:VIEW（订单查看权限）
     * - SHIPMENT:CREATE（发运创建权限）
     * - ATTACHMENT:UPLOAD（附件上传权限）
     * - DATA:EXPORT（数据导出权限）
     */
    private String permissionCode;

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

    // ==================== 常量定义：权限模块 ====================

    /**
     * 权限模块常量
     */
    public static final class Module {
        /** 用户管理 */
        public static final String USER = "USER";

        /** 角色管理 */
        public static final String ROLE = "ROLE";

        /** 订单管理 */
        public static final String ORDER = "ORDER";

        /** 合作方管理 */
        public static final String PARTNER = "PARTNER";

        /** 发运管理 */
        public static final String SHIPMENT = "SHIPMENT";

        /** 附件管理 */
        public static final String ATTACHMENT = "ATTACHMENT";

        /** 异常管理 */
        public static final String EXCEPTION = "EXCEPTION";

        /** 看板管理 */
        public static final String DASHBOARD = "DASHBOARD";

        /** 数据管理 */
        public static final String DATA = "DATA";
    }

    /**
     * 权限操作常量
     */
    public static final class Action {
        /** 所有权限 */
        public static final String ALL = "*";

        /** 查看 */
        public static final String VIEW = "VIEW";

        /** 创建 */
        public static final String CREATE = "CREATE";

        /** 更新 */
        public static final String UPDATE = "UPDATE";

        /** 删除 */
        public static final String DELETE = "DELETE";

        /** 审核 */
        public static final String AUDIT = "AUDIT";

        /** 上传 */
        public static final String UPLOAD = "UPLOAD";

        /** 下载 */
        public static final String DOWNLOAD = "DOWNLOAD";

        /** 导出 */
        public static final String EXPORT = "EXPORT";

        /** 导入 */
        public static final String IMPORT = "IMPORT";
    }

    // ==================== 辅助方法 ====================

    /**
     * 解析权限代码
     *
     * @return 权限代码数组，[0]=模块，[1]=操作
     */
    public String[] parsePermissionCode() {
        if (permissionCode == null || !permissionCode.contains(":")) {
            return new String[]{"", ""};
        }
        return permissionCode.split(":", 2);
    }

    /**
     * 获取权限模块
     *
     * @return 权限模块，如"ORDER"、"SHIPMENT"
     */
    public String getModule() {
        String[] parts = parsePermissionCode();
        return parts[0];
    }

    /**
     * 获取权限操作
     *
     * @return 权限操作，如"VIEW"、"CREATE"
     */
    public String getAction() {
        String[] parts = parsePermissionCode();
        return parts.length > 1 ? parts[1] : "";
    }

    /**
     * 判断是否为所有权限
     *
     * @return true-所有权限，false-特定权限
     */
    public boolean isAllPermission() {
        return Action.ALL.equals(getAction());
    }

    /**
     * 判断是否为指定模块的权限
     *
     * @param module 权限模块
     * @return true-属于该模块，false-不属于该模块
     */
    public boolean isModule(String module) {
        return module != null && module.equals(getModule());
    }

    /**
     * 构建权限代码
     *
     * @param module 权限模块
     * @param action 权限操作
     * @return 权限代码，如"ORDER:VIEW"
     */
    public static String buildPermissionCode(String module, String action) {
        if (module == null || module.isEmpty()) {
            throw new IllegalArgumentException("权限模块不能为空");
        }
        if (action == null || action.isEmpty()) {
            throw new IllegalArgumentException("权限操作不能为空");
        }
        return module + ":" + action;
    }

    /**
     * 构建所有权限代码
     *
     * @param module 权限模块
     * @return 所有权限代码，如"ORDER:*"
     */
    public static String buildAllPermissionCode(String module) {
        return buildPermissionCode(module, Action.ALL);
    }
}
