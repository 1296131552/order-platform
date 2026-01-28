package com.company.system.service.basic;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.system.enums.model.SettingField;
import com.company.system.model.dto.SettingDTO;
import com.company.system.model.entity.Setting;
import com.company.system.model.vo.SettingVO;

public interface SettingService extends IService<Setting> {
     /**
     * 获取设置值
     * @param settingField 设置字段
     * @return 设置值
     */
    <T> T getSettingValue(SettingField settingField);

    /**
     * 获取公共设置值
     * @param settingField 设置字段
     * @return 设置值
     */
    <T> T getPublicSettingValue(SettingField settingField);

    /**
     * 获取所有设置值
     * @return 设置值
     */
    List<SettingVO> getSettingVOS();

    /**
     * 根据字段名获取设置
     * @param field 字段名
     * @return 设置
     */
    Setting getSettingByField(String field);

    /**
     * 根据字段名获取公共设置
     * @param field 字段名
     * @return 设置
     */
    Setting getPublicSettingByField(String field);

    /**
     * 修改设置
     * @param settingDTOS 设置DTO列表
     */
    void modifySettings(List<SettingDTO> settingDTOS);
}
