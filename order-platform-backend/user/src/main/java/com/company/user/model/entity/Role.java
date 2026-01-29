package com.company.user.model.entity;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.common.model.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色表
 * @TableName t_role
 */
@TableName(value = "t_role")
@Data
@EqualsAndHashCode(callSuper = true)// 仅使用 id 字段计算 equals 和 hashCode
public class Role extends BaseEntity {
    /**
     * 角色名
     */
    private String name;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 父角色的ID(如果没有则为0)
     */
    private Integer parentNodeId;

    /**
     * 层级(如果是根节点则为0)
     */
    private Integer level;

    /**
     * 从角色列表中提取角色ID
     * @param roles 角色列表
     * @return 角色ID列表
     */
    public static List<Integer> extractIds(Collection<Role> roles) {
            return roles.stream()
                .map(Role::getId)
                .collect(Collectors.toList());
    }

    /**
     * 从角色列表中提取全局角色
     * @param roles 角色列表
     * @return 全局角色列表
     */
    public static List<Role> getGlobalRoles(List<Role> roles) {
        return roles.stream()
                .filter(role -> role.getParentNodeId() == -1)
                .collect(Collectors.toList());
    }

    /**
     * 构建角色ID到角色的映射
     * @param roles 角色列表
     * @return 角色ID到角色的映射
     */
    public static Map<Integer, Role> buildMap(List<Role> roles) {
        return roles.stream().collect(Collectors.toMap(Role::getId, role -> role));
    }

    /**
     * 构建父角色ID到子角色列表的映射
     * @param roles 角色列表
     * @return 父角色ID到子角色列表的映射
     */
    public static Map<Integer, List<Role>> buildParentChildMap(List<Role> roles) {
        return roles.stream()
                .filter(role -> role.getParentNodeId() != 0)
                .collect(Collectors.groupingBy(Role::getParentNodeId));
    }

    /**
     * 按创建时间从早到晚排序
     * @param roles 角色列表
     * @return 排序后的角色列表
     */
    public static List<Role> sortByCreateTime(Collection<Role> roles) {
        return roles.stream()
                .sorted(Comparator.comparing(Role::getCreateTime))
                .collect(Collectors.toList());
    }
}
