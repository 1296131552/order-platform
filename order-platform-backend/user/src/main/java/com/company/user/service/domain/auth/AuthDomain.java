package com.company.user.service.domain.auth;


import com.company.user.model.vo.JwtVO;
import com.company.user.model.vo.UserVO;
import org.springframework.security.core.Authentication;

public interface AuthDomain {
    /**
     * 用户登录
     * @param user 用户信息
     * @param authentication 认证信息
     * @return JwtVO
     */
    JwtVO login(UserVO user, Authentication authentication);

    /**
     * 刷新访问令牌
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
