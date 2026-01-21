package com.company.order.visual.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.SetOperations;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * TokenBlacklistService 单元测试
 * <p>
 * 测试覆盖：
 * - 黑名单操作：add、isBlacklisted
 * - 版本号管理：get、set、increment、refresh（防旧Token复活）
 * - 活跃Token追踪：add、remove、clear
 * - Redis故障处理：fail-open策略
 * - TTL设置：防止内存泄漏 [P1-1, P1-2修复]
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TokenBlacklistService 单元测试")
class TokenBlacklistServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    private TokenBlacklistService blacklistService;

    private static final Long TEST_USER_ID = 12345L;
    private static final String TEST_TOKEN_ID = "test-token-abc123";

    @BeforeEach
    void setUp() {
        blacklistService = new TokenBlacklistService(redisTemplate);

        // Mock RedisTemplate 的 opsForXxx 方法
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    // ==================== 黑名单操作测试 ====================

    @Nested
    @DisplayName("黑名单操作")
    class BlacklistTests {

        @Test
        @DisplayName("加入黑名单 - 成功")
        void testAddToBlacklist_Success() {
            long remainingMillis = 3600000L; // 1小时
            TokenInfo tokenInfo = createValidTokenInfo(remainingMillis);

            when(valueOperations.setIfAbsent(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(true);

            blacklistService.addToBlacklist(tokenInfo);

            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Long> valueCaptor = ArgumentCaptor.forClass(Long.class);
            ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);
            ArgumentCaptor<TimeUnit> unitCaptor = ArgumentCaptor.forClass(TimeUnit.class);

            verify(valueOperations).setIfAbsent(keyCaptor.capture(), valueCaptor.capture(),
                    ttlCaptor.capture(), unitCaptor.capture());

            assertThat(keyCaptor.getValue()).isEqualTo(RedisKeyConstants.BLACKLIST_PREFIX + TEST_TOKEN_ID);
            assertThat(valueCaptor.getValue()).isGreaterThan(System.currentTimeMillis());
            // getRemainingMillis受时间流逝影响，允许1秒误差
            assertThat(ttlCaptor.getValue())
                    .isGreaterThanOrEqualTo(remainingMillis - 1000)
                    .isLessThanOrEqualTo(remainingMillis);
            assertThat(unitCaptor.getValue()).isEqualTo(TimeUnit.MILLISECONDS);
        }

        @Test
        @DisplayName("加入黑名单 - 幂等性（已存在则跳过）")
        void testAddToBlacklist_Idempotent() {
            TokenInfo tokenInfo = createValidTokenInfo(3600000L);

            when(valueOperations.setIfAbsent(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(false);  // key已存在

            blacklistService.addToBlacklist(tokenInfo);

            verify(valueOperations, times(1)).setIfAbsent(anyString(), anyLong(), anyLong(), any(TimeUnit.class));
        }

        @Test
        @DisplayName("检查黑名单 - 在黑名单中")
        void testIsBlacklisted_True() {
            when(redisTemplate.hasKey(anyString())).thenReturn(true);

            boolean result = blacklistService.isBlacklisted(TEST_TOKEN_ID);

            assertThat(result).isTrue();
            verify(redisTemplate).hasKey(RedisKeyConstants.BLACKLIST_PREFIX + TEST_TOKEN_ID);
        }

        @Test
        @DisplayName("检查黑名单 - 不在黑名单中")
        void testIsBlacklisted_False() {
            when(redisTemplate.hasKey(anyString())).thenReturn(false);

            boolean result = blacklistService.isBlacklisted(TEST_TOKEN_ID);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("检查黑名单 - Redis故障（fail-open）")
        void testIsBlacklisted_RedisFailure() {
            when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("Redis down"));

            boolean result = blacklistService.isBlacklisted(TEST_TOKEN_ID);

            // fail-open策略：Redis故障时放行
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("加入黑名单 - Token已过期则跳过")
        void testAddToBlacklist_AlreadyExpired() {
            TokenInfo tokenInfo = createValidTokenInfo(0L);  // 已过期

            blacklistService.addToBlacklist(tokenInfo);

            // 不应该调用Redis
            verify(redisTemplate, never()).opsForValue();
        }

        @Test
        @DisplayName("加入黑名单 - Token无效则跳过")
        void testAddToBlacklist_InvalidToken() {
            TokenInfo tokenInfo = TokenInfo.invalid();

            blacklistService.addToBlacklist(tokenInfo);

            // 不应该调用Redis
            verify(redisTemplate, never()).opsForValue();
        }
    }

    // ==================== 版本号管理测试 [P1-1修复] ====================

    @Nested
    @DisplayName("版本号管理")
    class VersionTests {

        @Test
        @DisplayName("获取版本号 - 存在则返回")
        void testGetUserTokenVersion_Exists() {
            Long version = 5L;
            when(valueOperations.get(anyString())).thenReturn(version.toString());

            Long result = blacklistService.getUserTokenVersion(TEST_USER_ID);

            assertThat(result).isEqualTo(version);
            verify(valueOperations).get(RedisKeyConstants.VERSION_PREFIX + TEST_USER_ID);
        }

        @Test
        @DisplayName("获取版本号 - 不存在则返回null")
        void testGetUserTokenVersion_NotExists() {
            when(valueOperations.get(anyString())).thenReturn(null);

            Long result = blacklistService.getUserTokenVersion(TEST_USER_ID);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("获取版本号 - Redis故障返回null")
        void testGetUserTokenVersion_RedisFailure() {
            when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis down"));

            Long result = blacklistService.getUserTokenVersion(TEST_USER_ID);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("设置版本号 - 带有30天TTL [P1-1修复]")
        void testSetUserTokenVersion_WithTTL() {
            Long version = 1L;

            blacklistService.setUserTokenVersion(TEST_USER_ID, version);

            ArgumentCaptor<TimeUnit> unitCaptor = ArgumentCaptor.forClass(TimeUnit.class);
            ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);

            verify(valueOperations).set(anyString(), eq(version), ttlCaptor.capture(), unitCaptor.capture());

            // 验证TTL为30天
            assertThat(ttlCaptor.getValue()).isEqualTo(30L);
            assertThat(unitCaptor.getValue()).isEqualTo(TimeUnit.DAYS);
        }

        @Test
        @DisplayName("设置版本号 - userId为null不执行")
        void testSetUserTokenVersion_NullUserId() {
            blacklistService.setUserTokenVersion(null, 1L);

            verify(redisTemplate, never()).opsForValue();
        }

        @Test
        @DisplayName("刷新版本号TTL - 防止活跃用户版本号键过期 [P1-1安全修复]")
        void testRefreshUserTokenVersion_Success() {
            when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

            blacklistService.refreshUserTokenVersion(TEST_USER_ID);

            verify(redisTemplate).expire(
                    eq(RedisKeyConstants.VERSION_PREFIX + TEST_USER_ID),
                    eq(30L),
                    eq(TimeUnit.DAYS)
            );
        }

        @Test
        @DisplayName("刷新版本号TTL - userId为null不执行")
        void testRefreshUserTokenVersion_NullUserId() {
            blacklistService.refreshUserTokenVersion(null);

            verify(redisTemplate, never()).expire(anyString(), anyLong(), any(TimeUnit.class));
        }

        @Test
        @DisplayName("递增版本号 - 成功并重置TTL")
        void testIncrementTokenVersion_Success() {
            Long newVersion = 3L;
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.increment(anyString())).thenReturn(newVersion);
            when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

            Long result = blacklistService.incrementTokenVersion(TEST_USER_ID);

            assertThat(result).isEqualTo(newVersion);
            verify(valueOperations).increment(RedisKeyConstants.VERSION_PREFIX + TEST_USER_ID);
            verify(redisTemplate).expire(eq(RedisKeyConstants.VERSION_PREFIX + TEST_USER_ID),
                    eq(30L), eq(TimeUnit.DAYS));
        }

        @Test
        @DisplayName("递增版本号 - Redis故障返回null")
        void testIncrementTokenVersion_RedisFailure() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.increment(anyString())).thenThrow(new RuntimeException("Redis down"));

            Long result = blacklistService.incrementTokenVersion(TEST_USER_ID);

            assertThat(result).isNull();
        }
    }

    // ==================== 活跃Token管理测试 [P1-2修复] ====================

    @Nested
    @DisplayName("活跃Token管理")
    class ActiveTokensTests {

        @Test
        @DisplayName("添加活跃Token - 带有7天TTL [P1-2修复]")
        void testAddActiveToken_WithTTL() {
            blacklistService.addActiveToken(TEST_USER_ID, TEST_TOKEN_ID);

            verify(setOperations).add(anyString(), eq(TEST_TOKEN_ID));
            verify(redisTemplate).expire(
                    eq(RedisKeyConstants.ACTIVE_TOKENS_PREFIX + TEST_USER_ID),
                    eq(7L),
                    eq(TimeUnit.DAYS)
            );
        }

        @Test
        @DisplayName("添加活跃Token - 参数为null不执行")
        void testAddActiveToken_NullParameters() {
            blacklistService.addActiveToken(null, TEST_TOKEN_ID);
            blacklistService.addActiveToken(TEST_USER_ID, null);

            verify(redisTemplate, never()).opsForSet();
        }

        @Test
        @DisplayName("移除活跃Token - 成功")
        void testRemoveActiveToken_Success() {
            when(redisTemplate.opsForSet()).thenReturn(setOperations);

            blacklistService.removeActiveToken(TEST_USER_ID, TEST_TOKEN_ID);

            verify(setOperations).remove(
                    RedisKeyConstants.ACTIVE_TOKENS_PREFIX + TEST_USER_ID,
                    TEST_TOKEN_ID
            );
        }

        @Test
        @DisplayName("清除所有活跃Token - 成功")
        void testClearActiveTokens_Success() {
            blacklistService.clearActiveTokens(TEST_USER_ID);

            verify(redisTemplate).delete(RedisKeyConstants.ACTIVE_TOKENS_PREFIX + TEST_USER_ID);
        }

        @Test
        @DisplayName("清除所有活跃Token - userId为null不执行")
        void testClearActiveTokens_NullUserId() {
            blacklistService.clearActiveTokens(null);

            verify(redisTemplate, never()).delete(anyString());
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建有效TokenInfo用于测试
     */
    private TokenInfo createValidTokenInfo(long remainingMillis) {
        Date expiration = Date.from(Instant.now().plusMillis(remainingMillis));
        return TokenInfo.generated("raw-token", TEST_USER_ID, TEST_TOKEN_ID, 1L, expiration);
    }
}
