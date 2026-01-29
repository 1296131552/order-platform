package com.company.user.service.facade;

import com.company.user.model.dto.LoginDTO;
import com.company.user.model.dto.RegisterDTO;
import com.company.user.model.vo.JwtVO;
import org.springframework.stereotype.Service;

@Service
public interface AuthFacade {
    /**
     * 用户登录
     * @param loginDTO 登录DTO
     * @return JwtVO
     */
    JwtVO login(LoginDTO loginDTO);

    /**
     * 用户注册
     * @param dto 注册DTO
     * @return JwtVO
     */
    JwtVO register(RegisterDTO dto);

    /**
     * 刷新令牌
     * @param refreshToken 刷新令牌
     * @return JwtVO
     */
    JwtVO refreshToken(String refreshToken);

    /**
     * 退出登录
     * @param accessToken 访问令牌
     * @param refreshToken 刷新令牌
     */
    void logout(String accessToken, String refreshToken);
}
