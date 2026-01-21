package com.company.order.visual.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.order.visual.common.handler.MetaObjectHandlerImpl;
import com.company.order.visual.common.response.Result;
import com.company.order.visual.user.dto.UserCreateRequest;
import com.company.order.visual.user.dto.UserQueryRequest;
import com.company.order.visual.user.dto.UserVO;
import com.company.order.visual.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 * <p>
 * 职责：
 * - 用户查询
 * - 用户信息管理
 * <p>
 * 设计说明：登录/登出接口已移至 AuthController (/api/auth)
 */
@Tag(name = "用户管理", description = "用户查询、管理接口（需认证）")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    // 注意：登录/登出接口已移至 AuthController (/api/auth)

    @Operation(
            summary = "获取用户详情",
            description = "根据用户ID查询用户详细信息，包括角色列表"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "查询成功",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "用户不存在"
            )
    })
    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(
            @Parameter(description = "用户ID", example = "1", required = true)
            @PathVariable Long userId) {
        UserVO userVO = userService.getUserById(userId);
        return Result.ok(userVO);
    }

    @Operation(
            summary = "分页查询用户",
            description = """
                    支持多条件组合查询用户列表。

                    **查询条件：**
                    - 账号信息：用户名、用户编码、邮箱、手机号（模糊查询）
                    - 基本信息：真实姓名、职位、工号（模糊查询）
                    - 状态筛选：是否启用、是否锁定
                    - 组织权限：部门ID、角色ID
                    - 时间范围：创建时间、最后登录时间

                    **排序：**
                    - 默认按创建时间倒序
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "查询成功",
                    useReturnTypeSchema = true
            )
    })
    @GetMapping("/list")
    public Result<Page<UserVO>> pageUsers(
            @Parameter(description = "用户查询条件")
            UserQueryRequest request) {
        Page<UserVO> page = userService.pageUsers(request);
        return Result.ok(page);
    }

    // TODO: 更新、删除功能
    @Operation(summary = "创建用户", description = "管理员创建新用户")
    @PostMapping
    public Result<Long> createUser(@Valid @RequestBody UserCreateRequest request) {
        // 从 ThreadLocal 获取当前登录用户 ID（JWT 认证过滤器已设置）
        Long operatorId = MetaObjectHandlerImpl.getOperatorId();

        Long newUserId = userService.createUser(request, operatorId);
        return Result.ok(newUserId);
    }
}
