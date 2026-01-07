package com.order.platform.common.aspect;

import cn.hutool.json.JSONUtil;
import com.order.platform.common.annotation.OperationLog;
import com.order.platform.common.dto.CurrentUser;
import com.order.platform.common.dto.OperationLogDTO;
import com.order.platform.common.enums.BusinessType;
import com.order.platform.common.enums.OperationModule;
import com.order.platform.common.enums.OperationType;
import com.order.platform.common.holder.CurrentUserHolder;
import com.order.platform.common.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 *
 * 功能说明：
 * - 拦截带有 @OperationLog 注解的方法
 * - 记录操作人、操作类型、操作结果等信息
 * - 异步保存日志，不影响业务性能
 *
 * 工作流程：
 * 1. 方法执行前：获取当前用户、请求信息
 * 2. 方法执行：捕获执行结果和耗时
 * 3. 方法执行后：构建日志对象并异步保存
 *
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;

    /**
     * SpEL 表达式解析器
     */
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 参数名称发现器
     */
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 环绕通知：拦截带有 @OperationLog 注解的方法
     */
    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint point, OperationLog operationLog) throws Throwable {

        // 1. 获取方法信息
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();

        // 2. 获取当前用户
        CurrentUser user = CurrentUserHolder.get();

        // 3. 获取请求信息
        HttpServletRequest request = getCurrentRequest();
        String clientIp = getClientIp(request);

        // 4. 构建日志对象
        OperationLogDTO logDTO = OperationLogDTO.builder()
                .operatorId(user != null ? user.getId() : -1L)
                .operatorName(user != null ? user.getRealName() : "系统")
                .operatorUserCode(user != null ? user.getUserCode() : "")
                .operatorEmployeeNo(user != null ? user.getEmployeeNo() : "")
                .operatorDepartmentId(user != null ? user.getDepartmentId() : -1L)
                .operatorDepartmentName(user != null ? user.getDepartmentName() : "")
                .operatorPosition(user != null ? user.getPosition() : "")
                .businessType(operationLog.business().getCode())
                .operationType(operationLog.type().getCode())
                .operationModule(operationLog.module().getCode())
                .operationDesc(operationLog.description())
                .operationIp(clientIp)
                .requestPath(request != null ? request.getRequestURI() : "")
                .requestMethod(request != null ? request.getMethod() : "")
                .operationTime(LocalDateTime.now())
                .build();

        long startTime = System.currentTimeMillis();
        Object result = null;

        try {
            // 5. 执行目标方法
            result = point.proceed();

            // 6. 记录成功
            logDTO.setOperationResult("SUCCESS");

            // 7. 解析业务关联信息（SpEL）
            resolveBusinessInfo(operationLog, point.getArgs(), method, result, logDTO);

            return result;

        } catch (Exception e) {
            // 记录失败
            logDTO.setOperationResult("FAILED");
            logDTO.setResultDesc(e.getMessage());
            throw e;

        } finally {
            // 8. 记录耗时
            long duration = System.currentTimeMillis() - startTime;
            logDTO.setOperationDuration((int) duration);

            // 9. 异步保存日志
            operationLogService.saveAsync(logDTO);

            log.debug("操作日志记录: className={}, methodName={}, operatorId={}, duration={}ms",
                    className, methodName, logDTO.getOperatorId(), duration);
        }
    }

    /**
     * 解析业务关联信息（使用 SpEL 表达式）
     */
    private void resolveBusinessInfo(OperationLog operationLog, Object[] args, Method method, Object result, OperationLogDTO logDTO) {

        // 创建 SpEL 上下文
        StandardEvaluationContext context = new StandardEvaluationContext();

        // 设置方法参数到上下文
        String[] parameterNames = nameDiscoverer.getParameterNames(method);
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }

        // 设置返回值到上下文
        context.setVariable("result", result);

        // 解析 businessId
        if (StrUtil.isNotBlank(operationLog.businessId())) {
            String businessId = parseExpression(operationLog.businessId(), context);
            if (StrUtil.isNotBlank(businessId)) {
                try {
                    logDTO.setBusinessId(Long.parseLong(businessId));
                } catch (NumberFormatException e) {
                    log.warn("解析 businessId 失败: {}", businessId);
                }
            }
        }

        // 解析 businessNo
        if (StrUtil.isNotBlank(operationLog.businessNo())) {
            logDTO.setBusinessNo(parseExpression(operationLog.businessNo(), context));
        }

        // 解析 businessName（存入 extra_info）
        if (StrUtil.isNotBlank(operationLog.businessName())) {
            String businessName = parseExpression(operationLog.businessName(), context);
            String extraInfo = String.format("{\"businessName\":\"%s\"}", businessName);
            logDTO.setExtraInfo(extraInfo);
        }
    }

    /**
     * 解析 SpEL 表达式
     *
     * @param expressionString 表达式字符串
     * @param context 上下文
     * @return 解析结果
     */
    private String parseExpression(String expressionString, EvaluationContext context) {
        try {
            Expression expression = parser.parseExpression(expressionString);
            Object value = expression.getValue(context);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.warn("解析 SpEL 表达式失败: {}, error={}", expressionString, e.getMessage());
            return null;
        }
    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

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
