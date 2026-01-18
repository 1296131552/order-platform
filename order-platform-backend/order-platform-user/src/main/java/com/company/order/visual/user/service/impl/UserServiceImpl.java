package com.company.order.visual.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.order.visual.common.exception.BusinessException;
import com.company.order.visual.common.response.ResponseCode;
import com.company.order.visual.user.converter.UserConverter;
import com.company.order.visual.user.dto.*;
import com.company.order.visual.user.entity.User;
import com.company.order.visual.user.mapper.UserMapper;
import com.company.order.visual.user.mapper.UserRoleMapper;
import com.company.order.visual.user.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现
 * <p>
 * 职责：
 * - 用户认证与登录
 * - 用户查询与分页
 * - 登录信息更新
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private UserConverter userConverter;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ==================== 登录 ====================

    /**
     * 用户登录
     * <p>
     * 流程：查询用户 → 验证状态与密码 → 更新登录信息 → 返回用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request) {
        // 1. 查询用户（支持用户名/邮箱/手机号登录）
        User user = findUserByAccount(request.getAccount());

        // 2. 验证用户状态和密码
        validateUserForLogin(user, request.getPassword());

        // 3. 更新登录信息
        updateLoginInfo(user);

        // 4. 构建响应（使用转换器，消除重复代码）
        UserVO userVO = userConverter.toVO(user);

        return LoginResponse.builder()
                .user(userVO)
                .token(null)  // TODO: 集成 JWT 后生成 token
                .build();
    }

    /**
     * 按账号查询用户（支持用户名/邮箱/手机号）
     */
    private User findUserByAccount(String account) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getIsDeleted, false)
                .and(w -> w.eq(User::getUsername, account)
                        .or().eq(User::getEmail, account)
                        .or().eq(User::getPhone, account));

        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException(ResponseCode.LOGIN_FAILED);
        }
        return user;
    }

    /**
     * 验证用户登录状态和密码
     * <p>
     * 拆分职责：单独的验证方法便于测试和复用
     */
    private void validateUserForLogin(User user, String rawPassword) {
        if (!user.getIsEnabled()) {
            throw new BusinessException(ResponseCode.USER_DISABLED);
        }
        if (user.getIsLocked()) {
            throw new BusinessException(ResponseCode.USER_LOCKED);
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BusinessException(ResponseCode.LOGIN_FAILED);
        }
    }

    /**
     * 更新用户登录信息
     */
    private void updateLoginInfo(User user) {
        user.setLastLoginTime(LocalDateTime.now());
        user.setLoginCount(user.getLoginCount() + 1);
        userMapper.updateById(user);
    }

    // ==================== 创建用户 ====================
    // TODO: 实现创建用户功能

    // ==================== 更新/删除 ====================
    // TODO: 实现更新、删除功能

    // ==================== 查询 ====================

    @Override
    public UserVO getUserById(Long userId) {
        User user = getUserByIdOrThrow(userId);
        return userConverter.toVO(user);
    }

    @Override
    public Page<UserVO> pageUsers(UserQueryRequest request) {
        // 1. 构建查询条件
        LambdaQueryWrapper<User> wrapper = buildQueryWrapper(request);

        // 2. 分页查询
        Page<User> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<User> userPage = userMapper.selectPage(page, wrapper);

        // 3. 批量转换（解决 N+1 问题）
        List<UserVO> vos = userConverter.toVO(userPage.getRecords());

        // 4. 构建返回结果
        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(vos);
        return voPage;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<User> buildQueryWrapper(UserQueryRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getIsDeleted, false);

        // ==================== 账号信息 ====================
        if (StringUtils.hasText(request.getUsername())) {
            wrapper.like(User::getUsername, request.getUsername());
        }
        if (StringUtils.hasText(request.getUserCode())) {
            wrapper.like(User::getUserCode, request.getUserCode());
        }
        if (StringUtils.hasText(request.getEmail())) {
            wrapper.like(User::getEmail, request.getEmail());
        }
        if (StringUtils.hasText(request.getPhone())) {
            wrapper.like(User::getPhone, request.getPhone());
        }

        // ==================== 基本信息 ====================
        if (StringUtils.hasText(request.getRealName())) {
            wrapper.like(User::getRealName, request.getRealName());
        }
        if (StringUtils.hasText(request.getPosition())) {
            wrapper.like(User::getPosition, request.getPosition());
        }
        if (StringUtils.hasText(request.getEmployeeNo())) {
            wrapper.like(User::getEmployeeNo, request.getEmployeeNo());
        }

        // ==================== 状态筛选 ====================
        if (request.getIsEnabled() != null) {
            wrapper.eq(User::getIsEnabled, request.getIsEnabled());
        }
        if (request.getIsLocked() != null) {
            wrapper.eq(User::getIsLocked, request.getIsLocked());
        }

        // ==================== 组织与权限 ====================
        if (request.getDepartmentId() != null) {
            wrapper.eq(User::getDepartmentId, request.getDepartmentId());
        }

        // ==================== 时间范围 ====================
        if (request.getCreatedAtStart() != null) {
            wrapper.ge(User::getCreatedAt, request.getCreatedAtStart());
        }
        if (request.getCreatedAtEnd() != null) {
            wrapper.le(User::getCreatedAt, request.getCreatedAtEnd());
        }
        if (request.getLastLoginTimeStart() != null) {
            wrapper.ge(User::getLastLoginTime, request.getLastLoginTimeStart());
        }
        if (request.getLastLoginTimeEnd() != null) {
            wrapper.le(User::getLastLoginTime, request.getLastLoginTimeEnd());
        }

        // 默认按创建时间倒序
        wrapper.orderByDesc(User::getCreatedAt);
        return wrapper;
    }

    /**
     * 根据 ID 获取用户，不存在或已删除时抛出异常
     * <p>
     * 修复 NPE 风险：先判断 null，再判断 isDeleted
     */
    private User getUserByIdOrThrow(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }
        return user;
    }
}
