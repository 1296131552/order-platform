package com.company.order.visual.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求
 */
@Data
@Schema(description = "用户登录请求")
public class LoginRequest {

    @Schema(
            description = "登录账号（支持用户名/邮箱/手机号）",
            example = "admin",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "账号不能为空")
    private String account;

    @Schema(
            description = "登录密码",
            example = "password123",
            requiredMode = Schema.RequiredMode.REQUIRED,
            format = "password"
    )
    @NotBlank(message = "密码不能为空")
    private String password;

}
