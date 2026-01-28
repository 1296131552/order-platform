package com.company.system.converter;

import java.util.List;

import org.mapstruct.Mapper;

import com.company.system.model.entity.Setting;
import com.company.system.model.vo.SettingVO;

@Mapper(componentModel = "spring")
public interface SettingConverter {

    /**
     * 将设置实体转换为设置VO
     * @param setting 设置实体
     * @return 设置VO
     */
    SettingVO toSettingVO(Setting setting);

    /**
     * 将设置实体列表转换为设置VO列表
     * @param settings 设置实体列表
     * @return 设置VO列表
     */
    List<SettingVO> toSettingVOS(List<Setting> settings);
}
