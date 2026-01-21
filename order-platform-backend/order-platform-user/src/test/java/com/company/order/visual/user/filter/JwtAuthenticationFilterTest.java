package com.company.order.visual.user.filter;

import com.company.order.visual.common.security.JwtService;
import com.company.order.visual.common.security.TokenBlacklistService;
import com.company.order.visual.common.security.TokenInfo;
import com.company.order.visual.user.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;

/**
 * JwtAuthenticationFilter 单元测试
 * <p>
 * 测试覆盖：
 * - 无Authorization头 - 跳过认证
 * - 无效Token - 跳过认证
 * - 黑名单Token - 跳过认证
 * - 版本号不匹配 - 跳过认证（密码重置场景）
 * - 有效Token - 设置SecurityContext
 * - 已认证用户 - 跳过重复认证
 * - 异常处理 - 不抛出异常，继续过滤器链
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter 单元测试")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistService blacklistService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtFilter;

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final Long USER_ID = 12345L;
    private static final String TOKEN_ID = "token-abc-123";
    private static final Long TOKEN_VERSION = 1L;

    @BeforeEach
    void setUp() {
        jwtFilter = new JwtAuthenticationFilter(jwtService, blacklistService, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    // ==================== 无Token场景 ====================

    @Nested
    @DisplayName("无Token场景")
    class NoTokenTests {

        @Test
        @DisplayName("无Authorization头 - 跳过认证")
        void testNoAuthHeader() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn(null);

            jwtFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain, atLeastOnce()).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verifyNoInteractions(jwtService, blacklistService, userDetailsService);
        }

        @Test
        @DisplayName("Authorization头为空 - 跳过认证")
        void testEmptyAuthHeader() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn("");

            jwtFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain, atLeastOnce()).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Authorization头无Bearer前缀 - 跳过认证")
        void testAuthHeaderWithoutBearer() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

            jwtFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain, atLeastOnce()).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    // ==================== 无效Token场景 ====================

    @Nested
    @DisplayName("无效Token场景")
    class InvalidTokenTests {

        @Test
        @DisplayName("Token解析失败 - 跳过认证")
        void testParseTokenFailed() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
            when(jwtService.parseToken(VALID_TOKEN)).thenReturn(TokenInfo.invalid());

            jwtFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain, atLeastOnce()).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(blacklistService, never()).isBlacklisted(any());
        }

        @Test
        @DisplayName("Token已过期 - 跳过认证")
        void testExpiredToken() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
            when(jwtService.parseToken(VALID_TOKEN)).thenReturn(TokenInfo.invalid());

            jwtFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain, atLeastOnce()).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    // ==================== 黑名单场景 ====================

    @Nested
    @DisplayName("黑名单Token场景")
    class BlacklistedTokenTests {

        @Test
        @DisplayName("Token在黑名单中 - 跳过认证")
        void testBlacklistedToken() throws ServletException, IOException {
            TokenInfo tokenInfo = createValidTokenInfo();
            when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
            when(jwtService.parseToken(VALID_TOKEN)).thenReturn(tokenInfo);
            when(blacklistService.isBlacklisted(TOKEN_ID)).thenReturn(true);

            jwtFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain, atLeastOnce()).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(blacklistService).isBlacklisted(TOKEN_ID);
        }
    }

    // ==================== 版本号校验场景 ====================

    @Nested
    @DisplayName("版本号校验场景")
    class VersionValidationTests {

        @Test
        @DisplayName("版本号不匹配 - 跳过认证（密码重置场景）")
        void testVersionMismatch() throws ServletException, IOException {
            TokenInfo tokenInfo = createValidTokenInfo();
            when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
            when(jwtService.parseToken(VALID_TOKEN)).thenReturn(tokenInfo);
            when(blacklistService.isBlacklisted(TOKEN_ID)).thenReturn(false);
            when(blacklistService.getUserTokenVersion(USER_ID)).thenReturn(2L); // 版本号不匹配

            jwtFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain, atLeastOnce()).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("版本号为null（首次登录） - 通过认证")
        void testVersionNull_FirstLogin() throws ServletException, IOException {
            TokenInfo tokenInfo = createValidTokenInfo();
            UserDetails userDetails = createUserDetails();

            when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
            when(jwtService.parseToken(VALID_TOKEN)).thenReturn(tokenInfo);
            when(blacklistService.isBlacklisted(TOKEN_ID)).thenReturn(false);
            when(blacklistService.getUserTokenVersion(USER_ID)).thenReturn(null);
            when(userDetailsService.loadUserById(USER_ID)).thenReturn(userDetails);

            jwtFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain, atLeastOnce()).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        }

        @Test
        @DisplayName("版本号匹配 - 通过认证")
        void testVersionMatch() throws ServletException, IOException {
            TokenInfo tokenInfo = createValidTokenInfo();
            UserDetails userDetails = createUserDetails();

            when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
            when(jwtService.parseToken(VALID_TOKEN)).thenReturn(tokenInfo);
            when(blacklistService.isBlacklisted(TOKEN_ID)).thenReturn(false);
            when(blacklistService.getUserTokenVersion(USER_ID)).thenReturn(TOKEN_VERSION);
            when(userDetailsService.loadUserById(USER_ID)).thenReturn(userDetails);

            jwtFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain, atLeastOnce()).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        }
    }

    // ==================== 认证成功场景 ====================

    @Nested
    @DisplayName("认证成功场景")
    class SuccessAuthenticationTests {

        @Test
        @DisplayName("有效Token - 设置SecurityContext")
        void testValidToken_AuthenticationSuccess() throws ServletException, IOException {
            TokenInfo tokenInfo = createValidTokenInfo();
            UserDetails userDetails = createUserDetails();

            when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
            when(jwtService.parseToken(VALID_TOKEN)).thenReturn(tokenInfo);
            when(blacklistService.isBlacklisted(TOKEN_ID)).thenReturn(false);
            when(blacklistService.getUserTokenVersion(USER_ID)).thenReturn(null);
            when(userDetailsService.loadUserById(USER_ID)).thenReturn(userDetails);

            jwtFilter.doFilterInternal(request, response, filterChain);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
            assertThat(authentication.getCredentials()).isNull();
            assertThat(authentication.getAuthorities()).hasSize(1);

            verify(filterChain, atLeastOnce()).doFilter(request, response);
        }

        @Test
        @DisplayName("已认证用户 - 跳过重复认证")
        void testAlreadyAuthenticated() throws ServletException, IOException {
            // 预先设置认证
            Authentication existingAuth = mock(Authentication.class);
            SecurityContextHolder.getContext().setAuthentication(existingAuth);

            jwtFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain, atLeastOnce()).doFilter(request, response);
            verifyNoInteractions(jwtService, blacklistService, userDetailsService);
        }
    }

    // ==================== 异常处理场景 ====================

    @Nested
    @DisplayName("异常处理场景")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("JwtService异常 - 不抛出异常，继续过滤器链")
        void testJwtServiceException() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
            when(jwtService.parseToken(VALID_TOKEN)).thenThrow(new RuntimeException("JWT parsing error"));

            jwtFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain, atLeastOnce()).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("黑名单服务异常 - 不抛出异常，继续过滤器链")
        void testBlacklistServiceException() throws ServletException, IOException {
            TokenInfo tokenInfo = createValidTokenInfo();
            when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
            when(jwtService.parseToken(VALID_TOKEN)).thenReturn(tokenInfo);
            when(blacklistService.isBlacklisted(TOKEN_ID)).thenThrow(new RuntimeException("Redis error"));

            jwtFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain, atLeastOnce()).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("UserDetailsService异常 - 不抛出异常，继续过滤器链")
        void testUserDetailsServiceException() throws ServletException, IOException {
            TokenInfo tokenInfo = createValidTokenInfo();
            when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
            when(jwtService.parseToken(VALID_TOKEN)).thenReturn(tokenInfo);
            when(blacklistService.isBlacklisted(TOKEN_ID)).thenReturn(false);
            when(blacklistService.getUserTokenVersion(USER_ID)).thenReturn(null);
            when(userDetailsService.loadUserById(USER_ID)).thenThrow(new RuntimeException("User not found"));

            jwtFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain, atLeastOnce()).doFilter(request, response);
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建有效TokenInfo用于测试
     */
    private TokenInfo createValidTokenInfo() {
        return TokenInfo.generated(VALID_TOKEN, USER_ID, TOKEN_ID, TOKEN_VERSION,
                java.util.Date.from(java.time.Instant.now().plusSeconds(3600)));
    }

    /**
     * 创建UserDetails用于测试
     */
    private UserDetails createUserDetails() {
        return new User("testuser", "password", Collections.singletonList(() -> "ROLE_USER"));
    }
}
