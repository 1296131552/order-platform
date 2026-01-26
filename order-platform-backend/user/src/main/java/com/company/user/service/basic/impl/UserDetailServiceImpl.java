package com.company.user.service.basic.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.user.model.entity.UserDetail;
import com.company.user.service.basic.UserDetailService;
import com.company.user.mapper.UserDetailMapper;

@Service
public class UserDetailServiceImpl extends ServiceImpl<UserDetailMapper, UserDetail>
    implements UserDetailService {

    /**
     * 创建用户详情
     * @param userId 用户ID
     * @param userDetail 用户详情
     */
    @Override
    public void createDetail(Integer userId, UserDetail userDetail) {
        // 设置用户ID
        userDetail.setId(userId);
        // 保存用户详情
        save(userDetail);
    }


}
