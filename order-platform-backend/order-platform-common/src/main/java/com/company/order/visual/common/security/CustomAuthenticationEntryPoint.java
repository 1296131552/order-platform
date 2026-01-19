package com.company.order.visual.common.security;

import com.company.order.visual.common.response.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 认证异常统一处理
 * <p>
 * 实现：
 * - AuthenticationEntryPoint: 处理 401 未认证
 * - AccessDeniedHandler: 处理 403 无权限
 */
@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint, AccessDeniedHandler{
    
    private final ObjectMapper objectMapper = new ObjectMapper();


    private void handleError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Result<Void> result = Result.fail(status, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException{
                            log.debug("认证失败: {}", authException.getMessage());
                            handleError(response,401,"未认证，请先登录");
                        }
    
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        log.debug("权限不足: {}", accessDeniedException.getMessage());
        handleError(response, 403, "权限不足");
    }
}
