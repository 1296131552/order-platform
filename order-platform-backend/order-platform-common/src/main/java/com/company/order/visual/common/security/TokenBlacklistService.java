package com.company.order.visual.common.security;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Token 黑名单服务
 * <p>
 * 职责:
 * - 管理 Token 黑名单（单设备退出登录）
 * - 管理 Token 版本号（密码重置后批量失效）
 * - 追踪用户活跃 Tokens
 * <p>
 * Redis 故障策略：采用 fail-open 策略，Redis 故障时放行请求
 * 权衡：优先保证系统可用性，代价是 Redis 故障期间已退出的 Token 可能仍然有效
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final long VERSION_TTL_DAYS = 30;
    private static final long ACTIVE_TOKENS_TTL_DAYS = 7;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 将 Token 加入黑名单
     *
     * @param tokenInfo token详细信息
     */
    public void addToBlacklist(TokenInfo tokenInfo) {
        if (tokenInfo == null || !tokenInfo.isValid()) {
            return;
        }
        String key = RedisKeyConstants.BLACKLIST_PREFIX + tokenInfo.getTokenId();
        long ttlMs = tokenInfo.getRemainingMillis();
        if (ttlMs <= 0) {
            log.debug("Token 已过期,无需加入黑名单,tokenId={}", tokenInfo.getTokenId());
            return;
        }

        try {
            Long expiryTime = System.currentTimeMillis() + ttlMs;
            // 只在 key 不存在时设置（幂等）
            Boolean added = redisTemplate.opsForValue().setIfAbsent(key, expiryTime, ttlMs, TimeUnit.MILLISECONDS);

            if (Boolean.TRUE.equals(added)) {
                log.info("Token 已加入黑名单,tokenId={}, 原过期时间={}", tokenInfo.getTokenId(), expiryTime);
            } else {
                log.debug("Token 已在黑名单中,跳过,tokenId={}", tokenInfo.getTokenId());
            }
        } catch (Exception e) {
            log.error("Redis 操作失败,无法加入黑名单,tokenId={}", tokenInfo.getTokenId(), e);
        }
    }

    /**
     * 检查 Token 是否在黑名单中
     * <p>
     * Redis 故障策略：返回 false（放行）
     * 权衡：优先保证可用性，Redis 故障期间已退出 Token 可能仍然有效
     *
     * @param tokenId Token ID
     * @return true=在黑名单中，false=不在或 Redis 故障
     */
    public boolean isBlacklisted(String tokenId) {
        String key = RedisKeyConstants.BLACKLIST_PREFIX + tokenId;
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis 查询失败,tokenId={}", tokenId, e);
            return false;  // Redis 故障时放行
        }
    }

    /**
     * 获取用户的 Token 版本号
     *
     * @param userId 用户 ID
     * @return 版本号,不存在返回 null
     */
    public Long getUserTokenVersion(Long userId){
        if (userId == null) {
            return null;
        }
        String key = RedisKeyConstants.VERSION_PREFIX + userId;
        try {
            Object version = redisTemplate.opsForValue().get(key);
            return version != null ? Long.parseLong(version.toString()) : null;
        } catch (Exception e) {
            log.error("获取用户 Token 版本失败,userId={}", userId, e);
            return null;
        }
    }

    /**
     * 设置用户 Token 版本号
     * <p>
     * 带有30天过期时间，登录时会重置过期时间，保证活跃用户版本号键永不过期
     *
     * @param userId 用户 ID
     * @param version 版本号
     */
    public void setUserTokenVersion(Long userId, Long version) {
        if (userId == null || version == null) {
            log.warn("设置 Token 版本参数无效:userId={}, version={}", userId, version);
            return;
        }
        String key = RedisKeyConstants.VERSION_PREFIX + userId;
        try {
            redisTemplate.opsForValue().set(key, version, VERSION_TTL_DAYS, TimeUnit.DAYS);
            log.debug("用户 {} Token 版本号设置为:{}, TTL={}天", userId, version, VERSION_TTL_DAYS);
        } catch (Exception e) {
            log.error("设置用户 Token 版本失败,userId={}", userId, e);
        }
    }

    /**
     * 刷新用户 Token 版本号的过期时间
     * <p>
     * 用于登录时重置过期时间，保证活跃用户的版本号键永不过期
     * 避免版本号键过期后导致旧Token"复活"的安全漏洞
     *
     * @param userId 用户 ID
     */
    public void refreshUserTokenVersion(Long userId) {
        if (userId == null) {
            return;
        }
        String key = RedisKeyConstants.VERSION_PREFIX + userId;
        try {
            Boolean result = redisTemplate.expire(key, VERSION_TTL_DAYS, TimeUnit.DAYS);
            if (Boolean.TRUE.equals(result)) {
                log.debug("刷新用户 {} Token 版本号过期时间成功", userId);
            }
        } catch (Exception e) {
            log.error("刷新用户 Token 版本号过期时间失败,userId={}", userId, e);
        }
    }

    /**
     * 递增用户 Token 版本号（密码重置时调用）
     * <p>
     * 递增后重置过期时间，保证新版本号键不会立即过期
     *
     * @param userId 用户 ID
     * @return 新版本号
     */
    public Long incrementTokenVersion(Long userId) {
        if (userId == null) {
            return null;
        }
        String key = RedisKeyConstants.VERSION_PREFIX + userId;
        try {
            Long newVersion = redisTemplate.opsForValue().increment(key);
            // 重置过期时间
            redisTemplate.expire(key, VERSION_TTL_DAYS, TimeUnit.DAYS);
            log.info("用户 {} Token 版本号递增至:{}, TTL={}天", userId, newVersion, VERSION_TTL_DAYS);
            return newVersion;
        } catch (Exception e) {
            log.error("递增用户 Token 版本失败,userId={}", userId, e);
            return null;
        }
    }

    /**
     * 添加用户活跃 Token
     * <p>
     * 带有7天过期时间，与Token有效期一致
     *
     * @param userId 用户 ID
     * @param tokenId Token ID
     */
    public void addActiveToken(Long userId, String tokenId) {
        if (userId == null || tokenId == null) {
            return;
        }
        String key = RedisKeyConstants.ACTIVE_TOKENS_PREFIX + userId;
        try {
            redisTemplate.opsForSet().add(key, tokenId);
            // 重置过期时间（保证活跃用户Token集合不会过期）
            redisTemplate.expire(key, ACTIVE_TOKENS_TTL_DAYS, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("添加活跃 Token 失败,userId={}, tokenId={}", userId, tokenId, e);
        }
    }

    /**
     * 移除用户活跃 Token
     *
     * @param userId 用户 ID
     * @param tokenId Token ID
     */
    public void removeActiveToken(Long userId, String tokenId) {
        if (userId == null || tokenId == null) {
            return;
        }
        String key = RedisKeyConstants.ACTIVE_TOKENS_PREFIX + userId;
        try {
            redisTemplate.opsForSet().remove(key, tokenId);
        } catch (Exception e) {
            log.error("移除活跃 Token 失败,userId={}, tokenId={}", userId, tokenId, e);
        }
    }

    /**
     * 清除用户所有活跃 Token 记录
     *
     * @param userId 用户 ID
     */
    public void clearActiveTokens(Long userId) {
        if (userId == null) {
            return;
        }
        String key = RedisKeyConstants.ACTIVE_TOKENS_PREFIX + userId;
        try {
            redisTemplate.delete(key);
            log.info("清除用户 {} 所有活跃 Token", userId);
        } catch (Exception e) {
            log.error("清除活跃 Token 失败，userId={}", userId, e);
        }
    }
}
