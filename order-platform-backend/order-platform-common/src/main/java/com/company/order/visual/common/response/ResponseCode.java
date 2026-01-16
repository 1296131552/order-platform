package com.company.order.visual.common.response;

/**
 * 响应码枚举
 *
 * @author Order Platform Team
 */
public enum ResponseCode {

    // ========== 通用 ==========
    SUCCESS(200, "success"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器错误"),

    // ========== 订单模块 ==========
    ORDER_NOT_FOUND(1001, "订单不存在"),
    ORDER_STATUS_INVALID(1002, "订单状态无效"),
    ORDER_LINE_NOT_FOUND(1003, "订单行不存在"),

    // ========== 发运模块 ==========
    SHIPMENT_NOT_FOUND(2001, "发运批次不存在"),
    SHIPMENT_STATUS_INVALID(2002, "发运状态无效"),
    RECEIPT_NOT_FOUND(2003, "签收记录不存在"),

    // ========== 合作方模块 ==========
    PARTNER_NOT_FOUND(3001, "合作方不存在"),
    PARTNER_TYPE_INVALID(3002, "合作方类型无效"),

    // ========== 用户模块 ==========
    USER_NOT_FOUND(4001, "用户不存在"),
    USER_DISABLED(4002, "用户已禁用"),
    USER_LOCKED(4003, "用户已锁定"),
    LOGIN_FAILED(4004, "用户名或密码错误"),
    TOKEN_INVALID(4005, "Token无效或已过期"),

    // ========== 附件模块 ==========
    ATTACHMENT_NOT_FOUND(5001, "附件不存在"),
    ATTACHMENT_UPLOAD_FAILED(5002, "附件上传失败"),

    // ========== 异常模块 ==========
    EXCEPTION_NOT_FOUND(6001, "异常记录不存在"),
    EXCEPTION_STATUS_INVALID(6002, "异常状态无效");

    private final Integer code;
    private final String message;

    ResponseCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
