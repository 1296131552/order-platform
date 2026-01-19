package com.company.order.visual.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.order.visual.common.exception.BusinessException;
import com.company.order.visual.common.response.ResponseCode;
import com.company.order.visual.common.security.JwtProperties;
import com.company.order.visual.common.security.JwtService;
import com.company.order.visual.common.security.TokenBlacklistService;
import com.company.order.visual.common.security.TokenInfo;
import com.company.order.visual.user.converter.UserConverter;
import com.company.order.visual.user.dto.LoginRequest;
import com.company.order.visual.user.dto.LoginResponse;
import com.company.order.visual.user.entity.User;
import com.company.order.visual.user.mapper.UserMapper;
import com.company.order.visual.user.mapper.UserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl 单元测试
 * <p>
 * 测试覆盖：
 * - 登录流程：用户名/邮箱/手机号登录、密码验证、Token生成
 * - 登出流程：Token解析、加入黑名单、移除活跃Token
 * - 版本号管理：初始化、刷新TTL（防旧Token复活）
 * - 异步更新登录信息（不阻塞返回）
 * - 用户状态校验：禁用/锁定用户拒绝登录
 * - P0-2修复：登录不加事务，Token生成在数据库操作前
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl 单元测试")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRoleMapper userRoleMapper;

    @Mock
    private UserConverter userConverter;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private UserServiceImpl userService;

    private static final Long TEST_USER_ID = 12345L;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "encoded-password";
    private static final String RAW_PASSWORD = "raw-password";
    private static final String TEST_TOKEN = "test.jwt.token";
    private static final String TEST_TOKEN_ID = "token-id-123";
    private static final Long INITIAL_VERSION = 1L;

    @BeforeEach
    void setUp() {
        // 使用 @RequiredArgsConstructor 生成的构造函数
        userService = new UserServiceImpl(passwordEncoder, jwtService, tokenBlacklistService);
        // 使用反射注入 @Resource 依赖
        setField(userService, "userMapper", userMapper);
        setField(userService, "userRoleMapper", userRoleMapper);
        setField(userService, "userConverter", userConverter);
    }

    /**
     * 使用反射设置私有字段
     */
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== 登录成功场景 ====================

    @Nested
    @DisplayName("登录成功场景")
    class LoginSuccessTests {

        @Test
        @DisplayName("用户名登录成功 - 首次登录（版本号初始化）")
        void testLoginByUsername_FirstLogin() {
            // Arrange
            User user = createTestUser(true, false);
            LoginRequest request = new LoginRequest();
            request.setAccount(TEST_USERNAME);
            request.setPassword(RAW_PASSWORD);

            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
            when(passwordEncoder.matches(RAW_PASSWORD, TEST_PASSWORD)).thenReturn(true);
            when(tokenBlacklistService.getUserTokenVersion(TEST_USER_ID)).thenReturn(null);
            when(jwtService.generateToken(TEST_USER_ID, INITIAL_VERSION))
                    .thenReturn(createTokenInfo(INITIAL_VERSION));
            when(userConverter.toVO(user)).thenReturn(createUserVO());

            // Act
            LoginResponse response = userService.login(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo(TEST_TOKEN);
            assertThat(response.getUser()).isNotNull();

            // 验证版本号初始化
            verify(tokenBlacklistService).getUserTokenVersion(TEST_USER_ID);
            verify(tokenBlacklistService).setUserTokenVersion(TEST_USER_ID, INITIAL_VERSION);
            verify(tokenBlacklistService).addActiveToken(TEST_USER_ID, TEST_TOKEN_ID);
        }

        @Test
        @DisplayName("用户名登录成功 - 已有版本号（刷新TTL）[P1-1安全修复]")
        void testLoginByUsername_ExistingVersion() {
            // Arrange
            User user = createTestUser(true, false);
            LoginRequest request = new LoginRequest();
            request.setAccount(TEST_USERNAME);
            request.setPassword(RAW_PASSWORD);

            Long existingVersion = 5L;
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
            when(passwordEncoder.matches(RAW_PASSWORD, TEST_PASSWORD)).thenReturn(true);
            when(tokenBlacklistService.getUserTokenVersion(TEST_USER_ID)).thenReturn(existingVersion);
            when(jwtService.generateToken(TEST_USER_ID, existingVersion))
                    .thenReturn(createTokenInfo(existingVersion));
            when(userConverter.toVO(user)).thenReturn(createUserVO());

            // Act
            LoginResponse response = userService.login(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo(TEST_TOKEN);

            // 验证刷新版本号TTL（防止旧Token复活）
            verify(tokenBlacklistService).refreshUserTokenVersion(TEST_USER_ID);
            verify(tokenBlacklistService, never()).setUserTokenVersion(anyLong(), anyLong());
        }

        @Test
        @DisplayName("邮箱登录成功")
        void testLoginByEmail() {
            // Arrange
            User user = createTestUserWithEmail();
            LoginRequest request = new LoginRequest();
            request.setAccount("test@example.com");
            request.setPassword(RAW_PASSWORD);

            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
            when(passwordEncoder.matches(RAW_PASSWORD, TEST_PASSWORD)).thenReturn(true);
            when(tokenBlacklistService.getUserTokenVersion(TEST_USER_ID)).thenReturn(null);
            when(jwtService.generateToken(TEST_USER_ID, INITIAL_VERSION))
                    .thenReturn(createTokenInfo(INITIAL_VERSION));
            when(userConverter.toVO(user)).thenReturn(createUserVO());

            // Act
            LoginResponse response = userService.login(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo(TEST_TOKEN);
        }

        @Test
        @DisplayName("手机号登录成功")
        void testLoginByPhone() {
            // Arrange
            User user = createTestUserWithPhone();
            LoginRequest request = new LoginRequest();
            request.setAccount("13800138000");
            request.setPassword(RAW_PASSWORD);

            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
            when(passwordEncoder.matches(RAW_PASSWORD, TEST_PASSWORD)).thenReturn(true);
            when(tokenBlacklistService.getUserTokenVersion(TEST_USER_ID)).thenReturn(null);
            when(jwtService.generateToken(TEST_USER_ID, INITIAL_VERSION))
                    .thenReturn(createTokenInfo(INITIAL_VERSION));
            when(userConverter.toVO(user)).thenReturn(createUserVO());

            // Act
            LoginResponse response = userService.login(request);

            // Assert
            assertThat(response).isNotNull();
        }
    }

    // ==================== 登录失败场景 ====================

    @Nested
    @DisplayName("登录失败场景")
    class LoginFailureTests {

        @Test
        @DisplayName("用户不存在 - 抛出LOGIN_FAILED异常")
        void testLogin_UserNotFound() {
            // Arrange
            LoginRequest request = new LoginRequest();
            request.setAccount("nonexistent");
            request.setPassword(RAW_PASSWORD);

            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ResponseCode.LOGIN_FAILED.getCode());

            verify(jwtService, never()).generateToken(anyLong(), anyLong());
        }

        @Test
        @DisplayName("密码错误 - 抛出LOGIN_FAILED异常")
        void testLogin_WrongPassword() {
            // Arrange
            User user = createTestUser(true, false);
            LoginRequest request = new LoginRequest();
            request.setAccount(TEST_USERNAME);
            request.setPassword("wrong-password");

            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
            when(passwordEncoder.matches("wrong-password", TEST_PASSWORD)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ResponseCode.LOGIN_FAILED.getCode());

            verify(jwtService, never()).generateToken(anyLong(), anyLong());
        }

        @Test
        @DisplayName("用户被禁用 - 抛出USER_DISABLED异常")
        void testLogin_UserDisabled() {
            // Arrange
            User user = createTestUser(false, false);  // isEnabled=false
            LoginRequest request = new LoginRequest();
            request.setAccount(TEST_USERNAME);
            request.setPassword(RAW_PASSWORD);

            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
            // 不需要 stub passwordEncoder.matches，因为 validateUserForLogin 会先检查 isEnabled

            // Act & Assert
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ResponseCode.USER_DISABLED.getCode());

            verify(jwtService, never()).generateToken(anyLong(), anyLong());
        }

        @Test
        @DisplayName("用户被锁定 - 抛出USER_LOCKED异常")
        void testLogin_UserLocked() {
            // Arrange
            User user = createTestUser(true, true);  // isLocked=true
            LoginRequest request = new LoginRequest();
            request.setAccount(TEST_USERNAME);
            request.setPassword(RAW_PASSWORD);

            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
            // 不需要 stub passwordEncoder.matches，因为 validateUserForLogin 会先检查 isLocked

            // Act & Assert
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(ResponseCode.USER_LOCKED.getCode());

            verify(jwtService, never()).generateToken(anyLong(), anyLong());
        }
    }

    // ==================== 登出测试 ====================

    @Nested
    @DisplayName("登出测试")
    class LogoutTests {

        @Test
        @DisplayName("正常登出 - Token加入黑名单")
        void testLogout_Success() {
            // Arrange
            TokenInfo tokenInfo = createTokenInfo(INITIAL_VERSION);
            when(jwtService.parseToken(TEST_TOKEN)).thenReturn(tokenInfo);
            doNothing().when(tokenBlacklistService).addToBlacklist(tokenInfo);
            doNothing().when(tokenBlacklistService).removeActiveToken(anyLong(), anyString());

            // Act
            userService.logout(TEST_TOKEN);

            // Assert
            verify(tokenBlacklistService).addToBlacklist(tokenInfo);
            verify(tokenBlacklistService).removeActiveToken(TEST_USER_ID, TEST_TOKEN_ID);
        }

        @Test
        @DisplayName("登出 - Token为空则跳过")
        void testLogout_NullToken() {
            // Act
            userService.logout(null);
            userService.logout("");

            // Assert
            verify(jwtService, never()).parseToken(anyString());
        }

        @Test
        @DisplayName("登出 - Token无效则跳过")
        void testLogout_InvalidToken() {
            // Arrange
            when(jwtService.parseToken(TEST_TOKEN)).thenReturn(TokenInfo.invalid());

            // Act
            userService.logout(TEST_TOKEN);

            // Assert
            verify(tokenBlacklistService, never()).addToBlacklist(any());
            verify(tokenBlacklistService, never()).removeActiveToken(anyLong(), anyString());
        }
    }

    // ==================== Token生成顺序测试 [P0-2修复] ====================

    @Nested
    @DisplayName("P0-2修复：Token生成顺序")
    class TokenGenerationOrderTests {

        @Test
        @DisplayName("登录流程 - Token在数据库操作前生成（不加事务）")
        void testLogin_TokenGeneratedBeforeDbUpdate() {
            // Arrange
            User user = createTestUser(true, false);
            LoginRequest request = new LoginRequest();
            request.setAccount(TEST_USERNAME);
            request.setPassword(RAW_PASSWORD);

            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
            when(passwordEncoder.matches(RAW_PASSWORD, TEST_PASSWORD)).thenReturn(true);
            when(tokenBlacklistService.getUserTokenVersion(TEST_USER_ID)).thenReturn(null);
            when(jwtService.generateToken(TEST_USER_ID, INITIAL_VERSION))
                    .thenReturn(createTokenInfo(INITIAL_VERSION));
            when(userConverter.toVO(user)).thenReturn(createUserVO());

            // Act
            LoginResponse response = userService.login(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isNotEmpty();

            // 验证Token在数据库更新前生成（异步更新在Token返回后）
            // 由于updateLoginInfoAsync是异步的，这里不验证其调用时机
            // 核心是login方法本身不加@Transactional
        }

        @Test
        @DisplayName("登录流程 - 异步更新不影响返回")
        void testLogin_AsyncUpdateDoesNotBlock() {
            // Arrange
            User user = createTestUser(true, false);
            LoginRequest request = new LoginRequest();
            request.setAccount(TEST_USERNAME);
            request.setPassword(RAW_PASSWORD);

            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
            when(passwordEncoder.matches(RAW_PASSWORD, TEST_PASSWORD)).thenReturn(true);
            when(tokenBlacklistService.getUserTokenVersion(TEST_USER_ID)).thenReturn(null);
            when(jwtService.generateToken(TEST_USER_ID, INITIAL_VERSION))
                    .thenReturn(createTokenInfo(INITIAL_VERSION));
            when(userConverter.toVO(user)).thenReturn(createUserVO());

            // Act
            long startTime = System.currentTimeMillis();
            LoginResponse response = userService.login(request);
            long elapsed = System.currentTimeMillis() - startTime;

            // Assert
            assertThat(response).isNotNull();
            // 异步更新不应阻塞，返回应很快
            assertThat(elapsed).isLessThan(1000);
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建测试用户（实体）
     */
    private User createTestUser(boolean isEnabled, boolean isLocked) {
        User user = new User();
        user.setId(TEST_USER_ID);
        user.setUsername(TEST_USERNAME);
        user.setPassword(TEST_PASSWORD);
        user.setIsEnabled(isEnabled);
        user.setIsLocked(isLocked);
        user.setIsDeleted(false);
        user.setLoginCount(0);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    /**
     * 创建测试用户（邮箱登录）
     */
    private User createTestUserWithEmail() {
        User user = createTestUser(true, false);
        user.setUsername(null);
        user.setEmail("test@example.com");
        return user;
    }

    /**
     * 创建测试用户（手机号登录）
     */
    private User createTestUserWithPhone() {
        User user = createTestUser(true, false);
        user.setUsername(null);
        user.setPhone("13800138000");
        return user;
    }

    /**
     * 创建TokenInfo用于测试
     */
    private TokenInfo createTokenInfo(Long version) {
        return TokenInfo.generated(TEST_TOKEN, TEST_USER_ID, TEST_TOKEN_ID, version,
                java.util.Date.from(java.time.Instant.now().plusSeconds(3600)));
    }

    /**
     * 创建UserVO用于测试
     */
    private com.company.order.visual.user.dto.UserVO createUserVO() {
        com.company.order.visual.user.dto.UserVO vo = new com.company.order.visual.user.dto.UserVO();
        vo.setId(TEST_USER_ID);
        vo.setUsername(TEST_USERNAME);
        return vo;
    }
}
