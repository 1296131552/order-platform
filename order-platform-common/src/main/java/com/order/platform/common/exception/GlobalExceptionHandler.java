package com.order.platform.common.exception;

import com.order.platform.common.enums.ResponseCode;
import com.order.platform.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * 功能说明：
 * - 统一处理所有异常
 * - 记录详细的异常日志
 * - 返回友好的错误信息
 *
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 [{}] {}: {}", request.getRequestURI(), e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数校验异常 [{}]: {}", request.getRequestURI(), message);
        return Result.fail(ResponseCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数绑定异常 [{}]: {}", request.getRequestURI(), message);
        return Result.fail(ResponseCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("约束违反异常 [{}]: {}", request.getRequestURI(), message);
        return Result.fail(ResponseCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * SQL异常
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException e, HttpServletRequest request) {
        log.error("数据完整性异常 [{}]: {}", request.getRequestURI(), e.getMessage(), e);
        return Result.fail(ResponseCode.INTERNAL_ERROR.getCode(), "数据操作失败，请检查数据格式");
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 [{}]: {}", request.getRequestURI(), e.getMessage(), e);
        return Result.fail(ResponseCode.INTERNAL_ERROR);
    }
}
