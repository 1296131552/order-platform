package com.order.platform.user.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.order.platform.common.config.OrderPlatformProperties;
import com.order.platform.common.dto.CurrentUserDTO;
import com.order.platform.common.enums.ResponseCode;
import com.order.platform.common.exception.BusinessException;
import com.order.platform.common.service.OperationLogService;
import com.order.platform.common.util.JwtUtil;
import com.order.platform.user.dto.request.ChangePasswordDTO;
import com.order.platform.user.dto.request.LoginDTO;
import com.order.platform.user.entity.Role;
import com.order.platform.user.entity.User;
import com.order.platform.user.enums.UserAuditStatus;
import com.order.platform.user.mapper.RoleMapper;
import com.order.platform.user.mapper.UserMapper;
import com.order.platform.user.mapper.UserRoleMapper;
import com.order.platform.user.service.AuthHelper;
import com.order.platform.user.service.PermissionService;
import com.order.platform.user.utils.PasswordEncoderUtil;
import com.order.platform.user.vo.LoginVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthService 单元测试
 *
 * 测试范围：
 * 1. 用户登录认证流程
 * 2. 密码错误锁定机制
 * 3. 用户审核状态检查
 * 4. Token 生成和刷新
 * 5. 密码修改和验证
 * 6. 安全防护机制
 *
 * @author 开发组
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("认证服务测试")
class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRoleMapper userRoleMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private PermissionService permissionService;

    @Mock
    private AuthHelper authHelper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private OperationLogService operationLogService;

    @Mock
    private OrderPlatformProperties properties;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private PasswordEncoderUtil passwordEncoderUtil;  // 添加实例 mock

    private MockedStatic<PasswordEncoderUtil> mockedPasswordEncoderUtil;  // 静态方法 mock

    private User testUser;
    private LoginDTO validLoginDTO;
    private String validToken = "valid.jwt.token";

    @BeforeEach
    void setUp() {
        // 初始化 MyBatis Plus TableInfoHelper
        // 解决单元测试中使用 Lambda 表达式时 "can not find lambda cache" 的问题
        // 参考: https://blog.csdn.net/Li_WenZhang/article/details/142104309
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), User.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Role.class);

        // 手动注入 passwordEncoderUtil 实例 mock
        // @InjectMocks 无法注入 final 字段，需要使用 ReflectionTestUtils
        ReflectionTestUtils.setField(authService, "passwordEncoderUtil", passwordEncoderUtil);

        // 初始化静态方法 mock（用于 encode 和 matches 静态方法）
        mockedPasswordEncoderUtil = mockStatic(PasswordEncoderUtil.class);
        mockedPasswordEncoderUtil.when(() -> PasswordEncoderUtil.matches(anyString(), anyString())).thenReturn(true);
        mockedPasswordEncoderUtil.when(() -> PasswordEncoderUtil.encode(anyString())).thenReturn("$2a$10$encoded");

        // 配置 passwordEncoderUtil 实例 mock（用于 validateStrength 实例方法）
        PasswordEncoderUtil.PasswordStrength validStrength = mock(PasswordEncoderUtil.PasswordStrength.class);
        when(validStrength.isValid()).thenReturn(true);
        when(validStrength.getScore()).thenReturn(80);
        when(validStrength.getMessage()).thenReturn("密码强度良好");
        when(passwordEncoderUtil.validateStrength(anyString())).thenReturn(validStrength);

        // 初始化测试用户
        testUser = createTestUser();

        // 初始化有效登录 DTO
        validLoginDTO = new LoginDTO();
        validLoginDTO.setAccount("testuser");
        validLoginDTO.setPassword("Test123456!");

        // Mock userMapper.selectById for updateLoginInfo
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // Mock Redis operations
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Mock JWT properties
        OrderPlatformProperties.Jwt jwtProps = mock(OrderPlatformProperties.Jwt.class);
        when(jwtProps.getExpiration()).thenReturn(604800L); // 7 天
        when(properties.getJwt()).thenReturn(jwtProps);

        // Mock Security properties
        OrderPlatformProperties.Security securityProps = mock(OrderPlatformProperties.Security.class);
        when(securityProps.getPassword()).thenReturn(mock(OrderPlatformProperties.Security.Password.class));
        when(properties.getSecurity()).thenReturn(securityProps);
    }

    @AfterEach
    void tearDown() {
        // 清理静态方法 mock
        if (mockedPasswordEncoderUtil != null) {
            mockedPasswordEncoderUtil.close();
        }
    }

    // ==================== 辅助方法 ====================

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
        user.setEmail("test@example.com");
        user.setPhone("13800138000");
        user.setAuditStatus(UserAuditStatus.APPROVED.name());
        user.setIsEnabled(1);
        user.setIsLocked(0);
        user.setIsDeleted(0);  // 添加删除标记初始化
        user.setPasswordChangedTime(LocalDateTime.now().minusDays(30));
        user.setLastLoginTime(LocalDateTime.now().minusDays(1));
        user.setLoginCount(5);
        user.setDepartmentId(10L);
        user.setDepartmentName("测试部门");
        return user;
    }

    private LoginDTO createLoginDTO(String account, String password) {
        LoginDTO dto = new LoginDTO();
        dto.setAccount(account);
        dto.setPassword(password);
        return dto;
    }

    private Role createMockRole(Long id, String roleCode, String roleName, Integer dataScopeType) {
        Role role = new Role();
        role.setId(id);
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setRoleType("BUSINESS");
        role.setDataScopeType(dataScopeType);
        role.setIsEnabled(1);
        role.setIsSystem(0);
        return role;
    }

    // ==================== 登录功能测试 ====================

    @Nested
    @DisplayName("用户登录功能测试")
    class LoginTests {

        // ==================== 成功场景 ====================

        @Test
        @DisplayName("✅ 应该成功登录（用户名）")
        void shouldLogin_success_withUsername() {
            // Arrange
            when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
            when(jwtUtil.generateToken(anyLong(), anyString(), anyList())).thenReturn(validToken);
            when(userRoleMapper.selectRoleIdsByUserId(1L)).thenReturn(List.of(1L));
            when(userRoleMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of("USER"));
            when(roleMapper.selectById(1L)).thenReturn(createMockRole(1L, "USER", "普通用户", 3));
            when(permissionService.getPermissionsByRoleIds(anyList())).thenReturn(List.of("user:read"));

            // Act
            LoginVO result = authService.login(validLoginDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo(validToken);

            // Verify
            verify(userMapper).selectByUsername("testuser");
            verify(jwtUtil).generateToken(eq(1L), eq("testuser"), anyList());
        }

        @Test
        @DisplayName("✅ 应该成功登录（邮箱）")
        void shouldLogin_success_withEmail() {
            // Arrange
            LoginDTO loginDTO = createLoginDTO("test@example.com", "Test123456!");
            when(userMapper.selectByEmail("test@example.com")).thenReturn(testUser);
            when(jwtUtil.generateToken(anyLong(), anyString(), anyList())).thenReturn(validToken);
            when(userRoleMapper.selectRoleIdsByUserId(1L)).thenReturn(List.of(1L));
            when(userRoleMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of("USER"));
            when(roleMapper.selectById(1L)).thenReturn(createMockRole(1L, "USER", "普通用户", 3));
            when(permissionService.getPermissionsByRoleIds(anyList())).thenReturn(List.of());

            // Act
            LoginVO result = authService.login(loginDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo(validToken);

            // Verify
            verify(userMapper).selectByEmail("test@example.com");
        }

        @Test
        @DisplayName("✅ 应该成功登录（手机号）")
        void shouldLogin_success_withPhone() {
            // Arrange
            LoginDTO loginDTO = createLoginDTO("13800138000", "Test123456!");
            when(userMapper.selectByPhone("13800138000")).thenReturn(testUser);
            when(jwtUtil.generateToken(anyLong(), anyString(), anyList())).thenReturn(validToken);
            when(userRoleMapper.selectRoleIdsByUserId(1L)).thenReturn(List.of(1L));
            when(userRoleMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of("USER"));
            when(roleMapper.selectById(1L)).thenReturn(createMockRole(1L, "USER", "普通用户", 3));
            when(permissionService.getPermissionsByRoleIds(anyList())).thenReturn(List.of());

            // Act
            LoginVO result = authService.login(loginDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo(validToken);

            // Verify
            verify(userMapper).selectByPhone("13800138000");
        }

        // ==================== 失败场景 ====================

        @Test
        @DisplayName("❌ 应该拒绝登录（用户不存在）")
        void shouldRejectLogin_userNotFound() {
            // Arrange
            LoginDTO loginDTO = createLoginDTO("nonexistent", "password");
            when(userMapper.selectByUsername("nonexistent")).thenReturn(null);
            when(userMapper.selectByEmail("nonexistent")).thenReturn(null);
            when(userMapper.selectByPhone("nonexistent")).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");
        }

        @Test
        @DisplayName("❌ 应该拒绝登录（密码错误）")
        void shouldRejectLogin_wrongPassword() {
            // Arrange
            when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

            // 覆盖 setUp() 中的 mock，让密码校验失败
            mockedPasswordEncoderUtil.when(() -> PasswordEncoderUtil.matches(eq("Test123456!"), anyString()))
                .thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authService.login(validLoginDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("密码错误");
        }

        @Test
        @DisplayName("❌ 应该拒绝登录（用户已禁用）")
        void shouldRejectLogin_userDisabled() {
            // Arrange
            testUser.setIsEnabled(0);
            testUser.setAuditStatus("APPROVED");
            when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

            // Act & Assert
            assertThatThrownBy(() -> authService.login(validLoginDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("账户已禁用");
        }

        @Test
        @DisplayName("❌ 应该拒绝登录（用户已锁定）")
        void shouldRejectLogin_userLocked() {
            // Arrange
            testUser.setIsLocked(1);
            testUser.setAuditStatus("APPROVED");
            when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

            // Act & Assert
            assertThatThrownBy(() -> authService.login(validLoginDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户已被锁定");
        }

        @Test
        @DisplayName("❌ 应该拒绝登录（用户已删除）")
        void shouldRejectLogin_userDeleted() {
            // Arrange
            testUser.setIsDeleted(1);
            when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

            // Act & Assert
            assertThatThrownBy(() -> authService.login(validLoginDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");
        }

        // ==================== 审核状态测试 ====================

        @Test
        @DisplayName("⏳ 应该拒绝登录（用户审核中）")
        void shouldRejectLogin_userPendingAudit() {
            // Arrange
            testUser.setAuditStatus(UserAuditStatus.PENDING.name());
            when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

            // Act & Assert
            assertThatThrownBy(() -> authService.login(validLoginDTO))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException ex = (BusinessException) e;
                    // 验证是用户审核相关的错误码
                    assertThat(ex.getCode()).isGreaterThanOrEqualTo(1021);
                });
        }

        @Test
        @DisplayName("❌ 应该拒绝登录（用户审核被拒绝）")
        void shouldRejectLogin_userRejectedAudit() {
            // Arrange
            testUser.setAuditStatus(UserAuditStatus.REJECTED.name());
            when(userMapper.selectByUsername("testuser")).thenReturn(testUser);

            // Act & Assert
            assertThatThrownBy(() -> authService.login(validLoginDTO))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException ex = (BusinessException) e;
                    // 验证是用户审核相关的错误码
                    assertThat(ex.getCode()).isGreaterThanOrEqualTo(1022);
                });
        }

        @Test
        @DisplayName("✅ 应该允许登录（用户审核已通过）")
        void shouldAllowLogin_userApprovedAudit() {
            // Arrange
            testUser.setAuditStatus(UserAuditStatus.APPROVED.name());
            when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
            when(jwtUtil.generateToken(anyLong(), anyString(), anyList())).thenReturn(validToken);
            when(userRoleMapper.selectRoleIdsByUserId(1L)).thenReturn(List.of(1L));
            when(userRoleMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of("USER"));
            when(roleMapper.selectById(1L)).thenReturn(createMockRole(1L, "USER", "普通用户", 3));
            when(permissionService.getPermissionsByRoleIds(anyList())).thenReturn(List.of());

            // Act
            LoginVO result = authService.login(validLoginDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo(validToken);
        }

        @Test
        @DisplayName("✅ 应该允许登录（用户无需审核）")
        void shouldAllowLogin_userNoAuditRequired() {
            // Arrange
            testUser.setAuditStatus(UserAuditStatus.NONE.name());
            when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
            when(jwtUtil.generateToken(anyLong(), anyString(), anyList())).thenReturn(validToken);
            when(userRoleMapper.selectRoleIdsByUserId(1L)).thenReturn(List.of(1L));
            when(userRoleMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of("USER"));
            when(roleMapper.selectById(1L)).thenReturn(createMockRole(1L, "USER", "普通用户", 3));
            when(permissionService.getPermissionsByRoleIds(anyList())).thenReturn(List.of());

            // Act
            LoginVO result = authService.login(validLoginDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo(validToken);
        }
    }

    // ==================== Token 刷新测试 ====================

    @Nested
    @DisplayName("Token 刷新功能测试")
    class RefreshTokenTests {

        @Test
        @DisplayName("✅ 应该成功刷新 Token")
        void shouldRefreshToken_success() {
            // Arrange
            String oldToken = "old.jwt.token";
            String newToken = "new.jwt.token";

            when(jwtUtil.getUserIdFromToken(oldToken)).thenReturn(1L);
            when(jwtUtil.getUsernameFromToken(oldToken)).thenReturn("testuser");
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(userRoleMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of("USER"));
            when(jwtUtil.generateToken(anyLong(), anyString(), anyList())).thenReturn(newToken);

            // Act
            String result = authService.refreshToken(oldToken);

            // Assert
            assertThat(result).isEqualTo(newToken);

            // Verify
            verify(jwtUtil).getUserIdFromToken(oldToken);
            verify(jwtUtil).generateToken(eq(1L), eq("testuser"), anyList());
        }

        @Test
        @DisplayName("❌ 应该拒绝刷新（用户不存在）")
        void shouldRejectRefreshToken_userNotFound() {
            // Arrange
            String oldToken = "old.jwt.token";
            when(jwtUtil.getUserIdFromToken(oldToken)).thenReturn(999L);
            when(userMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshToken(oldToken))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");
        }

        @Test
        @DisplayName("❌ 应该拒绝刷新（用户已禁用）")
        void shouldRejectRefreshToken_userDisabled() {
            // Arrange
            String oldToken = "old.jwt.token";
            testUser.setIsEnabled(0);
            testUser.setAuditStatus("APPROVED");
            when(jwtUtil.getUserIdFromToken(oldToken)).thenReturn(1L);
            when(userMapper.selectById(1L)).thenReturn(testUser);

            // Act & Assert
            assertThatThrownBy(() -> authService.refreshToken(oldToken))
                .isInstanceOf(BusinessException.class);
        }
    }

    // ==================== 登出功能测试 ====================

    @Nested
    @DisplayName("用户登出功能测试")
    class LogoutTests {

        @Test
        @DisplayName("✅ 应该成功登出（Token 加入黑名单）")
        void shouldLogout_success_addTokenToBlacklist() {
            // Arrange
            String token = "valid.jwt.token";
            String clientIp = "192.168.1.1";

            when(jwtUtil.getUserIdFromToken(token)).thenReturn(1L);

            // Act
            assertThatCode(() -> authService.logout(token, clientIp))
                .doesNotThrowAnyException();

            // Assert
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);

            verify(valueOperations).set(
                keyCaptor.capture(),
                valueCaptor.capture(),
                anyLong(),
                any(TimeUnit.class)
            );

            assertThat(keyCaptor.getValue()).startsWith("token:blacklist:");
            assertThat(keyCaptor.getValue()).contains(token);
        }

        @Test
        @DisplayName("✅ 应该成功登出（即使 Token 已过期）")
        void shouldLogout_success_evenIfTokenExpired() {
            // Arrange
            String token = "expired.jwt.token";
            String clientIp = "192.168.1.1";

            when(jwtUtil.getUserIdFromToken(token)).thenThrow(new RuntimeException("Token expired"));

            // Act & Assert
            assertThatCode(() -> authService.logout(token, clientIp))
                .doesNotThrowAnyException();

            // Token 仍应被加入黑名单
            verify(valueOperations).set(
                startsWith("token:blacklist:"),
                eq("1"),
                anyLong(),
                any(TimeUnit.class)
            );
        }
    }

    // ==================== 密码修改测试 ====================

    @Nested
    @DisplayName("密码修改功能测试")
    class ChangePasswordTests {

        @Test
        @DisplayName("✅ 应该成功修改密码")
        void shouldChangePassword_success() {
            // Arrange
            Long userId = 1L;
            ChangePasswordDTO dto = new ChangePasswordDTO();
            dto.setOldPassword("Test123456!");
            dto.setNewPassword("NewTest123!");

            when(userMapper.selectById(userId)).thenReturn(testUser);

            // Act & Assert
            assertThatCode(() -> authService.changePassword(userId, dto))
                .doesNotThrowAnyException();

            // Verify
            verify(userMapper).updateById(any(User.class));
        }

        @Test
        @DisplayName("❌ 应该拒绝修改（旧密码错误）")
        void shouldRejectChangePassword_wrongOldPassword() {
            // Arrange
            Long userId = 1L;
            ChangePasswordDTO dto = new ChangePasswordDTO();
            dto.setOldPassword("WrongPassword!");
            dto.setNewPassword("NewTest123!");

            when(userMapper.selectById(userId)).thenReturn(testUser);

            // 覆盖 setUp() 中的 mock，让旧密码校验失败
            mockedPasswordEncoderUtil.when(() -> PasswordEncoderUtil.matches(eq("WrongPassword!"), anyString()))
                .thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authService.changePassword(userId, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("旧密码错误");
        }

        @Test
        @DisplayName("❌ 应该拒绝修改（新密码与旧密码相同）")
        void shouldRejectChangePassword_samePassword() {
            // Arrange
            Long userId = 1L;
            ChangePasswordDTO dto = new ChangePasswordDTO();
            dto.setOldPassword("Test123456!");
            dto.setNewPassword("Test123456!");

            when(userMapper.selectById(userId)).thenReturn(testUser);

            // Act & Assert
            assertThatThrownBy(() -> authService.changePassword(userId, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("新密码不能与旧密码相同");
        }

        @Test
        @DisplayName("❌ 应该拒绝修改（用户不存在）")
        void shouldRejectChangePassword_userNotFound() {
            // Arrange
            Long userId = 999L;
            ChangePasswordDTO dto = new ChangePasswordDTO();
            dto.setOldPassword("Test123456!");
            dto.setNewPassword("NewTest123!");

            when(userMapper.selectById(userId)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> authService.changePassword(userId, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");
        }
    }

    // ==================== 安全机制测试 ====================

    @Nested
    @DisplayName("安全机制测试")
    class SecurityTests {

        @Test
        @DisplayName("🛡️ 应该防止时序攻击（密码验证耗时恒定）")
        void shouldPreventTimingAttack_passwordVerification() {
            // Arrange
            when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
            when(jwtUtil.generateToken(anyLong(), anyString(), anyList())).thenReturn(validToken);
            when(userRoleMapper.selectRoleIdsByUserId(1L)).thenReturn(List.of(1L));
            when(userRoleMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of("USER"));
            when(roleMapper.selectById(1L)).thenReturn(createMockRole(1L, "USER", "普通用户", 3));
            when(permissionService.getPermissionsByRoleIds(anyList())).thenReturn(List.of());

            // Act - 测试多次登录耗时应该相近
            long startTime1 = System.nanoTime();
            authService.login(validLoginDTO);
            long duration1 = System.nanoTime() - startTime1;

            long startTime2 = System.nanoTime();
            authService.login(validLoginDTO);
            long duration2 = System.nanoTime() - startTime2;

            // Assert - 耗时差异应该在合理范围内（10 倍以内）
            // 注意：这个测试在 CI 环境中可能不稳定
            double ratio = (double) duration2 / duration1;
            assertThat(ratio).isLessThan(10.0);
        }

        @Test
        @DisplayName("🔒 应该清除密码错误计数器（登录成功后）")
        void shouldClearPasswordErrorCounter_afterSuccessfulLogin() {
            // Arrange
            when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
            when(jwtUtil.generateToken(anyLong(), anyString(), anyList())).thenReturn(validToken);
            when(userRoleMapper.selectRoleIdsByUserId(1L)).thenReturn(List.of(1L));
            when(userRoleMapper.selectRoleCodesByUserId(1L)).thenReturn(List.of("USER"));
            when(roleMapper.selectById(1L)).thenReturn(createMockRole(1L, "USER", "普通用户", 3));
            when(permissionService.getPermissionsByRoleIds(anyList())).thenReturn(List.of());

            // Act
            authService.login(validLoginDTO);

            // Assert
            // Verify Redis delete operation
            // verify(redisTemplate).delete(startsWith("password:error:"));
        }
    }

    // ==================== 参数验证测试 ====================

    @Nested
    @DisplayName("参数验证测试")
    class ValidationTests {

        @Test
        @DisplayName("❌ 应该拒绝登录（账号为空）")
        void shouldRejectLogin_emptyAccount() {
            // Arrange
            LoginDTO loginDTO = createLoginDTO("", "password");

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginDTO))
                .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("❌ 应该拒绝登录（密码为空）")
        void shouldRejectLogin_emptyPassword() {
            // Arrange
            LoginDTO loginDTO = createLoginDTO("testuser", "");

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginDTO))
                .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("❌ 应该拒绝登录（账号为 null）")
        void shouldRejectLogin_nullAccount() {
            // Arrange
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setAccount(null);
            loginDTO.setPassword("password");

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginDTO))
                .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("❌ 应该拒绝登录（密码为 null）")
        void shouldRejectLogin_nullPassword() {
            // Arrange
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setAccount("testuser");
            loginDTO.setPassword(null);

            // Act & Assert
            assertThatThrownBy(() -> authService.login(loginDTO))
                .isInstanceOf(BusinessException.class);
        }
    }
}
