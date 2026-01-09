package com.order.platform.common.enums;

import lombok.Getter;

/**
 * 响应码枚举
 */
@Getter
public enum ResponseCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    INVALID_PARAMETER(400, "参数错误"),

    // 用户相关 1000-1999
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_ALREADY_EXISTS(1003, "用户已存在"),
    TOKEN_INVALID(1004, "Token 无效"),
    TOKEN_EXPIRED(1005, "Token 已过期"),
    VALIDATION_ERROR(1006, "参数验证失败"),
    USER_DISABLED(1007, "账户已禁用"),
    USER_LOCKED(1008, "账户已锁定"),
    PASSWORD_ERROR(1009, "密码错误"),
    PASSWORD_EXPIRED(1010, "密码已过期"),
    EMAIL_ALREADY_EXISTS(1011, "邮箱已存在"),
    PHONE_ALREADY_EXISTS(1012, "手机号已存在"),
    USERNAME_ALREADY_EXISTS(1013, "用户名已存在"),
    USER_AUDIT_PENDING(1021, "账号正在审核中，请耐心等待或联系管理员"),
    USER_AUDIT_REJECTED(1022, "账号审核未通过，如需帮助请联系管理员"),
    USER_NO_ROLE(1023, "账号未分配角色，请联系管理员"),

    // 订单相关 2000-2999
    ORDER_NOT_FOUND(2001, "订单不存在"),
    ORDER_STATUS_ERROR(2002, "订单状态错误"),
    ORDER_CANNOT_CANCEL(2003, "订单无法取消"),
    ORDER_LINE_NOT_FOUND(2004, "订单行不存在"),

    // 发运相关 3000-3999
    SHIPMENT_NOT_FOUND(3001, "发运单不存在"),
    SHIPMENT_STATUS_ERROR(3002, "发运状态错误"),
    SHIPMENT_LINE_NOT_FOUND(3003, "快递单不存在"),
    SHIPMENT_LINE_STATUS_ERROR(3004, "快递单状态错误"),

    // 签收相关 4000-4999
    RECEIPT_NOT_FOUND(4001, "签收单不存在"),
    RECEIPT_ALREADY_CONFIRMED(4002, "签收单已确认"),

    // 异常相关 5000-5999
    EXCEPTION_NOT_FOUND(5001, "异常单不存在"),
    EXCEPTION_ALREADY_HANDLED(5002, "异常已处理");

    private final Integer code;
    private final String message;

    ResponseCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
