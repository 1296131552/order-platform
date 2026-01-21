package com.company.order.visual.user.controller;

import com.company.order.visual.common.response.Result;
import com.company.order.visual.user.dto.LoginRequest;
import com.company.order.visual.user.dto.LoginResponse;
import com.company.order.visual.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
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
 * 设计说明：认证（/auth）与用户管理（/user）分离
 */
@Tag(name = "认证管理", description = "用户登录、登出接口")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private UserService userService;

    @Operation(
            summary = "用户登录",
            description = """
                    支持用户名/邮箱/手机号登录。

                    **业务逻辑：**
                    1. 校验账号密码
                    2. 检查账号状态（是否启用、是否锁定）
                    3. 生成 JWT Token
                    4. 记录登录日志（最后登录时间、IP、登录次数+1）

                    **注意事项：**
                    - 登录成功后，后续请求需在 Header 中携带 Token：`Authorization: Bearer {token}`
                    - Token 默认有效期 7 天
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "登录成功",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "账号或密码错误",
                    content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @SecurityRequirements  // 登录接口不需要认证
    @PostMapping("/login")
    public Result<LoginResponse> login(
            @Parameter(description = "登录请求参数", required = true)
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.ok(response);
    }

    @Operation(
            summary = "用户登出",
            description = """
                    将当前 Token 加入黑名单，使其失效。

                    **业务逻辑：**
                    1. 从 Authorization Header 提取 Token
                    2. 将 Token 加入 Redis 黑名单
                    3. 后续请求将返回 401 未授权
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "登出成功",
                    useReturnTypeSchema = true
            )
    })
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
