package com.order.platform.user.service.impl;

import com.order.platform.user.entity.Role;
import com.order.platform.user.mapper.RoleMapper;
import com.order.platform.user.mapper.RolePermissionMapper;
import com.order.platform.user.mapper.UserRoleMapper;
import com.order.platform.user.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 *
 * 功能说明：
 * - 根据角色列表查询用户权限
 * - 支持多角色权限合并
 * - 权限去重和缓存
 *
 * 业务规则：
 * - 多角色权限叠加：用户拥有多个角色时，权限是所有角色权限的并集
 * - 权限去重：相同的权限只保留一个
 * - 通配符支持：ORDER:* 包含 ORDER:VIEW、ORDER:CREATE 等
 *
 * 性能优化：
 * - 使用批量查询避免 N+1 问题
 * - 缓存角色权限（TODO）
 *
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;

    /**
     * 根据角色ID列表查询权限代码列表
     *
     * 实现逻辑：
     * 1. 参数校验（角色ID列表为空时返回空列表）
     * 2. 批量查询权限代码（使用 RolePermissionMapper.selectPermissionCodesByRoleIds）
     * 3. 去重后返回
     *
     * 性能优化：
     * - 使用批量查询，避免 N+1 问题
     * - TODO: 添加 Redis 缓存（5分钟过期）
     *
     * @param roleIds 角色ID列表
     * @return 权限代码列表（去重后）
     */
    @Override
    public List<String> getPermissionsByRoleIds(List<Long> roleIds) {
        // 1. 参数校验：角色ID列表为空时返回空列表
        if (roleIds == null || roleIds.isEmpty()) {
            log.debug("角色ID列表为空，返回空权限列表");
            return Collections.emptyList();
        }

        try {
            // 2. 批量查询权限代码（避免 N+1 查询问题）
            List<String> permissions = rolePermissionMapper.selectPermissionCodesByRoleIds(roleIds);

            // 3. 去重后返回
            List<String> distinctPermissions = permissions.stream()
                    .distinct()
                    .collect(Collectors.toList());

            log.debug("查询角色权限成功: roleIds={}, permissionsCount={}", roleIds, distinctPermissions.size());
            return distinctPermissions;

        } catch (Exception e) {
            log.error("查询角色权限失败: roleIds={}", roleIds, e);
            return Collections.emptyList();
        }
    }

    /**
     * 根据角色代码列表查询权限代码列表
     *
     * 实现逻辑：
     * 1. 根据角色代码查询角色信息
     * 2. 提取角色ID列表
     * 3. 调用 getPermissionsByRoleIds 查询权限
     *
     * @param roleCodes 角色代码列表
     * @return 权限代码列表（去重后）
     */
    @Override
    public List<String> getPermissionsByRoleCodes(List<String> roleCodes) {
        // 1. 参数校验
        if (roleCodes == null || roleCodes.isEmpty()) {
            log.debug("角色代码列表为空，返回空权限列表");
            return Collections.emptyList();
        }

        try {
            // 2. 根据角色代码查询角色ID
            List<Long> roleIds = new ArrayList<>();
            for (String roleCode : roleCodes) {
                Role role = roleMapper.selectByRoleCode(roleCode);
                if (role != null) {
                    roleIds.add(role.getId());
                } else {
                    log.warn("角色代码对应的角色不存在: roleCode={}", roleCode);
                }
            }

            // 3. 如果没有找到有效角色，返回空列表
            if (roleIds.isEmpty()) {
                log.debug("未找到有效角色，返回空权限列表: roleCodes={}", roleCodes);
                return Collections.emptyList();
            }

            // 4. 查询权限
            return getPermissionsByRoleIds(roleIds);

        } catch (Exception e) {
            log.error("根据角色代码查询权限失败: roleCodes={}", roleCodes, e);
            return Collections.emptyList();
        }
    }

    /**
     * 检查指定角色是否拥有指定权限
     *
     * 实现逻辑：
     * 1. 查询角色的所有权限
     * 2. 检查是否包含指定权限
     * 3. 支持 * 通配符（ORDER:* 包含 ORDER:VIEW）
     *
     * @param roleId 角色ID
     * @param permissionCode 权限代码
     * @return true-拥有权限，false-无权限
     */
    @Override
    public boolean hasRolePermission(Long roleId, String permissionCode) {
        // 1. 参数校验
        if (roleId == null || permissionCode == null || permissionCode.isEmpty()) {
            return false;
        }

        try {
            // 2. 查询角色的所有权限
            List<String> permissions = rolePermissionMapper.selectPermissionCodesByRoleId(roleId);

            // 3. 检查是否包含指定权限
            return checkPermission(permissions, permissionCode);

        } catch (Exception e) {
            log.error("检查角色权限失败: roleId={}, permissionCode={}", roleId, permissionCode, e);
            return false;
        }
    }

    /**
     * 检查用户是否拥有指定权限
     *
     * 实现逻辑：
     * 1. 查询用户的所有角色ID
     * 2. 查询这些角色的所有权限
     * 3. 检查是否包含指定权限
     *
     * @param userId 用户ID
     * @param permissionCode 权限代码
     * @return true-拥有权限，false-无权限
     */
    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        // 1. 参数校验
        if (userId == null || permissionCode == null || permissionCode.isEmpty()) {
            return false;
        }

        try {
            // 2. 查询用户的所有角色ID
            List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
            if (roleIds.isEmpty()) {
                log.debug("用户未分配角色: userId={}", userId);
                return false;
            }

            // 3. 查询这些角色的所有权限
            List<String> permissions = getPermissionsByRoleIds(roleIds);

            // 4. 检查是否包含指定权限
            return checkPermission(permissions, permissionCode);

        } catch (Exception e) {
            log.error("检查用户权限失败: userId={}, permissionCode={}", userId, permissionCode, e);
            return false;
        }
    }

    /**
     * 获取所有可用的权限代码列表
     *
     * @return 所有权限代码列表（去重）
     */
    @Override
    public List<String> getAllPermissions() {
        try {
            List<String> permissions = rolePermissionMapper.selectAllDistinctPermissionCodes();
            log.debug("查询所有权限成功: count={}", permissions.size());
            return permissions;
        } catch (Exception e) {
            log.error("查询所有权限失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取指定模块的所有权限代码
     *
     * @param module 权限模块
     * @return 该模块的权限代码列表
     */
    @Override
    public List<String> getPermissionsByModule(String module) {
        if (module == null || module.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<String> permissions = rolePermissionMapper.selectPermissionCodesByModule(module);
            log.debug("查询模块权限成功: module={}, count={}", module, permissions.size());
            return permissions;
        } catch (Exception e) {
            log.error("查询模块权限失败: module={}", module, e);
            return Collections.emptyList();
        }
    }

    /**
     * 清除权限缓存
     *
     * TODO: 实现 Redis 缓存后，在这里清除缓存
     *
     * @param roleId 角色ID
     */
    @Override
    public void clearCache(Long roleId) {
        log.info("清除角色权限缓存: roleId={}", roleId);
        // TODO: 如果使用 Redis，在这里清除缓存
        // 示例：redisTemplate.delete("role:permissions:" + roleId);
    }

    /**
     * 批量清除权限缓存
     *
     * TODO: 实现 Redis 缓存后，在这里清除缓存
     *
     * @param roleIds 角色ID列表
     */
    @Override
    public void clearCacheBatch(List<Long> roleIds) {
        log.info("批量清除角色权限缓存: count={}", roleIds.size());
        // TODO: 如果使用 Redis，可以批量删除
        // 示例：roleIds.forEach(id -> redisTemplate.delete("role:permissions:" + id));
    }

    /**
     * 检查权限列表是否包含指定权限
     *
     * 支持通配符：
     * - * 包含所有权限
     * - MODULE:* 包含该模块的所有权限（如 ORDER:* 包含 ORDER:VIEW）
     *
     * @param permissions 权限列表
     * @param permissionCode 要检查的权限代码
     * @return true-包含，false-不包含
     */
    private boolean checkPermission(List<String> permissions, String permissionCode) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        // 1. 检查是否有超级管理员权限（*）
        if (permissions.contains("*") || permissions.contains("ALL:*")) {
            return true;
        }

        // 2. 检查是否完全匹配
        if (permissions.contains(permissionCode)) {
            return true;
        }

        // 3. 检查通配符权限（如 ORDER:* 包含 ORDER:VIEW）
        if (permissionCode.contains(":")) {
            String module = permissionCode.substring(0, permissionCode.indexOf(":"));
            String wildcardPermission = module + ":*";
            if (permissions.contains(wildcardPermission)) {
                return true;
            }
        }

        return false;
    }
}
