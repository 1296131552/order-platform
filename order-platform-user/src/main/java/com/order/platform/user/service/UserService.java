package com.order.platform.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.order.platform.user.dto.request.UserCreateDTO;
import com.order.platform.user.dto.request.UserQueryDTO;
import com.order.platform.user.dto.request.UserUpdateDTO;
import com.order.platform.user.vo.UserPageVO;

/**
 * 用户服务接口
 *
 * 功能说明：
 * - 用户CRUD操作
 * - 用户分页查询（支持多条件筛选）
 * - 用户状态管理（启用/禁用/锁定/解锁）
 * - 用户名/邮箱/手机号唯一性校验
 *
 * 核心功能：
 * - 创建用户（自动加密密码、生成用户编号）
 * - 更新用户（部分字段更新）
 * - 删除用户（逻辑删除）
 * - 分页查询（支持多条件筛选）
 * - 启用/禁用用户
 * - 锁定/解锁用户
 *
 * 业务规则：
 * - 用户名唯一性校验
 * - 邮箱唯一性校验
 * - 手机号唯一性校验
 * - 密码BCrypt加密存储
 * - 用户编号自动生成（USER + 流水号）
 *
 * @since 1.0.0
 */
public interface UserService {

    /**
     * 分页查询用户
     *
     * 功能说明：
     * - 支持多条件组合查询
     * - 支持分页
     * - 支持排序
     * - 返回用户角色列表
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
     * 排序字段：
     * - created_at：创建时间（默认）
     * - username：用户名
     * - login_count：登录次数
     *
     * @param queryDTO 查询请求DTO
     * @return 分页结果（包含用户列表和角色信息）
     */
    Page<UserPageVO> queryUserPage(UserQueryDTO queryDTO);

    /**
     * 根据ID查询用户详情
     *
     * 功能说明：
     * - 查询用户完整信息
     * - 包含用户角色列表
     * - 包含用户权限列表
     *
     * @param userId 用户ID
     * @return 用户详情VO，不存在返回null
     */
    UserPageVO getUserById(Long userId);

    /**
     * 创建用户
     *
     * 功能说明：
     * - 验证用户名唯一性
     * - 验证邮箱唯一性
     * - 验证手机号唯一性
     * - 密码BCrypt加密
     * - 生成用户编号（USER + 流水号）
     * - 设置默认值（is_enabled=1, is_locked=0, login_count=0）
     * - 记录操作日志
     *
     * 业务规则：
     * 1. 用户名必须唯一
     * 2. 邮箱必须唯一（如果提供）
     * 3. 手机号必须唯一（如果提供）
     * 4. 密码长度6-20字符
     * 5. 默认启用状态
     * 6. 默认未锁定状态
     *
     * 异常处理：
     * - 用户名已存在：抛出BusinessException("用户名已存在")
     * - 邮箱已存在：抛出BusinessException("邮箱已存在")
     * - 手机号已存在：抛出BusinessException("手机号已存在")
     *
     * @param createDTO 创建用户请求DTO
     * @param createdBy 创建人ID
     * @return 创建的用户ID
     */
    Long createUser(UserCreateDTO createDTO, Long createdBy);

    /**
     * 更新用户
     *
     * 功能说明：
     * - 验证用户存在性
     * - 验证邮箱唯一性（如果修改邮箱）
     * - 验证手机号唯一性（如果修改手机号）
     * - 只更新非空字段
     * - 更新部门名称（如果修改部门ID）
     * - 记录操作日志
     *
     * 业务规则：
     * 1. 用户必须存在
     * 2. 邮箱必须唯一（如果修改）
     * 3. 手机号必须唯一（如果修改）
     * 4. 用户名不可修改
     * 5. 密码通过单独接口修改
     *
     * 注意事项：
     * - 不更新用户名
     * - 不更新密码
     * - 不更新登录信息（last_login_time等）
     *
     * @param userId    用户ID
     * @param updateDTO 更新用户请求DTO
     * @param updatedBy 更新人ID
     */
    void updateUser(Long userId, UserUpdateDTO updateDTO, Long updatedBy);

    /**
     * 删除用户（逻辑删除）
     *
     * 功能说明：
     * - 逻辑删除（is_deleted=1）
     * - 软删除，数据可恢复
     * - 记录操作日志
     *
     * 业务规则：
     * 1. 用户必须存在
     * 2. 不能删除自己
     * 3. 删除后用户无法登录
     *
     * 异常处理：
     * - 用户不存在：抛出BusinessException("用户不存在")
     * - 不能删除自己：抛出BusinessException("不能删除自己")
     *
     * @param userId      用户ID
     * @param operatorId  操作人ID
     */
    void deleteUser(Long userId, Long operatorId);

    /**
     * 启用/禁用用户
     *
     * 功能说明：
     * - 切换用户启用状态
     * - 禁用后用户无法登录
     * - 记录操作日志
     *
     * 业务规则：
     * 1. 用户必须存在
     * 2. 不能禁用自己
     * 3. is_enabled：0-禁用，1-启用
     *
     * 异常处理：
     * - 用户不存在：抛出BusinessException("用户不存在")
     * - 不能禁用自己：抛出BusinessException("不能禁用自己")
     *
     * @param userId     用户ID
     * @param isEnabled  是否启用（0-禁用，1-启用）
     * @param operatorId 操作人ID
     */
    void updateUserStatus(Long userId, Integer isEnabled, Long operatorId);

    /**
     * 锁定用户
     *
     * 功能说明：
     * - 锁定用户账户
     * - 锁定后用户无法登录
     * - 设置锁定原因和锁定时间
     * - 记录操作日志
     *
     * 业务规则：
     * 1. 设置is_locked=1
     * 2. 设置locked_time
     * 3. 设置locked_reason
     *
     * @param userId     用户ID
     * @param reason     锁定原因
     * @param operatorId 操作人ID
     */
    void lockUser(Long userId, String reason, Long operatorId);

    /**
     * 解锁用户
     *
     * 功能说明：
     * - 解锁用户账户
     * - 清除锁定信息
     * - 记录操作日志
     *
     * 业务规则：
     * 1. 设置is_locked=0
     * 2. 清除locked_time
     * 3. 清除locked_reason
     *
     * @param userId     用户ID
     * @param operatorId 操作人ID
     */
    void unlockUser(Long userId, Long operatorId);

    /**
     * 验证用户名唯一性
     *
     * @param username 用户名
     * @return true-用户名可用，false-用户名已存在
     */
    boolean isUsernameUnique(String username);

    /**
     * 验证邮箱唯一性
     *
     * @param email 邮箱
     * @return true-邮箱可用，false-邮箱已存在
     */
    boolean isEmailUnique(String email);

    /**
     * 验证手机号唯一性
     *
     * @param phone 手机号
     * @return true-手机号可用，false-手机号已存在
     */
    boolean isPhoneUnique(String phone);
}
