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
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

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
     * 数据完整性异常
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException e, HttpServletRequest request) {
        log.error("数据完整性异常 [{}]: {}", request.getRequestURI(), e.getMessage(), e);

        // 尝试提取更具体的异常信息
        Throwable rootCause = e.getRootCause();
        if (rootCause instanceof SQLIntegrityConstraintViolationException) {
            return handleSQLIntegrityConstraintViolation((SQLIntegrityConstraintViolationException) rootCause, request);
        }

        return Result.fail(ResponseCode.INTERNAL_ERROR.getCode(), "数据操作失败，请检查数据格式");
    }

    /**
     * 唯一键冲突异常
     *
     * 异常场景：
     * - 用户名重复（uk_username）
     * - 邮箱重复（uk_email）
     * - 手机号重复（uk_phone）
     *
     * 处理流程：
     * 1. 捕获 DuplicateKeyException
     * 2. 解析冲突的索引名称
     * 3. 返回友好的错误提示
     *
     * 注意事项：
     * - 异常消息格式因数据库而异（MySQL/PostgreSQL/Oracle）
     * - 需要处理不同的索引命名格式
     *
     * @param e 唯一键冲突异常
     * @param request HTTP请求
     * @return 统一响应结果
     */
    @ExceptionHandler(java.sql.DuplicateKeyException.class)
    public Result<Void> handleDuplicateKeyException(java.sql.DuplicateKeyException e, HttpServletRequest request) {
        log.error("数据库唯一索引冲突 [{}]: {}", request.getRequestURI(), e.getMessage(), e);

        // 提取异常信息
        String errorMessage = e.getMessage();
        String conflictField = null;
        String friendlyMessage = null;

        // TODO(human): 实现唯一键冲突解析逻辑
        //
        // 背景：当注册用户时，如果用户名/邮箱/手机号已存在，数据库会抛出 DuplicateKeyException
        //       我们需要从异常消息中提取具体的冲突字段，并返回友好的错误提示
        //
        // 您的任务：实现以下逻辑
        // 1. 从 errorMessage 中提取冲突的索引名称（uk_username, uk_email, uk_phone）
        // 2. 根据索引类型设置 conflictField（username/email/phone）
        // 3. 设置对应的 friendlyMessage
        //
        // 提示：
        // - MySQL 异常消息格式：Duplicate entry 'xxx' for key 'uk_username'
        // - 可以使用字符串匹配或正则表达式提取索引名
        // - 考虑不同数据库的消息格式差异
        //
        // 示例代码结构（请完善）：
        // if (errorMessage != null) {
        //     if (errorMessage.contains("uk_username")) {
        //         conflictField = "username";
        //         friendlyMessage = "用户名已存在";
        //     } else if (errorMessage.contains("uk_email")) {
        //         conflictField = "email";
        //         friendlyMessage = "邮箱已被注册";
        //     } else if (errorMessage.contains("uk_phone")) {
        //         conflictField = "phone";
        //         friendlyMessage = "手机号已被注册";
        //     }
        // }

        // 临时默认消息（等您实现后会替换为友好提示）
        if (friendlyMessage == null) {
            friendlyMessage = "操作失败，数据冲突（" + conflictField + "）";
        }

        return Result.fail(ResponseCode.SYSTEM_ERROR.getCode(), friendlyMessage);
    }

    /**
     * SQL完整性约束违反异常
     *
     * 处理 SQLIntegrityConstraintViolationException，提取更具体的错误信息
     */
    private Result<Void> handleSQLIntegrityConstraintViolation(SQLIntegrityConstraintViolationException e, HttpServletRequest request) {
        log.error("SQL约束违反 [{}]: {}", request.getRequestURI(), e.getMessage(), e);

        // 提取约束名称
        String constraintName = null;
        try {
            constraintName = e.getConstraintName();
        } catch (Exception ex) {
            log.warn("无法提取约束名称", ex);
        }

        String message = "数据完整性约束违反";
        if (constraintName != null) {
            message += "（约束：" + constraintName + "）";
        }

        return Result.fail(ResponseCode.INTERNAL_ERROR.getCode(), message);
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
