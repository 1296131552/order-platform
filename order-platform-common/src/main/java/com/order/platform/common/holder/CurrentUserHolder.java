package com.order.platform.common.holder;

import com.order.platform.common.dto.CurrentUserDTO;

/**
 * 当前登录用户信息持有者
 *
 * 使用说明：
 * - 基于 ThreadLocal 实现线程隔离
 * - 在拦截器中设置用户信息
 * - 在业务代码中获取当前登录用户
 *
 * 生命周期：
 * 1. 请求到达 → 拦截器解析 Token → 设置用户信息
 * 2. 业务处理 → 通过 get() 获取当前用户
 * 3. 请求结束 → 拦截器清理用户信息 → 防止内存泄漏
 *
 * 示例：
 * <pre>
 * {@code
 * // 在 Service 中获取当前用户
 * public void createOrder(Order order) {
 *     CurrentUserDTO user = CurrentUserHolder.get();
 *     order.setCreateBy(user.getId());
 *     order.setCreateByName(user.getUsername());
 *     // ...
 * }
 * }
 * </pre>
 *
 * @since 1.0.0
 */
public class CurrentUserHolder {

    private static final ThreadLocal<CurrentUserDTO> USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置当前用户信息
     *
     * @param user 当前用户
     */
    public static void set(CurrentUserDTO user) {
        USER_THREAD_LOCAL.set(user);
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前用户，未登录返回 null
     */
    public static CurrentUserDTO get() {
        return USER_THREAD_LOCAL.get();
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID，未登录返回 null
     */
    public static Long getUserId() {
        CurrentUserDTO user = get();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名，未登录返回 null
     */
    public static String getUsername() {
        CurrentUserDTO user = get();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 清除当前用户信息
     *
     * 注意：必须在请求结束时调用，防止 ThreadLocal 内存泄漏
     */
    public static void clear() {
        USER_THREAD_LOCAL.remove();
    }

    /**
     * 检查是否已登录
     *
     * @return true-已登录，false-未登录
     */
    public static boolean isAuthenticated() {
        return get() != null;
    }
}
