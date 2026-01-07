package com.order.platform.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.order.platform.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper
 *
 * 功能说明：
 * - 用户基础CRUD操作（继承BaseMapper）
 * - 用户登录查询
 * - 用户状态查询
 *
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户（登录验证用）
     *
     * 查询条件：
     * - 用户名匹配
     * - 未删除（is_deleted = 0）
     *
     * 注意：不包含 is_enabled 和 is_locked 条件
     * 这些状态检查在Service层进行，便于记录具体错误信息
     *
     * @param username 用户名
     * @return 用户实体，不存在返回null
     */
    @Select("SELECT * FROM t_user " +
            "WHERE username = #{username} " +
            "AND is_deleted = 0 " +
            "LIMIT 1")
    User selectByUsername(String username);

    /**
     * 根据用户编号查询用户
     *
     * @param userCode 用户编号
     * @return 用户实体，不存在返回null
     */
    @Select("SELECT * FROM t_user " +
            "WHERE user_code = #{userCode} " +
            "AND is_deleted = 0 " +
            "LIMIT 1")
    User selectByUserCode(String userCode);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户实体，不存在返回null
     */
    @Select("SELECT * FROM t_user " +
            "WHERE email = #{email} " +
            "AND is_deleted = 0 " +
            "LIMIT 1")
    User selectByEmail(String email);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户实体，不存在返回null
     */
    @Select("SELECT * FROM t_user " +
            "WHERE phone = #{phone} " +
            "AND is_deleted = 0 " +
            "LIMIT 1")
    User selectByPhone(String phone);
}
