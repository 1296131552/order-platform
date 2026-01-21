package com.company.order.visual.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.order.visual.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 原子更新登录信息
     * <p>
     * 使用数据库级别的 login_count + 1，避免并发覆盖丢失
     *
     * @param userId 用户ID
     */
    @Update("UPDATE t_user SET last_login_time = NOW(3), login_count = login_count + 1 WHERE id = #{userId}")
    void updateLoginInfo(Long userId);
}
