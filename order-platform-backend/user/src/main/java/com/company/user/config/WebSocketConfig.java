package com.company.user.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 * 配置STOMP协议的WebSocket端点和消息代理
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Resource
    private WebSocketHandshakeInterceptor webSocketHandshakeInterceptor;
    /**
     * 配置STOMP协议的WebSocket端点
     * 客户端连接到该端点进行通信
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // 添加STOMP协议的WebSocket端点
                .setAllowedOriginPatterns("*")  // 允许所有源
                .addInterceptors(webSocketHandshakeInterceptor) // 添加握手拦截器
                .withSockJS();  // 启用SockJS
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 配置消息代理，指定消息前缀
        registry.enableSimpleBroker("/topic");
        // 配置应用程序的消息前缀
        registry.setApplicationDestinationPrefixes("/app");
    }
}
