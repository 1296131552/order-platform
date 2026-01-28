package com.company.user.converter;

import com.company.user.model.dto.AddUserDTO;
import com.company.user.model.dto.LoginDTO;
import com.company.user.model.dto.RegisterDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuthConverter {
    /**
     * 注册用户DTO转换为添加用户DTO
     * @param dto 注册用户DTO
     * @param defaultRoleIds 默认角色ID列表
     * @return 添加用户DTO
     */
    @Mapping(source = "dto.username", target = "name")
    @Mapping(source = "defaultRoleIds", target = "roleIds")
    AddUserDTO toAddUserDTO(RegisterDTO dto, List<Integer> defaultRoleIds);

    LoginDTO toLoginDTO(RegisterDTO dto);
}