package com.company.user.model.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;
import com.baomidou.mybatisplus.annotation.TableField;

import com.company.common.model.entity.BaseEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
public class BasePermission extends BaseEntity {
    /**
     * 权限名称
     */
    private String name;

    /**
     * 路径名
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 权限码
     */
    private String permissionCode;

    /**
     * 父节点ID(无父结点则为0)
     */
    private Integer parentId;

    /**
     * 权限排序
     */
    @TableField(value = "`order`")
    private Integer order;

    /**
     * 是否启用(1:启用 0:不启用)
     */
    private Boolean isValid;

    /**
     * 从权限列表中提取权限ID列表
     * @param permissions 权限列表
     * @return 权限ID列表
     */
    public static <T extends BasePermission> List<Integer> extractPermissionIds(List<T> permissions) {
        return permissions.stream().map(BasePermission::getId).distinct().collect(Collectors.toList());
    }

    /**
     * 构建权限Map，key为权限ID，value为权限对象
     * @param permissions 权限列表
     * @param <T> 权限类型，必须继承自BasePermission
     * @return 权限Map
     */
    public static <T extends BasePermission> Map<Integer, T> buildMap(List<T> permissions) {
        return permissions.stream().collect(Collectors.toMap(BasePermission::getId, permission -> permission));
    }

    /**
     * 构建父节点ID和子节点列表的Map，key为父节点ID，value为子节点列表
     * @param permissions 权限列表
     * @param <T> 权限类型，必须继承自BasePermission
     * @return 父节点ID和子节点列表的Map
     */
    public static <T extends BasePermission> Map<Integer, List<T>> buildParentChildMap(List<T> permissions) {
        return permissions.stream().collect(Collectors.groupingBy(BasePermission::getParentId));
    }

    /**
     * 从权限列表中提取权限码列表
     * @param permissions 权限列表
     * @param <T> 权限类型，必须继承自BasePermission
     * @return 权限码列表
     */
    public static <T extends BasePermission> List<String> extractPermissionCodes(List<T> permissions) {
        return permissions.stream().map(BasePermission::getPermissionCode).filter(StringUtils::hasText).distinct().collect(Collectors.toList());
    }

    /**
     * 将权限ID列表转换为无效权限列表
     * @param entityClass 权限实体类
     * @param permissionIds 权限ID列表
     * @param <T> 权限类型，必须继承自BasePermission
     * @return 无效权限列表
     */
    public static <T extends BasePermission> List<T> toNotValidPermission(Class<T> entityClass, List<Integer> permissionIds) {
        return permissionIds.stream().map(permissionId -> {
            T permission = null;
            try {
                permission = entityClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("创建权限实例失败", e);
            }
            permission.setId(permissionId);
            permission.setIsValid(false);
            return permission;
        }).collect(Collectors.toList());
    }
}
