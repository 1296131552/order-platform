package com.company.user.service.basic.impl;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.company.user.service.basic.UserOnlineService;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户在线服务实现类
 * 负责管理用户的上线、下线和查询在线状态
 */
@Slf4j
@Service
public class UserOnlineServiceImpl implements UserOnlineService {
    /**
     * 在线用户集合，存储所有在线用户的ID
     */
    private final Set<Integer> onlineUsers = ConcurrentHashMap.newKeySet();

    /**
     * 在线用户映射表，键为用户ID，值为WebSocket会话ID
     */
    private final ConcurrentHashMap<Integer, Set<String>> userSessions = new ConcurrentHashMap<>();

    /**
     * 用户上线
     * @param userId 用户ID
     * @param sessionId WebSocket会话ID
     */
    @Override
    public void userOnline(Integer userId, String sessionId) {
        if (onlineUsers.add(userId)) {
            log.info("用户 {} 上线", userId);
        }
        // 添加会话到用户的会话集合
        Set<String> sessions = userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
        sessions.add(sessionId);
    }

    /**
     * 用户离线（移除特定会话）
     * @param userId 用户ID
     * @param sessionId WebSocket会话ID
     */
    @Override
    public void userOffline(Integer userId, String sessionId) {
        Set<String> sessions = userSessions.get(userId);    // 获取用户的会话集合
        if (sessions != null) {
            sessions.remove(sessionId);    // 从会话集合中移除指定会话
            if (sessions.isEmpty()) {
                // 如果用户没有会话了，从在线用户集合中移除
                onlineUsers.remove(userId);
                log.info("用户 {} 下线", userId);
            }
        }
    }

    /**
     * 获取所有在线用户的ID列表
     * @return 在线用户ID列表
     */
    @Override
    public List<Integer> getOnlineUserIds() {
        return onlineUsers.stream().toList();
    }

    /**
     * 获取在线用户数量
     * @return 在线用户数量
     */
    @Override
    public int getOnlineCount() {
        return onlineUsers.size();
    }

    /**
     * 判断用户是否在线
     * @param userId 用户ID
     * @return 如果用户在线则返回true，否则返回false
     */
    @Override
    public boolean isUserOnline(Integer userId) {
        return onlineUsers.contains(userId);
    }

    
}
