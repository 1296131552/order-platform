package com.company.common.exception;

import java.util.List;

import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.validation.FieldError;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;

import io.micrometer.common.util.StringUtils;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;

import com.company.common.enums.result.GlobalResultCode;
import com.company.common.model.dto.ResultDTO;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import static com.company.common.utils.ExceptionUtil.getFirstFieldErrorByOrder;

import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常处理
 * 根据异常类型返回对应的HTTP状态码
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultDTO<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return ResultDTO.failure(e.getCode(), e.getMessage());
    }

    /**
     * 处理认证失败异常（用户名或密码错误）
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResultDTO<?> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("认证失败：{}", e.getMessage());
        return ResultDTO.failure(GlobalResultCode.UNAUTHORIZED.getCode(), e.getMessage());
    }

    /**
     * 处理访问拒绝异常（无权限访问）
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResultDTO<?> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("访问被拒绝：{}", e.getMessage());
        return ResultDTO.of(GlobalResultCode.FORBIDDEN);
    }

    /**
     * 处理参数验证异常
     * 按照字段在类中的声明顺序返回第一个错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultDTO<?> handleValidationException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        
        String errorMessage;
        if (fieldErrors.isEmpty()) {
            errorMessage = GlobalResultCode.PARAM_ERROR.getMessage();
        } else {
            // 按照字段在类中的声明顺序排序
            FieldError firstError = getFirstFieldErrorByOrder(fieldErrors, e.getBindingResult().getTarget());
            errorMessage = firstError.getDefaultMessage();
            errorMessage = StringUtils.isNotBlank(errorMessage) ? errorMessage : GlobalResultCode.PARAM_ERROR.getMessage();
        }

        return ResultDTO.failure(GlobalResultCode.PARAM_ERROR.getCode(), errorMessage);
    }

    /**
     * 处理参数类型转换异常（包括枚举转换失败）
     */
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            TypeMismatchException.class,
            ConversionFailedException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultDTO<?> handleTypeMismatchException(Exception e) {
        if (e instanceof MethodArgumentTypeMismatchException ex) {
            // 提取参数名、无效值和目标类型
            String paramName = ex.getName();
            String invalidValue = ex.getValue() != null ? ex.getValue().toString() : "null";
            Class<?> requiredType = ex.getRequiredType();

            // 记录详细的异常信息
            if (requiredType != null && requiredType.isEnum()) {
                log.warn("枚举参数转换失败：参数[{}]，值[{}]，目标类型[{}]",
                        paramName, invalidValue, requiredType.getSimpleName());
            } else {
                log.warn("参数类型转换失败：参数[{}]，值[{}]，目标类型[{}]",
                        paramName, invalidValue, requiredType != null ? requiredType.getSimpleName() : "unknown");
            }
        }

        return ResultDTO.of(GlobalResultCode.ENUM_ERROR);
    }
    
    
    /**
     * 处理JSON反序列化异常（包括枚举值不匹配等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultDTO<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        // 检查是否是枚举值格式异常
        Throwable cause = e.getCause();
        if (cause instanceof InvalidFormatException ife) {
            // 提取字段名和无效值
            String fieldName = ife.getPath().isEmpty() ? "未知字段" : ife.getPath().get(ife.getPath().size() - 1).getFieldName();
            String invalidValue = ife.getValue() != null ? ife.getValue().toString() : "null";
            String targetType = ife.getTargetType() != null ? ife.getTargetType().getSimpleName() : "unknown";

            // 记录详细的异常信息
            log.warn("JSON参数转换失败：字段[{}]，值[{}]，目标类型[{}]", fieldName, invalidValue, targetType);
        } else {
            // 其他JSON反序列化异常
            log.warn("JSON反序列化异常：{}", e.getMessage());
        }
        return ResultDTO.of(GlobalResultCode.PARAM_ERROR);
    }
    
    /**
     * 处理404异常（接口不存在）
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResultDTO<?> handleNoHandlerFoundException(NoHandlerFoundException e) {
        return ResultDTO.of(GlobalResultCode.INTERFACE_NOT_EXIST);
    }
    
    /**
     * 处理请求方法不支持异常（例如：应该用GET请求，但使用了POST请求）
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResultDTO<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return ResultDTO.of(GlobalResultCode.METHOD_NOT_ALLOWED);
    }
    
    /**
     * 处理参数类型校验异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultDTO<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数类型校验失败：{}", e.getMessage());
        return ResultDTO.failure(GlobalResultCode.PARAM_ERROR.getCode(), e.getMessage());
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResultDTO<?> handleException(Exception e) {
        log.error("系统异常：{}", e.getMessage(), e);
        return ResultDTO.failure();
    }
}