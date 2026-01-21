package com.company.order.visual.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.order.visual.user.dto.*;

public interface UserService {
    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户登出
     * <p>
     * 将Token加入黑名单，并移除活跃Token记录
     *
     * @param token JWT Token
     */
    void logout(String token);

    /**
     * # TODO 创建用户
     */
    Long createUser(UserCreateRequest request,Long operatorId);  

    /**
     * # TODO 更新用户
     */
    // void updateUser(UserUpdateRequest request);

    /**
     * # TODO 删除用户（软删除）
     */
    // void deleteUser(Long userId);

    /**
     * 根据ID获取用户详情
     */
    UserVO getUserById(Long userId);

    /**
     * 分页查询用户
     */
    Page<UserVO> pageUsers(UserQueryRequest request);

    // ==================== #TODO 以下接口等权限模块完成后再添加 ====================
    // void changePassword(Long userId, String oldPassword, String newPassword);
    // void resetPassword(Long userId, String newPassword);
}
