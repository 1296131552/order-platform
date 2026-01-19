package com.company.order.visual.user.filter;

import com.company.order.visual.common.security.JwtService;
import com.company.order.visual.common.security.TokenBlacklistService;
import com.company.order.visual.common.security.TokenInfo;
import com.company.order.visual.user.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * <p>
 * 执行流程：
 * 1. 提取 Authorization Header 中的 Token
 * 2. 解析 Token（一次解析，获取所有信息）
 * 3. 检查黑名单
 * 4. 验证版本号
 * 5. 设置 SecurityContextHolder
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;
    private final UserDetailsServiceImpl userDetailsService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        // 已认证则跳过
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(BEARER_PREFIX.length());

        try {
            // 一次解析，获取所有信息
            TokenInfo tokenInfo = jwtService.parseToken(jwt);

            if (!tokenInfo.isValid()) {
                log.debug("Token 无效或已过期");
                filterChain.doFilter(request, response);
                return;
            }

            // 检查黑名单
            if (blacklistService.isBlacklisted(tokenInfo.getTokenId())) {
                log.debug("Token 已在黑名单中");
                filterChain.doFilter(request, response);
                return;
            }

            // 验证版本号（密码重置后所有旧 token 失效）
            Long currentVersion = blacklistService.getUserTokenVersion(tokenInfo.getUserId());
            if (currentVersion != null && !tokenInfo.getVersion().equals(currentVersion)) {
                log.debug("Token 版本号不匹配，tokenVersion={}, currentVersion={}",
                        tokenInfo.getVersion(), currentVersion);
                filterChain.doFilter(request, response);
                return;
            }

            // 加载用户详情并设置 SecurityContext
            UserDetails userDetails = userDetailsService.loadUserById(tokenInfo.getUserId());

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            log.debug("用户认证成功，userId={}", tokenInfo.getUserId());

        } catch (Exception e) {
            log.error("认证过程异常", e);
        } 
        
        filterChain.doFilter(request, response);
        
    }
}
