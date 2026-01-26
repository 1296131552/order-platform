package com.company.system.model.vo;

import lombok.Data;

@Data
public class SettingVO {
    /**
     * 设置ID
     */
    private Integer id;

    /**
     * 设置的字段名
     */
    private String field;

    /**
     * 设置的标题
     */
    private String title;

    /**
     * 设置的描述
     */
    private String description;

    /**
     * 设置的值
     */
    private Object value;
}
