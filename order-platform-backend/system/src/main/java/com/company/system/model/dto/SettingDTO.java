package com.company.system.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class SettingDTO {
    private Integer id;
    private Object value;

    /**
     * 从SettingDTO列表中提取ID列表
     * @param settingDTOs SettingDTO列表
     * @return ID列表
     */
    public static List<Integer> extractIds(List<SettingDTO> settingDTOs) {
        return settingDTOs.stream().map(SettingDTO::getId).toList();
    }
}

