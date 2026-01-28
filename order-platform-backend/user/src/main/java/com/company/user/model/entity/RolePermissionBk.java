package com.company.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.user.model.entity.base.BaseRolePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 角色页面表
 * @TableName role_view
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="role_permission_bk")
@Data
public class RolePermissionBk extends BaseRolePermission {
    /**
     * 构造函数，显式继承父类构造函数
     * @param roleId 角色id
     * @param permissionId 权限id
     */
    public RolePermissionBk(Integer roleId, Integer permissionId) {
        super(roleId, permissionId);
    }
}
