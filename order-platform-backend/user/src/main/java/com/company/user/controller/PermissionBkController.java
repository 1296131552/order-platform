package com.company.user.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.company.common.model.dto.ResultDTO;
import com.company.common.utils.JwtUtil;
import com.company.user.enums.result.PermissionResultCode;
import com.company.user.model.dto.PermissionBkDTO;
import com.company.user.model.vo.PermissionBkVO;
import com.company.user.service.facade.PermissionBkFacade;

import jakarta.annotation.Resource;

/**
 * 页面管理
 */
@RestController
@RequestMapping("/permission/bk")
// @OperateModule("权限管理")
public class PermissionBkController {
    @Resource
    private PermissionBkFacade permissionBkFacade;
    @Resource
    private JwtUtil jwtUtil;

     /**
     * 获取页面树
     * @return 页面树
     */
    @GetMapping("/tree")
    // @OperateLog(type = OperationType.SELECT, description = "获取页面树")
    public ResultDTO<List<PermissionBkVO>> getViewTree(@RequestHeader("Authorization") String authHeader) {
        Integer userId = jwtUtil.getUserIdFromAuthHeader(authHeader);
        List<PermissionBkVO> viewTree = permissionBkFacade.getViewTree(userId);
        return ResultDTO.of(PermissionResultCode.GET_VIEW_TREE_SUCCESS, viewTree);
    }

    /**
     * 获取菜单树
     * @param authHeader 授权头
     * @return 菜单树
     */
    @GetMapping("/menu/tree")
    // @OperateLog(type = OperationType.SELECT, description = "获取菜单树")
    public ResultDTO<List<PermissionBkVO>> getMenuTree(@RequestHeader("Authorization") String authHeader) {
        Integer userId = jwtUtil.getUserIdFromAuthHeader(authHeader);
        List<PermissionBkVO> menuTree = permissionBkFacade.getMenuTree(userId);
        return ResultDTO.of(PermissionResultCode.GET_MENU_TREE_SUCCESS, menuTree);
    }

    /**
     * 获取菜单和按钮树
     * @param targetRoleId 目标角色ID
     * @param authHeader 授权头
     * @return 菜单和按钮树
     */
    @GetMapping("/menu/button/tree")
    // @OperateLog(type = OperationType.SELECT, description = "获取菜单和按钮树")
    public ResultDTO<List<PermissionBkVO>> getMenuAndButtonTree(@RequestParam(required = false) Integer targetRoleId, @RequestHeader("Authorization") String authHeader) {
        Integer userId = jwtUtil.getUserIdFromAuthHeader(authHeader);
        List<PermissionBkVO> menuAndButtonTree = permissionBkFacade.getMenuAndButtonTree(userId, targetRoleId);
        return ResultDTO.of(PermissionResultCode.GET_MENU_AND_BUTTON_TREE_SUCCESS, menuAndButtonTree);
    }

    /**
     * 获取权限列表
     * @param keyword 权限名称关键词
     * @return 权限列表
     */
    @GetMapping("/list")
    // @OperateLog(type = OperationType.SELECT, description = "获取权限列表")
    public ResultDTO<List<PermissionBkVO>> getViewList(@RequestParam(required = false) String keyword, @RequestHeader("Authorization") String authHeader) {
        Integer userId = jwtUtil.getUserIdFromAuthHeader(authHeader);
        List<PermissionBkVO> viewList = permissionBkFacade.getViewVOS(userId, keyword);
        return ResultDTO.of(PermissionResultCode.GET_LIST_SUCCESS, viewList);
    }

    /**
     * 获取权限码列表
     * @return 权限码列表
     */
    @GetMapping("/code")
    // @OperateLog(type = OperationType.SELECT, description = "获取权限码列表")
    public ResultDTO<List<String>> getPermissionCode(@RequestHeader("Authorization") String authHeader) {
        Integer userId = jwtUtil.getUserIdFromAuthHeader(authHeader);
        List<String> permissionCodeList = permissionBkFacade.getPermissionCode(userId);
        return ResultDTO.of(PermissionResultCode.GET_PERMISSION_CODE_SUCCESS, permissionCodeList);
    }

    /**
     * 添加权限
     * @param authHeader 授权头
     * @param permissionBkDTO 权限DTO
     * @return 结果
     */
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('permission:menu:add')")
    // @OperateLog(type = OperationType.INSERT, description = "添加权限")
    public ResultDTO<Void> addPermission(@RequestHeader("Authorization") String authHeader, @RequestBody @Validated(PermissionBkDTO.Add.class) PermissionBkDTO permissionBkDTO) {
        Integer userId = jwtUtil.getUserIdFromAuthHeader(authHeader);
        permissionBkFacade.addPermission(userId, permissionBkDTO);
        return ResultDTO.of(PermissionResultCode.ADD_SUCCESS);
    }

    /**
     * 修改权限
     * @param authHeader 授权头
     * @param permissionBkDTO 权限DTO
     * @return 结果
     */
    @PostMapping("/modify")
    @PreAuthorize("hasAuthority('permission:menu:modify')")
    // @OperateLog(type = OperationType.UPDATE, description = "修改权限")
    public ResultDTO<Void> modifyPermission(@RequestHeader("Authorization") String authHeader, @RequestBody @Validated(PermissionBkDTO.Modify.class) PermissionBkDTO permissionBkDTO) {
        Integer userId = jwtUtil.getUserIdFromAuthHeader(authHeader);
        permissionBkFacade.modifyPermission(userId, permissionBkDTO);
        return ResultDTO.of(PermissionResultCode.MODIFY_SUCCESS);
    }

    /**
     * 删除权限
     * @param authHeader 授权头
     * @param permissionId 权限ID
     * @return 结果
     */
    @GetMapping("/delete")
    @PreAuthorize("hasAuthority('permission:menu:delete')")
    // @OperateLog(type = OperationType.DELETE, description = "删除权限")
    public ResultDTO<Void> deletePermission(@RequestHeader("Authorization") String authHeader, @RequestParam Integer permissionId) {
        Integer userId = jwtUtil.getUserIdFromAuthHeader(authHeader);
        permissionBkFacade.deletePermission(userId, permissionId);
        return ResultDTO.of(PermissionResultCode.DELETE_SUCCESS);
    }

    /**
     * 移动权限
     * @param permissionId 权限ID
     * @param isUp 是否上移
     * @return 结果
     */
    @GetMapping("/move")
    @PreAuthorize("hasAuthority('permission:menu:move')")
    // @OperateLog(type = OperationType.UPDATE, description = "移动权限")
    public ResultDTO<Void> movePermission(@RequestParam Integer permissionId, @RequestParam Boolean isUp) {
        permissionBkFacade.movePermission(permissionId, isUp);
        return ResultDTO.of(PermissionResultCode.MOVE_SUCCESS);
    }

}
