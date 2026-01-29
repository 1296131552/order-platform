package com.company.user.service.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

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
import org.springframework.security.core.Authentication;

import com.company.common.enums.result.AuthResultCode;
import com.company.common.exception.BusinessException;
import com.company.common.utils.JwtUtil;
import com.company.user.model.entity.User;
import com.company.user.model.vo.JwtVO;
import com.company.user.model.vo.UserVO;
import com.company.user.service.basic.UserService;
import com.company.user.service.domain.auth.impl.AuthDomainImpl;

/**
 * AuthDomain 单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AuthDomain 单元测试")
class AuthDomainTest {

    @InjectMocks
    private AuthDomainImpl authDomain;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    private UserVO testUserVO;
    private User testUser;

    private static final String TEST_USERNAME = "testuser";
    private static final Integer TEST_USER_ID = 1;
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";

    @BeforeEach
    void setUp() {
        testUserVO = new UserVO();
        testUserVO.setId(TEST_USER_ID);
        testUserVO.setUsername(TEST_USERNAME);
        testUserVO.setIsValid(true);

        testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setUsername(TEST_USERNAME);
        testUser.setPassword("encoded_password");
        testUser.setIsValid(true);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());
        testUser.setIsDelete(false);
    }

    @Nested
    @DisplayName("用户登录测试")
    class LoginTests {

        @Test
        @DisplayName("登录成功返回JwtVO")
        void login_Success_ReturnsJwtVO() {
            // Given
            when(jwtUtil.generateAccessToken(TEST_USERNAME, TEST_USER_ID)).thenReturn(ACCESS_TOKEN);
            when(jwtUtil.generateRefreshToken(TEST_USERNAME, TEST_USER_ID)).thenReturn(REFRESH_TOKEN);

            // When
            JwtVO jwtVO = authDomain.login(testUserVO, authentication);

            // Then
            assertThat(jwtVO).isNotNull();
            assertThat(jwtVO.getAccessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(jwtVO.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
            assertThat(jwtVO.getUsername()).isEqualTo(TEST_USERNAME);
            assertThat(jwtVO.getUserId()).isEqualTo(TEST_USER_ID);

            verify(jwtUtil, times(1)).generateAccessToken(TEST_USERNAME, TEST_USER_ID);
            verify(jwtUtil, times(1)).generateRefreshToken(TEST_USERNAME, TEST_USER_ID);
        }
    }

    @Nested
    @DisplayName("令牌刷新测试")
    class RefreshTokenTests {

        @Test
        @DisplayName("刷新令牌成功返回新的JwtVO")
        void refreshToken_ValidToken_ReturnsNewJwtVO() {
            // Given
            when(jwtUtil.isRefreshToken(REFRESH_TOKEN)).thenReturn(true);
            when(jwtUtil.isRefreshTokenInBlacklist(REFRESH_TOKEN)).thenReturn(false);
            when(jwtUtil.isTokenExpired(REFRESH_TOKEN)).thenReturn(false);
            when(jwtUtil.extractUsername(REFRESH_TOKEN)).thenReturn(TEST_USERNAME);
            when(userService.getUser(TEST_USERNAME)).thenReturn(testUser);
            when(jwtUtil.generateAccessToken(TEST_USERNAME, TEST_USER_ID)).thenReturn("new_access_token");
            when(jwtUtil.generateRefreshToken(TEST_USERNAME, TEST_USER_ID)).thenReturn("new_refresh_token");

            // When
            JwtVO jwtVO = authDomain.refreshToken(REFRESH_TOKEN);

            // Then
            assertThat(jwtVO).isNotNull();
            assertThat(jwtVO.getAccessToken()).isEqualTo("new_access_token");
            assertThat(jwtVO.getRefreshToken()).isEqualTo("new_refresh_token");
            assertThat(jwtVO.getUsername()).isEqualTo(TEST_USERNAME);
            assertThat(jwtVO.getUserId()).isEqualTo(TEST_USER_ID);

            verify(jwtUtil, times(1)).addRefreshTokenToBlacklist(REFRESH_TOKEN);
        }

        @Test
        @DisplayName("刷新令牌类型错误时抛出异常")
        void refreshToken_InvalidTokenType_ThrowsException() {
            // Given
            when(jwtUtil.isRefreshToken(REFRESH_TOKEN)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> authDomain.refreshToken(REFRESH_TOKEN))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", AuthResultCode.INVALID_REFRESH_TOKEN.getCode());

            verify(jwtUtil, never()).generateAccessToken(anyString(), any());
            verify(jwtUtil, never()).generateRefreshToken(anyString(), any());
        }

        @Test
        @DisplayName("刷新令牌在黑名单中时抛出异常")
        void refreshToken_BlacklistedToken_ThrowsException() {
            // Given
            when(jwtUtil.isRefreshToken(REFRESH_TOKEN)).thenReturn(true);
            when(jwtUtil.isRefreshTokenInBlacklist(REFRESH_TOKEN)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authDomain.refreshToken(REFRESH_TOKEN))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", AuthResultCode.REFRESH_TOKEN_BLACKLISTED.getCode());

            verify(jwtUtil, never()).generateAccessToken(anyString(), any());
            verify(jwtUtil, never()).generateRefreshToken(anyString(), any());
        }

        @Test
        @DisplayName("刷新令牌过期时抛出异常")
        void refreshToken_ExpiredToken_ThrowsException() {
            // Given
            when(jwtUtil.isRefreshToken(REFRESH_TOKEN)).thenReturn(true);
            when(jwtUtil.isRefreshTokenInBlacklist(REFRESH_TOKEN)).thenReturn(false);
            when(jwtUtil.isTokenExpired(REFRESH_TOKEN)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authDomain.refreshToken(REFRESH_TOKEN))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", AuthResultCode.REFRESH_TOKEN_EXPIRED.getCode());

            verify(jwtUtil, never()).generateAccessToken(anyString(), any());
            verify(jwtUtil, never()).generateRefreshToken(anyString(), any());
        }

        @Test
        @DisplayName("用户被禁用时刷新令牌失败")
        void refreshToken_BannedUser_ThrowsException() {
            // Given
            testUser.setIsValid(false);
            when(jwtUtil.isRefreshToken(REFRESH_TOKEN)).thenReturn(true);
            when(jwtUtil.isRefreshTokenInBlacklist(REFRESH_TOKEN)).thenReturn(false);
            when(jwtUtil.isTokenExpired(REFRESH_TOKEN)).thenReturn(false);
            when(jwtUtil.extractUsername(REFRESH_TOKEN)).thenReturn(TEST_USERNAME);
            when(userService.getUser(TEST_USERNAME)).thenReturn(testUser);

            // When & Then
            assertThatThrownBy(() -> authDomain.refreshToken(REFRESH_TOKEN))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", AuthResultCode.USER_BANNED.getCode());

            verify(jwtUtil, never()).generateAccessToken(anyString(), any());
            verify(jwtUtil, never()).generateRefreshToken(anyString(), any());
        }
    }

    @Nested
    @DisplayName("退出登录测试")
    class LogoutTests {

        @Test
        @DisplayName("退出登录成功将令牌加入黑名单")
        void logout_WithRefreshToken_BlacklistsBothTokens() {
            // Given
            when(jwtUtil.isRefreshToken(REFRESH_TOKEN)).thenReturn(true);

            // When
            authDomain.logout(ACCESS_TOKEN, REFRESH_TOKEN);

            // Then
            verify(jwtUtil, times(1)).addAccessTokenToBlacklist(ACCESS_TOKEN);
            verify(jwtUtil, times(1)).addRefreshTokenToBlacklist(REFRESH_TOKEN);
        }

        @Test
        @DisplayName("退出登录时只提供访问令牌")
        void logout_OnlyAccessToken_BlacklistsOnlyAccessToken() {
            // Given
            when(jwtUtil.isRefreshToken(anyString())).thenReturn(false);

            // When
            authDomain.logout(ACCESS_TOKEN, null);

            // Then
            verify(jwtUtil, times(1)).addAccessTokenToBlacklist(ACCESS_TOKEN);
            verify(jwtUtil, never()).addRefreshTokenToBlacklist(anyString());
        }

        @Test
        @DisplayName("退出登录时提供空刷新令牌")
        void logout_EmptyRefreshToken_BlacklistsOnlyAccessToken() {
            // When
            authDomain.logout(ACCESS_TOKEN, "");

            // Then
            verify(jwtUtil, times(1)).addAccessTokenToBlacklist(ACCESS_TOKEN);
            verify(jwtUtil, never()).addRefreshTokenToBlacklist(anyString());
        }

        @Test
        @DisplayName("退出登录时刷新令牌无效时不加入黑名单")
        void logout_InvalidRefreshToken_OnlyBlacklistsAccessToken() {
            // Given
            when(jwtUtil.isRefreshToken(REFRESH_TOKEN)).thenReturn(false);

            // When
            authDomain.logout(ACCESS_TOKEN, REFRESH_TOKEN);

            // Then
            verify(jwtUtil, times(1)).addAccessTokenToBlacklist(ACCESS_TOKEN);
            verify(jwtUtil, never()).addRefreshTokenToBlacklist(REFRESH_TOKEN);
        }

        @Test
        @DisplayName("退出登录时刷新令牌有效则加入黑名单")
        void logout_ValidRefreshToken_BlacklistsBothTokens() {
            // Given
            when(jwtUtil.isRefreshToken(REFRESH_TOKEN)).thenReturn(true);

            // When
            authDomain.logout(ACCESS_TOKEN, REFRESH_TOKEN);

            // Then
            verify(jwtUtil, times(1)).addAccessTokenToBlacklist(ACCESS_TOKEN);
            verify(jwtUtil, times(1)).addRefreshTokenToBlacklist(REFRESH_TOKEN);
        }
    }
}
