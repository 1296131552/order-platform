package com.order.platform.common.config;

import com.order.platform.common.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 *
 * 功能说明：
 * - 跨域配置（CORS）
 * - 拦截器配置（认证、日志等）
 * 
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 拦截器配置
     *
     * 拦截所有 Controller 请求，由 @RequireLogin 注解决定是否验证 Token
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")           // 拦截所有路径
                .excludePathPatterns(             // 排除不需要拦截的路径
                        "/api/auth/login",       // 登录接口
                        "/api/auth/register",    // 注册接口
                        "/swagger-ui/**",        // Swagger UI
                        "/v3/api-docs/**",       // API 文档
                        "/doc.html",             // Knife4j 文档
                        "/webjars/**",           // Swagger 静态资源
                        "/favicon.ico",          // 网站图标
                        "/error"                 // 错误页面
                );
    }
}
