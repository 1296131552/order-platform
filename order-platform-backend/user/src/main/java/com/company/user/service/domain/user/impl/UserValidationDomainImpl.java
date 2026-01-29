package com.company.user.service.domain.user.impl;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.company.user.model.entity.User;
import com.company.user.service.basic.UserService;
import com.company.user.service.domain.user.UserValidationDomain;

import jakarta.annotation.Resource;

public class UserValidationDomainImpl implements UserValidationDomain {
    @Resource
    private UserService userService;
    @Resource
    private PasswordEncoder passwordEncoder;
    /**
     * 校验用户是否存在
     * @param username 用户名
     * @return true 存在 false 不存在
     */
    @Override
    public boolean validateUserExists(String username) {
        return userService.validateUserExists(username);
    }

    /**
     * 校验用户ID是否存在
     * @param userId 用户ID
     * @return true 存在 false 不存在
     */
    @Override
    public boolean validateUserExists(Integer userId) {
        return userService.validateUserExists(userId);
    }

    /**
     * 校验用户ID是否匹配
     * @param currentUserId 当前用户ID
     * @param targetUserId 目标用户ID
     * @return true 匹配 false 不匹配
     */
    @Override
    public boolean validateUserIdMatch(Integer currentUserId, Integer targetUserId) {
        return currentUserId.equals(targetUserId);
    }

    /**
     * 校验新密码与确认密码是否匹配
     * @param newPassword 新密码
     * @param confirmPassword 确认密码
     * @return true 匹配 false 不匹配
     */
    @Override
    public boolean validateNewPasswordAndConfirmPassword(String newPassword, String confirmPassword) {
        return newPassword.equals(confirmPassword);
    }

    /**
     * 校验密码是否正确
     * @param currentUserId 当前用户ID
     * @param currentPassword 当前密码
     * @return true 正确 false 错误
     */
    @Override
    public boolean validatePassword(Integer currentUserId, String currentPassword) {
        User user = userService.getById(currentUserId);
        return passwordEncoder.matches(currentPassword, user.getPassword());
    }
}
