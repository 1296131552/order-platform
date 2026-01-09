package com.order.platform.user.service;

import com.order.platform.common.dto.CurrentUserDTO;
import com.order.platform.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 认证辅助工具
 *
 * 功能说明：
 * - User 实体 → CurrentUserDTO 转换
 * - 解耦 User 模块与 Common 模块
 * - 只提取必要字段到 CurrentUserDTO
 *
 * 设计原则：
 * - User 实体有25字段（完整用户信息）
 * - CurrentUserDTO 有11字段（认证层信息）
 * - 只复制Token中需要携带的核心字段
 *
 * 转换字段映射：
 * - User.id → CurrentUserDTO.id
 * - User.username → CurrentUserDTO.username
 * - User.realName → CurrentUserDTO.realName
 * - User.email → CurrentUserDTO.email
 * - User.phone → CurrentUserDTO.phone
 * - User.avatar → CurrentUserDTO.avatar
 * - User.departmentId → CurrentUserDTO.departmentId
 * - User.departmentName → CurrentUserDTO.departmentName
 * - User.userCode → CurrentUserDTO.userCode
 * - User.employeeNo → CurrentUserDTO.employeeNo
 * - User.position → CurrentUserDTO.position
 * - roles（参数传入） → CurrentUserDTO.roles
 *
 * 不转换的字段（User有但CurrentUserDTO没有）：
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
     * 将 User 实体转换为 CurrentUserDTO
     *
     * 使用场景：
     * - 用户登录成功后，构建CurrentUserDTO存入ThreadLocal
     * - 生成Token时，将用户信息存入Claims
     * - 从数据库查询User后，返回给前端
     *
     * @param user  User 实体（25字段）
     * @param roles 角色代码列表
     * @return CurrentUserDTO（11字段）
     */
    public CurrentUserDTO toCurrentUserDTO(User user, List<String> roles) {
        if (user == null) {
            log.warn("User实体为null，返回null");
            return null;
        }

        if (roles == null) {
            roles = List.of();
        }

        return CurrentUserDTO.builder()
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
     * 将 User 实体转换为 CurrentUserDTO（无角色）
     *
     * 使用场景：
     * - Token中没有角色信息时
     * - 不需要角色信息的场景
     *
     * @param user User 实体
     * @return CurrentUserDTO（roles为空列表）
     */
    public CurrentUserDTO toCurrentUserDTO(User user) {
        return toCurrentUserDTO(user, List.of());
    }
}
