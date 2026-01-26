package com.company.user.service.domain.user.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.common.enums.result.UserResultCode;
import com.company.common.exception.BusinessException;
import com.company.common.model.dto.PageQueryDTO;
import com.company.user.converter.UserConverter;
import com.company.user.mapper.UserMapper;
import com.company.user.model.dto.GetUsersDTO;
import com.company.user.model.entity.Role;
import com.company.user.model.entity.User;
import com.company.user.model.entity.UserDetail;
import com.company.user.model.vo.UserVO;
import com.company.user.service.basic.UserDetailService;
import com.company.user.service.basic.UserOnlineService;
import com.company.user.service.basic.UserRoleService;
import com.company.user.service.basic.UserService;
import com.company.user.service.domain.user.UserQueryDomain;

import jakarta.annotation.Resource;
@Service
public class UserQueryDomainImpl implements UserQueryDomain{
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;
    @Resource
    private UserDetailService userDetailService;
    @Resource
    private UserRoleService userRoleService;
    @Resource
    private UserOnlineService userOnlineService;

    /**
     * 获取用户
     * @param currentUsername 当前用户名
     * @return 用户VO
     */
    @Override
    public UserVO getUser(String currentUsername) {
        // 获取用户
        User user = userService.getUser(currentUsername);

        // 获取用户详情
        UserDetail userDetail = userDetailService.getById(user.getId());

        // 获取用户角色
        List<Role> roles = userRoleService.getRolesByUserId(user.getId());

        return UserConverter.INSTANCE.toUserVO(user, userDetail, roles);
    }

    /**
     * 获取用户
     * @param userId 用户ID
     * @return 用户VO
     */
    @Override
    public UserVO getUser(Integer userId) {
        // 获取用户
        User user = userService.getById(userId);

        // 校验用户是否存在
        if (!userService.validateUserExists(userId)) {
            throw new BusinessException(UserResultCode.USER_NOT_EXISTS);
        }

        // 获取用户详情
        UserDetail userDetail = userDetailService.getById(user.getId());

        // 获取用户角色
        List<Role> roles = userRoleService.getRolesByUserId(user.getId());

        return UserConverter.INSTANCE.toUserVO(user, userDetail, roles);
    }

    /**
     * 获取用户列表
     * @param dto 获取用户列表请求参数
     * @param permittedRoleIds 有权限的角色ID列表
     * @param hasPermissionDisplay 是否仅显示有权限操作的用户
     * @return 用户列表
     */
    @Override
    public IPage<UserVO> getUsers(PageQueryDTO<GetUsersDTO> dto, List<Integer> permittedRoleIds, boolean hasPermissionDisplay) {
        // 获取基础用户列表
        IPage<UserVO> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        IPage<UserVO> users = userMapper.getUsers(page, dto.getParams());

        // 设置用户列表中每个用户的hasPermission属性
        setUsersHasPermission(permittedRoleIds, users);

        // 是否仅显示有权限操作的用户
        if (hasPermissionDisplay) {
            // 过滤出有权限操作的用户
            filterUsersHasPermission(users);
        }

        return users;
    }

    /**
     * 获取在线用户列表
     * @return 在线用户列表
     */
    @Override
    public List<UserVO> getOnlineUsers() {
        List<Integer> userIds = userOnlineService.getOnlineUserIds();
        if (CollectionUtils.isEmpty(userIds)) {
            return new ArrayList<>();
        }

        List<User> users = userService.listByIds(userIds);
        List<UserDetail> userDetails = userDetailService.listByIds(userIds);
        return UserConverter.INSTANCE.toUserVOS(users, userDetails);
    }

    /**
     * 验证用户列表中每个用户是否有指定的权限
     * @param permittedRoleIds 权限角色ID列表
     * @param users 用户列表
     */
    private void setUsersHasPermission(List<Integer> permittedRoleIds, IPage<UserVO> users) {
        // 遍历每个UserVO，检查权限
        for (UserVO userVO : users.getRecords()) {
            // 获取列表用户的角色
            List<Role> roles = userVO.getRoles();

            // 获取列表用户的角色ID
            List<Integer> userRoleIds = Role.extractIds(roles);

            // 如果用户的所有角色ID都包含在权限角色列表中，则hasPermission为true
            boolean hasPermission = new HashSet<>(permittedRoleIds).containsAll(userRoleIds);
            userVO.setHasPermission(hasPermission);
        }
    }

    /**
     * 过滤出用户列表中有权限的用户
     * @param users 用户列表
     */
    private void filterUsersHasPermission(IPage<UserVO> users) {
        users.getRecords().removeIf(userVO -> !userVO.getHasPermission());
    }
}
