package com.company.common.enums.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.company.common.enums.CodeEnum;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 状态枚举
 */
@Getter
@AllArgsConstructor
public enum Status implements CodeEnum<String> {

    SUCCESS("SUCCESS", "成功"),
    FAILED("FAILED", "失败"),
    ;

    @EnumValue
    private final String code;
    @JsonValue
    private final String description;

    /**
     * 根据编码获取枚举
     * @param code 编码
     * @return 枚举实例
     */
    public static Status getByCode(String code) {
        return CodeEnum.getByCode(Status.class, code);
    }
}