package com.company.user.enums.model;

import java.util.List;

import com.company.common.enums.CodeEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RolesType implements CodeEnum<Integer>{
        /**
     * 所有角色
     */
    ALL(0),
    /**
     * 子角色
     */
    CHILD(1),
    /**
     * 子角色和全局角色
     */
    CHILD_AND_GLOBAL(2),
    /**
     * 全局角色
     */
    GLOBAL(3),
    /**
     * 子角色和当前用户角色
     */
    CHILD_AND_SELF(4),
    /**
     * 非全局角色
     */
    NOT_GLOBAL(5),
    ;

    private final Integer code;

    public static RolesType getByCode(Integer code) {
        return CodeEnum.getByCode(RolesType.class, code);
    }

    /**
     * 将RolesType映射为对应的RoleType列表
     * @return 对应的RoleType列表
     */
    public List<RoleType> toRoleTypes() {
        return switch (this) {
            case ALL -> List.of(RoleType.ALL);
            case CHILD -> List.of(RoleType.CHILD);
            case CHILD_AND_GLOBAL -> List.of(RoleType.CHILD, RoleType.GLOBAL);
            case GLOBAL -> List.of(RoleType.GLOBAL);
            case CHILD_AND_SELF -> List.of(RoleType.CHILD, RoleType.SELF);
            case NOT_GLOBAL -> List.of(RoleType.NOT_GLOBAL);
        };
    }
}
