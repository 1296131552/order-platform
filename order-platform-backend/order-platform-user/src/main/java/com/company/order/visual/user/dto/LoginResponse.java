package com.company.order.visual.user.dto;

import lombok.*;
/**
 * 登录响应
 * 设计原则：避免 DTO 重复，复用 UserVO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    /**
     * 用户信息（复用 UserVO）
     */
    private UserVO user;

    /**
     * # TODO JWT Token（JWT 阶段添加，当前暂为 null）
     */
    private String token;
}
