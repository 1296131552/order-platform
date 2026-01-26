package com.company.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.company.common.config.SecurityProperties;
import com.company.common.enums.result.GlobalResultCode;
import com.company.common.model.dto.ResultDTO;
import com.company.user.filter.JwtAuthenticationFilter;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Resource
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Resource
    private SecurityProperties securityProperties;

    @Value("${springdoc.api-docs.path:/v3/api-docs}")
    private String apiDocsPath;

    @Value("${springdoc.swagger-ui.path:/docs}")
    private String swaggerUiPath;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 安全过滤器链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 启用CORS
            .authorizeHttpRequests(auth -> {
                // 动态添加允许访问的路径
                for (String path : securityProperties.getPermitAllPaths()) {
                    auth.requestMatchers(path).permitAll();
                    log.info("Security: 允许无认证访问路径: {}", path);
                }
                
                // 添加固定的文档访问路径
                auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", apiDocsPath, swaggerUiPath).permitAll()
                    .anyRequest().authenticated(); // 其他请求需要认证
            })
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 不使用session
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // 自定义认证和授权处理
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    // 认证失败（未登录或token无效）- 这是正常的业务流程，不是异常
                    handleAuthResponse(response, GlobalResultCode.UNAUTHORIZED);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    // 授权失败（权限不足）- 这是正常的业务流程，不是异常
                    handleAuthResponse(response, GlobalResultCode.FORBIDDEN);
                })
            );
        
        return http.build();
    }

    /**
     * CORS配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");    // 允许所有来源
        configuration.addAllowedMethod("*");    // 允许所有请求方法
        configuration.addAllowedHeader("*");    // 允许所有请求头
        configuration.setAllowCredentials(true); // 允许发送凭证
        configuration.setMaxAge(3600L); // 预检请求的缓存时间（秒）
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 对所有路径应用此配置
        return source;
    }

    /**
     * 处理认证和授权响应
     */
    private void handleAuthResponse(HttpServletResponse response, GlobalResultCode globalResultCode) throws IOException {
        response.setStatus(HttpStatus.OK.value()); // 返回200状态码
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                ResultDTO.failure(globalResultCode.getCode(), globalResultCode.getMessage())
        ));
    }

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}