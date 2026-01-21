package com.company.order.visual.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.order.visual.user.entity.User;
import com.company.order.visual.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security UserDetailsService 实现
 * <p>
 * 职责：
 * - 根据 userId 加载用户详情
 * - 供 JWT 认证过滤器使用
 * <p>
 * 设计说明（Framework Hack）：
 * Spring Security 的 UserDetailsService 接口方法名为 loadUserByUsername，
 * 但我们的系统使用 userId 作为认证主体（Token 的 subject 存储的是 userId）。
 * 这是一个框架接口与业务设计的妥协，username 参数实际存储的是 userId。
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Framework Hack: username 参数实际是 userId（Token subject）
        Long userId = Long.parseLong(username);
        return loadUserById(userId);
    }

    /**
     * 根据 userId 加载用户详情
     *
     * @param userId 用户 ID
     * @return UserDetails
     * @throws UsernameNotFoundException 用户不存在
     */
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getId, userId)
                        .eq(User::getIsDeleted, false)
        );

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + userId);
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(String.valueOf(user.getId()))
                .password(user.getPassword())
                .disabled(!user.getIsEnabled())
                .accountLocked(user.getIsLocked())
                .accountExpired(false)
                .credentialsExpired(false)
                .roles("USER")
                .build();
    }
}
