package com.company.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.common.model.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 用户主表
 * @TableName t_user
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="user")
@Data
@Accessors(chain = true) // 链式赋值
public class User extends BaseEntity{
    /**
     * 用户名
     */
    private String username;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 是否有效(1:有效 0:无效)
     */
    private Boolean isValid;
}
