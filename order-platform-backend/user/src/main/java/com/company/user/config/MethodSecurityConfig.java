package com.company.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * 方法级安全配置
 * 启用Spring Security的方法级权限控制
 */
@Configuration
@EnableMethodSecurity(
    prePostEnabled = true,     // 启用@PreAuthorize和@PostAuthorize
    securedEnabled = true,     // 启用@Secured
    jsr250Enabled = true       // 启用@RolesAllowed
)
public class MethodSecurityConfig {
    // Spring Security会自动配置相关的bean
}