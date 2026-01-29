package com.company.user.service.basic;

import java.util.List;

public interface UserOnlineService {
    /**
     * 用户上线
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    void userOnline(Integer userId, String sessionId);

    /**
     * 用户下线
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    void userOffline(Integer userId, String sessionId);

    /**
     * 获取所有在线用户的ID列表
     * @return 在线用户ID列表
     */
    List<Integer> getOnlineUserIds();

    /**
     * 获取在线用户数量
     * @return 在线用户数量
     */
    int getOnlineCount();

    /**
     * 判断用户是否在线
     * @param userId 用户ID
     * @return 如果用户在线则返回true，否则返回false
     */
    boolean isUserOnline(Integer userId);
}
