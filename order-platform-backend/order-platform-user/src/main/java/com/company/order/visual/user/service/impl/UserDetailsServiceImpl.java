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
 * - 根据 username（实际是 userId）加载用户详情
 * - 供 JWT 认证过滤器使用
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username 实际存储的是 userId
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
