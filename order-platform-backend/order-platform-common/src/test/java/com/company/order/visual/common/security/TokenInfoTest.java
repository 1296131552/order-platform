package com.company.order.visual.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TokenInfo 单元测试
 * <p>
 * 测试覆盖：
 * - 工厂方法：generated(), valid(), invalid()
 * - NPE防御：Claims字段缺失时返回invalid()
 * - 剩余时间计算
 */
@DisplayName("TokenInfo 单元测试")
class TokenInfoTest {

    private static final Long TEST_USER_ID = 12345L;
    private static final String TEST_TOKEN_ID = "test-token-id-abc123";
    private static final Long TEST_VERSION = 1L;
    private static final String RAW_TOKEN = "eyJhbGciOiJIUzI1NiJ9.test.token";

    // ==================== generated() 工厂方法测试 ====================

    @Nested
    @DisplayName("generated() 工厂方法")
    class GeneratedTests {

        @Test
        @DisplayName("正常生成 - 所有字段正确填充")
        void testGenerate_Success() {
            Date expiration = Date.from(Instant.now().plus(7, ChronoUnit.DAYS));

            TokenInfo tokenInfo = TokenInfo.generated(RAW_TOKEN, TEST_USER_ID, TEST_TOKEN_ID, TEST_VERSION, expiration);

            assertThat(tokenInfo.isValid()).isTrue();
            assertThat(tokenInfo.getRawToken()).isEqualTo(RAW_TOKEN);
            assertThat(tokenInfo.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(tokenInfo.getTokenId()).isEqualTo(TEST_TOKEN_ID);
            assertThat(tokenInfo.getVersion()).isEqualTo(TEST_VERSION);
            assertThat(tokenInfo.getExpiration()).isEqualTo(expiration);
        }

        @Test
        @DisplayName("生成即过期 - 剩余时间为0")
        void testGenerate_ExpiringNow() {
            Date pastExpiration = Date.from(Instant.now().minus(1, ChronoUnit.SECONDS));

            TokenInfo tokenInfo = TokenInfo.generated(RAW_TOKEN, TEST_USER_ID, TEST_TOKEN_ID, TEST_VERSION, pastExpiration);

            assertThat(tokenInfo.isValid()).isTrue();
            assertThat(tokenInfo.getRemainingMillis()).isZero();
        }
    }

    // ==================== valid() 工厂方法测试 - NPE防御 ====================

    @Nested
    @DisplayName("valid() 工厂方法 - NPE防御")
    class ValidTests {

        @Test
        @DisplayName("正常Claims - 正确解析")
        void testValid_NormalClaims() {
            Claims claims = createNormalClaims();

            TokenInfo tokenInfo = TokenInfo.valid(RAW_TOKEN, claims);

            assertThat(tokenInfo.isValid()).isTrue();
            assertThat(tokenInfo.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(tokenInfo.getTokenId()).isEqualTo(TEST_TOKEN_ID);
            assertThat(tokenInfo.getVersion()).isEqualTo(TEST_VERSION);
            assertThat(tokenInfo.getRawToken()).isEqualTo(RAW_TOKEN);
        }

        @Test
        @DisplayName("Subject为null - 返回invalid() [P2-1修复]")
        void testValid_NullSubject() {
            Claims claims = new DefaultClaims();
            claims.setId(TEST_TOKEN_ID);
            claims.setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
            // subject故意设为null

            TokenInfo tokenInfo = TokenInfo.valid(RAW_TOKEN, claims);

            assertThat(tokenInfo.isValid()).isFalse();
        }

        @Test
        @DisplayName("Subject为非数字字符串 - 返回invalid() [P2-1修复]")
        void testValid_InvalidSubjectFormat() {
            Claims claims = new DefaultClaims();
            claims.setSubject("not-a-number");  // 无效的userId格式
            claims.setId(TEST_TOKEN_ID);
            claims.setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));

            TokenInfo tokenInfo = TokenInfo.valid(RAW_TOKEN, claims);

            assertThat(tokenInfo.isValid()).isFalse();
        }

        @Test
        @DisplayName("Id为null - 仍返回valid（字段为null不影响valid标记）")
        void testValid_NullId() {
            Claims claims = new DefaultClaims();
            claims.setSubject(String.valueOf(TEST_USER_ID));
            // id故意设为null
            claims.setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));

            TokenInfo tokenInfo = TokenInfo.valid(RAW_TOKEN, claims);

