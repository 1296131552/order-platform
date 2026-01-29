package com.company.common.enums.result;

import com.company.common.enums.IResult;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 返回状态码枚举
 */
@Getter
@AllArgsConstructor
public enum GlobalResultCode implements IResult {
    SUCCESS(200, "操作成功"), 
    ERROR(500, "服务器错误"),
    PARAM_ERROR(400, "参数格式错误"),
    ENUM_ERROR(400, "枚举值格式错误"),
    UNAUTHORIZED(401, "请登录"),
    FORBIDDEN(403, "没有相关权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERFACE_NOT_EXIST(404, "接口不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    RATE_LIMIT_EXCEEDED(429, "访问过于频繁，请稍后再试"),
    ;

    private final Integer code;
    private final String message;
}
