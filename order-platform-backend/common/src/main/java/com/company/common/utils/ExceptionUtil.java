package com.company.common.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.aspectj.lang.ProceedingJoinPoint;

@Component
public class ExceptionUtil {
    /**
     * 根据字段在类中的声明顺序获取第一个字段错误
     */
    public static FieldError getFirstFieldErrorByOrder(List<FieldError> fieldErrors, Object target) {
        if (fieldErrors.size() == 1) {
            return fieldErrors.get(0);
        }

        // 获取目标类的字段声明顺序
        Class<?> targetClass = target.getClass();
        List<String> fieldOrder = Arrays.stream(targetClass.getDeclaredFields())
                .map(Field::getName)
                .toList();

        // 创建字段名到顺序的映射
        Map<String, Integer> fieldIndexMap = fieldOrder.stream()
                .collect(Collectors.toMap(
                        fieldName -> fieldName,
                        fieldOrder::indexOf
                ));

        // 按照字段声明顺序排序并返回第一个
        return fieldErrors.stream()
                .filter(error -> fieldIndexMap.containsKey(error.getField()))
                .min(Comparator.comparing(error -> fieldIndexMap.get(error.getField())))
                .orElse(fieldErrors.get(0)); // 如果找不到匹配的字段，返回原始第一个
    }

    /**
     * 构建详细的错误信息
     * @param e 异常对象
     * @param joinPoint 连接点对象
     * @return 详细的错误信息
     */
    public String buildDetailedErrorMessage(Exception e, ProceedingJoinPoint joinPoint) {
        StringBuilder errorBuilder = new StringBuilder();

        // 1. 异常基本信息
        errorBuilder.append("异常类型: ").append(e.getClass().getSimpleName()).append("\n");
        errorBuilder.append("异常消息: ").append(e.getMessage() != null ? e.getMessage() : "无消息").append("\n");

        // 2. 异常发生位置
        errorBuilder.append("发生方法: ")
                .append(joinPoint.getTarget().getClass().getSimpleName())
                .append(".")
                .append(joinPoint.getSignature().getName())
                .append("\n");

        // 3. 异常根本原因（如果有的话）
        Throwable rootCause = getRootCause(e);
        if (rootCause != e && rootCause != null) {
            errorBuilder.append("根本原因: ")
                    .append(rootCause.getClass().getSimpleName())
                    .append(" - ")
                    .append(rootCause.getMessage() != null ? rootCause.getMessage() : "无消息")
                    .append("\n");
        }

        // 4. 异常堆栈的关键信息（只记录前几行，避免过长）
        errorBuilder.append("关键堆栈: ");
        StackTraceElement[] stackTrace = e.getStackTrace();
        if (stackTrace.length > 0) {
            // 记录前3行堆栈信息
            int maxLines = Math.min(3, stackTrace.length);
            for (int i = 0; i < maxLines; i++) {
                StackTraceElement element = stackTrace[i];
                errorBuilder.append("\n  at ")
                        .append(element.getClassName())
                        .append(".")
                        .append(element.getMethodName())
                        .append("(")
                        .append(element.getFileName())
                        .append(":")
                        .append(element.getLineNumber())
                        .append(")");
            }

            if (stackTrace.length > maxLines) {
                errorBuilder.append("\n  ... (还有 ").append(stackTrace.length - maxLines).append(" 行)");
            }
        }
        return errorBuilder.toString();
    }

    /**
     * 获取异常的根本原因
     * @param throwable 异常对象
     * @return 根本原因异常
     */
    private Throwable getRootCause(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }
}
