package com.company.common.exception;

import com.company.common.enums.IResult;
import com.company.common.enums.result.GlobalResultCode;

import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {
    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 构造方法
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = GlobalResultCode.ERROR.getCode();
    }

    /**
     * 构造方法
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造方法
     * @param resultCode 结果码枚举
     */
    public BusinessException(IResult resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 构造方法
     * @param resultCode 结果码枚举
     * @param message 错误消息
     */
    public BusinessException(IResult resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
}
