package com.order.platform.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.order.platform.common.enums.ResponseCode;
import com.order.platform.common.exception.BusinessException;
import com.order.platform.user.dto.request.UserCreateDTO;
import com.order.platform.user.dto.request.UserQueryDTO;
import com.order.platform.user.dto.request.UserUpdateDTO;
import com.order.platform.user.vo.UserPageVO;
import com.order.platform.user.entity.User;
import com.order.platform.user.mapper.UserMapper;
import com.order.platform.user.service.UserService;
import com.order.platform.user.utils.PasswordEncoderUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 *
 * 功能说明：
 * - 用户CRUD操作实现
 * - 用户分页查询实现
 * - 用户状态管理实现
 * - 唯一性校验实现
 *
 * 核心功能：
 * - 创建用户（密码加密、生成用户编号）
 * - 更新用户（部分字段更新、唯一性校验）
 * - 删除用户（逻辑删除）
 * - 分页查询（多条件筛选）
 * - 状态管理（启用/禁用/锁定/解锁）
 *
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoderUtil passwordEncoderUtil;

    // ==================== 查询操作 ====================

    @Override
    public Page<UserPageVO> queryUserPage(UserQueryDTO queryDTO) {
        log.info("分页查询用户，查询条件：{}", queryDTO);

        // 1. 构建分页对象
        Page<User> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 用户名模糊查询
        if (StringUtils.hasText(queryDTO.getUsername())) {
            wrapper.like(User::getUsername, queryDTO.getUsername());
        }

        // 真实姓名模糊查询
        if (StringUtils.hasText(queryDTO.getRealName())) {
            wrapper.like(User::getRealName, queryDTO.getRealName());
        }

        // 邮箱模糊查询
        if (StringUtils.hasText(queryDTO.getEmail())) {
            wrapper.like(User::getEmail, queryDTO.getEmail());
        }

        // 手机号精确查询
        if (StringUtils.hasText(queryDTO.getPhone())) {
            wrapper.eq(User::getPhone, queryDTO.getPhone());
        }

        // 部门ID精确查询
        if (queryDTO.getDepartmentId() != null) {
            wrapper.eq(User::getDepartmentId, queryDTO.getDepartmentId());
        }

        // 是否启用精确查询
        if (queryDTO.getIsEnabled() != null) {
            wrapper.eq(User::getIsEnabled, queryDTO.getIsEnabled());
        }

        // 是否锁定精确查询
        if (queryDTO.getIsLocked() != null) {
            wrapper.eq(User::getIsLocked, queryDTO.getIsLocked());
        }

        // 3. 排序（默认按创建时间倒序）
        applySort(wrapper, queryDTO.getSortField(), queryDTO.getSortOrder());

        // 4. 执行分页查询
        Page<User> userPage = userMapper.selectPage(page, wrapper);

        // 5. 转换为VO
        Page<UserPageVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserPageVO> voList = userPage.getRecords().stream()
                .map(this::convertToPageVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        log.info("分页查询用户成功，总数：{}", voPage.getTotal());
        return voPage;
    }

    @Override
    public UserPageVO getUserById(Long userId) {
        log.info("查询用户详情，userId：{}", userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "用户不存在");
        }

        return convertToPageVO(user);
    }

    // ==================== 创建操作 ====================

    @Override
    @Transactional
    public Long createUser(UserCreateDTO createDTO, Long createdBy) {
        log.info("创建用户，username：{}, realName：{}", createDTO.getUsername(), createDTO.getRealName());

        // 校验1：用户名
        if (!isUsernameUnique(createDTO.getUsername())) {
            throw new BusinessException(
                ResponseCode.USER_ALREADY_EXISTS,
                "用户名[" + createDTO.getUsername() + "]已存在"
            );
        }

        // 校验2：邮箱
        if (StringUtils.hasText(createDTO.getEmail())) {
            if (!isEmailUnique(createDTO.getEmail())) {
                throw new BusinessException(
                    ResponseCode.USER_ALREADY_EXISTS,
                    "邮箱[" + createDTO.getEmail() + "]已存在"
                );
            }
        }
        
        // 校验3：手机号
        if (StringUtils.hasText(createDTO.getPhone())) {
            if (!isPhoneUnique(createDTO.getPhone())) {
                throw new BusinessException(
                    ResponseCode.USER_ALREADY_EXISTS,
                    "手机号[" + createDTO.getPhone() + "]已存在"
                );
            }
        }
        // 1. 转换DTO为Entity
        User user = new User();
        BeanUtils.copyProperties(createDTO, user);

        // 2. 密码BCrypt加密
        String encryptedPassword = passwordEncoderUtil.encode(createDTO.getPassword());
        user.setPassword(encryptedPassword);

        // 3. 生成用户编号（USER + 当前时间戳 + 随机数）
        String userCode = generateUserCode();
        user.setUserCode(userCode);

        // 4. 设置默认值
        user.setIsEnabled(1);  // 默认启用
        user.setIsLocked(0);   // 默认未锁定
        user.setLoginCount(0); // 登录次数为0

        // 5. 设置创建信息
        user.setCreatedBy(createdBy);

        // 6. 保存到数据库
        userMapper.insert(user);

        log.info("创建用户成功，userId：{}, userCode：{}", user.getId(), userCode);
        return user.getId();
    }

    // ==================== 更新操作 ====================

    @Override
    @Transactional
    public void updateUser(Long userId, UserUpdateDTO updateDTO, Long updatedBy) {
        log.info("更新用户，userId：{}", userId);

        // 1. 查询用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "用户不存在");
        }

        // ==================== 业务校验开始 ====================

        // 校验1：邮箱（条件校验 + 排除自己）
        if (StringUtils.hasText(updateDTO.getEmail())) {
            User emailUser = userMapper.selectByEmail(updateDTO.getEmail());
            if (emailUser != null && !emailUser.getId().equals(userId)) {
                log.warn("邮箱冲突：userId={}, email={}, existingUserId={}",
                    userId, updateDTO.getEmail(), emailUser.getId());
                throw new BusinessException(
                    ResponseCode.EMAIL_ALREADY_EXISTS,
                    "邮箱[" + updateDTO.getEmail() + "]已被其他用户使用"
                );
            }
            log.debug("邮箱校验通过：userId={}, email={}", userId, updateDTO.getEmail());
        }

        // 校验2：手机号（条件校验 + 排除自己）
        if (StringUtils.hasText(updateDTO.getPhone())) {
            User phoneUser = userMapper.selectByPhone(updateDTO.getPhone());
            if (phoneUser != null && !phoneUser.getId().equals(userId)) {
                log.warn("手机号冲突：userId={}, phone={}, existingUserId={}",
                    userId, updateDTO.getPhone(), phoneUser.getId());
                throw new BusinessException(
                    ResponseCode.PHONE_ALREADY_EXISTS,
                    "手机号[" + updateDTO.getPhone() + "]已被其他用户使用"
                );
            }
            log.debug("手机号校验通过：userId={}, phone={}", userId, updateDTO.getPhone());
        }

        // ==================== 业务校验结束 ====================

        // 1. 转换DTO为Entity（只复制非空字段）
        BeanUtils.copyProperties(updateDTO, user, "id", "username", "password");

        // 2. 如果修改了部门ID，同步更新部门名称（这里简化处理，实际应查询部门表）
        if (updateDTO.getDepartmentId() != null) {
            user.setDepartmentName(updateDTO.getDepartmentName());
        }

        // 3. 设置更新信息
        user.setUpdatedBy(updatedBy);

        // 4. 更新到数据库
        userMapper.updateById(user);

        log.info("更新用户成功，userId：{}", userId);
    }

    // ==================== 删除操作 ====================

    @Override
    @Transactional
    public void deleteUser(Long userId, Long operatorId) {
        log.info("删除用户，userId：{}, operatorId：{}", userId, operatorId);

        // 1. 查询用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "用户不存在");
        }

        // 2. 不能删除自己
        if (userId.equals(operatorId)) {
            throw new BusinessException(ResponseCode.FORBIDDEN, "不能删除自己");
        }

        // 3. 逻辑删除
        userMapper.deleteById(userId);

        log.info("删除用户成功，userId：{}", userId);
    }

    // ==================== 状态管理 ====================

    @Override
    @Transactional
    public void updateUserStatus(Long userId, Integer isEnabled, Long operatorId) {
        log.info("更新用户状态，userId：{}, isEnabled：{}", userId, isEnabled);

        // 1. 查询用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "用户不存在");
        }

        // 2. 不能禁用自己
        if (userId.equals(operatorId) && isEnabled == 0) {
            throw new BusinessException(ResponseCode.FORBIDDEN, "不能禁用自己");
        }

        // 3. 更新状态
        user.setIsEnabled(isEnabled);
        userMapper.updateById(user);

        log.info("更新用户状态成功，userId：{}, isEnabled：{}", userId, isEnabled);
    }

    @Override
    @Transactional
    public void lockUser(Long userId, String reason, Long operatorId) {
        log.info("锁定用户，userId：{}, reason：{}", userId, reason);

        // 1. 查询用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "用户不存在");
        }

        // 2. 锁定用户
        user.setIsLocked(1);
        user.setLockedTime(LocalDateTime.now());
        user.setLockedReason(reason);
        userMapper.updateById(user);

        log.info("锁定用户成功，userId：{}", userId);
    }

    @Override
    @Transactional
    public void unlockUser(Long userId, Long operatorId) {
        log.info("解锁用户，userId：{}", userId);

        // 1. 查询用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "用户不存在");
        }

        // 2. 解锁用户
        user.setIsLocked(0);
        user.setLockedTime(null);
        user.setLockedReason(null);
        userMapper.updateById(user);

        log.info("解锁用户成功，userId：{}", userId);
    }

    // ==================== 唯一性校验 ====================

    @Override
    public boolean isUsernameUnique(String username) {
        // TODO(human): 请在这里实现用户名唯一性校验
        // 1. 调用 userMapper.selectByUsername(username) 查询用户
        // 2. 如果查询结果为 null，说明用户名可用，返回 true
        // 3. 如果查询结果不为 null，说明用户名已存在，返回 false

        // 示例代码框架：
        // User existingUser = userMapper.selectByUsername(username);
        // return existingUser == null; 
        User existingUserByUsername = userMapper.selectByUsername(username);
        return existingUserByUsername == null;
    }

    @Override
    public boolean isEmailUnique(String email) {
        // TODO(human): 请在这里实现邮箱唯一性校验
        // 1. 调用 userMapper.selectByEmail(email) 查询用户
        // 2. 如果查询结果为 null，说明邮箱可用，返回 true
        // 3. 如果查询结果不为 null，说明邮箱已存在，返回 false
        User existingUserByEmail = userMapper.selectByEmail(email);
        return existingUserByEmail == null;
    }

    @Override
    public boolean isPhoneUnique(String phone) {
        // TODO(human): 请在这里实现手机号唯一性校验
        // 1. 调用 userMapper.selectByPhone(phone) 查询用户
        // 2. 如果查询结果为 null，说明手机号可用，返回 true
        // 3. 如果查询结果不为 null，说明手机号已存在，返回 false
        User existingUserByPhone = userMapper.selectByPhone(phone);
        return existingUserByPhone == null;
    }

    // ==================== 辅助方法 ====================

    /**
     * 转换User实体为UserPageVO
     *
     * @param user 用户实体
     * @return UserPageVO
     */
    private UserPageVO convertToPageVO(User user) {
        return UserPageVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .userCode(user.getUserCode())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .departmentId(user.getDepartmentId())
                .departmentName(user.getDepartmentName())
                .position(user.getPosition())
                .employeeNo(user.getEmployeeNo())
                .isEnabled(user.getIsEnabled())
                .isLocked(user.getIsLocked())
                .lockedTime(user.getLockedTime())
                .lockedReason(user.getLockedReason())
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .loginCount(user.getLoginCount())
                .createdAt(user.getCreatedAt())
                .createdBy(user.getCreatedBy())
                .updatedAt(user.getUpdatedAt())
                .remark(user.getRemark())
                .build();
    }

    /**
     * 生成用户编号
     *
     * 格式：USER + 时间戳后8位 + 4位随机数
     * 示例：USER202401011234
     *
     * @return 用户编号
     */
    private String generateUserCode() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 10000);
        return String.format("USER%08d%04d", timestamp % 100000000, random);
    }

    /**
     * 应用动态排序
     *
     * @param wrapper   LambdaQueryWrapper
     * @param sortField 排序字段名
     * @param sortOrder 排序方向（ASC/DESC）
     */
    private void applySort(LambdaQueryWrapper<User> wrapper, String sortField, String sortOrder) {
        // 默认排序：按创建时间倒序
        if (!StringUtils.hasText(sortField)) {
            wrapper.orderByDesc(User::getCreatedAt);
            return;
        }

        // 根据字段名映射到Lambda表达式（类型安全）
        boolean isAsc = "ASC".equalsIgnoreCase(sortOrder);
        switch (sortField) {
            case "id":
                wrapper.orderBy(true, !isAsc, User::getId);
                break;
            case "username":
                wrapper.orderBy(true, !isAsc, User::getUsername);
                break;
            case "realName":
                wrapper.orderBy(true, !isAsc, User::getRealName);
                break;
            case "email":
                wrapper.orderBy(true, !isAsc, User::getEmail);
                break;
            case "phone":
                wrapper.orderBy(true, !isAsc, User::getPhone);
                break;
            case "departmentId":
                wrapper.orderBy(true, !isAsc, User::getDepartmentId);
                break;
            case "createdAt":
                wrapper.orderBy(true, !isAsc, User::getCreatedAt);
                break;
            case "updatedAt":
                wrapper.orderBy(true, !isAsc, User::getUpdatedAt);
                break;
            case "lastLoginTime":
                wrapper.orderBy(true, !isAsc, User::getLastLoginTime);
                break;
            case "loginCount":
                wrapper.orderBy(true, !isAsc, User::getLoginCount);
                break;
            default:
                // 未知字段，使用默认排序
                log.warn("未知的排序字段: {}，使用默认排序", sortField);
                wrapper.orderByDesc(User::getCreatedAt);
        }
    }
}
