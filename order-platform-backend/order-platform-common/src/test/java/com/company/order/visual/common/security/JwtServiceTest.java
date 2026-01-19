package com.company.order.visual.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JwtService 单元测试
 * <p>
 * 测试覆盖：
 * - Token生成：verify签名、载荷正确性
 * - Token解析：正常解析、过期Token、无效Token
 * - 签名验证：篡改Token被拒绝
 * - 往返一致性：生成的Token能被正确解析
 */
@DisplayName("JwtService 单元测试")
class JwtServiceTest {

    private static final String TEST_SECRET = "test-secret-key-for-jwt-testing-must-be-long-enough-for-hs256";
    private static final Long EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L; // 7天

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(TEST_SECRET);
        properties.setExpiration(EXPIRATION_MS);
        jwtService = new JwtService(properties);
    }

    // ==================== generateToken() 测试 ====================

    @Nested
    @DisplayName("generateToken() - Token生成")
    class GenerateTokenTests {

        @Test
        @DisplayName("正常生成 - 返回有效TokenInfo")
        void testGenerate_Success() {
            Long userId = 12345L;
            Long version = 1L;

            TokenInfo tokenInfo = jwtService.generateToken(userId, version);

            assertThat(tokenInfo.isValid()).isTrue();
            assertThat(tokenInfo.getRawToken()).isNotEmpty();
            assertThat(tokenInfo.getUserId()).isEqualTo(userId);
            assertThat(tokenInfo.getVersion()).isEqualTo(version);
            assertThat(tokenInfo.getTokenId()).isNotEmpty();
            assertThat(tokenInfo.getExpiration()).isNotNull();
            assertThat(tokenInfo.getRemainingMillis())
                    .isGreaterThan(EXPIRATION_MS - 1000)  // 允许1秒误差
                    .isLessThan(EXPIRATION_MS + 1000);
        }

        @Test
        @DisplayName("不同用户 - TokenId不同")
        void testGenerate_DifferentUsers() {
            TokenInfo token1 = jwtService.generateToken(1L, 1L);
            TokenInfo token2 = jwtService.generateToken(2L, 1L);

            assertThat(token1.getTokenId()).isNotEqualTo(token2.getTokenId());
            assertThat(token1.getRawToken()).isNotEqualTo(token2.getRawToken());
        }

        @Test
        @DisplayName("同一用户多次生成 - TokenId不同")
        void testGenerate_SameUserMultipleTimes() {
            Long userId = 1L;
            TokenInfo token1 = jwtService.generateToken(userId, 1L);
            TokenInfo token2 = jwtService.generateToken(userId, 1L);

            assertThat(token1.getTokenId()).isNotEqualTo(token2.getTokenId());
            assertThat(token1.getRawToken()).isNotEqualTo(token2.getRawToken());
        }

        @Test
        @DisplayName("不同版本号 - Token不同")
        void testGenerate_DifferentVersions() {
            Long userId = 1L;
            TokenInfo token1 = jwtService.generateToken(userId, 1L);
            TokenInfo token2 = jwtService.generateToken(userId, 2L);

            assertThat(token1.getTokenId()).isNotEqualTo(token2.getTokenId());
            assertThat(token1.getVersion()).isEqualTo(1L);
            assertThat(token2.getVersion()).isEqualTo(2L);
        }
    }

    // ==================== parseToken() 测试 ====================

    @Nested
    @DisplayName("parseToken() - Token解析")
    class ParseTokenTests {

        @Test
        @DisplayName("解析有效Token - 返回正确信息")
        void testParse_ValidToken() {
            Long userId = 12345L;
            Long version = 5L;
            TokenInfo generated = jwtService.generateToken(userId, version);

            TokenInfo parsed = jwtService.parseToken(generated.getRawToken());

            assertThat(parsed.isValid()).isTrue();
            assertThat(parsed.getUserId()).isEqualTo(userId);
            assertThat(parsed.getVersion()).isEqualTo(version);
            assertThat(parsed.getTokenId()).isEqualTo(generated.getTokenId());
            // JWT expiration精度为秒级，转换后毫秒部分被置零，使用秒级时间戳比较
            long generatedSeconds = generated.getExpiration().getTime() / 1000;
            long parsedSeconds = parsed.getExpiration().getTime() / 1000;
            assertThat(parsedSeconds).isEqualTo(generatedSeconds);
        }

        @Test
        @DisplayName("解析空字符串 - 返回invalid")
        void testParse_EmptyString() {
            TokenInfo parsed = jwtService.parseToken("");

            assertThat(parsed.isValid()).isFalse();
        }

        @Test
        @DisplayName("解析null - 返回invalid")
        void testParse_Null() {
            TokenInfo parsed = jwtService.parseToken(null);

            assertThat(parsed.isValid()).isFalse();
        }

        @Test
        @DisplayName("解析篡改Token - 返回invalid")
        void testParse_TamperedToken() {
            TokenInfo generated = jwtService.generateToken(123L, 1L);
            String tampered = generated.getRawToken() + "tampered";

            TokenInfo parsed = jwtService.parseToken(tampered);

            assertThat(parsed.isValid()).isFalse();
        }

        @Test
        @DisplayName("解析错误格式Token - 返回invalid")
        void testParse_MalformedToken() {
            TokenInfo parsed = jwtService.parseToken("not.a.valid.jwt");

            assertThat(parsed.isValid()).isFalse();
        }

        @Test
        @DisplayName("解析使用其他密钥签名的Token - 返回invalid")
        void testParse_WrongSecret() {
            // 用另一个JwtService生成Token
            JwtProperties otherProperties = new JwtProperties();
            otherProperties.setSecret("different-secret-key-for-testing-wrong-secret");
            otherProperties.setExpiration(EXPIRATION_MS);
            JwtService otherService = new JwtService(otherProperties);

            TokenInfo tokenFromOther = otherService.generateToken(123L, 1L);

            // 用原JwtService解析
            TokenInfo parsed = jwtService.parseToken(tokenFromOther.getRawToken());

            assertThat(parsed.isValid()).isFalse();
        }
    }

    // ==================== 过期Token测试 ====================

    @Nested
    @DisplayName("过期Token处理")
    class ExpiredTokenTests {

        @Test
        @DisplayName("解析已过期Token - 返回invalid")
        void testParse_ExpiredToken() throws InterruptedException {
            // 创建一个即将过期的Token（1ms后过期）
            JwtProperties shortLifeProperties = new JwtProperties();
            shortLifeProperties.setSecret(TEST_SECRET);
            shortLifeProperties.setExpiration(1L);  // 1ms
            JwtService shortLifeService = new JwtService(shortLifeProperties);

            TokenInfo token = shortLifeService.generateToken(123L, 1L);

            // 等待Token过期
            TimeUnit.MILLISECONDS.sleep(10);

            TokenInfo parsed = jwtService.parseToken(token.getRawToken());

            assertThat(parsed.isValid()).isFalse();
        }
    }

    // ==================== 往返一致性测试 ====================

    @Nested
    @DisplayName("往返一致性")
    class RoundTripTests {

        @Test
        @DisplayName("生成-解析往返 - 所有字段一致")
        void testRoundTrip_AllFieldsPreserved() {
            Long userId = 99999L;
            Long version = 42L;

            TokenInfo generated = jwtService.generateToken(userId, version);
            TokenInfo parsed = jwtService.parseToken(generated.getRawToken());

            assertThat(parsed.getUserId()).isEqualTo(generated.getUserId());
            assertThat(parsed.getVersion()).isEqualTo(generated.getVersion());
            assertThat(parsed.getTokenId()).isEqualTo(generated.getTokenId());
            // JWT expiration精度为秒级，往返后毫秒被置零，使用秒级时间戳比较
            long generatedSeconds = generated.getExpiration().getTime() / 1000;
            long parsedSeconds = parsed.getExpiration().getTime() / 1000;
            assertThat(parsedSeconds).isEqualTo(generatedSeconds);
            assertThat(parsed.getRawToken()).isEqualTo(generated.getRawToken());
        }

        @Test
        @DisplayName("批量往返测试 - 多个用户")
        void testRoundTrip_MultipleUsers() {
            Long[] userIds = {1L, 100L, 999999L, Long.MAX_VALUE};
            Long[] versions = {0L, 1L, 100L, Long.MAX_VALUE};

            for (Long userId : userIds) {
                for (Long version : versions) {
                    TokenInfo generated = jwtService.generateToken(userId, version);
                    TokenInfo parsed = jwtService.parseToken(generated.getRawToken());

                    assertThat(parsed.isValid()).isTrue();
                    assertThat(parsed.getUserId()).isEqualTo(userId);
                    assertThat(parsed.getVersion()).isEqualTo(version);
                }
            }
        }
    }

    // ==================== TokenInfo.generated() 验证 ====================

    @Nested
    @DisplayName("Token生成正确性")
    class TokenStructureTests {

        @Test
        @DisplayName("Token包含三段 - 使用.分隔")
        void testTokenStructure_HasThreeParts() {
            TokenInfo tokenInfo = jwtService.generateToken(123L, 1L);
            String rawToken = tokenInfo.getRawToken();

            String[] parts = rawToken.split("\\.");
            assertThat(parts).hasSize(3);
        }

        @Test
        @DisplayName("Token是JWT格式 - 可解析为Claims")
        void testTokenStructure_JwtFormat() {
            TokenInfo generated = jwtService.generateToken(123L, 1L);
            TokenInfo parsed = jwtService.parseToken(generated.getRawToken());

            // 如果能成功解析且valid为true，说明是有效的JWT格式
            assertThat(parsed.isValid()).isTrue();
        }
    }
}
