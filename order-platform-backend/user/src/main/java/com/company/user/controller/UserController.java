package com.company.user.controller;

import java.util.List;

// import org.springframework.boot.actuate.endpoint.OperationType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.user.enums.result.UserResultCode;
import com.company.user.model.dto.AddUserDTO;
import com.company.user.model.dto.ChangePasswordDTO;
import com.company.user.model.dto.GetUsersDTO;
import com.company.user.model.dto.ModifyUserDTO;
import com.company.user.model.dto.UpdateUserDTO;
import com.company.user.model.vo.UserVO;
import com.company.user.service.facade.UserFacade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.company.common.model.dto.PageQueryDTO;
import com.company.common.model.dto.ResultDTO;
import com.company.common.utils.JwtUtil;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

/**
 * 用户管理
 */
@Tag(name = "用户管理", description = "用户信息管理、用户列表、封禁/解封等接口")
@RestController
// @OperateModule("用户管理")
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserFacade userFacade;
    @Resource
    private JwtUtil jwtUtil;

    /**
     * 添加用户
     */
    @Operation(summary = "添加用户", description = "管理员添加新用户，需要user:add权限")
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('user:add')")
    // @OperateLog(type = OperationType.INSERT, description = "添加用户") # TODO 加入审计，记录操作
    public ResultDTO<Void> addUser(@RequestBody @Valid AddUserDTO addUserDTO,
            @Parameter(description = "访问令牌 (格式: Bearer {token})", required = true) @RequestHeader(value = "Authorization") String authHeader) {
        Integer userId = jwtUtil.getUserIdFromAuthHeader(authHeader);
        userFacade.addUser(userId, addUserDTO);
        return ResultDTO.of(UserResultCode.USER_ADD_SUCCESS);
    }

    /**
     * 获取用户信息
     */
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的个人信息")
    @GetMapping("/get")
    // @OperateLog(type = OperationType.SELECT, description = "获取用户信息") # TODO
    public ResultDTO<UserVO> getUser(
            @Parameter(description = "访问令牌 (格式: Bearer {token})", required = true) @RequestHeader(value = "Authorization") String authHeader) {
        Integer userId = jwtUtil.getUserIdFromAuthHeader(authHeader);
        return ResultDTO.of(UserResultCode.USER_INFO_GET_SUCCESS, userFacade.getUser(userId));
    }

    /**
     * 获取用户列表
     * @param request 获取用户列表请求参数
     * @return 用户列表
     */
    @Operation(summary = "获取用户列表", description = "分页查询用户列表，支持条件筛选")
    @PostMapping("/list")
    // @OperateLog(type = OperationType.SELECT, description = "获取用户列表") # TODO
    public ResultDTO<IPage<UserVO>> getUserList(
            @Parameter(description = "访问令牌 (格式: Bearer {token})", required = true) @RequestHeader(value = "Authorization") String authHeader,
            @RequestBody @Valid PageQueryDTO<GetUsersDTO> request) {
        Integer userId = jwtUtil.getUserIdFromAuthHeader(authHeader);
        return ResultDTO.of(UserResultCode.USER_LIST_GET_SUCCESS, userFacade.getUsers(userId, request));
    }

    /**
     * 修改用户(指修改别的用户)
     */
    @Operation(summary = "修改用户", description = "管理员修改其他用户信息，需要user:modify权限")
    @PostMapping("/modify")
    @PreAuthorize("hasAuthority('user:modify')")    // 权限校验
    // @OperateLog(type = OperationType.UPDATE, description = "修改用户")
    public ResultDTO<Void> modifyUser(
            @Parameter(description = "访问令牌 (格式: Bearer {token})", required = true) @RequestHeader(value = "Authorization") String authHeader,
            @RequestBody @Valid ModifyUserDTO modifyUserDTO) {
        Integer userId = jwtUtil.getUserIdFromAuthHeader(authHeader);
        userFacade.modifyUser(userId, modifyUserDTO);
        return ResultDTO.of(UserResultCode.USER_MODIFY_SUCCESS);
    }
    /**
     * 更新用户信息(修改自己的个人信息)
     */
    @Operation(summary = "更新个人信息", description = "用户修改自己的个人信息")
    @PostMapping("/update")
    // @OperateLog(type = OperationType.UPDATE, description = "更新用户信息")
    public ResultDTO<Void> updateUser(
            @Parameter(description = "访问令牌 (格式: Bearer {token})", required = true) @RequestHeader(value = "Authorization") String authHeader,
            @RequestBody @Valid UpdateUserDTO request) {
        Integer userId = jwtUtil.getUserIdFromAuthHeader(authHeader);
        userFacade.updateUser(userId, request);
        return ResultDTO.of(UserResultCode.USER_INFO_UPDATE_SUCCESS);
    }

    /**
     * 封禁用户
     */
    @Operation(summary = "封禁用户", description = "管理员封禁指定用户，需要user:ban权限")
    @PostMapping("/ban")
    @PreAuthorize("hasAuthority('user:ban')")
    // @OperateLog(type = OperationType.UPDATE, description = "封禁用户")
    public ResultDTO<Void> banUser(
            @Parameter(description = "访问令牌 (格式: Bearer {token})", required = true) @RequestHeader(value = "Authorization") String authHeader,
            @RequestBody List<Integer> userIds) {
        Integer userId = jwtUtil.getUserIdFromAuthHeader(authHeader);
        userFacade.banUsers(userId, userIds);
        return ResultDTO.of(UserResultCode.USER_BAN_SUCCESS);
    }

    /**
     * 解封用户
     */
    @Operation(summary = "解封用户", description = "管理员解封被禁用的用户，需要user:unban权限")
    @PostMapping("/unban")
    @PreAuthorize("hasAuthority('user:unban')") // # 权限校验
    // @OperateLog(type = OperationType.UPDATE, description = "解封用户")
    public ResultDTO<Void> unbanUser(
            @Parameter(description = "访问令牌 (格式: Bearer {token})", required = true) @RequestHeader(value = "Authorization") String authHeader,
            @RequestBody List<Integer> userIds) {
        Integer userId = jwtUtil.getUserIdFromAuthHeader(authHeader);
        userFacade.unbanUsers(userId, userIds);
        return ResultDTO.of(UserResultCode.USER_UNBAN_SUCCESS);
    }

    /**
     * 修改密码
     * 需要用户先登录，通过JWT令牌获取当前用户身份
     */
    @Operation(summary = "修改密码", description = "用户修改自己的登录密码")
    @PostMapping("/change-password")
    // @OperateLog(type = OperationType.UPDATE, description = "修改密码")
    public ResultDTO<Void> changePassword(
            @Parameter(description = "访问令牌 (格式: Bearer {token})", required = true) @RequestHeader(value = "Authorization") String authHeader,
            @RequestBody @Valid ChangePasswordDTO changePasswordDTO) {
        Integer userId = jwtUtil.getUserIdFromAuthHeader(authHeader);
        userFacade.changePassword(userId, changePasswordDTO);
        return ResultDTO.of(UserResultCode.CHANGE_PASSWORD_SUCCESS);
    }
}
