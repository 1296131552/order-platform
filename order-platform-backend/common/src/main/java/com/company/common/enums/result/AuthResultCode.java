package com.company.common.enums.result;

import com.company.common.enums.IResult;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户模块特定的返回状态码枚举
 */
@Getter
@AllArgsConstructor
public enum AuthResultCode implements IResult {

    USERNAME_EXISTS(1001, "用户名已存在"),
    INVALID_CREDENTIALS(1002, "用户名或密码错误"),
    INVALID_REFRESH_TOKEN(1003, "无效的刷新令牌"),
    REFRESH_TOKEN_BLACKLISTED(1004, "刷新令牌已失效，请重新登录"),
    REFRESH_TOKEN_EXPIRED(1005, "刷新令牌已过期，请重新登录"),
    USERNAME_NOT_EXISTS(1006, "用户名不存在"),
    USER_BANNED(1007, "用户被封禁, 请联系管理员解封"),
    AUTHORIZATION_HEADER_INVALID(1008, "Authorization 头格式错误"),
    PASSWORD_MISMATCH(1009, "密码不一致"),

    REGISTER_SUCCESS(GlobalResultCode.SUCCESS.getCode(), "注册成功"),
    LOGIN_SUCCESS(GlobalResultCode.SUCCESS.getCode(), "登录成功"),
    LOGOUT_SUCCESS(GlobalResultCode.SUCCESS.getCode(), "退出成功"),
    ;

    private final Integer code;
    private final String message;
} 
