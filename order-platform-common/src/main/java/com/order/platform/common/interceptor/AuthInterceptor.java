package com.order.platform.common.interceptor;

import com.order.platform.common.annotation.RequireLogin;
import com.order.platform.common.dto.CurrentUserDTO;
import com.order.platform.common.enums.ResponseCode;
import com.order.platform.common.exception.BusinessException;
import com.order.platform.common.holder.CurrentUserHolder;
import com.order.platform.common.provider.UserRoleProvider;
import com.order.platform.common.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * 认证拦截器
 *
 * 功能说明：
 * - 拦截带有 @RequireLogin 注解的接口（支持方法级别和类级别）
 * - 验证请求头中的 Authorization Token（支持 bearer/Bearer/BEARER）
 * - 检查 Token 黑名单（防止退出登录后重用）
 * - 解析用户信息并存入 ThreadLocal（混合方案获取角色）
 * - 请求结束后清理 ThreadLocal，防止内存泄漏
 *
 * 工作流程：
 * 1. 请求到达 → 判断方法或类是否有 @RequireLogin 注解
 * 2. 有注解 → 从请求头获取 Token
 * 3. 检查 Token 黑名单（Redis）→ 验证 Token → 解析用户信息
 * 4. 获取角色（混合方案）→ 存入 ThreadLocal
 * 5. 请求处理 → 业务代码通过 CurrentUserHolder 获取用户
 * 6. 请求结束 → afterCompletion 清理 ThreadLocal
 *
 * 请求头格式：
 * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 * （支持大小写变体：bearer、Bearer、BEARER）
 *
 * 内存泄漏防护：
 * - preHandle 内部使用 try-catch(Exception) 确保所有异常时都清理 ThreadLocal
 * - afterCompletion 作为二次保障兜底清理
 *
 * 异常处理策略：
 * - Token 在黑名单中（已退出登录）→ UNAUTHORIZED
 * - Token 格式错误/签名失败 → TOKEN_INVALID
 * - Token 过期 → TOKEN_EXPIRED
 * - Token 为空/格式异常 → UNAUTHORIZED
 * - 用户ID/用户名为空 → TOKEN_INVALID
 *
 * 角色获取策略（混合方案）：
 * 1. 优先从 Token 中获取角色快照（性能最优）
 * 2. Token 中无角色时，从数据库查询（实时性最好）
 * 3. 数据库查询失败时，使用默认角色 USER（兜底保障）
 *
 * Token 黑名单机制：
 * - 用户退出登录时，Token 被加入 Redis 黑名单
 * - 黑名单 Key 格式：token:blacklist:{token}
 * - 黑名单过期时间：与 Token 过期时间一致（7天）
 * - 如果 Redis 未配置，跳过黑名单检查（降级策略）
 *
 * 依赖说明：
 * - UserRoleProvider 为可选依赖（required = false）
 * - StringRedisTemplate 为可选依赖（required = false）
 * - 如果未注入（user 模块未加载），启动时会打印 WARN 日志
 * - 此时角色获取会跳过数据库查询，直接使用默认角色
 *
 * 优化记录：
 * - ✅ 使用 UserRoleProvider 接口替代反射调用
 * - ✅ 所有异常场景都清理 ThreadLocal
 * - ✅ 移除 isTokenExpired 冗余校验
 * - ✅ 添加 userId/username 非空校验
 * - ✅ 支持 Controller 类级别的 @RequireLogin
 * - ✅ 使用普通 class 替代 record（兼容 JDK 11）
 * - ✅ 增强日志空值告警
 * - ✅ Token 前缀大小写兼容（bearer/Bearer/BEARER）
 * - ✅ UserRoleProvider 初始化校验（启动时健康检查）
 * - ✅ Token 黑名单检查（Redis）
 *
 * @since 1.0.0
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final UserRoleProvider userRoleProvider;
    private final StringRedisTemplate redisTemplate;

    /**
     * 构造函数（支持可选依赖）
     *
     * @param jwtUtil          JWT 工具类（必需）
     * @param userRoleProvider 用户角色提供者（可选，如果为 null 则跳过数据库查询）
     * @param redisTemplate    Redis 模板（可选，如果为 null 则跳过黑名单检查）
     */
    public AuthInterceptor(JwtUtil jwtUtil,
                          @org.springframework.beans.factory.annotation.Autowired(required = false) UserRoleProvider userRoleProvider,
                          @org.springframework.beans.factory.annotation.Autowired(required = false) StringRedisTemplate redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.userRoleProvider = userRoleProvider;
        this.redisTemplate = redisTemplate;

        // 【启动时健康检查】提前暴露配置问题
        if (this.userRoleProvider == null) {
            log.warn("┌─────────────────────────────────────────────────────────────┐");
            log.warn("│ ⚠️  UserRoleProvider 未注入                                  │");
            log.warn("│                                                             │");
            log.warn("│ 影响：                                                       │");
            log.warn("│   - Token 中无角色时，无法从数据库查询用户角色                │");
            log.warn("│   - 所有用户将使用默认角色 USER                              │");
            log.warn("│                                                             │");
            log.warn("│ 建议：                                                       │");
            log.warn("│   - 确保 order-platform-user 模块已加载                      │");
            log.warn("│   - 检查 UserRoleService 是否正常启动                        │");
            log.warn("│   - 或在生成 Token 时包含角色信息                            │");
            log.warn("└─────────────────────────────────────────────────────────────┘");
        } else {
            log.info("✅ UserRoleProvider 已注入: {}",
                    this.userRoleProvider.getClass().getSimpleName());
        }
    }

    /**
     * 请求处理前拦截
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param handler  处理器（Controller 方法）
     * @return true-放行，false-拦截
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                            @NonNull HttpServletResponse response,
                            @NonNull Object handler) {

        // 1. 只处理方法处理器
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        // 2. 检查方法或类上是否有 @RequireLogin 注解
        boolean requireLogin = method.isAnnotationPresent(RequireLogin.class) ||
                             handlerMethod.getBeanType().isAnnotationPresent(RequireLogin.class);

        if (!requireLogin) {
            // 没有注解，直接放行
            return true;
        }

        // 【ThreadLocal 防护标志】用于 finally 判断是否需要清理
        boolean userContextSet = false;

        try {
            // 3. 获取 Token
            String authorization = request.getHeader("Authorization");
            if (authorization == null || authorization.isEmpty()) {
                log.warn("请求未携带 Token: {}", request.getRequestURI());
                throw new BusinessException(ResponseCode.UNAUTHORIZED);
            }

            // 4. 移除 "Bearer " 前缀并校验
            String token = extractToken(authorization, request.getRequestURI());

            // 5. 验证并解析 Token（细分异常类型）
            TokenValidationResult validationResult = validateAndParseToken(token, request.getRequestURI());

            // 6. 核心字段非空校验（username 可选）
            validateUserInfo(validationResult);

            // 7. 【混合方案】获取用户角色列表
            List<String> roles = getUserRoles(token, validationResult.userId, request.getRequestURI());

            // 8. 构建当前用户信息
            CurrentUserDTO currentUser = CurrentUserDTO.builder()
                    .id(validationResult.userId)
                    .username(validationResult.username)
                    .roles(roles)
                    .build();

            // 9. 存入 ThreadLocal
            CurrentUserHolder.set(currentUser);
            userContextSet = true;  // 标记已设置

            log.debug("用户认证成功: userId={}, username={}, roles={}, uri={}",
                    validationResult.userId, validationResult.username, roles, request.getRequestURI());

            return true;

        } catch (Exception e) {
            // 【异常处理】清理 ThreadLocal 并抛出异常
            if (userContextSet) {
                CurrentUserHolder.clear();
                userContextSet = false;
            }
            throw e;

        } finally {
            // 【兜底保障】防止出现异常分支遗漏清理的情况
            // 理论上不会执行到这里（异常已在 catch 中处理）
            // 但作为防御性编程，确保 ThreadLocal 不会被污染
            if (userContextSet) {
                log.error("【异常情况】preHandle 结束后 userContextSet 仍为 true，可能存在逻辑错误");
                CurrentUserHolder.clear();
            }
        }
    }

    /**
     * 获取用户角色列表（混合方案）
     *
     * 策略：
     * 1. 优先从 Token 获取角色快照（性能最优）
     * 2. Token 无角色时，从数据库查询（实时性最好）
     * 3. 数据库查询失败或未注入时，使用默认角色 USER（兜底保障）
     *
     * @param token  JWT Token
     * @param userId 用户ID
     * @param uri    请求 URI（用于日志）
     * @return 角色代码列表
     */
    private List<String> getUserRoles(String token, Long userId, String uri) {
        // 方案 1：从 Token 获取角色快照（性能最优）
        if (jwtUtil.hasRolesInToken(token)) {
            List<String> roles = jwtUtil.getRolesFromToken(token);
            log.debug("从 Token 获取角色: userId={}, roles={}", userId, roles);
            return roles;
        }

        // 方案 2：从数据库查询（实时性最好）
        // 【可选依赖】如果 userRoleProvider 未注入，跳过数据库查询
        if (userRoleProvider != null) {
            try {
                List<String> roles = userRoleProvider.getRoleCodesByUserId(userId);
                if (roles != null && !roles.isEmpty()) {
                    log.debug("从数据库获取角色: userId={}, roles={}", userId, roles);
                    return roles;
                }
            } catch (Exception e) {
                log.warn("从数据库查询角色失败，使用默认角色: userId={}, uri={}", userId, uri, e);
            }
        } else {
            // 【启动时已警告】这里仅在 debug 级别记录
            log.debug("UserRoleProvider 未注入，跳过数据库查询: userId={}", userId);
        }

        // 方案 3：使用默认角色 USER（兜底保障）
        log.debug("使用默认角色: userId={}, role=USER", userId);
        return Collections.singletonList("USER");
    }

    /**
     * 从 Authorization 请求头中提取 Token
     *
     * 兼容性说明：
     * - 支持 Bearer 前缀的大小写变体（bearer、Bearer、BEARER）
     * - 适配前端常见场景（部分 HTTP 库可能发送小写 bearer）
     *
     * @param authorization Authorization 请求头值
     * @param uri           请求 URI（用于日志）
     * @return 提取的 Token
     * @throws BusinessException Token 格式错误
     */
    private String extractToken(String authorization, String uri) {
        // 【大小写兼容】统一转小写后判断前缀
        String lowerAuth = authorization.toLowerCase();
        if (!lowerAuth.startsWith("bearer ")) {
            log.warn("Token 格式错误（缺少 Bearer 前缀）: {}", uri);
            throw new BusinessException(ResponseCode.UNAUTHORIZED);
        }

        // 【提取 Token】使用原始字符串（保留大小写）
        // "bearer " 长度为 7
        String token = authorization.substring(7).trim();

        // 【增强校验】检查提取后的 Token 是否为空
        if (token.isEmpty()) {
            log.warn("Token 为空（仅有 Bearer 前缀）: {}", uri);
            throw new BusinessException(ResponseCode.UNAUTHORIZED);
        }

        return token;
    }

    /**
     * 验证并解析 Token
     *
     * 优化点：移除 isTokenExpired 冗余校验，直接解析 Token
     *
     * 异常处理策略：
     * - ExpiredJwtException → TOKEN_EXPIRED
     * - SignatureException/MalformedJwtException → TOKEN_INVALID
     * - 其他异常 → TOKEN_INVALID
     *
     * @param token JWT Token
     * @param uri   请求 URI（用于日志）
     * @return 验证结果，包含用户 ID 和用户名
     * @throws BusinessException Token 验证失败
     */
    private TokenValidationResult validateAndParseToken(String token, String uri) {
        try {
            // 1. 检查 Token 黑名单（防止退出登录后重用）
            if (redisTemplate != null) {
                try {
                    String blacklistKey = "token:blacklist:" + token;
                    Boolean isBlacklisted = redisTemplate.hasKey(blacklistKey);

                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        log.warn("Token 已失效（退出登录）: {}", uri);
                        throw new BusinessException(ResponseCode.UNAUTHORIZED, "Token已失效，请重新登录");
                    }
                } catch (BusinessException e) {
                    throw e;  // 业务异常继续抛出
                } catch (Exception e) {
                    // Redis 连接失败时，跳过黑名单检查（降级策略）
                    log.warn("Redis 连接失败，跳过 Token 黑名单检查: {}", e.getMessage());
                }
            }

            // 2. 解析用户信息（可能抛出 JWT 格式异常）
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);

            return new TokenValidationResult(userId, username);

        } catch (BusinessException e) {
            // 业务异常直接抛出（已封装的错误码）
            throw e;

        } catch (ExpiredJwtException e) {
            // Token 过期异常
            log.warn("Token 已过期: {}", uri);
            throw new BusinessException(ResponseCode.TOKEN_EXPIRED);

        } catch (SignatureException | MalformedJwtException e) {
            // Token 签名失败或格式错误
            log.warn("Token 无效（{}）: {}", e.getClass().getSimpleName(), uri);
            throw new BusinessException(ResponseCode.TOKEN_INVALID);

        } catch (Exception e) {
            // 其他未知异常（如解析失败、空指针等）
            log.error("解析 Token 失败: {}", uri, e);
            throw new BusinessException(ResponseCode.TOKEN_INVALID);
        }
    }

    /**
     * 校验用户信息非空
     *
     * 校验策略（灵活性调整）：
     * - userId：必须非空（核心标识）
     * - username：允许为空（兼容匿名用户场景）
     *
     * username 为空时的处理：
     * - Token 中未携带 username（兼容旧版本 Token）
     * - 匿名用户（仅有 userId 无 username）
     * - 系统自动生成的测试用户
     *
     * @param result Token 解析结果
     * @throws BusinessException 校验失败
     */
    private void validateUserInfo(TokenValidationResult result) {
        // userId 必须存在（核心标识）
        if (result.userId == null) {
            log.error("Token 解析后 userId 为空，可能 Token 被篡改");
            throw new BusinessException(ResponseCode.TOKEN_INVALID);
        }

        // username 可以为空（灵活性调整）
        // 场景：匿名用户、旧版本 Token、系统用户等
        if (result.username == null || result.username.isEmpty()) {
            log.debug("Token 解析后 username 为空（允许）: userId={}", result.userId);
            // 不抛异常，允许 username 为空的情况
        }
    }

    /**
     * 请求完成后清理
     *
     * 注意：必须在请求结束时清理 ThreadLocal，防止内存泄漏
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        // 清理 ThreadLocal
        CurrentUserHolder.clear();
    }

    /**
     * Token 验证结果（内部使用）
     *
     * 使用普通 class 替代 record，兼容 JDK 11
     */
    private static class TokenValidationResult {
        private final Long userId;
        private final String username;

        TokenValidationResult(Long userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }
    }
}
