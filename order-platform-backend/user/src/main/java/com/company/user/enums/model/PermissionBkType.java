package com.company.user.enums.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.company.common.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PermissionBkType implements PermissionType {
    DIRECTORY(1, "目录"),
    MENU(2, "菜单"),
    PAGE(3, "页面"),
    BUTTON(4, "按钮"),
    ;

    @EnumValue  // MyBatis-Plus会使用这个值存储到数据库
    private final Integer code;
    
    @JsonValue  // JSON序列化时返回这个值
    private final String type;

    public static PermissionBkType getByCode(Integer code) {
        return CodeEnum.getByCode(PermissionBkType.class, code);
    }
}