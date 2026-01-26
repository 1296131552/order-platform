package com.company.user.enums.model;

import com.company.common.enums.CodeEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor
public enum RoleType implements CodeEnum<Integer>{
    //  - 管理员：可以分配 ALL 或 GLOBAL 角色
    // - 部门主管：只能分配 CHILD（下属）角色
    // - 普通用户：只能查看 SELF（自己的）角色
    ALL(0),
    CHILD(1),
    GLOBAL(2),
    SELF(3),
    NOT_GLOBAL(4)
    ;
    private final Integer code;
}
