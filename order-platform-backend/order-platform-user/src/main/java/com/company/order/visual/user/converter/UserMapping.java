package com.company.order.visual.user.converter;

import com.company.order.visual.user.dto.UserCreateRequest;
import com.company.order.visual.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * User 实体 MapStruct 转换器
 * <p>
 * 职责：DTO → Entity 转换，消除手动 setter 冗余代码
 * <p>
 * 编译期自动生成实现类，性能优于反射
 */
@Mapper(
        componentModel = "spring",  // 使用 Spring 容器管理
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE  // 忽略 null 值
)
public interface UserMapping {

    /**
     * UserCreateRequest → User
     * <p>
     * 注意：password 字段需要单独编码处理，这里忽略
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)  // 密码单独处理
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "lastLoginTime", ignore = true)
    @Mapping(target = "lastLoginIp", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "isEnabled", constant = "true")
    @Mapping(target = "isLocked", constant = "false")
    @Mapping(target = "loginCount", constant = "0")
    User toEntity(UserCreateRequest request);
}
