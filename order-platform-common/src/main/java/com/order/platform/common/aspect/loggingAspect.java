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

import java.util.Arrays;

/**
 * 日志切面
 *
 * 功能说明：
 * - 记录所有Controller层的请求信息
 * - 记录请求参数、响应时间、响应结果
 * - 记录异常信息
 * 
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

        // 构建请求日志
        StringBuilder requestLog = new StringBuilder();
        requestLog.append("\n=================== 请求开始 ===================\n");
        requestLog.append(String.format("请求URL: %s %s\n", request.getMethod(), request.getRequestURI()));
        requestLog.append(String.format("请求方法: %s.%s\n", className, methodName));
        requestLog.append(String.format("请求参数: %s\n", Arrays.toString(joinPoint.getArgs())));
        requestLog.append(String.format("来源IP: %s\n", getClientIp(request)));
        requestLog.append("================================================");

        log.info(requestLog.toString());

        Object result = null;
        try {
            // 执行方法
            result = joinPoint.proceed();

            // 计算执行时间
            long endTime = System.currentTimeMillis();
            long executeTime = endTime - startTime;

            // 构建响应日志
            StringBuilder responseLog = new StringBuilder();
            responseLog.append("\n=================== 响应结果 ===================\n");
            responseLog.append(String.format("请求方法: %s.%s\n", className, methodName));
            responseLog.append(String.format("执行耗时: %dms\n", executeTime));
            responseLog.append(String.format("响应内容: %s\n", formatResult(result)));
            responseLog.append("================================================");

            // 如果执行时间过长，记录警告
            if (executeTime > 3000) {
                log.warn("⚠️  {}", responseLog.toString());
            } else {
                log.info("✅ {}", responseLog.toString());
            }

            return result;
        } catch (Exception e) {
            // 计算执行时间
            long endTime = System.currentTimeMillis();
            long executeTime = endTime - startTime;

            // 构建异常日志
            StringBuilder errorLog = new StringBuilder();
            errorLog.append(String.format("❌ 请求异常 - %s.%s - 耗时: %dms\n", className, methodName, executeTime));
            errorLog.append(String.format("异常类型: %s\n", e.getClass().getName()));
            errorLog.append(String.format("异常信息: %s", e.getMessage()));

            log.error(errorLog.toString(), e);
            throw e;
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 格式化响应结果，避免日志过长
     */
    private String formatResult(Object result) {
        if (result == null) {
            return "null";
        }

        // 使用反射获取 Result 对象的实际内容
        try {
            Class<?> resultClass = result.getClass();

            // 获取 code 字段
            Object code = getField(result, resultClass, "code");
            // 获取 message 字段
            Object message = getField(result, resultClass, "message");
            // 获取 data 字段
            Object data = getField(result, resultClass, "data");

            StringBuilder sb = new StringBuilder();
            sb.append("Result{code=").append(code)
              .append(", message=").append(message);

            // 格式化 data 内容
            if (data != null) {
                String dataStr = formatData(data);
                sb.append(", data=").append(dataStr);
            }

            sb.append("}");

            // 限制最大长度
            String resultStr = sb.toString();
            if (resultStr.length() > 1000) {
                return resultStr.substring(0, 1000) + "... (总长度: " + resultStr.length() + ")";
            }
            return resultStr;

        } catch (Exception e) {
            // 如果反射失败，使用默认 toString
            String resultStr = result.toString();
            if (resultStr.length() > 1000) {
                return resultStr.substring(0, 1000) + "... (总长度: " + resultStr.length() + ")";
            }
            return resultStr;
        }
    }

    /**
     * 格式化 data 内容
     */
    private String formatData(Object data) {
        if (data == null) {
            return "null";
        }

        // 处理 Page 对象
        if (data.getClass().getName().contains("Page")) {
            Object total = getField(data, data.getClass(), "total");
            Object records = getField(data, data.getClass(), "records");
            String recordType = records instanceof java.util.Collection ?
                "[" + ((java.util.Collection<?>) records).size() + " 条记录]" : "records";
            return "Page{total=" + total + ", records=" + recordType + "}";
        }

        // 处理集合
        if (data instanceof java.util.Collection) {
            int size = ((java.util.Collection<?>) data).size();
            if (size > 3) {
                return "Collection[" + size + " 个元素] (示例: " +
                    ((java.util.Collection<?>) data).iterator().next() + ", ...)";
            }
            return data.toString();
        }

        // 其他对象直接返回 toString
        return data.toString();
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
