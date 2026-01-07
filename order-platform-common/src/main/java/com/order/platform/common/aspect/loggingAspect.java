package com.order.platform.common.aspect;

import jakarta.servlet.http.HttpServletRequest; // 处理 HTTP 请求的核心类
import lombok.extern.slf4j.Slf4j;               // SLF4J 日志规范打印日志

import java.lang.reflect.Method;
import java.util.Arrays;

// Spring AOP 中实现完整、灵活的切面逻辑的核心组件
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
     * 提供拦截规则: execution 
     */
    @Pointcut("execution(* com.order.platform.*.controller..*.*(..))")
    public void controllerPointcut() {
        // 切点方法：空方法，仅作为切点标记
    }

    /**
     * 环绕通知：记录请求和响应信息
     * Object: Java 语言中最基础、最核心的类
     * ProceedingJoinPoint 封装了被拦截的目标方法的所有信息
     * （方法名、入参、目标对象等），并提供 proceed() 方法
     * 手动触发目标方法执行
     * joinPoint.proceed() 方法本身会抛出 Throwable
     */
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求的信息 
        // 
        // RequestAttributes -> ServletRequestAttributes
        // Spring Web 框架中封装 Servlet 请求上下文属性的核心类
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();

        // 获取方法信息 
        // Signature -> MethodSignature
        // 签名 -> 方法签名
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
        try{
            // 执行目标方法
            result = joinPoint.proceed();
            
            //计算时间
            long endTime = System.currentTimeMillis();
            long executeTime = endTime - startTime;

            StringBuilder responseLog = new StringBuilder();
            responseLog.append(String.format("请求完成 - %s.%s - 耗时: %dms", className, methodName, executeTime));

            // 如果执行时间过长，记录警告 甲方要求 <= 3s
            if (executeTime > 3000) {
                log.warn("⚠️  {}", responseLog.toString());
            } else {
                log.info("✅ {}", responseLog.toString());
            }

            return result;
        } catch (Exception e){
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
}
