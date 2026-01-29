package com.company.user.service.basic;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.user.model.entity.User;
public interface UserService extends IService<User> {
    /**
     * 校验用户名是否存在
     * @param username 用户名
     * @return true 存在 false 不存在
     */
    boolean validateUserExists(String username);

    /**
     * 校验用户ID是否存在
     * @param userId 用户ID
     * @return true 存在 false 不存在
     */
    boolean validateUserExists(Integer userId);

    /**
     * 创建用户
     * @param username 用户名
     * @param encodedPassword 加密后的密码
     * @return 用户ID
     */
    Integer createUser(String username, String encodedPassword);

    /**
     * 获取用户
     * @param username 用户名
     * @return 用户信息
     */
    User getUser(String username);

    /**
     * 封禁用户
     * @param userIds 要封禁的用户ID列表
     */
    void banUsers(List<Integer> userIds);

    /**
     * 解封用户
     * @param userIds 要解封的用户ID列表
     */
    void unbanUsers(List<Integer> userIds);

    /**
     * 根据创建时间范围查询用户
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户列表
     */
    List<User> listByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

     /**
     * 获取在指定时间范围内创建的用户数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 在指定时间范围内创建的用户数量
     */
    Long getCreatedUserCount(LocalDateTime startTime, LocalDateTime endTime);
}
