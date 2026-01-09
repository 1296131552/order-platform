package com.order.platform.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.order.platform.common.annotation.OperationLog;
import com.order.platform.common.annotation.RequireLogin;
import com.order.platform.common.enums.BusinessType;
import com.order.platform.common.enums.OperationModule;
import com.order.platform.common.enums.OperationType;
import com.order.platform.common.holder.CurrentUserHolder;
import com.order.platform.common.response.Result;
import com.order.platform.user.dto.request.UserCreateDTO;
import com.order.platform.user.dto.request.UserQueryDTO;
import com.order.platform.user.dto.request.UserUpdateDTO;
import com.order.platform.user.vo.UserPageVO;
import com.order.platform.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 *
 * 功能说明：
 * - 用户CRUD操作
 * - 用户分页查询（支持多条件筛选）
 * - 用户状态管理（启用/禁用/锁定/解锁）
 *
 * 接口列表：
 * - GET    /api/users/page       - 分页查询用户
 * - GET    /api/users/{id}       - 查询用户详情
 * - POST   /api/users            - 创建用户
 * - PUT    /api/users/{id}       - 更新用户
 * - DELETE /api/users/{id}       - 删除用户
 * - PATCH  /api/users/{id}/status - 启用/禁用用户
 * - PATCH  /api/users/{id}/lock   - 锁定用户
 * - PATCH  /api/users/{id}/unlock - 解锁用户
 *
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户CRUD、状态管理等接口")
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户
     *
     * 接口说明：
     * - 支持多条件组合查询
     * - 支持分页
     * - 支持排序
     *
     * 查询条件：
     * - username：用户名（模糊查询）
     * - realName：真实姓名（模糊查询）
     * - email：邮箱（模糊查询）
     * - phone：手机号（精确查询）
     * - departmentId：部门ID（精确查询）
     * - isEnabled：是否启用（精确查询）
     * - isLocked：是否锁定（精确查询）
     *
     * 请求示例：
     * <pre>
     * GET /api/users/page?current=1&size=10&username=zhang
     * </pre>
     *
     * @param queryDTO 查询请求DTO
     * @return 分页结果
     */
    @GetMapping("/page")
    @RequireLogin
    @Operation(summary = "分页查询用户", description = "支持多条件筛选、分页、排序")
    public Result<Page<UserPageVO>> queryUserPage(UserQueryDTO queryDTO) {
        log.info("分页查询用户，查询条件：{}", queryDTO);
        Page<UserPageVO> page = userService.queryUserPage(queryDTO);
        return Result.success(page);
    }

    /**
     * 查询用户详情
     *
     * 接口说明：
     * - 根据用户ID查询用户完整信息
     * - 包含用户角色列表
     *
     * 请求示例：
     * <pre>
     * GET /api/users/1
     * </pre>
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    @RequireLogin
    @Operation(summary = "查询用户详情", description = "根据ID查询用户完整信息")
    public Result<UserPageVO> getUserById(
            @Parameter(description = "用户ID", required = true)
            @PathVariable("id") Long userId) {
        log.info("查询用户详情，userId：{}", userId);
        UserPageVO userVO = userService.getUserById(userId);
        return Result.success(userVO);
    }

    /**
     * 创建用户
     *
     * 接口说明：
     * - 创建新用户
     * - 密码自动加密存储
     * - 用户编号自动生成
     *
     * 请求示例：
     * <pre>
     * {
     *   "username": "zhangsan",
     *   "password": "123456",
     *   "realName": "张三",
     *   "email": "zhangsan@example.com",
     *   "phone": "13800138000",
     *   "departmentId": 10,
     *   "departmentName": "华东大区",
     *   "position": "客户经理"
     * }
     * </pre>
     *
     * @param createDTO 创建用户请求DTO
     * @return 创建的用户ID
     */
    @PostMapping
    @RequireLogin
    @Operation(summary = "创建用户", description = "创建新用户，密码自动加密")
    @OperationLog(
            module = OperationModule.USER,
            type = OperationType.CREATE,
            business = BusinessType.USER,
            description = "创建用户：#createDTO.username",
            operatorId = "#{@currentUserHolder.getUserId()}",
            operatorName = "#{@currentUserHolder.getRealName()}"
    )
    public Result<Long> createUser(@Valid @RequestBody UserCreateDTO createDTO) {
        log.info("创建用户，username：{}", createDTO.getUsername());

        // 获取当前用户ID
        Long currentUserId = CurrentUserHolder.getUserId();

        // 创建用户
        Long userId = userService.createUser(createDTO, currentUserId);

        return Result.success(userId);
    }

    /**
     * 更新用户
     *
     * 接口说明：
     * - 更新用户信息
     * - 只更新非空字段
     * - 用户名不可修改
     * - 密码通过单独接口修改
     *
     * 请求示例：
     * <pre>
     * {
     *   "realName": "张三三",
     *   "email": "zhangsan@example.com",
     *   "phone": "13800138000",
     *   "isEnabled": 1
     * }
     * </pre>
     *
     * @param userId    用户ID
     * @param updateDTO 更新用户请求DTO
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @RequireLogin
    @Operation(summary = "更新用户", description = "更新用户信息（部分字段）")
    @OperationLog(
            module = OperationModule.USER,
            type = OperationType.UPDATE,
            business = BusinessType.USER,
            businessId = "#userId",
            description = "更新用户信息",
            operatorId = "#{@currentUserHolder.getUserId()}",
            operatorName = "#{@currentUserHolder.getRealName()}"
    )
    public Result<Void> updateUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable("id") Long userId,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        log.info("更新用户，userId：{}", userId);

        // 获取当前用户ID
        Long currentUserId = CurrentUserHolder.getUserId();

        // 更新用户
        userService.updateUser(userId, updateDTO, currentUserId);

        return Result.success();
    }

    /**
     * 删除用户
     *
     * 接口说明：
     * - 逻辑删除用户
     * - 删除后用户无法登录
     * - 不能删除自己
     *
     * 请求示例：
     * <pre>
     * DELETE /api/users/10
     * </pre>
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @RequireLogin
    @Operation(summary = "删除用户", description = "逻辑删除用户，不能删除自己")
    @OperationLog(
            module = OperationModule.USER,
            type = OperationType.DELETE,
            business = BusinessType.USER,
            businessId = "#userId",
            description = "删除用户",
            operatorId = "#{@currentUserHolder.getUserId()}",
            operatorName = "#{@currentUserHolder.getRealName()}"
    )
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable("id") Long userId) {
        log.info("删除用户，userId：{}", userId);

        // 获取当前用户ID
        Long currentUserId = CurrentUserHolder.getUserId();

        // 删除用户
        userService.deleteUser(userId, currentUserId);

        return Result.success();
    }

    /**
     * 启用/禁用用户
     *
     * 接口说明：
     * - 切换用户启用状态
     * - 禁用后用户无法登录
     * - 不能禁用自己
     *
     * 请求示例：
     * <pre>
     * PATCH /api/users/10/status?isEnabled=0
     * </pre>
     *
     * @param userId    用户ID
     * @param isEnabled 是否启用（0-禁用，1-启用）
     * @return 操作结果
     */
    @PatchMapping("/{id}/status")
    @RequireLogin
    @Operation(summary = "启用/禁用用户", description = "切换用户启用状态，不能禁用自己")
    @OperationLog(
            module = OperationModule.USER,
            type = OperationType.UPDATE,
            business = BusinessType.USER,
            businessId = "#userId",
            description = "设置用户状态：isEnabled={{ isEnabled }}",
            operatorId = "#{@currentUserHolder.getUserId()}",
            operatorName = "#{@currentUserHolder.getRealName()}"
    )
    public Result<Void> updateUserStatus(
            @Parameter(description = "用户ID", required = true)
            @PathVariable("id") Long userId,
            @Parameter(description = "是否启用：0-禁用，1-启用", required = true)
            @RequestParam("isEnabled") Integer isEnabled) {
        log.info("更新用户状态，userId：{}, isEnabled：{}", userId, isEnabled);

        // 获取当前用户ID
        Long currentUserId = CurrentUserHolder.getUserId();

        // 更新状态
        userService.updateUserStatus(userId, isEnabled, currentUserId);

        return Result.success();
    }

    /**
     * 锁定用户
     *
     * 接口说明：
     * - 锁定用户账户
     * - 锁定后用户无法登录
     *
     * 请求示例：
     * <pre>
     * {
     *   "reason": "违规操作"
     * }
     * </pre>
     *
     * @param userId 用户ID
     * @param reason 锁定原因
     * @return 操作结果
     */
    @PatchMapping("/{id}/lock")
    @RequireLogin
    @Operation(summary = "锁定用户", description = "锁定用户账户")
    @OperationLog(
            module = OperationModule.USER,
            type = OperationType.UPDATE,
            business = BusinessType.USER,
            businessId = "#userId",
            description = "锁定用户：{{ reason }}",
            operatorId = "#{@currentUserHolder.getUserId()}",
            operatorName = "#{@currentUserHolder.getRealName()}"
    )
    public Result<Void> lockUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable("id") Long userId,
            @Parameter(description = "锁定原因", required = true)
            @RequestParam("reason") String reason) {
        log.info("锁定用户，userId：{}, reason：{}", userId, reason);

        // 获取当前用户ID
        Long currentUserId = CurrentUserHolder.getUserId();

        // 锁定用户
        userService.lockUser(userId, reason, currentUserId);

        return Result.success();
    }

    /**
     * 解锁用户
     *
     * 接口说明：
     * - 解锁用户账户
     * - 解锁后用户可以正常登录
     *
     * 请求示例：
     * <pre>
     * PATCH /api/users/10/unlock
     * </pre>
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @PatchMapping("/{id}/unlock")
    @RequireLogin
    @Operation(summary = "解锁用户", description = "解锁用户账户")
    @OperationLog(
            module = OperationModule.USER,
            type = OperationType.UPDATE,
            business = BusinessType.USER,
            businessId = "#userId",
            description = "解锁用户",
            operatorId = "#{@currentUserHolder.getUserId()}",
            operatorName = "#{@currentUserHolder.getRealName()}"
    )
    public Result<Void> unlockUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable("id") Long userId) {
        log.info("解锁用户，userId：{}", userId);

        // 获取当前用户ID
        Long currentUserId = CurrentUserHolder.getUserId();

        // 解锁用户
        userService.unlockUser(userId, currentUserId);

        return Result.success();
    }
}
