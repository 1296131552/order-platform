package com.order.platform.user.service;

import java.util.List;

/**
 * 权限服务接口
 *
 * 功能说明：
 * - 根据角色列表查询用户权限
 * - 支持多角色权限合并
 * - 权限去重和缓存
 *
 * 权限代码格式：
 * - 格式：{模块}:{操作}
 * - 示例：ORDER:VIEW、SHIPMENT:CREATE、DATA:EXPORT
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
 * @since 1.0.0
 */
public interface PermissionService {

    /**
     * 根据角色ID列表查询权限代码列表
     *
     * 业务规则：
     * 1. 查询每个角色的权限（通过 t_role_permission 表）
     * 2. 合并所有角色的权限（多角色权限叠加）
     * 3. 去重（相同的权限只保留一个）
     * 4. 如果角色列表为空，返回空列表
     *
     * 权限继承说明：
     * - 用户拥有多个角色时，权限是所有角色权限的并集
     * - 例如：用户有角色A（ORDER:VIEW）和角色B（ORDER:CREATE）
     *        则该用户拥有 ORDER:VIEW 和 ORDER:CREATE 两个权限
     *
     * 使用场景：
     * - 用户登录时查询完整权限列表
     * - 权限验证时获取用户权限
     * - 角色权限变更后刷新缓存
     *
     * @param roleIds 角色ID列表
     * @return 权限代码列表（去重后），如 ["ORDER:VIEW", "ORDER:CREATE", "SHIPMENT:VIEW"]
     */
    List<String> getPermissionsByRoleIds(List<Long> roleIds);

    /**
     * 根据角色代码列表查询权限代码列表
     *
     * 业务规则：
     * 1. 根据角色代码查询角色ID
     * 2. 查询每个角色的权限
     * 3. 合并去重后返回
     *
     * 使用场景：
     * - 用户登录时，已知角色代码列表，查询权限
     * - Token中包含角色代码，用于权限查询
     *
     * @param roleCodes 角色代码列表，如 ["CUSTOMER_MANAGER", "DATA_ADMIN"]
     * @return 权限代码列表（去重后）
     */
    List<String> getPermissionsByRoleCodes(List<String> roleCodes);

    /**
     * 检查指定角色是否拥有指定权限
     *
     * 业务规则：
     * 1. 查询角色的权限列表
     * 2. 检查是否包含指定权限
     * 3. 支持 * 通配符（ORDER:* 包含 ORDER:VIEW）
     *
     * 使用场景：
     * - 权限快速验证
     * - 接口权限检查
     *
     * @param roleId 角色ID
     * @param permissionCode 权限代码，如 "ORDER:CREATE"
     * @return true-拥有权限，false-无权限
     */
    boolean hasRolePermission(Long roleId, String permissionCode);

    /**
     * 检查用户是否拥有指定权限
     *
     * 业务规则：
     * 1. 查询用户的所有角色
     * 2. 查询这些角色的所有权限
     * 3. 检查是否包含指定权限
     * 4. 支持 * 通配符
     *
     * 使用场景：
     * - 用户权限验证
     * - 接口权限拦截
     *
     * @param userId 用户ID
     * @param permissionCode 权限代码
     * @return true-拥有权限，false-无权限
     */
    boolean hasPermission(Long userId, String permissionCode);

    /**
     * 获取所有可用的权限代码列表
     *
     * 业务规则：
     * 1. 查询 t_role_permission 表中所有不同的权限代码
     * 2. 去重后返回
     *
     * 使用场景：
     * - 权限列表展示
     * - 权限枚举生成
     * - 权限管理
     *
     * @return 所有权限代码列表（去重），如 ["ORDER:VIEW", "ORDER:CREATE", ...]
     */
    List<String> getAllPermissions();

    /**
     * 获取指定模块的所有权限代码
     *
     * 业务规则：
     * 1. 查询 t_role_permission 表
     * 2. 过滤出指定模块的权限
     * 3. 去重后返回
     *
     * 使用场景：
     * - 模块权限管理
     * - 模块权限展示
     *
     * @param module 权限模块，如 "ORDER"、"SHIPMENT"
     * @return 该模块的权限代码列表，如 ["ORDER:VIEW", "ORDER:CREATE", "ORDER:UPDATE"]
     */
    List<String> getPermissionsByModule(String module);

    /**
     * 清除权限缓存
     *
     * 使用场景：
     * - 角色权限变更后
     * - 权限重新分配后
     *
     * @param roleId 角色ID
     */
    void clearCache(Long roleId);

    /**
     * 批量清除权限缓存
     *
     * 使用场景：
     * - 系统权限配置变更后
     * - 权限批量调整后
     *
     * @param roleIds 角色ID列表
     */
    void clearCacheBatch(List<Long> roleIds);
}
