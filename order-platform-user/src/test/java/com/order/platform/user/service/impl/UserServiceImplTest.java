package com.order.platform.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.order.platform.common.enums.ResponseCode;
import com.order.platform.common.exception.BusinessException;
import com.order.platform.user.dto.request.UserCreateDTO;
import com.order.platform.user.dto.request.UserQueryDTO;
import com.order.platform.user.dto.request.UserUpdateDTO;
import com.order.platform.user.entity.User;
import com.order.platform.user.mapper.UserMapper;
import com.order.platform.user.utils.PasswordEncoderUtil;
import com.order.platform.user.vo.UserPageVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl 单元测试
 *
 * 测试范围：
 * - 用户分页查询
 * - 用户详情查询
 * - 创建用户（含唯一性校验）
 * - 更新用户（含唯一性校验）
 * - 删除用户（含权限校验）
 * - 用户状态管理
 * - 唯一性校验
 *
 * @author 开发组
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    // ==================== 查询操作测试 ====================

    @Nested
    @DisplayName("用户分页查询测试")
    class QueryUserPageTests {

        @Test
        @DisplayName("✅ 应该成功分页查询所有用户")
        void shouldSuccess_queryAllUsers() {
            // Arrange
            UserQueryDTO queryDTO = new UserQueryDTO();
            queryDTO.setCurrent(1L);
            queryDTO.setSize(10L);

            Page<User> mockPage = new Page<>(1L, 10L, 2L);
            User user1 = createMockUser(1L, "zhangsan", "张三");
            user1.setDepartmentId(10L);
            User user2 = createMockUser(2L, "lisi", "李四");
            mockPage.setRecords(List.of(user1, user2));

            when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

            // Act
            Page<UserPageVO> result = userService.queryUserPage(queryDTO);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTotal()).isEqualTo(2);
            assertThat(result.getRecords()).hasSize(2);

            verify(userMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("✅ 应该根据用户名模糊查询")
        void shouldSuccess_queryByUsername() {
            // Arrange
            UserQueryDTO queryDTO = new UserQueryDTO();
            queryDTO.setCurrent(1L);
            queryDTO.setSize(10L);
            queryDTO.setUsername("zhang");

            Page<User> mockPage = new Page<>(1L, 10L, 1L);
            User user = createMockUser(1L, "zhangsan", "张三");
            mockPage.setRecords(List.of(user));

            when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

            // Act
            Page<UserPageVO> result = userService.queryUserPage(queryDTO);

            // Assert
            assertThat(result.getRecords()).hasSize(1);
            assertThat(result.getRecords().get(0).getUsername()).isEqualTo("zhangsan");
        }

        @Test
        @DisplayName("✅ 应该根据部门ID精确查询")
        void shouldSuccess_queryByDepartmentId() {
            // Arrange
            UserQueryDTO queryDTO = new UserQueryDTO();
            queryDTO.setCurrent(1L);
            queryDTO.setSize(10L);
            queryDTO.setDepartmentId(10L);

            Page<User> mockPage = new Page<>(1L, 10L, 1L);
            User user = createMockUser(1L, "zhangsan", "张三");
            user.setDepartmentId(10L);
            mockPage.setRecords(List.of(user));

            when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

            // Act
            Page<UserPageVO> result = userService.queryUserPage(queryDTO);

            // Assert
            assertThat(result.getRecords()).hasSize(1);
            assertThat(result.getRecords().get(0).getDepartmentId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("✅ 应该根据启用状态筛选")
        void shouldSuccess_queryByIsEnabled() {
            // Arrange
            UserQueryDTO queryDTO = new UserQueryDTO();
            queryDTO.setCurrent(1L);
            queryDTO.setSize(10L);
            queryDTO.setIsEnabled(1);

            Page<User> mockPage = new Page<>(1L, 10L, 1L);
            User user = createMockUser(1L, "zhangsan", "张三");
            user.setIsEnabled(1);
            mockPage.setRecords(List.of(user));

            when(userMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

            // Act
            Page<UserPageVO> result = userService.queryUserPage(queryDTO);

            // Assert
            assertThat(result.getRecords()).hasSize(1);
            assertThat(result.getRecords().get(0).getIsEnabled()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("用户详情查询测试")
    class GetUserByIdTests {

        @Test
        @DisplayName("✅ 应该成功查询用户详情")
        void shouldSuccess_getUserById() {
            // Arrange
            Long userId = 1L;
            User user = createMockUser(userId, "zhangsan", "张三");

            when(userMapper.selectById(userId)).thenReturn(user);

            // Act
            UserPageVO result = userService.getUserById(userId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(userId);
            assertThat(result.getUsername()).isEqualTo("zhangsan");
            assertThat(result.getRealName()).isEqualTo("张三");

            verify(userMapper).selectById(userId);
        }

        @Test
        @DisplayName("⚠️ 应该抛出异常当用户不存在")
        void shouldThrowException_whenUserNotFound() {
            // Arrange
            Long userId = 999L;
            when(userMapper.selectById(userId)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");
        }
    }

    // ==================== 创建操作测试 ====================

    @Nested
    @DisplayName("创建用户测试")
    class CreateUserTests {

        @Test
        @DisplayName("✅ 应该成功创建用户")
        void shouldSuccess_createUser() {
            // Arrange
            UserCreateDTO createDTO = new UserCreateDTO();
            createDTO.setUsername("newuser");
            createDTO.setPassword("123456");
            createDTO.setRealName("新用户");
            createDTO.setEmail("new@example.com");
            createDTO.setPhone("13800138000");

            when(userMapper.selectByUsername("newuser")).thenReturn(null);
            when(userMapper.selectByEmail("new@example.com")).thenReturn(null);
            when(userMapper.selectByPhone("13800138000")).thenReturn(null);
            when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return 1;
            });

            // Act
            Long userId = userService.createUser(createDTO, 100L);

            // Assert
            assertThat(userId).isNotNull();
            assertThat(userId).isEqualTo(1L);

            verify(userMapper).selectByUsername("newuser");
            verify(userMapper).selectByEmail("new@example.com");
            verify(userMapper).selectByPhone("13800138000");
            verify(userMapper).insert(any(User.class));
        }

        @Test
        @DisplayName("⚠️ 应该抛出异常当用户名重复")
        void shouldThrowException_whenUsernameDuplicate() {
            // Arrange
            UserCreateDTO createDTO = new UserCreateDTO();
            createDTO.setUsername("existing");
            createDTO.setPassword("123456");

            User existingUser = createMockUser(1L, "existing", "已存在");
            when(userMapper.selectByUsername("existing")).thenReturn(existingUser);

            // Act & Assert
            assertThatThrownBy(() -> userService.createUser(createDTO, 100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名")
                .hasMessageContaining("已存在");
        }

        @Test
        @DisplayName("⚠️ 应该抛出异常当邮箱重复")
        void shouldThrowException_whenEmailDuplicate() {
            // Arrange
            UserCreateDTO createDTO = new UserCreateDTO();
            createDTO.setUsername("newuser");
            createDTO.setPassword("123456");
            createDTO.setEmail("existing@example.com");

            when(userMapper.selectByUsername("newuser")).thenReturn(null);
            User existingUser = createMockUser(1L, "existing", "已存在");
            when(userMapper.selectByEmail("existing@example.com")).thenReturn(existingUser);

            // Act & Assert
            assertThatThrownBy(() -> userService.createUser(createDTO, 100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("邮箱")
                .hasMessageContaining("已存在");
        }

        @Test
        @DisplayName("⚠️ 应该抛出异常当手机号重复")
        void shouldThrowException_whenPhoneDuplicate() {
            // Arrange
            UserCreateDTO createDTO = new UserCreateDTO();
            createDTO.setUsername("newuser");
            createDTO.setPassword("123456");
            createDTO.setPhone("13800138000");

            when(userMapper.selectByUsername("newuser")).thenReturn(null);
            User existingUser = createMockUser(1L, "existing", "已存在");
            when(userMapper.selectByPhone("13800138000")).thenReturn(existingUser);

            // Act & Assert
            assertThatThrownBy(() -> userService.createUser(createDTO, 100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("手机号")
                .hasMessageContaining("已存在");
        }

        @Test
        @DisplayName("✅ 应该设置默认值")
        void shouldSetDefaults_whenCreateUser() {
            // Arrange
            UserCreateDTO createDTO = new UserCreateDTO();
            createDTO.setUsername("newuser");
            createDTO.setPassword("123456");

            when(userMapper.selectByUsername(any())).thenReturn(null);
            when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return 1;
            });

            // Act
            userService.createUser(createDTO, 100L);

            // Assert
            verify(userMapper).insert(argThat(user ->
                user.getIsEnabled() == 1 &&
                user.getIsLocked() == 0 &&
                user.getLoginCount() == 0 &&
                user.getUserCode() != null &&
                user.getCreatedBy() == 100L
            ));
        }
    }

    // ==================== 更新操作测试 ====================

    @Nested
    @DisplayName("更新用户测试")
    class UpdateUserTests {

        @Test
        @DisplayName("✅ 应该成功更新用户")
        void shouldSuccess_updateUser() {
            // Arrange
            Long userId = 1L;
            UserUpdateDTO updateDTO = new UserUpdateDTO();
            updateDTO.setRealName("张三三");
            updateDTO.setEmail("new@example.com");

            User existingUser = createMockUser(userId, "zhangsan", "张三");
            when(userMapper.selectById(userId)).thenReturn(existingUser);
            when(userMapper.selectByEmail("new@example.com")).thenReturn(null);

            // Act
            userService.updateUser(userId, updateDTO, 100L);

            // Assert
            verify(userMapper).updateById(argThat(user ->
                user.getRealName().equals("张三三") &&
                user.getEmail().equals("new@example.com") &&
                user.getUpdatedBy() == 100L
            ));
        }

        @Test
        @DisplayName("⚠️ 应该抛出异常当用户不存在")
        void shouldThrowException_whenUserNotFound() {
            // Arrange
            Long userId = 999L;
            UserUpdateDTO updateDTO = new UserUpdateDTO();
            updateDTO.setRealName("张三三");

            when(userMapper.selectById(userId)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> userService.updateUser(userId, updateDTO, 100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");
        }

        @Test
        @DisplayName("⚠️ 应该抛出异常当邮箱被其他用户使用")
        void shouldThrowException_whenEmailUsedByOther() {
            // Arrange
            Long userId = 1L;
            UserUpdateDTO updateDTO = new UserUpdateDTO();
            updateDTO.setEmail("other@example.com");

            User existingUser = createMockUser(userId, "zhangsan", "张三");
            when(userMapper.selectById(userId)).thenReturn(existingUser);

            User otherUser = createMockUser(2L, "lisi", "李四");
            when(userMapper.selectByEmail("other@example.com")).thenReturn(otherUser);

            // Act & Assert
            assertThatThrownBy(() -> userService.updateUser(userId, updateDTO, 100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("邮箱")
                .hasMessageContaining("已被其他用户使用");
        }

        @Test
        @DisplayName("✅ 应该允许更新自己的邮箱")
        void shouldAllow_updateOwnEmail() {
            // Arrange
            Long userId = 1L;
            UserUpdateDTO updateDTO = new UserUpdateDTO();
            updateDTO.setEmail("own@example.com");

            User existingUser = createMockUser(userId, "zhangsan", "张三");
            existingUser.setEmail("own@example.com");
            when(userMapper.selectById(userId)).thenReturn(existingUser);
            when(userMapper.selectByEmail("own@example.com")).thenReturn(existingUser);

            // Act
            userService.updateUser(userId, updateDTO, 100L);

            // Assert
            verify(userMapper).updateById(any(User.class));
        }
    }

    // ==================== 删除操作测试 ====================

    @Nested
    @DisplayName("删除用户测试")
    class DeleteUserTests {

        @Test
        @DisplayName("✅ 应该成功删除用户")
        void shouldSuccess_deleteUser() {
            // Arrange
            Long userId = 1L;
            Long operatorId = 100L;

            User existingUser = createMockUser(userId, "zhangsan", "张三");
            when(userMapper.selectById(userId)).thenReturn(existingUser);
            when(userMapper.deleteById(userId)).thenReturn(1);

            // Act
            userService.deleteUser(userId, operatorId);

            // Assert
            verify(userMapper).deleteById(userId);
        }

        @Test
        @DisplayName("⚠️ 应该抛出异常当用户不存在")
        void shouldThrowException_whenUserNotFound() {
            // Arrange
            Long userId = 999L;
            when(userMapper.selectById(userId)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> userService.deleteUser(userId, 100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");
        }

        @Test
        @DisplayName("⚠️ 应该抛出异常当删除自己")
        void shouldThrowException_whenDeleteSelf() {
            // Arrange
            Long userId = 100L;
            Long operatorId = 100L;

            User existingUser = createMockUser(userId, "zhangsan", "张三");
            when(userMapper.selectById(userId)).thenReturn(existingUser);

            // Act & Assert
            assertThatThrownBy(() -> userService.deleteUser(userId, operatorId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能删除自己");
        }
    }

    // ==================== 状态管理测试 ====================

    @Nested
    @DisplayName("用户状态管理测试")
    class UpdateUserStatusTests {

        @Test
        @DisplayName("✅ 应该成功启用用户")
        void shouldSuccess_enableUser() {
            // Arrange
            Long userId = 1L;
            Long operatorId = 100L;

            User user = createMockUser(userId, "zhangsan", "张三");
            user.setIsEnabled(0);
            when(userMapper.selectById(userId)).thenReturn(user);

            // Act
            userService.updateUserStatus(userId, 1, operatorId);

            // Assert
            verify(userMapper).updateById(argThat(u ->
                u.getIsEnabled() == 1
            ));
        }

        @Test
        @DisplayName("✅ 应该成功禁用其他用户")
        void shouldSuccess_disableOtherUser() {
            // Arrange
            Long userId = 1L;
            Long operatorId = 100L;

            User user = createMockUser(userId, "zhangsan", "张三");
            when(userMapper.selectById(userId)).thenReturn(user);

            // Act
            userService.updateUserStatus(userId, 0, operatorId);

            // Assert
            verify(userMapper).updateById(argThat(u ->
                u.getIsEnabled() == 0
            ));
        }

        @Test
        @DisplayName("⚠️ 应该抛出异常当禁用自己")
        void shouldThrowException_whenDisableSelf() {
            // Arrange
            Long userId = 100L;
            Long operatorId = 100L;

            User user = createMockUser(userId, "zhangsan", "张三");
            when(userMapper.selectById(userId)).thenReturn(user);

            // Act & Assert
            assertThatThrownBy(() -> userService.updateUserStatus(userId, 0, operatorId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能禁用自己");
        }

        @Test
        @DisplayName("✅ 应该成功锁定用户")
        void shouldSuccess_lockUser() {
            // Arrange
            Long userId = 1L;
            String reason = "违规操作";
            Long operatorId = 100L;

            User user = createMockUser(userId, "zhangsan", "张三");
            when(userMapper.selectById(userId)).thenReturn(user);

            // Act
            userService.lockUser(userId, reason, operatorId);

            // Assert
            verify(userMapper).updateById(argThat(u ->
                u.getIsLocked() == 1 &&
                u.getLockedReason().equals(reason) &&
                u.getLockedTime() != null
            ));
        }

        @Test
        @DisplayName("✅ 应该成功解锁用户")
        void shouldSuccess_unlockUser() {
            // Arrange
            Long userId = 1L;
            Long operatorId = 100L;

            User user = createMockUser(userId, "zhangsan", "张三");
            user.setIsLocked(1);
            user.setLockedReason("违规操作");
            user.setLockedTime(LocalDateTime.now());
            when(userMapper.selectById(userId)).thenReturn(user);

            // Act
            userService.unlockUser(userId, operatorId);

            // Assert
            verify(userMapper).updateById(argThat(u ->
                u.getIsLocked() == 0 &&
                u.getLockedReason() == null &&
                u.getLockedTime() == null
            ));
        }
    }

    // ==================== 唯一性校验测试 ====================

    @Nested
    @DisplayName("唯一性校验测试")
    class UniquenessValidationTests {

        @Test
        @DisplayName("✅ 应该返回true当用户名可用")
        void shouldReturnTrue_whenUsernameAvailable() {
            // Arrange
            String username = "newuser";
            when(userMapper.selectByUsername(username)).thenReturn(null);

            // Act
            boolean result = userService.isUsernameUnique(username);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("✅ 应该返回false当用户名已存在")
        void shouldReturnFalse_whenUsernameExists() {
            // Arrange
            String username = "existing";
            User existingUser = createMockUser(1L, "existing", "已存在");
            when(userMapper.selectByUsername(username)).thenReturn(existingUser);

            // Act
            boolean result = userService.isUsernameUnique(username);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("✅ 应该返回true当邮箱可用")
        void shouldReturnTrue_whenEmailAvailable() {
            // Arrange
            String email = "new@example.com";
            when(userMapper.selectByEmail(email)).thenReturn(null);

            // Act
            boolean result = userService.isEmailUnique(email);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("✅ 应该返回false当邮箱已存在")
        void shouldReturnFalse_whenEmailExists() {
            // Arrange
            String email = "existing@example.com";
            User existingUser = createMockUser(1L, "existing", "已存在");
            when(userMapper.selectByEmail(email)).thenReturn(existingUser);

            // Act
            boolean result = userService.isEmailUnique(email);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("✅ 应该返回true当手机号可用")
        void shouldReturnTrue_whenPhoneAvailable() {
            // Arrange
            String phone = "13800138000";
            when(userMapper.selectByPhone(phone)).thenReturn(null);

            // Act
            boolean result = userService.isPhoneUnique(phone);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("✅ 应该返回false当手机号已存在")
        void shouldReturnFalse_whenPhoneExists() {
            // Arrange
            String phone = "13800138000";
            User existingUser = createMockUser(1L, "existing", "已存在");
            when(userMapper.selectByPhone(phone)).thenReturn(existingUser);

            // Act
            boolean result = userService.isPhoneUnique(phone);

            // Assert
            assertThat(result).isFalse();
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建模拟用户对象
     */
    private User createMockUser(Long id, String username, String realName) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRealName(realName);
        user.setPassword("$2a$10$encoded");
        user.setUserCode("USER" + id);
        user.setEmail(username + "@example.com");
        user.setPhone("13800138000");
        user.setIsEnabled(1);
        user.setIsLocked(0);
        user.setLoginCount(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy(1L);
        return user;
    }
}
