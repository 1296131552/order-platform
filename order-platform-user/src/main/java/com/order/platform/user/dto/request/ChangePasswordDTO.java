package com.order.platform.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 修改密码请求DTO
 *
 * 功能说明：
 * - 用户修改自己的密码
 * - 旧密码验证
 * - 新密码强度验证
 * - 新旧密码不能相同
 *
 * 字段说明：
 * - oldPassword：旧密码（验证身份）
 * - newPassword：新密码（强度验证）
 * - confirmPassword：确认密码（两次输入一致）
 *
 * 安全说明：
 * - 必须验证旧密码正确
 * - 新密码不能与旧密码相同
 * - 新密码必须符合强度要求
 * - 密码修改后记录操作日志
 *
 * 密码强度要求：
 * - 长度6-20字符
 * - 包含大小写字母、数字、特殊字符中的至少3种
 * - 不能包含用户名、手机号等个人信息
 *
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "修改密码请求")
public class ChangePasswordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 旧密码
     *
     * 验证规则：
     * - 非空
     * - 长度6-20字符
     * - 必须与当前密码一致
     *
     * 安全说明：
     * - 用于验证用户身份
     * - 防止他人修改密码
     */
    @Schema(description = "旧密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "旧密码不能为空")
    @Size(min = 6, max = 20, message = "旧密码长度为6-20字符")
    private String oldPassword;

    /**
     * 新密码
     *
     * 验证规则：
     * - 非空
     * - 长度6-20字符
     * - 必须符合密码强度要求
     * - 不能与旧密码相同
     *
     * 密码强度要求：
     * - 长度6-20字符
     * - 包含大小写字母、数字、特殊字符中的至少3种
     * - 不能包含用户名、手机号等个人信息
     * - 不能是常见弱密码（如123456、password等）
     *
     * 示例：
     * - ✅ Abc123!@#（符合强度）
     * - ❌ 123456（太简单）
     * - ❌ password（常见弱密码）
     */
    @Schema(description = "新密码", example = "Abc123!@#", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度为6-20字符")
    private String newPassword;

    /**
     * 确认密码
     *
     * 验证规则：
     * - 非空
     * - 必须与新密码一致
     *
     * 说明：
     * - 前端需要两次输入新密码进行确认
     * - 后端验证两次输入是否一致
     * - 防止用户输入错误
     */
    @Schema(description = "确认密码", example = "Abc123!@#", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    // ==================== 验证方法 ====================

    /**
     * 验证两次输入的新密码是否一致
     *
     * @return true-一致，false-不一致
     */
    public boolean isPasswordMatch() {
        if (newPassword == null || confirmPassword == null) {
            return false;
        }
        return newPassword.equals(confirmPassword);
    }

    /**
     * 验证新密码与旧密码是否不同
     *
     * @return true-不同，false-相同
     */
    public boolean isNewPasswordDifferent() {
        if (oldPassword == null || newPassword == null) {
            return false;
        }
        return !oldPassword.equals(newPassword);
    }

    /**
     * 验证新密码强度
     *
     * 强度要求：
     * 1. 长度6-20字符
     * 2. 包含大写字母
     * 3. 包含小写字母
     * 4. 包含数字
     * 5. 包含特殊字符
     * 6. 至少满足其中4种
     *
     * @return true-强度符合要求，false-强度不足
     */
    public boolean isNewPasswordStrong() {
        if (newPassword == null || newPassword.isEmpty()) {
            return false;
        }

        // 长度验证
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            return false;
        }

        // 强度计算
        int strength = 0;

        // 包含大写字母
        if (newPassword.matches(".*[A-Z].*")) {
            strength++;
        }

        // 包含小写字母
        if (newPassword.matches(".*[a-z].*")) {
            strength++;
        }

        // 包含数字
        if (newPassword.matches(".*\\d.*")) {
            strength++;
        }

        // 包含特殊字符
        if (newPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            strength++;
        }

        // 至少满足其中4种
        return strength >= 4;
    }

    /**
     * 检查新密码是否包含个人信息
     *
     * 检查项：
     * - 不能包含用户名
     * - 不能包含手机号
     * - 不能包含常见弱密码
     *
     * @param username 用户名
     * @param phone 手机号（可选）
     * @return true-不包含个人信息，false-包含个人信息
     */
    public boolean doesNotContainPersonalInfo(String username, String phone) {
        if (newPassword == null) {
            return false;
        }

        // 检查是否包含用户名
        if (username != null && newPassword.toLowerCase().contains(username.toLowerCase())) {
            return false;
        }

        // 检查是否包含手机号
        if (phone != null && newPassword.contains(phone)) {
            return false;
        }

        // 检查常见弱密码
        String[] weakPasswords = {
            "123456", "password", "123456789", "12345678",
            "111111", "qwerty", "abc123", "admin123"
        };

        for (String weak : weakPasswords) {
            if (newPassword.toLowerCase().contains(weak)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取密码强度描述
     *
     * @return 密码强度描述（弱/中/强）
     */
    public String getPasswordStrengthDescription() {
        if (!isNewPasswordStrong()) {
            return "弱";
        }

        // 进一步细分强度等级
        int strength = 0;
        if (newPassword.matches(".*[A-Z].*")) strength++;
        if (newPassword.matches(".*[a-z].*")) strength++;
        if (newPassword.matches(".*\\d.*")) strength++;
        if (newPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) strength++;

        if (strength == 4) {
            return "强";
        } else if (strength == 3) {
            return "中";
        } else {
            return "弱";
        }
    }
}
