package com.company.order.visual.user.converter;

import com.company.order.visual.user.dto.UserRoleResult;
import com.company.order.visual.user.dto.UserVO;
import com.company.order.visual.user.entity.User;
import com.company.order.visual.user.mapper.UserRoleMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户实体与 VO 转换器
 * <p>
 * 职责：消除 Entity → VO 转换的重复代码，统一转换逻辑
 * 解决 N+1 问题：批量转换时使用批量查询
 */
@Component
public class UserConverter {

    private final UserRoleMapper userRoleMapper;

    public UserConverter(UserRoleMapper userRoleMapper) {
        this.userRoleMapper = userRoleMapper;
    }

    // ==================== 单个转换 ====================

    /**
     * User → UserVO（单个转换，自动加载角色）
     */
    public UserVO toVO(User user) {
        List<UserVO.RoleInfo> roles = userRoleMapper.selectRolesByUserId(user.getId());
        return toVO(user, roles);
    }

    /**
     * User → UserVO（单个转换，角色已预加载）
     * <p>
     * userCode 不再从数据库读取，由 VO 的 getUserCode() 方法动态计算
     */
    public UserVO toVO(User user, List<UserVO.RoleInfo> roles) {
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                // userCode 由 getter 动态计算，不再从数据库读取
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .isEnabled(user.getIsEnabled())
                .isLocked(user.getIsLocked())
                .position(user.getPosition())
                .employeeNo(user.getEmployeeNo())
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .loginCount(user.getLoginCount())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .build();
    }

    // ==================== 批量转换（解决 N+1）====================

    /**
     * User List → UserVO List（批量转换，一次查询所有角色）
     * <p>
     * 性能优化：100 个用户从 101 次查询（1+100）降低到 2 次查询（1+1）
     */
    public List<UserVO> toVO(List<User> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }

        // 批量查询所有用户的角色
        List<Long> userIds = users.stream()
                .map(User::getId)
                .toList();

        List<UserRoleResult> roleResults = userRoleMapper.selectRolesByUserIds(userIds);

        // 按 userId 分组
        Map<Long, List<UserVO.RoleInfo>> roleMap = roleResults.stream()
                .collect(Collectors.groupingBy(
                        UserRoleResult::getUserId,
                        Collectors.mapping(
                                UserRoleResult::toRoleInfo,
                                Collectors.toList()
                        )
                ));

        // 转换每个用户
        return users.stream()
                .map(user -> toVO(user, roleMap.getOrDefault(user.getId(), List.of())))
                .toList();
    }
}
