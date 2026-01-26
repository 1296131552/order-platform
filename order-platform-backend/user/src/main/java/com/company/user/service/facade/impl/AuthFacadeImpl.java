package com.company.user.service.facade.impl;

import com.company.common.exception.BusinessException;
import com.company.system.api.SettingApi;
import com.company.user.converter.AuthConverter;
import com.company.common.enums.result.AuthResultCode;
import com.company.user.model.dto.AddUserDTO;
import com.company.user.model.dto.LoginDTO;
import com.company.user.model.dto.RegisterDTO;
import com.company.user.model.vo.JwtVO;
import com.company.user.model.vo.UserVO;
import com.company.user.service.basic.UserService;
import com.company.user.service.domain.auth.AuthDomain;
import com.company.user.service.domain.user.UserOperationDomain;
import com.company.user.service.domain.user.UserQueryDomain;
import com.company.user.service.facade.AuthFacade;
import jakarta.annotation.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthFacadeImpl implements AuthFacade {
    @Resource
    private UserService userService;

    @Resource
    private AuthDomain authDomain;
    @Resource
    private UserQueryDomain userQueryDomain;
    @Resource
    private UserOperationDomain userOperationDomain;

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private SettingApi settingApi;

    /**
     * 用户登录
     * @param loginDTO 登录DTO
     * @return JwtVO
     */
    @Override
    public JwtVO login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        // 认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );

        // 获取用户信息（包含角色信息，同时会验证角色是否存在）
        UserVO user = userQueryDomain.getUser(username);

        // 更新SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成JwtVO
        return authDomain.login(user, authentication);
    }

    /**
     * 用户注册
     * @param dto 注册DTO
     * @return JwtVO
     */
    @Override
    public JwtVO register(RegisterDTO dto) {
        // 校验用户名是否存在
        if (userService.validateUserExists(dto.getUsername())) {
            throw new BusinessException(AuthResultCode.USERNAME_EXISTS);
        }
        // 校验密码是否一致
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(AuthResultCode.PASSWORD_MISMATCH);
        }

        // 注册用户
        AddUserDTO addUserDTO = AuthConverter.INSTANCE.toAddUserDTO(dto, settingApi.getDefaultRoles());
        Integer id = userOperationDomain.addUser(addUserDTO);

        // 登录注册用户
        return login(AuthConverter.INSTANCE.toLoginDTO(dto));
    }

    /**
     * 刷新令牌
     * @param refreshToken 刷新令牌
     * @return JwtVO
     */
    @Override
    public JwtVO refreshToken(String refreshToken) {
        return authDomain.refreshToken(refreshToken);
    }

    /**
     * 退出登录
     * @param accessToken 访问令牌
     * @param refreshToken 刷新令牌
     */
    @Override
    public void logout(String accessToken, String refreshToken) {
        // 将令牌加入黑名单
        authDomain.logout(accessToken, refreshToken);
        // 清除SecurityContext
        SecurityContextHolder.clearContext();
    }
}
