package com.order.platform.user.vo;

import com.order.platform.common.dto.CurrentUserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录响应VO
 *
 * 功能说明：
 * - 返回完整的用户信息和权限信息
 * - 返回JWT Token及有效期
 * - 返回数据权限范围
 * - 支持首次登录强制改密功能
 *
 * 字段说明：
 * - token：JWT访问令牌
 * - tokenType：令牌类型（Bearer）
 * - expiresIn：过期时间（秒）
 * - requireChangePassword：是否需要修改密码（首次登录或密码过期）
 * - passwordExpireTime：密码过期时间
 * - userInfo：当前用户信息
 * - roles：用户角色列表
 * - permissions：用户权限列表
 * - dataScope：数据权限范围
 *
 * 数据结构：
 * - userInfo：继承自CurrentUser（11个核心字段）
 * - roles：角色代码列表（如["CUSTOMER_MANAGER"]）
 * - permissions：权限代码列表（如["ORDER:VIEW", "ORDER:CREATE"]）
 * - dataScope：数据权限上下文（类型、部门ID、部门名称）
 *
 * 安全说明：
 * - Token存储在客户端（LocalStorage/Cookie）
 * - 每次请求在Header中携带：Authorization: Bearer {token}
 * - Token过期后需要重新登录或刷新Token
 * - 敏感信息（如密码）不返回给前端
 * - 首次登录用户需要修改密码（requireChangePassword=true）
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * JWT访问令牌
     *
     * 说明：
     * - 用于后续API请求的身份验证
     * - 存储在客户端LocalStorage/Cookie
     * - 每次请求在Header中携带：Authorization: Bearer {token}
     *
     * 格式：
     * - eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * - 包含：userId、roles、签发时间、过期时间
     *
     * 有效期：
     * - 默认7天（604800秒）
     * - 过期后需要重新登录或刷新Token
     */
    @Schema(description = "JWT访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    /**
     * 令牌类型
     *
     * 说明：
     * - 固定值：Bearer
     * - HTTP Header格式：Authorization: Bearer {token}
     */
    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    /**
     * 过期时间（秒）
     *
     * 说明：
     * - 默认7天 = 604800秒
     * - 前端可以根据此值实现Token自动刷新
     */
    @Schema(description = "过期时间（秒）", example = "604800")
    private Long expiresIn;

    /**
     * 是否需要修改密码
     *
     * 说明：
     * - true：需要修改密码（首次登录或密码已过期）
     * - false：无需修改密码
     *
     * 使用场景：
     * - 首次登录强制改密（is_first_login=1）
     * - 密码过期提醒（password_expire_time < 当前时间）
     *
     * 前端处理：
     * - 当requireChangePassword=true时，应弹出改密对话框
     * - 强制用户修改密码后才能继续操作
     *
     * @since 1.0.1
     */
    @Schema(description = "是否需要修改密码", example = "false")
    private Boolean requireChangePassword;

    /**
     * 密码过期时间
     *
     * 说明：
     * - 密码的过期时间点
     * - null：密码永不过期
     * - 非null：在此时间后需要修改密码
     *
     * 使用场景：
     * - 前端可以提前提醒用户密码即将过期
     * - 结合requireChangePassword实现密码过期策略
     *
     * @since 1.0.1
     */
    @Schema(description = "密码过期时间", example = "2026-07-09T12:00:00")
    private LocalDateTime passwordExpireTime;

    /**
     * 当前用户信息
     *
     * 说明：
     * - 继承自CurrentUser（11个核心字段）
     * - 不包含敏感信息（如密码）
     *
     * 字段列表：
     * - id：用户ID
     * - username：用户名
     * - realName：真实姓名
     * - email：邮箱
     * - phone：手机号
     * - avatar：头像URL
     * - roles：角色代码列表（冗余，便于前端判断）
     * - departmentId：部门ID
     * - departmentName：部门名称
     * - userCode：用户编号
     * - employeeNo：工号
     * - position：职位
     */
    @Schema(description = "当前用户信息")
    private CurrentUserDTO userInfo;

    /**
     * 用户角色列表
     *
     * 说明：
     * - 角色代码列表（如["CUSTOMER_MANAGER"]）
     * - 可能包含多个角色
     * - 第一个角色为主角色（用于数据权限判断）
     *
     * 预定义角色：
     * - SYSTEM_ADMIN：系统管理员
     * - DATA_ADMIN：数据管理员
     * - CUSTOMER_MANAGER：客户经理
     * - PURCHASE_SPECIALIST：采购专员
     * - OPERATION_SPECIALIST：运营专员
     */
    @Schema(description = "用户角色列表", example = "[\"CUSTOMER_MANAGER\"]")
    private List<String> roles;

    /**
     * 用户权限列表
     *
     * 说明：
     * - 权限代码列表（如["ORDER:VIEW", "ORDER:CREATE"]）
     * - 格式：{模块}:{操作}
     * - 根据用户角色动态计算
     *
     * 权限模块：
     * - USER：用户管理
     * - ROLE：角色管理
     * - ORDER：订单管理
     * - PARTNER：合作方管理
     * - SHIPMENT：发运管理
     * - ATTACHMENT：附件管理
     * - EXCEPTION：异常管理
     * - DASHBOARD：看板管理
     * - DATA：数据管理
     *
     * 权限操作：
     * - *：所有权限
     * - VIEW：查看
     * - CREATE：创建
     * - UPDATE：更新
     * - DELETE：删除
     * - AUDIT：审核
     * - UPLOAD：上传
     * - DOWNLOAD：下载
     * - EXPORT：导出
     * - IMPORT：导入
     */
    @Schema(description = "用户权限列表", example = "[\"ORDER:VIEW\", \"ORDER:CREATE\"]")
    private List<String> permissions;

    /**
     * 数据权限范围
     *
     * 说明：
     * - 基于主角色的data_scope_type
     * - 用于前端UI控制（如：只展示"我的数据"按钮）
     * - 后端通过拦截器强制过滤数据
     *
     * 数据权限类型：
     * - ALL（1）：全部数据 - 系统管理员、数据管理员
     * - DEPARTMENT（2）：本部门数据 - 部门经理
     * - SELF（3）：本人数据 - 客户经理、采购专员、运营专员
     * - CUSTOM（4）：自定义范围 - 预留扩展
     */
    @Schema(description = "数据权限范围")
    private DataScopeInfo dataScope;

    /**
     * 数据权限信息
     *
     * 说明：
     * - 封装数据权限的详细信息
     * - 便于前端判断和展示
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "数据权限信息")
    public static class DataScopeInfo implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 数据权限类型
         *
         * 类型：
         * - ALL（1）：全部数据
         * - DEPARTMENT（2）：本部门数据
         * - SELF（3）：本人数据
         * - CUSTOM（4）：自定义范围
         */
        @Schema(description = "数据权限类型", example = "SELF")
        private String type;

        /**
         * 数据权限类型代码
         *
         * 用于：
         * - 前端判断
         * - 后端查询
         */
        @Schema(description = "数据权限类型代码", example = "3")
        private Integer typeCode;

        /**
         * 部门ID
         *
         * 说明：
         * - 当type=DEPARTMENT时使用
         * - 用于前端展示和后端查询
         */
        @Schema(description = "部门ID", example = "10")
        private Long departmentId;

        /**
         * 部门名称
         *
         * 说明：
         * - 用于前端展示
         * - 冗余字段，便于显示
         */
        @Schema(description = "部门名称", example = "华东大区")
        private String departmentName;

        /**
         * 数据权限描述
         *
         * 用于：
         * - 前端展示（如："只能查看自己的数据"）
         * - 用户提示
         */
        @Schema(description = "数据权限描述", example = "只能查看自己的数据")
        private String description;

        /**
         * 判断是否为全部数据权限
         *
         * @return true-全部数据权限，false-其他权限
         */
        public boolean isAllDataScope() {
            return "ALL".equals(type);
        }

        /**
         * 判断是否为本人数据权限
         *
         * @return true-本人数据权限，false-其他权限
         */
        public boolean isSelfDataScope() {
            return "SELF".equals(type);
        }

        /**
         * 判断是否为部门数据权限
         *
         * @return true-部门数据权限，false-其他权限
         */
        public boolean isDepartmentDataScope() {
            return "DEPARTMENT".equals(type);
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 判断用户是否拥有指定权限
     *
     * @param permissionCode 权限代码（如"ORDER:CREATE"）
     * @return true-拥有权限，false-无权限
     */
    public boolean hasPermission(String permissionCode) {
        if (permissions == null || permissionCode == null) {
            return false;
        }

        // 检查是否拥有所有权限
        if (permissions.contains("*") || permissions.contains("ALL:*")) {
            return true;
        }

        // 检查是否拥有指定权限
        return permissions.contains(permissionCode);
    }

    /**
     * 判断用户是否拥有指定模块的权限
     *
     * @param module 权限模块（如"ORDER"）
     * @return true-拥有该模块的权限，false-无权限
     */
    public boolean hasModulePermission(String module) {
        if (permissions == null || module == null) {
            return false;
        }

        String prefix = module + ":";
        return permissions.stream()
            .anyMatch(perm -> perm.equals(prefix + "*") || perm.startsWith(prefix));
    }

    /**
     * 判断用户是否拥有指定角色
     *
     * @param roleCode 角色代码（如"SYSTEM_ADMIN"）
     * @return true-拥有角色，false-无角色
     */
    public boolean hasRole(String roleCode) {
        if (roles == null || roleCode == null) {
            return false;
        }
        return roles.contains(roleCode);
    }

    /**
     * 判断是否为系统管理员
     *
     * @return true-系统管理员，false-普通用户
     */
    public boolean isSystemAdmin() {
        return hasRole("SYSTEM_ADMIN");
    }

    /**
     * 判断是否为数据管理员
     *
     * @return true-数据管理员，false-普通用户
     */
    public boolean isDataAdmin() {
        return hasRole("DATA_ADMIN");
    }
}
