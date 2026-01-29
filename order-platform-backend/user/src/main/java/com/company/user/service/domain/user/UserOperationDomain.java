package com.company.user.service.domain.user;

import java.util.List;

import com.company.user.model.dto.AddUserDTO;
import com.company.user.model.dto.ModifyUserDTO;
import com.company.user.model.dto.UpdateUserDTO;
import com.company.user.model.dto.ChangePasswordDTO;

public interface UserOperationDomain {
    /**
     * 创建用户
     * @param addUserDTO 用户信息
     * @return 用户ID
     */
    Integer addUser(AddUserDTO addUserDTO);

    /**
     * 修改用户
     * @param modifyUserDTO 修改用户请求参数
     */
    void modifyUser(ModifyUserDTO modifyUserDTO);

    /**
     * 更新用户
     * @param updateUserDTO 更新用户请求参数
     */
    void updateUser(UpdateUserDTO updateUserDTO);

    /**
     * 封禁用户
     * @param userIds 用户ID列表
     */
    void banUsers(List<Integer> userIds);

    /**
     * 解封用户
     * @param userIds 用户ID列表
     */
    void unbanUsers(List<Integer> userIds);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param changePasswordDTO 修改密码请求参数
     */
    void changePassword(Integer userId, ChangePasswordDTO changePasswordDTO);

}
