package com.order.platform.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要登录注解
 *
 * 使用说明：
 * - 标记在 Controller 方法上，表示该接口需要登录才能访问
 * - 拦截器会检查请求头中的 Authorization Token
 * - 未登录或 Token 过期会抛出异常
 *
 * 示例：
 * <pre>
 * {@code
 * @GetMapping("/user/profile")
 * @RequireLogin
 * public Result<User> getProfile() {
 *     // 需要登录才能访问
 * }
 * }
 * </pre>
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireLogin {
}
