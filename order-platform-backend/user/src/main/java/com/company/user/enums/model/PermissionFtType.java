package com.company.user.enums.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.company.common.enums.CodeEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PermissionFtType implements PermissionType {
    PAGE(1, "页面"),
    BUTTON(2, "按钮")
    ;
    @EnumValue
    private final Integer code;
    @JsonValue
    private final String type;

    public static PermissionFtType of(Integer code) {
        return CodeEnum.getByCode(PermissionFtType.class, code);
    }
}