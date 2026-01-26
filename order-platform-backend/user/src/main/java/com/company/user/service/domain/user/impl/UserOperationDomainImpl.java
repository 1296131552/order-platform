package com.company.user.service.domain.user.impl;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.common.enums.result.UserResultCode;
import com.company.common.exception.BusinessException;
import com.company.user.converter.UserConverter;
import com.company.user.model.dto.AddUserDTO;
import com.company.user.model.dto.ModifyUserDTO;
import com.company.user.model.dto.UpdateUserDTO;
import com.company.user.model.dto.ChangePasswordDTO;
import com.company.user.model.entity.User;
import com.company.user.model.entity.UserDetail;
import com.company.user.service.basic.UserDetailService;
import com.company.user.service.basic.UserRoleService;
import com.company.user.service.basic.UserService;
import com.company.user.service.domain.user.UserOperationDomain;

import jakarta.annotation.Resource;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserOperationDomainImpl implements UserOperationDomain{
    @Resource
    private UserService userService;
    @Resource
    private UserDetailService userDetailService;
    @Resource
    private UserRoleService userRoleService;
    @Resource
    private PasswordEncoder passwordEncoder;
    /**
     * 创建用户
     * @param addUserDTO 用户信息
     * @return 用户ID
     */
    @Override
    public Integer addUser(AddUserDTO addUserDTO) {
        // 新建用户
        String encodedPassword = passwordEncoder.encode(addUserDTO.getPassword());
        Integer userId = userService.createUser(addUserDTO.getUsername(), encodedPassword);

        // 绑定用户详情
        UserDetail userDetail = UserConverter.INSTANCE.addUserDTOToDetail(addUserDTO);
        userDetailService.createDetail(userId, userDetail);

        // 关联用户角色
        userRoleService.addUserRoles(userId, addUserDTO.getRoleIds());

        return userId;
    }
    /**
     * 修改用户
     * @param modifyUserDTO 修改用户请求参数
     */
    @Override
    public void modifyUser(ModifyUserDTO modifyUserDTO) {
        // 校验用户是否存在
        User user = userService.getById(modifyUserDTO.getId());
        if (user == null) {
            throw new BusinessException(UserResultCode.USER_NOT_EXISTS);
        }

        // 转换为Detail对象
        UserDetail userDetail = UserConverter.INSTANCE.modifyUserDTOToDetail(modifyUserDTO);

        // 更新Detail信息
        userDetailService.updateById(userDetail);

        // 更新用户角色
        userRoleService.updateUserRoles(user.getId(), modifyUserDTO.getRoleIds());
    }

      /**
     * 更新用户
     * @param updateUserDTO 更新用户请求参数
     */
    @Override
    public void updateUser(UpdateUserDTO updateUserDTO) {
        // 转换为Detail对象
        UserDetail userDetail = UserConverter.INSTANCE.updateUserDTOToDetail(updateUserDTO);

        // 更新Detail信息
        userDetailService.updateById(userDetail);
    }

    /**
     * 封禁用户
     * @param userIds 用户ID列表
     */
    @Override
    public void banUsers(List<Integer> userIds) {
        // 执行封禁
        userService.banUsers(userIds);
    }

    /**
     * 解封用户
     * @param userIds 用户ID列表
     */
    @Override
    public void unbanUsers(List<Integer> userIds) {
        // 执行解封
        userService.unbanUsers(userIds);
    }

    /**
     * 修改密码
     * @param userId 用户ID
     * @param changePasswordDTO 修改密码请求参数
     */
    @Override
    public void changePassword(Integer userId, ChangePasswordDTO changePasswordDTO) {
        // 获取用户
        User user = userService.getById(userId);

        // 加密并更新密码
        String encodedPassword = passwordEncoder.encode(changePasswordDTO.getNewPassword());
        userService.updateById(user.setPassword(encodedPassword));
    }
}
