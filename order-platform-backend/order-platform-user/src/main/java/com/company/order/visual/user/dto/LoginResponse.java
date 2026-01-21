package com.company.order.visual.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 登录响应
 * <p>
 * 设计原则：避免 DTO 重复，复用 UserVO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登录响应")
public class LoginResponse {

    @Schema(description = "用户信息")
    private UserVO user;

    @Schema(
            description = "JWT 访问令牌",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String token;
}
