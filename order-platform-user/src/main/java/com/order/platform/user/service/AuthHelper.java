package com.order.platform.user.service;

import com.order.platform.common.dto.CurrentUser;
import com.order.platform.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 认证辅助工具
 *
 * 功能说明：
 * - User 实体 → CurrentUser DTO 转换
 * - 解耦 User 模块与 Common 模块
 * - 只提取必要字段到 CurrentUser
 *
 * 设计原则：
 * - User 实体有25字段（完整用户信息）
 * - CurrentUser DTO 有11字段（认证层信息）
 * - 只复制Token中需要携带的核心字段
 *
 * 转换字段映射：
 * - User.id → CurrentUser.id
 * - User.username → CurrentUser.username
 * - User.realName → CurrentUser.realName
 * - User.email → CurrentUser.email
 * - User.phone → CurrentUser.phone
 * - User.avatar → CurrentUser.avatar
 * - User.departmentId → CurrentUser.departmentId
 * - User.departmentName → CurrentUser.departmentName
 * - User.userCode → CurrentUser.userCode
 * - User.employeeNo → CurrentUser.employeeNo
 * - User.position → CurrentUser.position
 * - roles（参数传入） → CurrentUser.roles
 *
 * 不转换的字段（User有但CurrentUser没有）：
 * - password：敏感信息，不应存入Token
 * - isEnabled, isLocked：状态信息，业务层使用
 * - loginCount, lastLoginTime, lastLoginIp：统计信息
 * - passwordChangedTime, passwordExpireTime：密码管理信息
 * - remark, createdAt, createdBy, updatedAt, updatedBy：系统字段
 *
 * @since 1.0.0
 */
@Slf4j
@Component
public class AuthHelper {

    /**
     * 将 User 实体转换为 CurrentUser DTO
     *
     * 使用场景：
     * - 用户登录成功后，构建CurrentUser存入ThreadLocal
     * - 生成Token时，将用户信息存入Claims
     * - 从数据库查询User后，返回给前端
     *
     * @param user  User 实体（25字段）
     * @param roles 角色代码列表
     * @return CurrentUser DTO（11字段）
     */
    public CurrentUser toCurrentUser(User user, List<String> roles) {
        if (user == null) {
            log.warn("User实体为null，返回null");
            return null;
        }

        if (roles == null) {
            roles = List.of();
        }

        return CurrentUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .roles(roles)
                .departmentId(user.getDepartmentId())
                .departmentName(user.getDepartmentName())
                .userCode(user.getUserCode())
                .employeeNo(user.getEmployeeNo())
                .position(user.getPosition())
                .build();
    }

    /**
     * 将 User 实体转换为 CurrentUser DTO（无角色）
     *
     * 使用场景：
     * - Token中没有角色信息时
     * - 不需要角色信息的场景
     *
     * @param user User 实体
     * @return CurrentUser DTO（roles为空列表）
     */
    public CurrentUser toCurrentUser(User user) {
        return toCurrentUser(user, List.of());
    }
}
