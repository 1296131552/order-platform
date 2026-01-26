package com.company.user.service.domain.user;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.company.common.model.dto.PageQueryDTO;
import com.company.user.model.dto.GetUsersDTO;
import com.company.user.model.vo.UserVO;

public interface UserQueryDomain {
    /**
     * 获取用户
     * @param currentUsername 当前用户名
     * @return 用户VO
     */
    UserVO getUser(String currentUsername);

    /**
     * 获取用户
     * @param userId 用户ID
     * @return 用户VO
     */
    UserVO getUser(Integer userId);

    /**
     * 获取用户列表
     * @param dto 获取用户列表请求参数
     * @param permittedRoleIds 有权限的角色ID列表
     * @param hasPermissionDisplay 是否仅显示有权限操作的用户
     * @return 用户列表
     */
    IPage<UserVO> getUsers(PageQueryDTO<GetUsersDTO> dto, List<Integer> permittedRoleIds, boolean hasPermissionDisplay);

    /**
     * 获取在线用户列表
     * @return 在线用户列表
     */
    List<UserVO> getOnlineUsers();
}
