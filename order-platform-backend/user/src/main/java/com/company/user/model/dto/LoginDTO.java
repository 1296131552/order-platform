package com.company.user.model.dto;

import com.company.user.annotation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "用户登录请求")
public class LoginDTO {
    @Schema(description = "用户名或邮箱", example = "admin", required = true)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "登录密码", example = "admin123", required = true)
    @ValidPassword
    private String password;
}