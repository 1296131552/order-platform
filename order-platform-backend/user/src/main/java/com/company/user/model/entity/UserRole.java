package com.company.user.model.entity;

import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.common.model.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
@EqualsAndHashCode(callSuper = true)
@TableName(value ="t_user_role")     // 中间表
@Data
public class UserRole extends BaseEntity {
    /**
     * 用户的ID(关联user表)
     */
    private Integer userId;
    
        /**
     * 角色的ID(关联role表)
     */
    private Integer roleId;

    /**
     * 从用户角色列表中提取角色ID列表
     * @param userRoles 用户角色列表
     * @return 角色ID列表
     */
    public static List<Integer> extractRoleIds(List<UserRole> userRoles) {
        return userRoles.stream().map(UserRole::getRoleId).distinct().collect(Collectors.toList());
    }
}
