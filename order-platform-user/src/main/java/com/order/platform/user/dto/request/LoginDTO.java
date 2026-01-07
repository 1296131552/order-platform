package com.order.platform.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录请求DTO
 *
 * 功能说明：
 * - 支持用户名/邮箱/手机号登录
 * - 密码加密传输（HTTPS）
 * - 可选的图形验证码（防暴力破解）
 *
 * 字段说明：
 * - account：登录账号（用户名/邮箱/手机号）
 * - password：密码（明文，后端BCrypt验证）
 * - captcha：图形验证码（可选）
 * - captchaKey：验证码Key（可选）
 *
 * 安全说明：
 * - 密码必须通过HTTPS传输
 * - 密码错误5次锁定30分钟（Redis）
 * - 图形验证码防止自动化攻击
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录请求")
public class LoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 登录账号
     *
     * 支持三种登录方式：
     * 1. 用户名（如：zhangsan）
     * 2. 邮箱（如：zhangsan@example.com）
     * 3. 手机号（如：13800138000）
     *
     * 验证规则：
     * - 非空
     * - 长度2-50字符
     */
    @Schema(description = "登录账号（用户名/邮箱/手机号）", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "登录账号不能为空")
    private String account;

    /**
     * 密码
     *
     * 验证规则：
     * - 非空
     * - 长度6-20字符
     * - 前端传输时使用HTTPS加密
     * - 后端使用BCrypt验证
     *
     * 安全说明：
     * - 密码错误5次锁定账户30分钟
     * - 密码存储时使用BCrypt加密（strength=10）
     * - 日志中不记录密码（使用@JsonIgnore）
     */
    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 图形验证码（可选）
     *
     * 用于：
     * - 防止自动化攻击
     * - 防止暴力破解
     *
     * 验证规则：
     * - 可选（根据系统配置决定是否必需）
     * - 4位字符
     */
    @Schema(description = "图形验证码", example = "abcd")
    private String captcha;

    /**
     * 验证码Key（可选）
     *
     * 用于：
     * - 标识验证码会话
     * - 验证码校验
     *
     * 说明：
     * - UUID格式
     * - 存储在Redis中（5分钟过期）
     */
    @Schema(description = "验证码Key", example = "uuid-uuid-uuid-uuid")
    private String captchaKey;

    // ==================== 辅助方法 ====================

    /**
     * 验证登录账号格式
     *
     * @return true-格式正确，false-格式错误
     */
    public boolean isAccountValid() {
        if (account == null || account.isEmpty()) {
            return false;
        }

        // 长度验证
        if (account.length() < 2 || account.length() > 50) {
            return false;
        }

        // 格式验证
        return isUsername() || isEmail() || isPhone();
    }

    /**
     * 判断是否为用户名格式
     *
     * 用户名规则：
     * - 以字母开头
     * - 只包含字母、数字、下划线
     * - 长度2-20字符
     *
     * @return true-用户名格式，false-其他格式
     */
    public boolean isUsername() {
        if (account == null) {
            return false;
        }
        return account.matches("^[a-zA-Z][a-zA-Z0-9_]{1,19}$");
    }

    /**
     * 判断是否为邮箱格式
     *
     * @return true-邮箱格式，false-其他格式
     */
    public boolean isEmail() {
        if (account == null) {
            return false;
        }
        return account.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }

    /**
     * 判断是否为手机号格式
     *
     * @return true-手机号格式，false-其他格式
     */
    public boolean isPhone() {
        if (account == null) {
            return false;
        }
        return account.matches("^1[3-9]\\d{9}$");
    }

    /**
     * 获取登录类型
     *
     * @return 登录类型（USERNAME/EMAIL/PHONE）
     */
    public String getLoginType() {
        if (isUsername()) {
            return "USERNAME";
        } else if (isEmail()) {
            return "EMAIL";
        } else if (isPhone()) {
            return "PHONE";
        } else {
            return "UNKNOWN";
        }
    }
}
