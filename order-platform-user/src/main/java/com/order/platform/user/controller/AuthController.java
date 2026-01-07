package com.order.platform.user.controller;

import com.order.platform.common.annotation.OperationLog;
import com.order.platform.common.dto.CurrentUser;
import com.order.platform.common.enums.BusinessType;
import com.order.platform.common.enums.OperationModule;
import com.order.platform.common.enums.OperationType;
import com.order.platform.common.holder.CurrentUserHolder;
import com.order.platform.common.response.Result;
import com.order.platform.user.dto.request.ChangePasswordDTO;
import com.order.platform.user.dto.request.LoginDTO;
import com.order.platform.user.dto.response.LoginVO;
import com.order.platform.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证登录控制器
 *
 * 功能说明：
 * - 用户登录认证（支持用户名/邮箱/手机号）
 * - 用户登出
 * - Token刷新
 * - 修改密码
 *
 * 接口列表：
 * - POST /api/auth/login - 用户登录
 * - POST /api/auth/logout - 用户登出
 * - POST /api/auth/refresh - 刷新Token
 * - POST /api/auth/change-password - 修改密码
 * - POST /api/auth/reset-password/{id} - 重置密码（管理员）
 *
 * @since 1.0.0
 */
@Slf4j                       // 自动生成日志对象
@RestController              // REST 接口控制器 所有方法返回值会自动序列化为 JSON
@RequestMapping("/api/auth") // 
@RequiredArgsConstructor     // 注入AuthService,自动为类中所有被 final 修饰的成员变量生成对应的构造方法
@Tag(name = "认证登录", description = "用户登录、登出、Token刷新等认证相关接口")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     *
     * 接口说明：
     * - 支持三种登录方式：用户名、邮箱、手机号
     * - 密码错误5次锁定账户30分钟
     * - 返回JWT Token和用户完整信息
     *
     * 请求示例：
     * <pre>
     * {
     *   "account": "zhangsan",
     *   "password": "123456"
     * }
     * </pre>
     *
     * 响应示例：
     * <pre>
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *     "tokenType": "Bearer",
     *     "expiresIn": 604800,
     *     "userInfo": {...},
     *     "roles": ["CUSTOMER_MANAGER"],
     *     "permissions": ["ORDER:VIEW", "ORDER:CREATE"],
     *     "dataScope": {...}
     *   }
     * }
     * </pre>
     *
     * @param loginDTO 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")    // Post
    @Operation(summary = "用户登录", description = "支持用户名/邮箱/手机号登录")
    @OperationLog(
            module = OperationModule.USER,
            type = OperationType.LOGIN,
            business = BusinessType.USER,
            businessId = "#result.data.userInfo.id",
            // 从返回值中获取完整的操作人信息
            operatorId = "#result.data.userInfo.id",
            operatorName = "#result.data.userInfo.realName",
            operatorUserCode = "#result.data.userInfo.userCode",
            operatorEmployeeNo = "#result.data.userInfo.employeeNo",
            operatorPosition = "#result.data.userInfo.position",
            description = "用户登录"
    )
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO,
                                  HttpServletRequest request) {
        // 切面会通过 SpEL 表达式从返回值中获取完整的用户信息
        String clientIp = getClientIp(request);
        log.info("用户登录请求: account={}, clientIp={}", loginDTO.getAccount(), clientIp);

        LoginVO loginVO = authService.login(loginDTO);

        log.info("用户登录成功: userId={}, username={}, clientIp={}",
            loginVO.getUserInfo().getId(), loginVO.getUserInfo().getUsername(), clientIp);

        return Result.success(loginVO);
    }

    /**
     * 用户登出
     *
     * 接口说明：
     * - 清除服务端Token缓存（可选）
     * - 记录登出操作日志
     * - 前端负责清除本地Token
     *
     * 请求头：
     * Authorization: Bearer {token}
     *
     * @param request HTTP请求
     * @return 操作结果
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户退出登录")
    @OperationLog(module = OperationModule.USER, type = OperationType.LOGOUT, description = "用户登出")
    public Result<Void> logout(HttpServletRequest request) {
        String token = extractToken(request);
        String clientIp = getClientIp(request);

        authService.logout(token, clientIp);

        log.info("用户登出成功: clientIp={}", clientIp);
        return Result.success();
    }

    /**
     * 刷新Token
     *
     * 接口说明：
     * - 使用旧Token换取新Token
     * - 复用旧Token中的用户信息和角色
     * - 重新查询权限（可能已变更）
     * - 新Token有效期7天
     *
     * 请求头：
     * Authorization: Bearer {oldToken}
     *
     * 响应示例：
     * <pre>
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     *   }
     * }
     * </pre>
     *
     * @param request HTTP请求
     * @return 新Token
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "使用旧Token换取新Token")
    @OperationLog(module = OperationModule.USER, type = OperationType.OTHER, description = "刷新Token")
    public Result<String> refreshToken(HttpServletRequest request) {
        String oldToken = extractToken(request);
        String newToken = authService.refreshToken(oldToken);

        log.info("Token刷新成功");
        return Result.success(newToken);
    }

    /**
     * 修改密码
     *
     * 接口说明：
     * - 用户修改自己的密码
     * - 需要验证旧密码
     * - 新密码必须符合强度要求
     * - 从 Token 中获取当前用户 ID（通过 CurrentUserHolder）
     *
     * 请求示例：
     * <pre>
     * {
     *   "oldPassword": "123456",
     *   "newPassword": "Abc123!@#",
     *   "confirmPassword": "Abc123!@#"
     * }
     * </pre>
     *
     * @param changePasswordDTO 修改密码请求
     * @param request HTTP请求
     * @return 操作结果
     */
    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "用户修改自己的密码")
    @OperationLog(module = OperationModule.USER, type = OperationType.UPDATE, description = "修改密码")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO,
                                        HttpServletRequest request) {
        // 从 ThreadLocal 获取当前登录用户（AuthInterceptor 已将用户信息存入）
        CurrentUser currentUser = CurrentUserHolder.get();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        Long userId = currentUser.getId();
        authService.changePassword(userId, changePasswordDTO);

        log.info("用户修改密码成功: userId={}", userId);
        return Result.success();
    }

    /**
     * 重置密码（管理员操作）
     *
     * 接口说明：
     * - 管理员重置用户密码
     * - 生成随机密码（10位）
     * - 返回新密码（仅返回一次）
     *
     * 权限要求：
     * - 需要USER:RESET权限
     * - 或系统管理员角色
     *
     * 响应示例：
     * <pre>
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": {
     *     "newPassword": "Abc123!@#xyZ"
     *   }
     * }
     * </pre>
     *
     * @param userId 用户ID
     * @return 新密码
     */
    @PostMapping("/reset-password/{id}")
    @Operation(summary = "重置密码", description = "管理员重置用户密码")
    @OperationLog(module = OperationModule.USER, type = OperationType.UPDATE, description = "重置密码")
    public Result<String> resetPassword(@PathVariable("id") Long userId) {
        String newPassword = authService.resetPassword(userId);

        log.warn("管理员重置用户密码: userId={}", userId);
        return Result.success(newPassword);
    }

    /**
     * 获取当前登录用户信息
     *
     * 接口说明：
     * - 根据Token获取用户信息
     * - 包含用户基本信息、角色、权限
     *
     * 请求头：
     * Authorization: Bearer {token}
     *
     * @param request HTTP请求
     * @return 当前用户信息
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前用户信息", description = "根据Token获取当前登录用户信息")
    public Result<Object> getCurrentUser(HttpServletRequest request) {
        String token = extractToken(request);
        Object currentUser = authService.getCurrentUser(token);

        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        return Result.success(currentUser);
    }

    // ==================== 私有方法 ====================

    /**
     * 从请求中提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.toLowerCase().startsWith("bearer ")) {
            return null;
        }
        return authorization.substring(7);
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况（X-Forwarded-For可能包含多个IP）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
