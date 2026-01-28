package com.company.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.user.enums.model.PermissionFtType;
import com.company.user.model.entity.base.BasePermission;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 前台权限表
 * @TableName permission_ft
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="permission_ft")
@Data
public class PermissionFt extends BasePermission {
    /**
     * 类型 1-页面 2-按钮
     */
    private PermissionFtType type;

    /**
     * 是否需要验证登录
     */
    private Integer needVerify;
}