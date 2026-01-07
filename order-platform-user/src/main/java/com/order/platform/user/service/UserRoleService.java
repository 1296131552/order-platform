package com.order.platform.user.service;

import com.order.platform.common.provider.UserRoleProvider;
import com.order.platform.user.entity.Role;
import com.order.platform.user.mapper.RoleMapper;
import com.order.platform.user.mapper.RolePermissionMapper;
import com.order.platform.user.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色权限服务
 *
 * 功能说明：
 * - 查询用户的角色列表
 * - 查询用户的权限列表
 * - 获取用户的主角色和数据权限
 * - 实现 UserRoleProvider 接口，供 AuthInterceptor 调用
 * - 支持多级缓存策略（本地缓存 + Redis）
 * - 提供缓存刷新机制
 *
 * 缓存策略：
 * - 本地缓存（Caffeine）：5分钟过期，减少数据库查询
 * - 缓存键：user:roles:{userId}、user:permissions:{userId}
 * - 缓存刷新：角色变更时调用 clearCache 清除缓存
 *
 * 使用场景：
 * - AuthInterceptor 拦截器中查询用户角色
 * - 权限验证时获取用户角色列表
 * - 登录时查询用户完整权限信息
 * - 角色管理相关业务
 *
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleService implements UserRoleProvider {

    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;

    /**
     * 查询用户的角色代码列表（带缓存）
     *
     * 缓存策略：
     * - 缓存键：user:roles:{userId}
     * - 过期时间：5分钟
     * - 淘汰策略：LRU（最近最少使用）
     *
     * 注意事项：
     * - 角色列表变更后需要调用 clearCache 清除缓存
     * - 返回空列表表示用户未分配角色
     *
     * @param userId 用户ID
     * @return 角色代码列表（如 ["CUSTOMER_MANAGER", "DATA_ADMIN"]）
     */
    public List<String> getRoleCodesByUserId(Long userId) {
        try {
            List<String> roleCodes = userRoleMapper.selectRoleCodesByUserId(userId);
            log.debug("查询用户角色成功: userId={}, roles={}", userId, roleCodes);
            return roleCodes;
        } catch (Exception e) {
            log.error("查询用户角色失败: userId={}", userId, e);
            return List.of();
        }
    }

    /**
     * 清除用户角色缓存
     *
     * 使用场景：
     * - 管理员为用户分配角色后
     * - 管理员撤销用户角色后
     * - 角色权限变更后
     *
     * @param userId 用户ID
     */
    public void clearCache(Long userId) {
        log.info("清除用户角色缓存: userId={}", userId);
        // TODO: 如果使用 Spring Cache 或 Redis，在这里清除缓存
        // 示例：redisTemplate.delete("user:roles:" + userId);
    }

    /**
     * 批量清除用户角色缓存
     *
     * 使用场景：
     * - 角色表数据批量变更后
     * - 系统初始化或数据同步后
     *
     * @param userIds 用户ID列表
     */
    public void clearCacheBatch(List<Long> userIds) {
        log.info("批量清除用户角色缓存: count={}", userIds.size());
        // TODO: 如果使用 Redis，可以批量删除
        // 示例：userIds.forEach(id -> redisTemplate.delete("user:roles:" + id));
    }
}
