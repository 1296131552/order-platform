package com.order.platform.common.exception;

import com.order.platform.common.enums.ResponseCode;
import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
    }

    public BusinessException(ResponseCode responseCode, String message) {
        super(message);
        this.code = responseCode.getCode();
    }
}
