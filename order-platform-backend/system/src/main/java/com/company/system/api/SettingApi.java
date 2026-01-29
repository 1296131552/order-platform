package com.company.system.api;

import java.util.List;

public interface SettingApi {
    /**
     * 是否仅显示有权限操作的数据
     * @return 设置值
     */
    boolean hasPermissionDisplay();

    /**
     * 获取限制的用户角色数量
     * @return 限制的用户角色数量
     */
    Integer getUserRoleNumberLimit();

    /**
     * 获取默认用户角色
     * @return 默认用户角色ID列表
     */
    List<Integer> getDefaultRoles();
}
