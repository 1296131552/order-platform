package com.company.user.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.company.common.Constant.JwtConstant;
import com.company.common.utils.JwtUtil;

import io.micrometer.common.lang.NonNull;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT认证过滤器
 * 对每个请求进行过滤，检查是否包含有效的JWT令牌
 * 如果包含有效令牌，将创建认证令牌并设置到SecurityContext中
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private UserDetailsService userDetailsService;
    @Resource
    private JwtUtil jwtUtils;

    /**
     * 对每个请求进行过滤，检查是否包含有效的JWT令牌
     * 如果包含有效令牌，将创建认证令牌并设置到SecurityContext中
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        final String authHeader = request.getHeader(JwtConstant.JWT_HEADER_NAME);

        String username = null;
        String token = null;

        if (authHeader != null) {
            try {
                token = jwtUtils.extractToken(authHeader);
                username = jwtUtils.extractUsername(token);
                
                // 刷新令牌不应该用于访问API，只有访问令牌才能用于认证
                if (jwtUtils.isRefreshToken(token)) {
                    filterChain.doFilter(request, response);
                    return;
                }
            } catch (Exception e) {
                // 这不是真正的异常，只是token无效或已过期
                // 继续过滤链，让SecurityContext保持为空，后续的安全配置会处理未认证的请求
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 如果成功提取用户名且SecurityContext中没有认证信息，则进行认证
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!userDetails.isEnabled()) {
                // 用户被禁用，拒绝访问
                filterChain.doFilter(request, response);
                return;
            }

            // 验证token是否有效
            if (jwtUtils.validateToken(token)) {
                // token有效，处理SecurityContext
                handleSecurityContext(request, userDetails);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 处理SecurityContext，创建认证令牌并设置到SecurityContext中
     * @param request HttpServletRequest对象
     * @param userDetails UserDetails对象，包含用户信息
     */
    private void handleSecurityContext(HttpServletRequest request, UserDetails userDetails) {
        // 创建认证令牌
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

        // 设置认证令牌的详细信息
        authenticationToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        // 设置SecurityContext中的认证信息
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}