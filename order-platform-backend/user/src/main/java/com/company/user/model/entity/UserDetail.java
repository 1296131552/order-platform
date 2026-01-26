package com.company.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.common.model.entity.BaseEntity;
import com.company.user.enums.model.UserSex;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_user_detail")
@Data
public class UserDetail extends BaseEntity{
    /**
     * 用户的名字
     */
    private String name;

    /**
     * 用户头像链接
     */
    private String avatarUrl;

    /**
     * 性别(1:男,2:女,3:未知)
     */
    private UserSex sex;

    /**
     * 签名
     */
    private String signature;
}
