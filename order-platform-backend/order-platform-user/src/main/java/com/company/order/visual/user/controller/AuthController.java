package com.company.order.visual.user.controller;

import com.company.order.visual.common.response.Result;
import com.company.order.visual.user.dto.LoginRequest;
import com.company.order.visual.user.dto.LoginResponse;
import com.company.order.visual.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * <p>
 * 职责：
 * - 用户登录
 * - 用户登出
 * <p>
 * 设计说明：认证（/api/auth）与用户管理（/api/user）分离
 */
@Tag(name = "认证管理", description = "登录、登出")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private UserService userService;

    @Operation(summary = "用户登录", description = "支持用户名/邮箱/手机号登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.ok(response);
    }

    @Operation(summary = "用户登出", description = "将当前Token加入黑名单")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        // 从Authorization Header提取Token
        String token = extractToken(request);
        userService.logout(token);
        return Result.ok();
    }

    /**
     * 从请求中提取Bearer Token
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
