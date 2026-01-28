package com.company.user.service.facade;

import java.util.List;

import com.company.user.model.dto.PermissionBkDTO;
import com.company.user.model.vo.PermissionBkVO;

public interface PermissionBkFacade {
    /**
     * 获取权限树
     * @param userId 用户ID
     * @return 权限树
     */
    List<PermissionBkVO> getViewTree(Integer userId);

    /**
     * 获取菜单树
     * @param userId 用户ID
     * @return 菜单树
     */
    List<PermissionBkVO> getMenuTree(Integer userId);

    /**
     * 获取菜单和按钮树
     * @param userId 用户ID
     * @return 菜单和按钮树
     */
    List<PermissionBkVO> getMenuAndButtonTree(Integer userId, Integer targetRoleId);

    /**
     * 获取目录树
     * @param userId 用户ID
     * @return 目录树
     */
    List<PermissionBkVO> getDirectoryTree(Integer userId);

    /**
     * 获取权限列表
     * @param keyword 权限名称关键词
     * @return 权限列表
     */
    List<PermissionBkVO> getViewVOS(Integer userId, String keyword);

    /**
     * 获取权限码列表
     * @param userId 用户ID
     * @return 权限码列表
     */
    List<String> getPermissionCode(Integer userId);

    /**
     * 添加权限
     * @param userId 用户ID
     * @param permissionBkDTO 权限DTO
     */
    void addPermission(Integer userId, PermissionBkDTO permissionBkDTO);

    /**
     * 修改权限
     * @param userId 用户ID
     * @param permissionBkDTO 权限DTO
     */
    void modifyPermission(Integer userId, PermissionBkDTO permissionBkDTO);

    /**
     * 删除权限
     * @param userId 用户ID
     * @param viewId 权限ID
     */
    void deletePermission(Integer userId, Integer viewId);

    /**
     * 移动权限
     * @param viewId 权限ID
     * @param isUp 是否上移
     */
    void movePermission(Integer viewId, Boolean isUp);
}
