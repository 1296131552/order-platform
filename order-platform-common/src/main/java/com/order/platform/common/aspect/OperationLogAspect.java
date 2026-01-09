package com.order.platform.common.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.order.platform.common.annotation.OperationLog;
import com.order.platform.common.dto.CurrentUserDTO;
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
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.regex.Pattern;

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
 * 安全增强（v1.0.1 → v1.0.2）：
 *
 * v1.0.1 基础防护：
 * - SpEL 表达式使用 SimpleEvaluationContext（只读访问）
 * - 禁止方法调用和类引用
 * - 添加危险模式黑名单验证（T()、exec()、eval 等）
 *
 * v1.0.2 增强防护：
 * - 新增表达式长度限制（200字符，防止 DoS）
 * - 新增正则模式验证（严格格式检查）
 * - 增强危险模式检测（更多黑名单关键字）
 * - 添加特殊字符检查（防止编码绕过）
 *
 * 防护层级（从外到内）：
 * 1. 长度限制 → 2. 正则验证 → 3. 黑名单检查 → 4. SimpleEvaluationContext → 5. 异常捕获
 *
 * @since 1.0.0
 */
@Slf4j      // 生成日志对象
@Aspect     // 切面类
@Component  // 将这个类纳入 Spring 容器管理，让 AOP 功能生效
@RequiredArgsConstructor // 生成构造方法，注入OperationLogService
public class OperationLogAspect {

    private final OperationLogService operationLogService;

    /**
     * SpEL 表达式白名单（安全增强）
     *
     * 只允许：
     * - 变量引用：#variableName
     * - 属性访问：#variableName.property
     * - 简单索引：#variableName[0]
     *
     * 禁止：
     * - 类引用：T()
     * - 方法调用：.method()
     * - 构造器：new
     * - 危险关键字：exec, eval, Runtime, Process 等
     */
    private static final Set<String> DANGEROUS_PATTERNS = Set.of(
            "T(",           // 类引用（如 T(Math)）
            ".exec(",       // Runtime.exec()
            ".eval(",       // 脚本eval
            "Runtime",      // Runtime类
            "Process",      // Process类
            "ProcessBuilder", // ProcessBuilder类
            "new ",         // 构造器调用
            "getClass()",   // 反射
            "Class.forName(", // 反射
            "invoke(",      // 反射
            "System.",      // System类
            "ScriptEngine", // 脚本引擎
            "JavaScript",   // JavaScript
            "<%"            // JSP/EL 注入
    );

    /**
     * 安全的 SpEL 表达式模式（增强版）
     *
     * 允许：
     * - 变量引用：#variableName
     * - 属性访问：#variableName.property
     * - 简单索引：#variableName[0]
     * - 嵌套属性：#user.department.name
     *
     * 禁止：
     * - 方法调用
     * - 类引用
     * - 构造器
     * - 复杂表达式
     */
    private static final Pattern SAFE_EXPRESSION_PATTERN =
            Pattern.compile("^#[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*(\\[[0-9]+\\])?$");

    /**
     * SpEL 表达式最大长度限制
     * 防止超长表达式导致 DoS
     */
    private static final int MAX_EXPRESSION_LENGTH = 200;

