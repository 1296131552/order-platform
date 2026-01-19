package com.company.order.visual.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.order.visual.common.exception.BusinessException;
import com.company.order.visual.common.response.ResponseCode;
import com.company.order.visual.common.security.JwtService;
import com.company.order.visual.common.security.RedisKeyConstants;
import com.company.order.visual.common.security.TokenBlacklistService;
import com.company.order.visual.common.security.TokenInfo;
import com.company.order.visual.user.converter.UserConverter;
import com.company.order.visual.user.dto.*;
import com.company.order.visual.user.entity.User;
import com.company.order.visual.user.mapper.UserMapper;
import com.company.order.visual.user.mapper.UserRoleMapper;
import com.company.order.visual.user.service.UserService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
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
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private UserConverter userConverter;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    // ==================== 登录/登出 ====================

    /**
     * 用户登出
     * <p>
     * 流程：解析Token → 加入黑名单 → 移除活跃Token记录
     */
    public void logout(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }

        TokenInfo tokenInfo = jwtService.parseToken(token);
        if (!tokenInfo.isValid()) {
            log.debug("登出时Token无效，无需处理");
            return;
        }

        // 加入黑名单
        tokenBlacklistService.addToBlacklist(tokenInfo);

        // 移除活跃Token记录
        tokenBlacklistService.removeActiveToken(tokenInfo.getUserId(), tokenInfo.getTokenId());

        log.info("用户登出成功, userId={}, tokenId={}", tokenInfo.getUserId(), tokenInfo.getTokenId());
    }

    /**
     * 用户登录
     * <p>
     * 流程：验证用户 → 生成Token → 异步更新登录信息 → 返回
     * <p>
     * 修复说明：移除事务注解，Token生成不依赖数据库操作，避免幽灵Token问题
     */
    public LoginResponse login(LoginRequest request) {
        // 1. 查询用户（支持用户名/邮箱/手机号登录）
        User user = findUserByAccount(request.getAccount());

        // 2. 验证用户状态和密码
        validateUserForLogin(user, request.getPassword());

        // 3. 获取或初始化 Token 版本号
        Long tokenVersion = tokenBlacklistService.getUserTokenVersion(user.getId());
        if (tokenVersion == null) {
            tokenVersion = RedisKeyConstants.INITIAL_TOKEN_VERSION;
            tokenBlacklistService.setUserTokenVersion(user.getId(), tokenVersion);
        } else {
            // 刷新版本号过期时间，防止活跃用户的版本号键过期导致旧Token复活
            tokenBlacklistService.refreshUserTokenVersion(user.getId());
        }

        // 4. 生成 Token（不加事务，在数据库操作之前）
        TokenInfo tokenInfo = jwtService.generateToken(user.getId(), tokenVersion);

        // 5. 记录活跃 Token（用于追踪）
        tokenBlacklistService.addActiveToken(user.getId(), tokenInfo.getTokenId());

        // 6. 异步更新登录信息（不阻塞返回，采用最终一致性）
        updateLoginInfoAsync(user.getId());

        // 7. 构建响应
        UserVO userVO = userConverter.toVO(user);

        return LoginResponse.builder()
                .user(userVO)
                .token(tokenInfo.getRawToken())
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
     * 异步更新用户登录信息
     * <p>
     * 采用最终一致性，不阻塞登录返回
     * 使用@Transactional确保数据一致性
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void updateLoginInfoAsync(Long userId) {
        try {
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setLastLoginTime(LocalDateTime.now());
                user.setLoginCount(user.getLoginCount() + 1);
                userMapper.updateById(user);
                log.debug("异步更新登录信息成功, userId={}", userId);
            }
        } catch (Exception e) {
            log.error("异步更新登录信息失败, userId={}", userId, e);
            // 不抛出异常，避免影响主流程
        }
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
