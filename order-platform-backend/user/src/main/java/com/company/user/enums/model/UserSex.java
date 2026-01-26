package com.company.user.enums.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.company.common.enums.CodeEnum;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserSex implements CodeEnum<Integer>{
    MAN(1, "男"),
    WOMAN(2, "女"),
    UNKNOWN(3, "未知");
    
    @EnumValue
    private final Integer code;

    @JsonValue
    private final String sex;

    public static UserSex getByCode(Integer code) {
        return CodeEnum.getByCode(UserSex.class, code);
    }
}
