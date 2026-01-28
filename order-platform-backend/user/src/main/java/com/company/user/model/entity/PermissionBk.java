package com.company.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.user.enums.model.PermissionBkType;
import com.company.user.model.entity.base.BasePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 页面表
 * @TableName view
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="permission_bk")
@Data
public class PermissionBk extends BasePermission {
    /**
     * 菜单的图标
     */
    private String icon;

    /**
     * 页面类型(1:目录, 2:菜单, 3:页面)
     */
    private PermissionBkType type;
}