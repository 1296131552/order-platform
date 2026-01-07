package com.order.platform.common.provider;

import java.util.List;

/**
 * 用户角色提供者接口
 *
 * 功能说明：
 * - 定义用户角色查询的标准接口
 * - 避免反射调用，提高类型安全性
 * - 解耦 common 模块与 user 模块的依赖关系
 *
 * 使用方式：
 * - AuthInterceptor 通过此接口获取用户角色
 * - UserRoleService 实现此接口提供具体实现
 * - Spring 自动注入，无需手动配置
 *
 * @since 1.0.0
 */
public interface UserRoleProvider {

    /**
     * 根据用户ID查询角色代码列表
     *
     * 查询条件：
     * - 用户ID匹配
     * - 角色已启用（is_enabled = 1）
     * - 未删除（is_deleted = 0）
     *
     * @param userId 用户ID
     * @return 角色代码列表（如 ["CUSTOMER_MANAGER", "DATA_ADMIN"]）
     *         不存在时返回空列表，不返回 null
     */
    List<String> getRoleCodesByUserId(Long userId);
}
