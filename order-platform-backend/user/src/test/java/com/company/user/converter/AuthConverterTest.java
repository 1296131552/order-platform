package com.company.user.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.company.user.model.dto.AddUserDTO;
import com.company.user.model.dto.LoginDTO;
import com.company.user.model.dto.RegisterDTO;

/**
 * AuthConverter 单元测试
 */
@DisplayName("AuthConverter 单元测试")
class AuthConverterTest {

    private AuthConverter authConverter;

    @BeforeEach
    void setUp() {
        // 使用 Mappers.getMapper 获取实例
        authConverter = AuthConverter.INSTANCE;
    }

    @Nested
    @DisplayName("RegisterDTO 转 AddUserDTO 测试")
    class RegisterToAddUserTests {

        @Test
        @DisplayName("转换成功 - 包含默认角色")
        void toAddUserDTO_WithDefaultRoles_Success() {
            // Given
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUsername("testuser");
            registerDTO.setPassword("password123");
            registerDTO.setConfirmPassword("password123");

            List<Integer> defaultRoleIds = Arrays.asList(1, 2);

            // When
            AddUserDTO result = authConverter.toAddUserDTO(registerDTO, defaultRoleIds);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("testuser"); // username 映射到 name
            assertThat(result.getRoleIds()).isEqualTo(defaultRoleIds);
            assertThat(result.getPassword()).isEqualTo("password123");
        }

        @Test
        @DisplayName("转换成功 - 空角色列表")
        void toAddUserDTO_EmptyRoles_Success() {
            // Given
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUsername("testuser");
            registerDTO.setPassword("password123");
            registerDTO.setConfirmPassword("password123");

            List<Integer> emptyRoleIds = Arrays.asList();

            // When
            AddUserDTO result = authConverter.toAddUserDTO(registerDTO, emptyRoleIds);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("testuser");
            assertThat(result.getRoleIds()).isEmpty();
        }
    }

    @Nested
    @DisplayName("RegisterDTO 转 LoginDTO 测试")
    class RegisterToLoginTests {

        @Test
        @DisplayName("转换成功")
        void toLoginDTO_Success() {
            // Given
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUsername("testuser");
            registerDTO.setPassword("password123");
            registerDTO.setConfirmPassword("password123");

            // When
            LoginDTO result = authConverter.toLoginDTO(registerDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getPassword()).isEqualTo("password123");
        }

        @Test
        @DisplayName("转换时密码保持一致")
        void toLoginDTO_PasswordMatches() {
            // Given
            String expectedPassword = "mySecretPassword123";
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUsername("testuser");
            registerDTO.setPassword(expectedPassword);
            registerDTO.setConfirmPassword(expectedPassword);

            // When
            LoginDTO result = authConverter.toLoginDTO(registerDTO);

            // Then
            assertThat(result.getPassword()).isEqualTo(expectedPassword);
        }
    }
}
