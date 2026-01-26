package com.company.system.api.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.company.system.api.SettingApi;
import com.company.system.enums.model.SettingField;
import com.company.system.service.basic.SettingService;

import jakarta.annotation.Resource;

@Component
public class SettingApiImpl implements SettingApi{
    @Resource
    private SettingService settingService;

    /**
     * 是否仅显示有权限操作的数据
     * @return 设置值
     */
    @Override
    public boolean hasPermissionDisplay() {
        return settingService.getSettingValue(SettingField.PERMISSION_DISPLAY);
    }

     /**
     * 获取限制的用户角色数量
     * @return 限制的用户角色数量
     */
     @Override
     public Integer getUserRoleNumberLimit() {
        return settingService.getSettingValue(SettingField.USER_ROLE_NUMBER_LIMIT);
     }

    /**
     * 获取默认用户角色
     * @return 默认用户角色ID列表
     */
    @Override
    public List<Integer> getDefaultRoles() {
         return settingService.getSettingValue(SettingField.DEFAULT_ROLES);
     }
}
