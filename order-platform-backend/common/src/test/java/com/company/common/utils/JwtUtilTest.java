package com.company.common.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import com.company.common.Constant.JwtConstant;
import com.company.common.enums.result.AuthResultCode;
import com.company.common.exception.BusinessException;

/**
 * JwtUtil 单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("JwtUtil 单元测试")
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private static final String TEST_SECRET = "dGhpc2lzYXRlc3RzZWNyZXRrZXlmb3Jqd3R0b2tlbmdlbmVyYXRpb24="; // Base64编码的测试密钥
    private static final String TEST_USERNAME = "testuser";
    private static final Integer TEST_USER_ID = 100;

    @BeforeEach
    void setUp() {
        // 设置测试用的配置值
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L); // 1小时
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 604800000L); // 7天

        // 初始化 key
        jwtUtil.init();

        // Mock RedisTemplate 的行为
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.hasKey(any())).thenReturn(false);
    }

    @Nested
    @DisplayName("访问令牌生成测试")
    class AccessTokenTests {

        @Test
        @DisplayName("生成访问令牌成功")
        void generateAccessToken_Success() {
            // When
            String token = jwtUtil.generateAccessToken(TEST_USERNAME, TEST_USER_ID);

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();

            // 验证令牌内容
            String username = jwtUtil.extractUsername(token);
            Integer userId = jwtUtil.extractUserId(token);

            assertThat(username).isEqualTo(TEST_USERNAME);
            assertThat(userId).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("访问令牌包含正确的类型声明")
        void accessToken_HasCorrectType() {
            // When
            String token = jwtUtil.generateAccessToken(TEST_USERNAME, TEST_USER_ID);

            // Then
            assertThat(jwtUtil.isAccessToken(token)).isTrue();
            assertThat(jwtUtil.isRefreshToken(token)).isFalse();
        }
    }

    @Nested
    @DisplayName("刷新令牌生成测试")
    class RefreshTokenTests {

        @Test
        @DisplayName("生成刷新令牌成功")
        void generateRefreshToken_Success() {
            // When
            String token = jwtUtil.generateRefreshToken(TEST_USERNAME, TEST_USER_ID);

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();

            // 验证令牌内容
            String username = jwtUtil.extractUsername(token);
            Integer userId = jwtUtil.extractUserId(token);

            assertThat(username).isEqualTo(TEST_USERNAME);
            assertThat(userId).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("刷新令牌包含正确的类型声明")
        void refreshToken_HasCorrectType() {
            // When
            String token = jwtUtil.generateRefreshToken(TEST_USERNAME, TEST_USER_ID);

            // Then
            assertThat(jwtUtil.isRefreshToken(token)).isTrue();
            assertThat(jwtUtil.isAccessToken(token)).isFalse();
        }
    }

    @Nested
    @DisplayName("令牌解析测试")
    class TokenParsingTests {

        @Test
        @DisplayName("从令牌中提取用户名")
        void extractUsername_Success() {
            // Given
            String token = jwtUtil.generateAccessToken(TEST_USERNAME, TEST_USER_ID);

            // When
            String username = jwtUtil.extractUsername(token);

            // Then
            assertThat(username).isEqualTo(TEST_USERNAME);
        }

        @Test
        @DisplayName("从令牌中提取用户ID")
        void extractUserId_Success() {
            // Given
            String token = jwtUtil.generateAccessToken(TEST_USERNAME, TEST_USER_ID);

            // When
            Integer userId = jwtUtil.extractUserId(token);

            // Then
            assertThat(userId).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("从令牌中提取过期时间")
        void extractExpiration_Success() {
            // Given
            String token = jwtUtil.generateAccessToken(TEST_USERNAME, TEST_USER_ID);

            // When
            Date expiration = jwtUtil.extractExpiration(token);

            // Then
            assertThat(expiration).isNotNull();
            assertThat(expiration).isAfter(new Date());
        }
    }

    @Nested
    @DisplayName("Authorization Header 解析测试")
    class AuthHeaderTests {

        @Test
        @DisplayName("从 Authorization Header 中提取令牌成功")
        void extractToken_Success() {
            // Given
            String authHeader = JwtConstant.BEARER_PREFIX + "some_token";

            // When
            String token = jwtUtil.extractToken(authHeader);

            // Then
            assertThat(token).isEqualTo("some_token");
        }

        @Test
        @DisplayName("Authorization Header 为 null 时抛出异常")
        void extractToken_NullHeader_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> jwtUtil.extractToken(null))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", AuthResultCode.AUTHORIZATION_HEADER_INVALID.getCode());
        }

        @Test
        @DisplayName("Authorization Header 不以 Bearer 开头时抛出异常")
        void extractToken_InvalidHeader_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> jwtUtil.extractToken("InvalidHeader token"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", AuthResultCode.AUTHORIZATION_HEADER_INVALID.getCode());
        }

        @Test
        @DisplayName("从 Authorization Header 中提取用户名")
        void getUsernameFromAuthHeader_Success() {
            // Given
            String token = jwtUtil.generateAccessToken(TEST_USERNAME, TEST_USER_ID);
            String authHeader = JwtConstant.BEARER_PREFIX + token;

            // When
            String username = jwtUtil.getUsernameFromAuthHeader(authHeader);

            // Then
            assertThat(username).isEqualTo(TEST_USERNAME);
        }

        @Test
        @DisplayName("从 Authorization Header 中提取用户ID")
        void getUserIdFromAuthHeader_Success() {
            // Given
            String token = jwtUtil.generateAccessToken(TEST_USERNAME, TEST_USER_ID);
            String authHeader = JwtConstant.BEARER_PREFIX + token;

            // When
            Integer userId = jwtUtil.getUserIdFromAuthHeader(authHeader);

            // Then
            assertThat(userId).isEqualTo(TEST_USER_ID);
        }
    }

    @Nested
    @DisplayName("令牌验证测试")
    class TokenValidationTests {

        @Test
        @DisplayName("有效令牌验证通过")
        void validateToken_ValidToken_ReturnsTrue() {
            // Given
            String token = jwtUtil.generateAccessToken(TEST_USERNAME, TEST_USER_ID);

            // When
            Boolean isValid = jwtUtil.validateToken(token);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("访问令牌在黑名单中时验证失败")
        void validateToken_AccessTokenInBlacklist_ReturnsFalse() {
            // Given
            String token = jwtUtil.generateAccessToken(TEST_USERNAME, TEST_USER_ID);
            when(redisTemplate.hasKey(any())).thenReturn(true);

            // When
            Boolean isValid = jwtUtil.validateToken(token);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("刷新令牌在黑名单中时验证失败")
        void validateToken_RefreshTokenInBlacklist_ReturnsFalse() {
            // Given
            String token = jwtUtil.generateRefreshToken(TEST_USERNAME, TEST_USER_ID);
            when(redisTemplate.hasKey(any())).thenReturn(true);

            // When
            Boolean isValid = jwtUtil.validateToken(token);

            // Then
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("令牌黑名单测试")
    class TokenBlacklistTests {

        @Test
        @DisplayName("将访问令牌加入黑名单")
        void addAccessTokenToBlacklist_Success() {
            // Given
            String token = jwtUtil.generateAccessToken(TEST_USERNAME, TEST_USER_ID);

            // When
            jwtUtil.addAccessTokenToBlacklist(token);

            // Then
            verify(valueOperations, times(1)).set(any(), eq(JwtConstant.BLACKLIST_VALUE), anyLong(), any());
        }

        @Test
        @DisplayName("将刷新令牌加入黑名单")
        void addRefreshTokenToBlacklist_Success() {
            // Given
            String token = jwtUtil.generateRefreshToken(TEST_USERNAME, TEST_USER_ID);

            // When
            jwtUtil.addRefreshTokenToBlacklist(token);

            // Then
            verify(valueOperations, times(1)).set(any(), eq(JwtConstant.BLACKLIST_VALUE), anyLong(), any());
        }

        @Test
        @DisplayName("检查访问令牌是否在黑名单中")
        void isAccessTokenInBlacklist_ReturnsCorrectStatus() {
            // Given
            String token = jwtUtil.generateAccessToken(TEST_USERNAME, TEST_USER_ID);
            when(redisTemplate.hasKey(any())).thenReturn(true);

            // When
            boolean inBlacklist = jwtUtil.isAccessTokenInBlacklist(token);

            // Then
            assertThat(inBlacklist).isTrue();
            verify(redisTemplate, times(1)).hasKey(any());
        }

        @Test
        @DisplayName("检查刷新令牌是否在黑名单中")
        void isRefreshTokenInBlacklist_ReturnsCorrectStatus() {
            // Given
            String token = jwtUtil.generateRefreshToken(TEST_USERNAME, TEST_USER_ID);
            when(redisTemplate.hasKey(any())).thenReturn(true);

            // When
            boolean inBlacklist = jwtUtil.isRefreshTokenInBlacklist(token);

            // Then
            assertThat(inBlacklist).isTrue();
            verify(redisTemplate, times(1)).hasKey(any());
        }
    }

    @Nested
    @DisplayName("无效令牌处理测试")
    class InvalidTokenTests {

        @Test
        @DisplayName("无效令牌类型检查返回false")
        void isAccessToken_InvalidToken_ReturnsFalse() {
            // Given
            String invalidToken = "invalid.token.string";

            // When
            boolean isAccess = jwtUtil.isAccessToken(invalidToken);
            boolean isRefresh = jwtUtil.isRefreshToken(invalidToken);

            // Then
            assertThat(isAccess).isFalse();
            assertThat(isRefresh).isFalse();
        }

        @Test
        @DisplayName("空令牌类型检查返回false")
        void isAccessToken_EmptyToken_ReturnsFalse() {
            // When
            boolean isAccess = jwtUtil.isAccessToken("");
            boolean isRefresh = jwtUtil.isRefreshToken("");

            // Then
            assertThat(isAccess).isFalse();
            assertThat(isRefresh).isFalse();
        }
    }
}
