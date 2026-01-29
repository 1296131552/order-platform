package com.company.user.service.basic.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.user.mapper.UserMapper;
import com.company.user.model.entity.User;
import com.company.user.service.basic.UserService;

import jakarta.annotation.Resource;

/**
 * 用户基础服务实现 - 职责单一，无复杂依赖
 * 提供用户基础操作，如创建、查询、更新、删除用户
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private UserMapper userMapper;
    
    /**
     * 检查用户是否存在
     * @param username 用户名
     * @return 如果用户存在则返回true, 否则返回false
     */
    @Override
    public boolean validateUserExists(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return getOne(queryWrapper) != null;
    }

    /**
     * 检查用户是否存在
     * @param userId 用户ID
     * @return 如果用户存在则返回true, 否则返回false
     */
    @Override
    public boolean validateUserExists(Integer userId) {
        return getById(userId) != null;
    }

    /**
     * 创建用户
     * @param username 用户名
     * @param password 密码
     */
    @Override
    public Integer createUser(String username, String password) {
        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        save(user);

        // 返回用户ID
        return user.getId();
    }

    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    @Override
    public User getUser(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return getOne(queryWrapper);
    }

    /**
     * 封禁用户
     * @param userIds 要封禁的用户ID列表
     */
    @Override
    public void banUsers(List<Integer> userIds) {
        // 将用户状态设置为无效
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(User::getId, userIds);
        updateWrapper.set(User::getIsValid, false);
        update(updateWrapper);
    }

    /**
     * 解封用户
     * @param userIds 要解封的用户ID列表
     */
    @Override
    public void unbanUsers(List<Integer> userIds) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(User::getId, userIds);
        updateWrapper.set(User::getIsValid, true);
        update(updateWrapper);
    }

    
    /**
     * 根据创建时间范围查询用户
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户列表
     */
    @Override
    public List<User> listByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return lambdaQuery()
                .between(User::getCreateTime, startTime, endTime)
                .list();
    }

    /**
     * 获取在指定时间范围内创建的用户数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 在指定时间范围内创建的用户数量
     */
    @Override
    public Long getCreatedUserCount(LocalDateTime startTime, LocalDateTime endTime) {
        return lambdaQuery()
                .between(User::getCreateTime, startTime, endTime)
                .count();
    }
}