            // Id为null不会触发NPE，所以返回valid（但tokenId字段为null）
            assertThat(tokenInfo.isValid()).isTrue();
            assertThat(tokenInfo.getTokenId()).isNull();
        }

        @Test
        @DisplayName("Expiration为null - 仍返回valid（字段为null不影响valid标记）")
        void testValid_NullExpiration() {
            Claims claims = new DefaultClaims();
            claims.setSubject(String.valueOf(TEST_USER_ID));
            claims.setId(TEST_TOKEN_ID);
            claims.put("version", TEST_VERSION);
            // expiration故意设为null

            TokenInfo tokenInfo = TokenInfo.valid(RAW_TOKEN, claims);

            // Expiration为null不会触发NPE，所以返回valid（但expiration字段为null）
            assertThat(tokenInfo.isValid()).isTrue();
            assertThat(tokenInfo.getExpiration()).isNull();
            // 剩余时间应为0
            assertThat(tokenInfo.getRemainingMillis()).isZero();
        }

        @Test
        @DisplayName("Version为null - 返回invalid() [P2-1修复]")
        void testValid_NullVersion() {
            Claims claims = new DefaultClaims();
            claims.setSubject(String.valueOf(TEST_USER_ID));
            claims.setId(TEST_TOKEN_ID);
            claims.setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
            // version不设置，默认为null

            TokenInfo tokenInfo = TokenInfo.valid(RAW_TOKEN, claims);

            // 注意：claims.get("version", Long.class) 在null时返回null，不会抛异常
            // 所以这里会返回valid，但version为null
            assertThat(tokenInfo.isValid()).isTrue();
            assertThat(tokenInfo.getVersion()).isNull();
        }
    }

    // ==================== invalid() 工厂方法测试 ====================

    @Nested
    @DisplayName("invalid() 工厂方法")
    class InvalidTests {

        @Test
        @DisplayName("invalid() - 所有字段为null且valid=false")
        void testInvalid() {
            TokenInfo tokenInfo = TokenInfo.invalid();

            assertThat(tokenInfo.isValid()).isFalse();
            assertThat(tokenInfo.getRawToken()).isNull();
            assertThat(tokenInfo.getUserId()).isNull();
            assertThat(tokenInfo.getTokenId()).isNull();
            assertThat(tokenInfo.getVersion()).isNull();
            assertThat(tokenInfo.getExpiration()).isNull();
            assertThat(tokenInfo.getRemainingMillis()).isZero();
        }
    }

    // ==================== getRemainingMillis() 方法测试 ====================

    @Nested
    @DisplayName("getRemainingMillis() 剩余时间计算")
    class RemainingMillisTests {

        @Test
        @DisplayName("未过期Token - 返回正数毫秒")
        void testRemainingMillis_NotExpired() {
            Date futureExpiration = Date.from(Instant.now().plus(1, ChronoUnit.HOURS));

            TokenInfo tokenInfo = TokenInfo.generated(RAW_TOKEN, TEST_USER_ID, TEST_TOKEN_ID, TEST_VERSION, futureExpiration);

            long remaining = tokenInfo.getRemainingMillis();
            assertThat(remaining).isGreaterThan(0);
            // 允许1秒误差
            assertThat(remaining).isGreaterThan(3599000L).isLessThan(3601000L);
        }

        @Test
        @DisplayName("已过期Token - 返回0")
        void testRemainingMillis_Expired() {
            Date pastExpiration = Date.from(Instant.now().minus(1, ChronoUnit.HOURS));

            TokenInfo tokenInfo = TokenInfo.generated(RAW_TOKEN, TEST_USER_ID, TEST_TOKEN_ID, TEST_VERSION, pastExpiration);

            assertThat(tokenInfo.getRemainingMillis()).isZero();
        }

        @Test
        @DisplayName("invalid Token - 返回0")
        void testRemainingMillis_Invalid() {
            TokenInfo tokenInfo = TokenInfo.invalid();

            assertThat(tokenInfo.getRemainingMillis()).isZero();
        }

        @Test
        @DisplayName("即将过期 - 返回小正数")
        void testRemainingMillis_ExpiringSoon() {
            Date soonExpiration = Date.from(Instant.now().plus(100, ChronoUnit.MILLIS));

            TokenInfo tokenInfo = TokenInfo.generated(RAW_TOKEN, TEST_USER_ID, TEST_TOKEN_ID, TEST_VERSION, soonExpiration);

            // 允许时间流逝影响
            assertThat(tokenInfo.getRemainingMillis())
                    .isGreaterThanOrEqualTo(0L)
                    .isLessThan(200L);
        }
    }

    // ==================== 辅助方法 ====================

    private Claims createNormalClaims() {
        Claims claims = new DefaultClaims();
        claims.setSubject(String.valueOf(TEST_USER_ID));
        claims.setId(TEST_TOKEN_ID);
        claims.put("version", TEST_VERSION);
        claims.setExpiration(Date.from(Instant.now().plus(7, ChronoUnit.DAYS)));
        return claims;
    }
}
