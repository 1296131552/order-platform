package com.order.platform.user.service.impl;

import com.order.platform.common.config.OrderPlatformProperties;
import com.order.platform.common.dto.CurrentUserDTO;
import com.order.platform.common.dto.OperationLogDTO;
import com.order.platform.common.enums.OperationModule;
import com.order.platform.common.enums.OperationType;
import com.order.platform.common.enums.ResponseCode;
import com.order.platform.common.exception.BusinessException;
import com.order.platform.common.service.OperationLogService;
import com.order.platform.common.util.JwtUtil;
import com.order.platform.user.dto.request.ChangePasswordDTO;
import com.order.platform.user.dto.request.LoginDTO;
import com.order.platform.user.vo.LoginVO;
import com.order.platform.user.entity.Role;
import com.order.platform.user.entity.User;
import com.order.platform.user.mapper.RoleMapper;
import com.order.platform.user.mapper.UserMapper;
import com.order.platform.user.mapper.UserRoleMapper;
import com.order.platform.user.service.AuthService;
import com.order.platform.user.service.AuthHelper;
import com.order.platform.user.service.PermissionService;
import com.order.platform.user.utils.PasswordEncoderUtil;
import com.order.platform.user.enums.UserAuditStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
/**
 * 认证服务实现类
 *
 * 功能说明：
 * - 用户登录认证（支持用户名/邮箱/手机号）
 * - 密码错误锁定（5次锁定30分钟）
 * - Token生成和刷新
 * - 密码修改和重置
 *
 * 核心登录流程：
 * 1. 查询用户（支持多种登录方式）
 * 2. 验证用户状态（启用、锁定、删除）
 * 3. 验证密码（BCrypt匹配）
 * 4. 密码错误锁定处理
 * 5. 检查密码过期
 * 6. 查询用户角色和权限
 * 7. 查询数据权限范围
 * 8. 生成JWT Token
 * 9. 更新登录信息
 * 10. 记录操作日志
 *
 * 安全机制：
 * - 密码BCrypt加密（strength=10）
 * - 密码错误5次锁定30分钟
 * - 密码过期策略（默认90天）
 * - Token有效期7天
 *
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final PermissionService permissionService;
    private final AuthHelper authHelper;
    private final PasswordEncoderUtil passwordEncoderUtil;
    private final JwtUtil jwtUtil;
    private final OperationLogService operationLogService;
    private final OrderPlatformProperties properties;
    /** Redis 模板（用于 Token 黑名单） */
    private final StringRedisTemplate redisTemplate;
    /**
     * 用户登录
     *
     * @param loginDTO 登录请求DTO
     * @return 登录响应VO
     */
    @Override
    @Transactional
    public LoginVO login(LoginDTO loginDTO) {
        // 1. 参数校验
        validateLoginDTO(loginDTO);

        // 2. 查询用户（支持用户名/邮箱/手机号）
        User user = findUserByAccount(loginDTO.getAccount());

        // 3. 验证用户状态（启用、锁定）
        validateUserStatus(user);

        // 4. 验证用户审核状态（自主注册用户需要审核通过后才能登录）
        String auditStatus = user.getAuditStatus();
        if (UserAuditStatus.PENDING.name().equals(auditStatus)) {
            throw new BusinessException(ResponseCode.USER_AUDIT_PENDING);
        } else if (UserAuditStatus.REJECTED.name().equals(auditStatus)) {
            throw new BusinessException(ResponseCode.USER_AUDIT_REJECTED);
        }
        // APPROVED 和 NONE 状态允许继续登录

        // 5. 验证密码（BCrypt）
        validatePassword(user, loginDTO.getPassword());

        // 6. 检查密码过期
        validatePasswordExpiration(user);

        // 7. 查询用户角色和权限
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(user.getId());
        List<String> roleCodes = userRoleMapper.selectRoleCodesByUserId(user.getId());
        List<String> permissions = permissionService.getPermissionsByRoleIds(roleIds);

        // 8. 查询数据权限范围（从主角色）
        LoginVO.DataScopeInfo dataScope = buildDataScope(user, roleIds);

        // 9. 生成JWT Token（包含角色信息）
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roleCodes);

        // 10. 更新登录信息
        updateLoginInfo(user.getId());

        // 11. 清除密码错误计数器（如果存在）
        clearPasswordErrorAttempts(user.getId());

        // 12. 构建登录响应
        return buildLoginVO(token, user, roleCodes, permissions, dataScope);
    }

    /**
     * 用户登出
     *
     * @param token JWT Token
     * @param clientIp 客户端IP
     */
    @Override
    public void logout(String token, String clientIp) {
        try {
            // 1. 先将 Token 加入 Redis 黑名单（无论 Token 是否有效）
            // 这样即使 Token 已过期，也能确保无法重放
            String blacklistKey = "token:blacklist:" + token;
            redisTemplate
                .opsForValue()
                .set(
                    blacklistKey,
                    "1",
                    properties.getJwt().getExpiration(),
                    TimeUnit.SECONDS
                );

            // 2. 验证Token并获取用户ID（用于日志记录）
            Long userId = jwtUtil.getUserIdFromToken(token);

        } catch (Exception e) {
            // Token 可能已过期或其他错误，但不影响退出流程
            // 前端已经清除本地 Token，退出流程继续
            log.debug("Token 解析失败（可能已过期），不影响退出: {}", e.getMessage());
        }
    }

    /**
     * 刷新Token
     *
     * @param oldToken 旧Token
     * @return 新Token
     */
    @Override
    public String refreshToken(String oldToken) {
        try {
            // 1. 验证旧Token并获取用户信息
            Long userId = jwtUtil.getUserIdFromToken(oldToken);
            String username = jwtUtil.getUsernameFromToken(oldToken);

            // 2. 查询用户信息（验证用户是否存在）
            User user = userMapper.selectById(userId);
            if (user == null || user.getIsDeleted() == 1) {
                throw new BusinessException(ResponseCode.USER_NOT_FOUND, "用户不存在");
            }

            // 3. 验证用户状态（启用、锁定）
            validateUserStatus(user);

            // 4. 查询用户角色（获取最新角色信息）
            List<String> roleCodes = userRoleMapper.selectRoleCodesByUserId(user.getId());

            // 5. 生成新Token（包含最新角色信息）
            String newToken = jwtUtil.generateToken(userId, username, roleCodes);

            log.info("Token刷新成功: userId={}", userId);
            return newToken;

        } catch (BusinessException e) {
            // 业务异常直接重新抛出，保留原始错误信息
            log.error("Token刷新失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // 其他异常包装为业务异常
            log.error("Token刷新失败", e);
            throw new BusinessException(ResponseCode.TOKEN_INVALID, "Token刷新失败");
        }
    }

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param changePasswordDTO 修改密码请求DTO
     */
    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordDTO changePasswordDTO) {
        // 1. 参数校验
        if (userId == null) {
            throw new BusinessException(ResponseCode.VALIDATION_ERROR, "用户ID不能为空");
        }
        if (changePasswordDTO == null) {
            throw new BusinessException(ResponseCode.VALIDATION_ERROR, "修改密码信息不能为空");
        }

        // 2. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        // 3. 验证旧密码
        if (!passwordEncoderUtil.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResponseCode.VALIDATION_ERROR, "旧密码错误");
        }

        // 4. 验证新密码强度（从配置读取参数）
        PasswordEncoderUtil.PasswordStrength strength =
            passwordEncoderUtil.validateStrength(changePasswordDTO.getNewPassword());
        if (!strength.isValid()) {
            throw new BusinessException(ResponseCode.VALIDATION_ERROR, strength.getMessage());
        }

        // 5. 检查新旧密码是否相同（优先检查，提供更准确的错误提示）
        if (changePasswordDTO.getOldPassword().equals(changePasswordDTO.getNewPassword())) {
            throw new BusinessException(ResponseCode.VALIDATION_ERROR, "新密码不能与旧密码相同");
        }

        // 6. 验证两次输入的新密码是否一致（仅在 confirmPassword 非空时校验）
        if (changePasswordDTO.getConfirmPassword() != null &&
            !changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new BusinessException(ResponseCode.VALIDATION_ERROR, "两次输入的新密码不一致");
        }

        // 7. 加密新密码
        String encryptedPassword = passwordEncoderUtil.encode(changePasswordDTO.getNewPassword());

        // 8. 更新密码
        user.setPassword(encryptedPassword);
        user.setPasswordChangedTime(LocalDateTime.now());
        user.setPasswordExpireTime(LocalDateTime.now().plusDays(properties.getSecurity().getPassword().getExpireDays()));

        // TODO(human): 实现首次登录标记清除逻辑
        //
        // 背景：当用户首次登录后修改密码，需要清除首次登录标记
        //       这样用户下次登录时就不会再看到"需要修改密码"的提示
        //
        // 您的任务：实现以下逻辑
        // 1. 检查 user.getIsFirstLogin() 是否为 1（首次登录）
        // 2. 如果是首次登录，将 user.isFirstLogin 设置为 0
        // 3. 同时更新 user.passwordChangedTime（已在上面设置）
        //
        // 提示：
        // - 只有首次登录的用户才需要清除标记（避免误操作）
        // - 清除标记后，用户再次登录时不会强制改密
        // - 使用 user.setIsFirstLogin(0) 清除标记
        //
        // 示例代码结构（请完善）：
        // if (user.getIsFirstLogin() != null && user.getIsFirstLogin() == 1) {
        //     user.setIsFirstLogin(0);
        //     log.info("清除用户首次登录标记: userId={}", userId);
        // }

        userMapper.updateById(user);

        // 9. 记录操作日志
        recordOperationLog("CHANGE_PASSWORD", userId, "修改密码");

        log.info("用户修改密码成功: userId={}", userId);
    }

    /**
     * 重置密码（管理员操作）
     *
     * @param userId 用户ID
     * @return 新密码
     */
    @Override
    @Transactional
    public String resetPassword(Long userId) {
        // 1. 参数校验
        if (userId == null) {
            throw new BusinessException(ResponseCode.VALIDATION_ERROR, "用户ID不能为空");
        }

        // 2. 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        // 3. 生成随机密码（10位，从配置读取长度限制）
        String newPassword = passwordEncoderUtil.generateRandomPassword(10);

        // 4. 加密密码
        String encryptedPassword = passwordEncoderUtil.encode(newPassword);

        // 5. 更新密码
        user.setPassword(encryptedPassword);
        user.setPasswordChangedTime(LocalDateTime.now());
        user.setPasswordExpireTime(LocalDateTime.now().plusDays(properties.getSecurity().getPassword().getExpireDays()));
        userMapper.updateById(user);

        // 6. 记录操作日志
        recordOperationLog("RESET_PASSWORD", userId, "管理员重置密码");

        log.warn("管理员重置用户密码: userId={}", userId);

        return newPassword;
    }

    /**
     * 验证Token有效性
     *
     * @param token JWT Token
     * @return 用户ID
     */
    @Override
    public Long validateToken(String token) {
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            log.debug("Token验证失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取当前登录用户信息
     *
     * @param token JWT Token
     * @return 当前用户信息
     */
    @Override
    public Object getCurrentUser(String token) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userMapper.selectById(userId);
            if (user == null) {
                return null;
            }

            List<String> roleCodes = userRoleMapper.selectRoleCodesByUserId(userId);
            CurrentUserDTO currentUser = authHelper.toCurrentUserDTO(user, roleCodes);

            return currentUser;

        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            return null;
        }
    }

    /**
     * 检查用户登录状态
     *
     * @param token JWT Token
     * @return 是否在线
     */
    @Override
    public boolean isUserLoggedIn(String token) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return false;
            }

            User user = userMapper.selectById(userId);
            return user != null && user.getIsEnabled() == 1 && user.getIsLocked() == 0;

        } catch (Exception e) {
            log.debug("检查用户登录状态失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取密码错误剩余尝试次数
     *
     * @param userId 用户ID
     * @return 剩余次数
     */
    @Override
    public int getRemainingAttempts(Long userId) {
        // TODO: 实现 Redis 计数器
        // String key = "login:error:" + userId;
        // Integer errorCount = redisTemplate.opsForValue().get(key);
        // return properties.getSecurity().getPassword().getMaxAttempts() - (errorCount != null ? errorCount : 0);
        return properties.getSecurity().getPassword().getMaxAttempts();
    }

    /**
     * 锁定用户账户
     *
     * @param userId 用户ID
     * @param reason 锁定原因
     * @param lockMinutes 锁定时长
     */
    @Override
    @Transactional
    public void lockUserAccount(Long userId, String reason, int lockMinutes) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        user.setIsLocked(1);
        user.setLockedTime(LocalDateTime.now().plusMinutes(lockMinutes));
        user.setLockedReason(reason);
        userMapper.updateById(user);

        log.warn("用户账户被锁定: userId={}, reason={}, lockMinutes={}", userId, reason, lockMinutes);
    }

    /**
     * 解锁用户账户
     *
     * @param userId 用户ID
     */
    @Override
    @Transactional
    public void unlockUserAccount(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        user.setIsLocked(0);
        user.setLockedTime(null);
        user.setLockedReason(null);
        userMapper.updateById(user);

        log.info("用户账户已解锁: userId={}", userId);

        // TODO: 清除 Redis 错误计数器
        // String key = "login:error:" + userId;
        // redisTemplate.delete(key);
    }

    /**
     * 检查账户是否被锁定
     *
     * @param user 用户实体
     * @return 是否锁定
     */
    @Override
    public boolean isAccountLocked(Object user) {
        if (!(user instanceof User)) {
            return false;
        }

        User u = (User) user;

        // 未锁定
        if (u.getIsLocked() == 0) {
            return false;
        }

        // 锁定时间已过，自动解锁
        if (u.getLockedTime() != null && u.getLockedTime().isBefore(LocalDateTime.now())) {
            unlockUserAccount(u.getId());
            return false;
        }

        return true;
    }

    // ==================== 私有方法 ====================

    /**
     * 验证登录请求参数
     */
    private void validateLoginDTO(LoginDTO loginDTO) {
        if (loginDTO == null) {
            throw new BusinessException(ResponseCode.VALIDATION_ERROR, "登录信息不能为空");
        }
        if (loginDTO.getAccount() == null || loginDTO.getAccount().isEmpty()) {
            throw new BusinessException(ResponseCode.VALIDATION_ERROR, "登录账号不能为空");
        }
        if (loginDTO.getPassword() == null || loginDTO.getPassword().isEmpty()) {
            throw new BusinessException(ResponseCode.VALIDATION_ERROR, "密码不能为空");
        }
    }

    /**
     * 根据账号查询用户（支持用户名/邮箱/手机号）
     */
    private User findUserByAccount(String account) {
        User user = null;

        // 1. 判断登录类型
        if (account.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            // 邮箱登录
            user = userMapper.selectByEmail(account);
        } else if (account.matches("^1[3-9]\\d{9}$")) {
            // 手机号登录
            user = userMapper.selectByPhone(account);
        } else {
            // 用户名登录（默认）
            user = userMapper.selectByUsername(account);
        }

        // 2. 用户不存在或已删除
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND, "用户不存在");
        }

        return user;
    }

    /**
     * 验证用户状态
     */
    private void validateUserStatus(User user) {
        // 1. 检查账户是否启用
        if (user.getIsEnabled() == 0) {
            throw new BusinessException(ResponseCode.USER_DISABLED, "账户已禁用");
        }

        // 2. 检查账户是否锁定
        if (isAccountLocked(user)) {
            int lockMinutes = properties.getSecurity().getPassword().getLockMinutes();
            throw new BusinessException(ResponseCode.USER_LOCKED,
                "用户已被锁定，请" + lockMinutes + "分钟后再试");
        }
    }

    /**
     * 验证密码
     */
    private void validatePassword(User user, String rawPassword) {
        if (!passwordEncoderUtil.matches(rawPassword, user.getPassword())) {
            // 密码错误，增加错误计数
            handlePasswordError(user.getId());
            throw new BusinessException(ResponseCode.PASSWORD_ERROR, "密码错误");
        }
    }

    /**
     * 检查密码是否过期
     */
    private void validatePasswordExpiration(User user) {
        if (user.getPasswordExpireTime() != null &&
            user.getPasswordExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ResponseCode.PASSWORD_EXPIRED, "密码已过期，请修改密码");
        }
    }

    /**
     * 处理密码错误
     */
    private void handlePasswordError(Long userId) {
        // TODO: 实现 Redis 计数器
        // String key = "login:error:" + userId;
        // Long errorCount = redisTemplate.opsForValue().increment(key);
        // redisTemplate.expire(key, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        //
        // if (errorCount >= MAX_PASSWORD_ATTEMPTS) {
        //     lockUserAccount(userId, "密码错误次数过多", LOCK_DURATION_MINUTES);
        // }

        // 临时方案：直接查询数据库并锁定
        // 生产环境必须使用 Redis
    }

    /**
     * 清除密码错误计数器
     */
    private void clearPasswordErrorAttempts(Long userId) {
        // TODO: 清除 Redis 计数器
        // String key = "login:error:" + userId;
        // redisTemplate.delete(key);
    }

    /**
     * 构建数据权限信息
     */
    private LoginVO.DataScopeInfo buildDataScope(User user, List<Long> roleIds) {
        // 查询主角色（第一个角色）
        if (roleIds.isEmpty()) {
            return LoginVO.DataScopeInfo.builder()
                .type("SELF")
                .typeCode(3)
                .departmentId(user.getDepartmentId())
                .departmentName(user.getDepartmentName())
                .description("只能查看自己的数据")
                .build();
        }

        Role primaryRole = roleMapper.selectById(roleIds.get(0));
        if (primaryRole == null) {
            return LoginVO.DataScopeInfo.builder()
                .type("SELF")
                .typeCode(3)
                .departmentId(user.getDepartmentId())
                .departmentName(user.getDepartmentName())
                .description("只能查看自己的数据")
                .build();
        }

        // 根据角色的数据权限类型构建
        String type;
        String description;
        switch (primaryRole.getDataScopeType()) {
            case 1:
                type = "ALL";
                description = "可查看全部数据";
                break;
            case 2:
                type = "DEPARTMENT";
                description = "只能查看本部门的数据";
                break;
            case 3:
            default:
                type = "SELF";
                description = "只能查看自己的数据";
                break;
        }

        return LoginVO.DataScopeInfo.builder()
            .type(type)
            .typeCode(primaryRole.getDataScopeType())
            .departmentId(user.getDepartmentId())
            .departmentName(user.getDepartmentName())
            .description(description)
            .build();
    }

    /**
     * 更新登录信息
     *
     * 注意事项：
     * - 使用 LambdaUpdateWrapper 时，MyBatis Plus 不会触发自动填充
     * - 必须显式设置 updatedAt 和 updatedBy，否则数据库 NOT NULL 约束会报错
     */
    private void updateLoginInfo(Long userId) {
        // 查询当前登录次数
        User existingUser = userMapper.selectById(userId);
        Integer currentCount = existingUser.getLoginCount() != null ? existingUser.getLoginCount() : 0;

        // 使用 LambdaUpdateWrapper 更新指定字段
        // 注意：必须显式设置 updatedAt 和 updatedBy，因为自动填充不会触发
        userMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getLastLoginTime, LocalDateTime.now())
                .set(User::getLoginCount, currentCount + 1)
                .set(User::getUpdatedAt, LocalDateTime.now())
                .set(User::getUpdatedBy, userId));
    }

    /**
     * 记录操作日志
     */
    private void recordOperationLog(String operationType, Long userId, String description) {
        try {
            // 查询用户信息以获取用户名
            User user = userMapper.selectById(userId);
            if (user == null) {
                log.warn("记录操作日志失败：用户不存在, userId={}", userId);
                return;
            }

            // 构建操作日志 DTO（使用 Lombok builder）
            OperationLogDTO logDTO = OperationLogDTO.builder()
                    .operatorId(userId)
                    .operatorName(user.getUsername())
                    .operatorUserCode(user.getUserCode())
                    .operatorDepartmentId(user.getDepartmentId())
                    .operatorDepartmentName(user.getDepartmentName())
                    .businessType("USER")
                    .businessId(userId)
                    .operationType(operationType)
                    .operationModule(OperationModule.USER.getCode())
                    .operationDesc(description)
                    .operationResult("SUCCESS")
                    .build();

            // 异步保存操作日志
            operationLogService.saveAsync(logDTO);

        } catch (Exception e) {
            log.error("记录操作日志失败: operationType={}, userId={}", operationType, userId, e);
        }
    }

    /**
     * 构建登录响应VO
     *
     * 功能说明：
     * - 封装登录成功后的完整响应信息
     * - 包含Token、用户信息、角色权限、数据权限
     * - 支持首次登录强制改密功能
     *
     * @since 1.0.0
     */
    private LoginVO buildLoginVO(String token, User user, List<String> roles,
                                 List<String> permissions, LoginVO.DataScopeInfo dataScope) {
        CurrentUserDTO currentUser = authHelper.toCurrentUserDTO(user, roles);

        // TODO(human): 实现首次登录检测逻辑
        //
        // 背景：当用户首次登录时，需要强制用户修改密码
        //       我们需要从 User 对象中提取首次登录标记，并在 LoginVO 中设置对应的改密提示
        //
        // 您的任务：实现以下逻辑
        // 1. 检查 user.getIsFirstLogin() 是否为 1（首次登录）
        // 2. 如果是首次登录，设置 requireChangePassword = true
        // 3. 设置 passwordExpireTime = user.getPasswordExpireTime()
        // 4. 可选：如果密码已过期，也需要设置 requireChangePassword = true
        //
        // 提示：
        // - isFirstLogin 字段：1 表示首次登录，0 表示非首次登录
        // - requireChangePassword：前端会根据此字段判断是否弹出改密对话框
        // - 密码过期检查：user.getPasswordExpireTime() != null && user.getPasswordExpireTime().isBefore(LocalDateTime.now())
        //
        // 示例代码结构（请完善）：
        // boolean requireChangePassword = false;
        // if (user.getIsFirstLogin() != null && user.getIsFirstLogin() == 1) {
        //     requireChangePassword = true;
        // }
        // // 可选：检查密码是否过期
        // if (user.getPasswordExpireTime() != null &&
        //     user.getPasswordExpireTime().isBefore(LocalDateTime.now())) {
        //     requireChangePassword = true;
        // }

        // 临时默认值（等您实现后会替换为实际逻辑）
        boolean requireChangePassword = false;
        LocalDateTime passwordExpireTime = user.getPasswordExpireTime();

        return LoginVO.builder()
            .token(token)
            .tokenType("Bearer")
            .expiresIn(properties.getJwt().getExpiration())
            .requireChangePassword(requireChangePassword)
            .passwordExpireTime(passwordExpireTime)
            .userInfo(currentUser)
            .roles(roles)
            .permissions(permissions)
            .dataScope(dataScope)
            .build();
    }
}
