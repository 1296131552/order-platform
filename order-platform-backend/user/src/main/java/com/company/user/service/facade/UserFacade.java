package com.company.user.service.facade;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.company.common.model.dto.PageQueryDTO;
import com.company.user.model.dto.AddUserDTO;
import com.company.user.model.dto.ChangePasswordDTO;
import com.company.user.model.dto.GetUsersDTO;
import com.company.user.model.dto.ModifyUserDTO;
import com.company.user.model.dto.UpdateUserDTO;
import com.company.user.model.vo.UserVO;

public interface UserFacade {
    /**
     * 添加用户
     * @param operatorUserId 操作人用户ID
     * @param addUserDTO 添加用户DTO
     *
     * @return 新增用户的ID
     */
    Integer addUser(Integer operatorUserId, AddUserDTO addUserDTO);

    /**
     * 获取用户
     * @param userId 用户ID
     * @return 用户VO
     */
    UserVO getUser(Integer userId);

    /**
     * 获取用户列表
     * @param currentUserId 当前用户ID
     * @param pageQueryDTO 分页查询DTO
     * @return 用户VO分页
     */
    IPage<UserVO> getUsers(Integer currentUserId, PageQueryDTO<GetUsersDTO> pageQueryDTO);

    /**
     * 修改用户
     * @param operatorUserId 操作人用户ID
     * @param modifyUserDTO 修改用户DTO
     */
    void modifyUser(Integer operatorUserId, ModifyUserDTO modifyUserDTO);

    /**
     * 更新用户
     * @param operatorUserId 操作人用户ID
     * @param updateUserDTO 更新用户DTO
     */
    void updateUser(Integer operatorUserId, UpdateUserDTO updateUserDTO);

    /**
     * 封禁用户
     * @param operatorUserId 操作人用户ID
     * @param userIds 用户ID列表
     */
    void banUsers(Integer operatorUserId, List<Integer> userIds);

    /**
     * 解封用户
     * @param operatorUserId 操作人用户ID
     * @param userIds 用户ID列表
     */
    void unbanUsers(Integer operatorUserId, List<Integer> userIds);

    /**
     * 改变密码
     * @param userId 用户ID
     * @param changePasswordDTO 改变密码DTO
     */
    void changePassword(Integer userId, ChangePasswordDTO changePasswordDTO);

}