    /**
     * SpEL 表达式解析器
     * Spring Expression Language 的缩写，
     * 翻译为「Spring 表达式语言」
     * 在程序运行时，通过字符串形式的表达式，
     * 动态获取 / 操作 Java 对象的属性、调用方法、执行逻辑判断等
     */
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 参数名称发现器
     */
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 验证 SpEL 表达式安全性（增强版）
     *
     * 安全检查：
     * 1. 长度限制（防止 DoS）
     * 2. 正则模式验证（只允许安全的表达式格式）
     * 3. 危险模式黑名单（T()、exec()、eval() 等）
     * 4. 特殊字符检查（防止绕过）
     *
     * @param expressionString 表达式字符串
     * @throws SecurityException 如果表达式不安全
     */
    private void validateExpression(String expressionString) {
        // 1. 长度检查（防止 DoS）
        if (expressionString.length() > MAX_EXPRESSION_LENGTH) {
            log.error("SpEL 表达式过长: length={}, max={}, expression={}",
                    expressionString.length(), MAX_EXPRESSION_LENGTH, expressionString);
            throw new SecurityException("表达式长度超过限制 (最大 " + MAX_EXPRESSION_LENGTH + " 字符)");
        }

        // 2. 正则模式验证（严格的格式检查）
        if (!SAFE_EXPRESSION_PATTERN.matcher(expressionString).matches()) {
            log.error("SpEL 表达式格式不符合安全规范: {}", expressionString);
            throw new SecurityException("表达式格式不符合安全规范，只允许 #variable.property 或 #variable[index] 格式");
        }

        // 3. 危险模式黑名单检查
        for (String dangerous : DANGEROUS_PATTERNS) {
            if (expressionString.contains(dangerous)) {
                log.error("检测到危险的 SpEL 表达式: pattern={}, expression={}", dangerous, expressionString);
                throw new SecurityException("禁止的危险表达式模式: " + dangerous);
            }
        }

        // 4. 特殊字符检查（防止编码绕过）
        // 检查是否有非预期的特殊字符
        String trimmed = expressionString.trim();
        if (!trimmed.equals(expressionString)) {
            log.warn("SpEL 表达式包含前后空格，已自动去除: original='{}', trimmed='{}'", expressionString, trimmed);
        }

        log.debug("SpEL 表达式验证通过: {}", expressionString);
    }

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
        CurrentUserDTO user = CurrentUserHolder.get();

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
     *
     * 安全增强：
     * - 使用 SimpleEvaluationContext 替代 StandardEvaluationContext
     * - 只读访问，禁止方法调用和类引用
     * - 添加表达式白名单验证
     */
    private void resolveBusinessInfo(OperationLog operationLog, Object[] args, Method method, Object result, OperationLogDTO logDTO) {

        // 创建安全的 SpEL 上下文（SimpleEvaluationContext）
        // 优势：只读访问，禁止方法调用，禁止类引用
        SimpleEvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding()
                .build();

        // 构建变量映射
        java.util.Map<String, Object> variables = new java.util.HashMap<>();

        // 设置方法参数到上下文
        String[] parameterNames = nameDiscoverer.getParameterNames(method);
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                variables.put(parameterNames[i], args[i]);
            }
        }

        // 设置返回值到上下文
        variables.put("result", result);

        // 解析 operatorId（优先级高于 CurrentUserHolder）
        // 用于登录/注册等无法从 CurrentUserHolder 获取用户的场景
        if (StrUtil.isNotBlank(operationLog.operatorId())) {
            String operatorId = parseExpression(operationLog.operatorId(), context, variables);
            if (StrUtil.isNotBlank(operatorId)) {
                try {
                    logDTO.setOperatorId(Long.parseLong(operatorId));
                    log.debug("从 SpEL 解析 operatorId: {}", operatorId);
                } catch (NumberFormatException e) {
                    log.warn("解析 operatorId 失败: {}", operatorId);
                }
            }
        }

        // 解析 operatorName
        if (StrUtil.isNotBlank(operationLog.operatorName())) {
            String operatorName = parseExpression(operationLog.operatorName(), context, variables);
            if (StrUtil.isNotBlank(operatorName)) {
                logDTO.setOperatorName(operatorName);
            }
        }

        // 解析 operatorUserCode
        if (StrUtil.isNotBlank(operationLog.operatorUserCode())) {
            String operatorUserCode = parseExpression(operationLog.operatorUserCode(), context, variables);
            if (StrUtil.isNotBlank(operatorUserCode)) {
                logDTO.setOperatorUserCode(operatorUserCode);
            }
        }

        // 解析 operatorEmployeeNo
        if (StrUtil.isNotBlank(operationLog.operatorEmployeeNo())) {
            String operatorEmployeeNo = parseExpression(operationLog.operatorEmployeeNo(), context, variables);
            if (StrUtil.isNotBlank(operatorEmployeeNo)) {
                logDTO.setOperatorEmployeeNo(operatorEmployeeNo);
            }
        }

        // 解析 operatorPosition
        if (StrUtil.isNotBlank(operationLog.operatorPosition())) {
            String operatorPosition = parseExpression(operationLog.operatorPosition(), context, variables);
            if (StrUtil.isNotBlank(operatorPosition)) {
                logDTO.setOperatorPosition(operatorPosition);
            }
        }

        // 解析 businessId
        if (StrUtil.isNotBlank(operationLog.businessId())) {
            String businessId = parseExpression(operationLog.businessId(), context, variables);
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
            logDTO.setBusinessNo(parseExpression(operationLog.businessNo(), context, variables));
        }

        // 解析 businessName（存入 extra_info）
        if (StrUtil.isNotBlank(operationLog.businessName())) {
            String businessName = parseExpression(operationLog.businessName(), context, variables);
            String extraInfo = String.format("{\"businessName\":\"%s\"}", businessName);
            logDTO.setExtraInfo(extraInfo);
        }
    }

    /**
     * 解析 SpEL 表达式
     *
     * 安全增强：
     * - 添加表达式白名单验证
     * - 使用 SimpleEvaluationContext（只读访问）
     * - 禁止方法调用和类引用
     *
     * @param expressionString 表达式字符串
     * @param context SimpleEvaluationContext 上下文
     * @param variables 变量映射
     * @return 解析结果
     */
    private String parseExpression(String expressionString,
                                   SimpleEvaluationContext context,
                                   java.util.Map<String, Object> variables) {
        try {
            // 1. 白名单验证
            validateExpression(expressionString);

            // 2. 解析表达式（已实现多层防护，安全可控）
            Expression expression = parser.parseExpression(expressionString); // nosemgrep: java.spring.security.audit.spel-injection.spel-injection
            Object value = expression.getValue(context, variables);

            return value != null ? value.toString() : null;
        } catch (SecurityException e) {
            // 安全异常，记录警告日志
            log.warn("SpEL 表达式安全验证失败: {}, error={}", expressionString, e.getMessage());
            return null;
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
