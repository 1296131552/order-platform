package com.company.user.service.domain.auth.impl;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.company.common.enums.result.AuthResultCode;
import com.company.user.model.entity.User;
import com.company.user.service.basic.UserRoleService;
import com.company.user.service.basic.UserService;

import jakarta.annotation.Resource;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private UserService userService;
    @Resource
    private UserRoleService userRoleService;
    // @Resource
    // private permissionBkQueryDomain permissionBkQueryDomain;

    /**
     * 根据用户名加载用户详情
     * @param username 用户名
     * @return 用户详情
     * @throws UsernameNotFoundException 用户名不存在异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户
        User user = userService.getUser(username);

        if (user == null) {
            throw new UsernameNotFoundException(AuthResultCode.USERNAME_NOT_EXISTS.getMessage());
        }

        // 获取当前角色的权限
        List<Integer> roleIds = userRoleService.getRoleIds(user.getId());

        // 获取用户权限码列表 
        // List<String> permissionCodes = permissionBkQueryDomain.getPermissionCode(roleIds);

        // 转换为Spring Security的Authority 
        // List<SimpleGrantedAuthority> authorities = permissionCodes.stream()
        //         .map(SimpleGrantedAuthority::new)
        //         .toList();

        // 返回UserDetails对象
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getIsValid(), // enabled - 账户是否启用
                true,   // accountNonExpired - 账户是否未过期
                true,   // credentialsNonExpired - 凭证是否未过期
                true,  // accountNonLocked - 账户是否未被锁定
                null
        );
    }
}