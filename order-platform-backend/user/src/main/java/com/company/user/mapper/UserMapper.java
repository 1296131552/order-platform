package com.company.user.mapper;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.company.user.model.dto.GetUsersDTO;
import com.company.user.model.entity.User;
import com.company.user.model.vo.UserVO;

public interface UserMapper extends BaseMapper<User>{
    /**
     * 获取用户列表
     * @param params 获取用户列表请求参数
     * @return 用户列表
     */
    IPage<UserVO> getUsers(IPage<UserVO> page, @Param("params") GetUsersDTO params);

}
