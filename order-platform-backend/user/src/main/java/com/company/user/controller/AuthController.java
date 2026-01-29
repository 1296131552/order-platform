package com.company.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.company.common.enums.result.AuthResultCode;
import com.company.common.model.dto.ResultDTO;
import com.company.common.utils.JwtUtil;
import com.company.user.enums.model.LoginType;
import com.company.user.model.dto.LoginDTO;
import com.company.user.model.dto.RegisterDTO;
import com.company.user.model.vo.JwtVO;
import com.company.user.service.facade.AuthFacade;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * 权限认证
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    @Resource
    private AuthFacade authFacade;
    @Resource
    private JwtUtil jwtUtil;
    /**
     * 用户登录
     */
    @PostMapping("/login")
    // @LoginLog(type = LoginType.LOGIN)
    public ResultDTO<JwtVO> login(@RequestBody @Valid LoginDTO loginDTO) {
        return ResultDTO.of(AuthResultCode.LOGIN_SUCCESS, authFacade.login(loginDTO));
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    // @LoginLog(type = LoginType.REGISTER)
    public ResultDTO<JwtVO> register(@RequestBody @Valid RegisterDTO registerDTO) {
        return ResultDTO.of(AuthResultCode.REGISTER_SUCCESS, authFacade.register(registerDTO));
    }

    /**
     * 刷新令牌
     * 当访问令牌过期, 服务器返回401, 客户端自动拦截并调用此接口
     */
    @GetMapping("/refresh")
    // @LoginLog(type = LoginType.REFRESH)
    public ResultDTO<JwtVO> refreshToken(@RequestParam String refreshToken) {
        return ResultDTO.of(AuthResultCode.LOGIN_SUCCESS, authFacade.refreshToken(refreshToken));
    }

    /**
     * 退出登录
     * 将当前访问令牌和对应的刷新令牌都加入Redis黑名单，使其立即失效
     * 同时清除SecurityContext
     */
    @GetMapping("/logout")
    // @LoginLog(type = LoginType.LOGOUT)
    public ResultDTO<Void> logout(@RequestHeader(value = "Authorization") String authHeader, @RequestParam(required = false) String refreshToken) {
        String accessToken = jwtUtil.extractToken(authHeader);
        authFacade.logout(accessToken, refreshToken);
        return ResultDTO.of(AuthResultCode.LOGOUT_SUCCESS);
    }
}
