package com.company.user.model.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.company.user.model.entity.base.BaseRolePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色前台权限表
 * @TableName role_permission_ft
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="role_permission_ft")
@Data
public class RolePermissionFt extends BaseRolePermission {
    /**
     * 构造函数，显式继承父类构造函数
     * @param roleId 角色id
     * @param permissionId 权限id
     */
    public RolePermissionFt(Integer roleId, Integer permissionId) {
        super(roleId, permissionId);
    }
}