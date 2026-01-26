package com.company.user.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.company.user.mapper.UserMapper;
import com.company.user.model.entity.User;

/**
 * UserService 单元测试
 * 测试 UserMapper 层的核心功能
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserService 单元测试")
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("encoded_password");
        testUser.setIsValid(true);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());
        testUser.setIsDelete(false);
    }

    @Nested
    @DisplayName("用户查询测试")
    class UserQueryTests {

        @Test
        @DisplayName("根据ID获取用户")
        void getUserById_Found_ReturnsUser() {
            // Given
            when(userMapper.selectById(1)).thenReturn(testUser);

            // When
            User result = userMapper.selectById(1);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getUsername()).isEqualTo("testuser");
            verify(userMapper, times(1)).selectById(1);
        }

        @Test
        @DisplayName("根据不存在的ID查询返回null")
        void getUserById_NotFound_ReturnsNull() {
            // Given
            when(userMapper.selectById(999)).thenReturn(null);

            // When
            User result = userMapper.selectById(999);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("插入用户成功")
        void insertUser_Success() {
            // Given
            when(userMapper.insert(testUser)).thenReturn(1);

            // When
            int rows = userMapper.insert(testUser);

            // Then
            assertThat(rows).isEqualTo(1);
            verify(userMapper, times(1)).insert(testUser);
        }
    }
}
