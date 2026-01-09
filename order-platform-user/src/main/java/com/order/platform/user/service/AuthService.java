package com.order.platform.user.service;

import com.order.platform.user.dto.request.ChangePasswordDTO;
import com.order.platform.user.dto.request.LoginDTO;
import com.order.platform.user.vo.LoginVO;

/**
 * 认证服务接口
 *
 * 功能说明：
 * - 用户登录认证（支持用户名/邮箱/手机号）
 * - 用户登出（清除Token，记录日志）
 * - Token刷新（无感刷新）
 * - 修改密码
 * - 密码重置
 * - 在线用户管理
 *
 * 登录流程：
 * 1. 查询用户（支持多种登录方式）
 * 2. 验证用户状态（启用、锁定、删除）
 * 3. 验证密码（BCrypt匹配）
 * 4. 密码错误锁定（5次锁定30分钟）
 * 5. 查询用户角色和权限
 * 6. 查询数据权限范围
 * 7. 生成JWT Token
 * 8. 更新登录信息（次数、时间、IP）
 * 9. 记录操作日志
 *
 * 安全机制：
 * - 密码BCrypt加密（strength=10）
 * - 密码错误5次锁定30分钟（Redis）
 * - 密码过期策略（默认90天）
 * - Token有效期7天
 * - 登录日志完整记录
 *
 * @since 1.0.0
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * 功能说明：
     * - 支持三种登录方式：用户名、邮箱、手机号
     * - 密码错误5次锁定账户30分钟（Redis计数器）
     * - 密码过期检查（90天）
     * - 生成JWT Token（7天有效期）
     * - 更新登录信息（次数、时间、IP）
     * - 记录操作日志
     *
     * 业务规则：
     * 1. 账号必须存在（用户名/邮箱/手机号）
     * 2. 账号必须启用（is_enabled=1）
     * 3. 账号未锁定（is_locked=0）或锁定时间已过
     * 4. 密码必须正确（BCrypt验证）
     * 5. 密码未过期（password_expire_time > NOW()）
     *
     * 异常处理：
     * - 用户不存在：抛出BusinessException("用户不存在")
     * - 账户已禁用：抛出BusinessException("账户已禁用")
     * - 账户已锁定：抛出BusinessException("账户已锁定，请30分钟后再试")
     * - 密码错误：抛出BusinessException("密码错误，剩余尝试次数：N次")
     * - 密码过期：抛出BusinessException("密码已过期，请修改密码")
     *
     * 安全说明：
     * - 密码错误使用Redis计数器（key: login:error:{userId}）
     * - 计数器30分钟自动过期
     * - 锁定后修改数据库is_locked和locked_time字段
     *
     * @param loginDTO 登录请求DTO
     * @return 登录响应VO（包含Token、用户信息、角色、权限、数据权限）
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 用户登出
     *
     * 功能说明：
     * - 清除客户端Token（前端负责）
     * - 清除服务端Token缓存（可选）
     * - 记录登出操作日志
     * - 更新在线状态（如果有在线用户表）
     *
     * 业务规则：
     * 1. 验证Token有效性
     * 2. 记录登出时间和IP
     * 3. 清除用户缓存信息
     *
     * @param token JWT Token
     * @param clientIp 客户端IP地址
     */
    void logout(String token, String clientIp);

    /**
     * 刷新Token
     *
     * 功能说明：
     * - 使用旧Token换取新Token
     * - 复用旧Token中的用户信息和角色
     * - 重新查询权限（可能已变更）
     * - 延长会话时间（无感刷新）
     *
     * 业务规则：
     * 1. 旧Token必须有效且未过期
     * 2. 新Token继承旧Token的所有信息
     * 3. 权限从数据库重新查询（确保最新）
     * 4. 新Token有效期7天
     *
     * 刷新场景：
     * - Token即将过期（前端判断）
     * - 用户权限变更后刷新
     * - 长时间操作后刷新
     *
     * @param oldToken 旧Token
     * @return 新Token
     */
    String refreshToken(String oldToken);

    /**
     * 修改密码
     *
     * 功能说明：
     * - 用户修改自己的密码
     * - 验证旧密码正确性
     * - 验证新密码强度
     * - 新旧密码不能相同
     * - 修改后记录操作日志
     *
     * 业务规则：
     * 1. 旧密码必须正确（BCrypt验证）
     * 2. 新密码必须符合强度要求（6-20字符，包含大小写字母、数字、特殊字符）
     * 3. 新旧密码不能相同
     * - 两次输入的新密码必须一致
     * - 新密码不能包含个人信息（用户名、手机号）
     * - 修改后密码立即生效
     *
     * 异常处理：
     * - 旧密码错误：抛出BusinessException("旧密码错误")
     * - 新密码强度不足：抛出BusinessException("密码强度不够")
     * - 新旧密码相同：抛出BusinessException("新密码不能与旧密码相同")
     * - 两次输入不一致：抛出BusinessException("两次输入的新密码不一致")
     *
     * @param userId              用户ID
     * @param changePasswordDTO 修改密码请求DTO
     */
    void changePassword(Long userId, ChangePasswordDTO changePasswordDTO);

    /**
     * 重置密码（管理员操作）
     *
     * 功能说明：
     * - 管理员重置用户密码
     * - 生成随机密码（10位，包含大小写字母、数字、特殊字符）
     * - 返回新密码（仅返回一次）
     * - 强制用户下次登录时修改密码
     *
     * 业务规则：
     * 1. 只有系统管理员可以重置密码
     * 2. 新密码随机生成，符合强度要求
     * 3. 重置后立即生效
     * 4. 用户下次登录时强制修改密码
     * 5. 记录操作日志（重置人、重置时间）
     *
     * 权限要求：
     * - 需要USER:RESET权限
     * - 或系统管理员角色
     *
     * @param userId 用户ID
     * @return 新密码（仅返回一次，建议通过邮件/短信发送）
     */
    String resetPassword(Long userId);

    /**
     * 验证Token有效性
     *
     * 功能说明：
     * - 验证Token签名是否正确
     * - 验证Token是否过期
     * - 返回用户ID
     *
     * 业务规则：
     * 1. Token签名必须正确
     * 2. Token未过期
     * 3. 用户存在且未删除
     *
     * 使用场景：
     * - 请求拦截器验证
     * - Token刷新前验证
     * - 获取当前用户信息
     *
     * @param token JWT Token
     * @return 用户ID，验证失败返回null
     */
    Long validateToken(String token);

    /**
     * 获取当前登录用户信息
     *
     * 功能说明：
     * - 根据Token获取用户信息
     * - 包含用户基本信息、角色、权限
     * - 用于前端展示用户信息
     *
     * 业务规则：
     * 1. Token必须有效
     * 2. 查询用户最新信息
     * 3. 查询用户角色和权限（缓存）
     *
     * @param token JWT Token
     * @return 当前用户信息VO，Token无效返回null
     */
    Object getCurrentUser(String token);

    /**
     * 检查用户登录状态
     *
     * 功能说明：
     * - 检查用户是否在线
     * - 检查Token是否有效
     * - 检查账户状态是否变更
     *
     * 业务规则：
     * 1. Token必须有效
     * 2. 账户必须启用
     * 3. 账户未锁定
     *
     * @param token JWT Token
     * @return true-在线且有效，false-离线或无效
     */
    boolean isUserLoggedIn(String token);

    /**
     * 获取密码错误剩余尝试次数
     *
     * 功能说明：
     * - 从Redis获取密码错误次数
     * - 计算剩余尝试次数
     * - 5次错误后锁定30分钟
     *
     * 业务规则：
     * - 总尝试次数：5次
     * - 锁定时间：30分钟
     * - Redis Key：login:error:{userId}
     *
     * @param userId 用户ID
     * @return 剩余尝试次数（0-5）
     */
    int getRemainingAttempts(Long userId);

    /**
     * 锁定用户账户
     *
     * 功能说明：
     * - 锁定用户账户（密码错误5次）
     * - 设置锁定原因和锁定时间
     * - 30分钟后自动解锁
     *
     * 业务规则：
     * 1. 设置is_locked=1
     * 2. 设置locked_time=NOW()+30分钟
     * 3. 设置locked_reason="密码错误次数过多"
     *
     * @param userId     用户ID
     * @param reason     锁定原因
     * @param lockMinutes 锁定时长（分钟）
     */
    void lockUserAccount(Long userId, String reason, int lockMinutes);

    /**
     * 解锁用户账户
     *
     * 功能说明：
     * - 管理员手动解锁
     * - 或30分钟后自动解锁
     *
     * 业务规则：
     * 1. 设置is_locked=0
     * 2. 清除locked_time和locked_reason
     * 3. 清除Redis错误计数器
     *
     * @param userId 用户ID
     */
    void unlockUserAccount(Long userId);

    /**
     * 检查账户是否被锁定
     *
     * 功能说明：
     * - 检查is_locked字段
     * - 检查锁定时间是否已过
     * - 如果锁定时间已过，自动解锁
     *
     * 业务规则：
     * 1. is_locked=0：未锁定
     * 2. is_locked=1且locked_time > NOW()：锁定中
     * 3. is_locked=1且locked_time <= NOW()：锁定已过期，自动解锁
     *
     * @param user 用户实体
     * @return true-账户锁定中，false-账户未锁定
     */
    boolean isAccountLocked(Object user);
}
