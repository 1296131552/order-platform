package com.company.user.service.basic;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.user.model.entity.UserDetail;

public interface UserDetailService extends IService<UserDetail> {
    /**
     * 创建用户详情
     * @param userId 用户ID
     * @param userDetail 用户详情
     */
    void createDetail(Integer userId, UserDetail userDetail);
}
