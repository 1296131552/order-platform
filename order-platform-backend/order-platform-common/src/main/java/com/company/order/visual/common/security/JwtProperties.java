package com.company.order.visual.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * JWT 配置属性
 */
@Data
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 密钥（至少 256 位 / 32字节）
     */
    @NotBlank(message = "JWT secret 不能为空")
    @Size(min = 32,message = "JWT secret 至少需要32字节")
    private String secret;

    /**
     * Token 过期时间（毫秒），默认 7 天
     */
    @Min(value = 60_000, message = "Token 过期时间不能少于 1 分钟")
    private Long expiration = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 初始版本号
     */
    @Min(value = 1,message = "版本号必须大于 0")
    private Long initialVersion = 1L;
}
