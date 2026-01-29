package com.company.system.model.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.company.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局设置表
 * @TableName setting
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value ="setting")
@Data
public class Setting extends BaseEntity {
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
    private String value;

    /**
     * 构建ID到设置实体的映射
     * @param settings 设置实体列表
     * @return ID到设置实体的映射
     */
    public static Map<Integer, Setting> buildMap(List<Setting> settings) {
        return settings.stream().collect(Collectors.toMap(Setting::getId, setting -> setting));
    }
}