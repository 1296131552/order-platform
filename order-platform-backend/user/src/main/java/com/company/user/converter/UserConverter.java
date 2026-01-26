package com.company.user.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.company.user.model.dto.AddUserDTO;
import com.company.user.model.dto.ModifyUserDTO;
import com.company.user.model.dto.UpdateUserDTO;
import com.company.user.model.entity.Role;
import com.company.user.model.entity.User;
import com.company.user.model.entity.UserDetail;
import com.company.user.model.vo.UserVO;

/**
 * DTO与实体类之间的转换器
 */
@Mapper(componentModel = "spring")
public interface UserConverter {
    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);

    /**
     * 将AddUserDTO对象转换为Detail对象
     */
    UserDetail addUserDTOToDetail(AddUserDTO request);

    /**
     * 将ModifyUserDTO对象转换为Detail对象
     */
    UserDetail modifyUserDTOToDetail(ModifyUserDTO request);

    /**
     * 将UpdateUserDTO对象转换为Detail对象
     */
    UserDetail updateUserDTOToDetail(UpdateUserDTO request);

    /**
     * 将User和Detail对象转换为UserVO对象
     */
    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "userDetail.createTime", target = "createTime")
    @Mapping(source = "userDetail.updateTime", target = "updateTime")
    UserVO toUserVO(User user, UserDetail userDetail, List<Role> roles);

    /**
     * 将User列表、Detail列表和Role列表转换为UserVO列表
     */
    default List<UserVO> toUserVOS(List<User> users, List<UserDetail> userDetails) {
        if (ObjectUtils.isEmpty(users) || ObjectUtils.isEmpty(userDetails)) {
            return new ArrayList<>();
        }
        List<UserVO> userVOS = new ArrayList<>();
        users.forEach(user -> {
            UserDetail userDetail = userDetails.stream()
                    .filter(d -> Objects.equals(d.getId(), user.getId()))
                    .findFirst()
                    .orElse(null);
            userVOS.add(toUserVO(user, userDetail, new ArrayList<>()));
        });
        return userVOS;
    }
}
