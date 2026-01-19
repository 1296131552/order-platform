package com.company.order.visual.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.order.visual.common.response.Result;
import com.company.order.visual.user.dto.UserQueryRequest;
import com.company.order.visual.user.dto.UserVO;
import com.company.order.visual.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理", description = "用户查询、管理")
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    private UserService userService;

    // 注意：登录/登出接口已移至 AuthController (/api/auth)

    @Operation(summary = "获取用户详情", description = "根据ID查询用户详细信息")
    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(@PathVariable Long userId) {
        UserVO userVO = userService.getUserById(userId);
        return Result.ok(userVO);
    }

    @Operation(summary = "分页查询用户", description = "支持多条件查询")
    @GetMapping("/list")
    public Result<Page<UserVO>> pageUsers(UserQueryRequest request) {
        Page<UserVO> page = userService.pageUsers(request);
        return Result.ok(page);
    }

    // TODO: 创建、更新、删除功能
}
