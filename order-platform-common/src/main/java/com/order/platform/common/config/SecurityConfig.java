package com.order.platform.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 安全配置
 *
 * 功能说明：
 * - 配置密码加密器
 * - 使用 BCrypt 算法进行密码加密和验证
 *
 * BCrypt 算法特点：
 * - 自动加盐，每次加密结果不同
 * - 单向加密，不可逆
 * - 安全性高，业界标准
 *
 * 使用示例：
 * <pre>
 * // 注入 PasswordEncoder
 * &#64;Autowired
 * private PasswordEncoder passwordEncoder;
 *
 * // 注册时加密密码
 * String encodedPassword = passwordEncoder.encode(rawPassword);
 *
 * // 登录时验证密码
 * boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
 * </pre>
 *
 * @since 1.0.1
 */
@Configuration
public class SecurityConfig {

    /**
     * 密码加密器
     *
     * 使用 BCryptPasswordEncoder：
     * - strength = 10，计算强度（4-31，默认10）
     * - 强度越高，加密越安全，但计算时间越长
     *
     * @return PasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
