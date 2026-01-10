package com.order.platform.common.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 日志切面（简化版）
 *
 * 功能说明：
 * - 记录所有 Controller 层的请求和响应
 * - 记录请求方法、URL、参数、响应时间
 * - 记录异常信息
 *
 * @author 开发组
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

    /**
     * 定义切点：所有Controller层
     */
    @Pointcut("execution(* com.order.platform.*.controller..*.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 环绕通知：记录请求和响应信息
     */
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();

        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        // 构建简洁的请求日志
        log.info("→ 请求: {} {} -> {}.{}, 参数: {}",
            request.getMethod(),
            request.getRequestURI(),
            className,
            methodName,
            formatArguments(joinPoint.getArgs())
        );

        try {
            // 执行方法
            Object result = joinPoint.proceed();

            // 计算执行时间
            long executeTime = System.currentTimeMillis() - startTime;

            // 构建响应日志
            log.info("← 响应: {}.{} - 耗费: {}ms, 结果: {}",
                className,
                methodName,
                executeTime,
                formatResult(result)
            );

            // 慢请求警告
            if (executeTime > 2000) {
                log.warn("⚠️  慢请求: {}.{} 耗费 {}ms", className, methodName, executeTime);
            }

            return result;

        } catch (Exception e) {
            // 计算执行时间
            long executeTime = System.currentTimeMillis() - startTime;

            // 构建异常日志
            log.error("✗ 异常: {} {} -> {}.{} - 耗费: {}ms, 错误: {}",
                request.getMethod(),
                request.getRequestURI(),
                className,
                methodName,
                executeTime,
                e.getMessage(),
                e
            );

            throw e;
        }
    }

    /**
     * 格式化方法参数（简化版）
     */
    private String formatArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return "无";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }

            Object arg = args[i];
            if (arg == null) {
                sb.append("null");
            } else if (arg instanceof HttpServletRequest) {
                sb.append("HttpServletRequest");
            } else if (arg instanceof jakarta.servlet.http.HttpServletResponse) {
                sb.append("HttpServletResponse");
            } else {
                // 简化显示：类名 + @ + hashCode
                String className = arg.getClass().getSimpleName();
                sb.append(className).append("@").append(Integer.toHexString(arg.hashCode()));
            }
        }

        return sb.toString();
    }

    /**
     * 格式化响应结果（简化版）
     */
    private String formatResult(Object result) {
        if (result == null) {
            return "null";
        }

        try {
            // 尝试获取 Result 对象的 code 和 message
            Class<?> resultClass = result.getClass();
            Object code = getField(result, resultClass, "code");
            Object message = getField(result, resultClass, "message");

            if (code != null) {
                return "Result{code=" + code + ", message=" + message + "}";
            }

            // 其他对象简化显示
            String className = resultClass.getSimpleName();
            return className + "@" + Integer.toHexString(result.hashCode());

        } catch (Exception e) {
            return result.toString();
        }
    }

    /**
     * 通过反射获取字段值
     */
    private Object getField(Object obj, Class<?> clazz, String fieldName) {
        try {
            java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            // 尝试从父类获取
            if (clazz.getSuperclass() != null) {
                return getField(obj, clazz.getSuperclass(), fieldName);
            }
            return null;
        }
    }
}
