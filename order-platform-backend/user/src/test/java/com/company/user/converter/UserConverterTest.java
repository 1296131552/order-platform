package com.company.user.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.company.user.model.dto.AddUserDTO;
import com.company.user.model.dto.ModifyUserDTO;
import com.company.user.model.dto.UpdateUserDTO;
import com.company.user.model.entity.Role;
import com.company.user.model.entity.User;
import com.company.user.model.entity.UserDetail;
import com.company.user.model.vo.UserVO;

/**
 * UserConverter 单元测试
 */
@DisplayName("UserConverter 单元测试")
class UserConverterTest {

    private UserConverter userConverter;

    private User testUser;
    private UserDetail testUserDetail;
    private List<Role> testRoles;

    @BeforeEach
    void setUp() {
        userConverter = UserConverter.INSTANCE;

        // 初始化测试数据
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("encoded_password");
        testUser.setIsValid(true);
        testUser.setCreateTime(LocalDateTime.of(2024, 1, 1, 0, 0));
        testUser.setUpdateTime(LocalDateTime.of(2024, 1, 1, 0, 0));
        testUser.setIsDelete(false);

        testUserDetail = new UserDetail();
        testUserDetail.setId(1);
        testUserDetail.setName("Test User");
        testUserDetail.setAvatarUrl("http://example.com/avatar.jpg");
        testUserDetail.setSignature("Hello, World!");
        testUserDetail.setCreateTime(LocalDateTime.of(2024, 1, 1, 0, 0));
        testUserDetail.setUpdateTime(LocalDateTime.of(2024, 1, 2, 0, 0));
        testUserDetail.setIsDelete(false);

        Role role1 = new Role();
        role1.setId(1);
        role1.setName("管理员");
        role1.setDescription("系统管理员");
        role1.setParentNodeId(0);
        role1.setLevel(0);

        Role role2 = new Role();
        role2.setId(2);
        role2.setName("普通用户");
        role2.setDescription("普通用户角色");
        role2.setParentNodeId(0);
        role2.setLevel(0);

        testRoles = Arrays.asList(role1, role2);
    }

    @Nested
    @DisplayName("AddUserDTO 转 UserDetail 测试")
    class AddUserDTOToDetailTests {

        @Test
        @DisplayName("转换成功")
        void addUserDTOToDetail_Success() {
            // Given
            AddUserDTO addUserDTO = new AddUserDTO();
            addUserDTO.setName("testuser");
            addUserDTO.setPassword("password123");
            addUserDTO.setRoleIds(Arrays.asList(1, 2));

            // When
            UserDetail result = userConverter.addUserDTOToDetail(addUserDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("testuser");
        }
    }

    @Nested
    @DisplayName("实体转 UserVO 测试")
    class EntityToUserVOTests {

        @Test
        @DisplayName("转换 User 和 UserDetail 为 UserVO 成功")
        void toUserVO_Success() {
            // When
            UserVO result = userConverter.toUserVO(testUser, testUserDetail, testRoles);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testUser.getId());
            assertThat(result.getUsername()).isEqualTo(testUser.getUsername());
            assertThat(result.getName()).isEqualTo(testUserDetail.getName());
            assertThat(result.getAvatarUrl()).isEqualTo(testUserDetail.getAvatarUrl());
            assertThat(result.getSignature()).isEqualTo(testUserDetail.getSignature());
            assertThat(result.getCreateTime()).isEqualTo(testUserDetail.getCreateTime());
            assertThat(result.getUpdateTime()).isEqualTo(testUserDetail.getUpdateTime());
        }

        @Test
        @DisplayName("UserDetail 为 null 时的转换")
        void toUserVO_NullDetail_Success() {
            // When
            UserVO result = userConverter.toUserVO(testUser, null, Collections.emptyList());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testUser.getId());
            assertThat(result.getUsername()).isEqualTo(testUser.getUsername());
        }

        @Test
        @DisplayName("Role 列表为空时的转换")
        void toUserVO_EmptyRoles_Success() {
            // When
            UserVO result = userConverter.toUserVO(testUser, testUserDetail, Collections.emptyList());

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testUser.getId());
        }
    }

    @Nested
    @DisplayName("批量转换测试")
    class BatchConversionTests {

        @Test
        @DisplayName("转换 User 列表和 UserDetail 列表为 UserVO 列表")
        void toUserVOS_Success() {
            // Given
            User user2 = new User();
            user2.setId(2);
            user2.setUsername("user2");

            UserDetail detail2 = new UserDetail();
            detail2.setId(2);
            detail2.setName("User Two");

            List<User> users = Arrays.asList(testUser, user2);
            List<UserDetail> userDetails = Arrays.asList(testUserDetail, detail2);

            // When
            List<UserVO> result = userConverter.toUserVOS(users, userDetails);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1);
            assertThat(result.get(0).getUsername()).isEqualTo("testuser");
            assertThat(result.get(1).getId()).isEqualTo(2);
            assertThat(result.get(1).getUsername()).isEqualTo("user2");
        }

        @Test
        @DisplayName("User 列表为空时返回空列表")
        void toUserVOS_EmptyUsers_ReturnsEmptyList() {
            // When
            List<UserVO> result = userConverter.toUserVOS(Collections.emptyList(), Collections.emptyList());

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("UserDetail 列表为空时返回空列表")
        void toUserVOS_EmptyDetails_ReturnsEmptyList() {
            // Given
            List<User> users = Arrays.asList(testUser);

            // When
            List<UserVO> result = userConverter.toUserVOS(users, Collections.emptyList());

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("UserDetail 不匹配对应 User 时使用 null")
        void toUserVOS_UnmatchedDetail_UsesNull() {
            // Given
            User user2 = new User();
            user2.setId(999); // 不存在的 ID
            user2.setUsername("ghostuser");

            List<User> users = Arrays.asList(user2);
            List<UserDetail> userDetails = Arrays.asList(testUserDetail);

            // When
            List<UserVO> result = userConverter.toUserVOS(users, userDetails);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(999);
            assertThat(result.get(0).getUsername()).isEqualTo("ghostuser");
        }
    }
}
