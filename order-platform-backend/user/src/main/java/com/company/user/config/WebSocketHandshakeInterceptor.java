package com.company.user.config;

import com.company.common.utils.JwtUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import org.jspecify.annotations.NonNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket握手拦截器
 * 用于在握手阶段验证JWT令牌
 */
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
    @Resource
    private JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler,
                                   @NonNull Map<String, Object> attributes) {
        String token = null;
        
        // 从URL参数中获取token（用于SockJS）
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    token = param.substring(6); // 去掉 "token="
                    break;
                }
            }
        }
        
        // 验证访问令牌
        if (StringUtils.isNotBlank(token) && jwtUtil.isAccessToken(token) && !jwtUtil.isTokenExpired(token)) {
            Integer userId = jwtUtil.extractUserId(token);
            // 将用户ID存储到属性中，后续可以在WebSocketHandler中使用
            attributes.put("userId", userId);
            return true;
        }

        return false;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request,
                               @NonNull ServerHttpResponse response,
                               @NonNull WebSocketHandler wsHandler,
                               Exception exception) {
        // 握手完成后的操作，可留空
    }
}
