package com.order.platform.user.utils;

import com.order.platform.common.config.OrderPlatformProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;

/**
 * 密码工具类
 *
 * 功能说明：
 * - 密码加密（BCrypt算法，strength=10）
 * - 密码验证（匹配BCrypt加密的密码）
 * - 密码强度验证（从配置读取参数）
 * - 随机密码生成（从配置读取参数）
 *
 * 安全特性：
 * - BCrypt：自适应哈希算法，每次加密结果不同
 * - Strength=10：安全性和性能的平衡点
 * - 自动加盐：BCrypt内部自动处理，无需手动加盐
 * - 单向加密：无法解密，只能验证匹配
 *
 * 配置管理：
 * - 密码最小长度：从 OrderPlatformProperties 读取
 * - 密码最大长度：从 OrderPlatformProperties 读取
 * - 密码最小强度：从 OrderPlatformProperties 读取
 *
 * 使用场景：
 * - 用户注册时加密密码
 * - 用户登录时验证密码
 * - 用户修改密码时加密新密码
 * - 重置密码时生成随机密码
 *
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordEncoderUtil {

    /**
     * BCrypt密码编码器
     *
     * strength=10 说明：
     * - 安全性：2^10次哈希迭代
     * - 性能：每次验证耗时约50-100ms
     * - 平衡点：安全性和性能的最佳平衡
     *
     * 安全等级对比：
     * - strength=4：弱（不推荐）
     * - strength=8：中（推荐）
     * - strength=10：强（推荐）✅ 当前使用
     * - strength=12：非常强（性能影响大）
     */
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    /**
     * 随机数生成器（用于生成随机密码）
     */
    private static final Random RANDOM = new SecureRandom();

    /**
     * 配置属性（从 application.yml 读取）
     */
    private final OrderPlatformProperties properties;

    /**
     * 加密密码
     *
     * 功能说明：
     * - 使用BCrypt算法加密密码
     * - 每次加密结果都不同（自动加盐）
     * - 单向加密，无法解密
     *
     * 算法特点：
     * - BCrypt：Blowfish加密算法的变体
     * - 自适应：计算能力增强时可增加strength
     * - 加盐：自动在密文中包含盐值
     *
     * 使用示例：
     * <pre>
     * String rawPassword = "123456";
     * String encodedPassword = PasswordEncoderUtil.encode(rawPassword);
     * // encodedPassword: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
     * </pre>
     *
     * @param rawPassword 明文密码
     * @return 加密后的密码（60字符）
     */
    public static String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("明文密码不能为空");
        }

        try {
            return passwordEncoder.encode(rawPassword);
        } catch (Exception e) {
            log.error("密码加密失败", e);
            throw new RuntimeException("密码加密失败", e);
        }
    }

    /**
     * 验证密码
     *
     * 功能说明：
     * - 验证明文密码与加密密码是否匹配
     * - 使用BCrypt的matches方法
     * - 时间恒定比较，防止计时攻击
     *
     * 安全说明：
     * - 时间恒定比较：即使密码错误，耗时也相同
     * - 防计时攻击：攻击者无法通过耗时判断密码是否正确
     *
     * 使用示例：
     * <pre>
     * String rawPassword = "123456";
     * String encodedPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
     * boolean matches = PasswordEncoderUtil.matches(rawPassword, encodedPassword);
     * // matches: true
     * </pre>
     *
     * @param rawPassword     明文密码（用户输入的密码）
     * @param encodedPassword 加密密码（数据库中存储的密码）
     * @return true-密码匹配，false-密码不匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }

        try {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        } catch (Exception e) {
            log.error("密码验证失败", e);
            return false;
        }
    }

    /**
     * 验证密码强度
     *
     * 功能说明：
     * - 检查密码是否符合强度要求
     * - 返回详细的强度分析结果
     * - 强度参数从配置读取（配置优先规则）
     *
     * 强度要求（至少满足 MIN_STRENGTH_SCORE 项）：
     * 1. 长度：minLength-maxLength 字符（从配置读取）
     * 2. 包含大写字母
     * 3. 包含小写字母
     * 4. 包含数字
     * 5. 包含特殊字符
     *
     * 使用示例：
     * <pre>
     * String password = "Abc123!@#";
     * PasswordStrength strength = passwordEncoderUtil.validateStrength(password);
     * if (strength.isValid()) {
     *     // 密码强度符合要求
     * }
     * </pre>
     *
     * @param password 待验证的密码
     * @return 密码强度分析结果
     */
    public PasswordStrength validateStrength(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordStrength(0, false, "密码不能为空");
        }

        // 从配置读取密码策略参数（配置优先规则）
        int minLength = properties.getSecurity().getPassword().getMinLength();
        int maxLength = properties.getSecurity().getPassword().getMaxLength();
        int minStrengthScore = properties.getSecurity().getPassword().getMinStrength();

        int strengthScore = 0;
        StringBuilder issues = new StringBuilder();

        // 1. 长度验证
        if (password.length() >= minLength && password.length() <= maxLength) {
            strengthScore++;
        } else {
            issues.append("长度必须是").append(minLength).append("-").append(maxLength)
                .append("字符；");
        }

        // 2. 大写字母验证
        if (password.matches(".*[A-Z].*")) {
            strengthScore++;
        } else {
            issues.append("缺少大写字母；");
        }

        // 3. 小写字母验证
        if (password.matches(".*[a-z].*")) {
            strengthScore++;
        } else {
            issues.append("缺少小写字母；");
        }

        // 4. 数字验证
        if (password.matches(".*\\d.*")) {
            strengthScore++;
        } else {
            issues.append("缺少数字；");
        }

        // 5. 特殊字符验证
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            strengthScore++;
        } else {
            issues.append("缺少特殊字符；");
        }

        // 从配置读取最小强度得分
        boolean isValid = strengthScore >= minStrengthScore;
        String message = isValid ? "密码强度符合要求" : "密码强度不足：" + issues.toString();

        return new PasswordStrength(strengthScore, isValid, message);
    }

    /**
     * 生成随机密码
     *
     * 功能说明：
     * - 生成指定长度的随机密码
     * - 包含大写字母、小写字母、数字、特殊字符
     * - 确保密码强度符合要求
     * - 密码长度限制从配置读取（配置优先规则）
     *
     * 参数说明：
     * - length：密码长度（默认10，范围从配置读取）
     * - useSpecialChars：是否使用特殊字符（默认true）
     *
     * 字符集：
     * - 大写字母：A-Z
     * - 小写字母：a-z
     * - 数字：0-9
     * - 特殊字符：!@#$%^&*()_+-=[]{}|;:,.<>?
     *
     * 使用示例：
     * <pre>
     * // 生成10位随机密码（含特殊字符）
     * String password = passwordEncoderUtil.generateRandomPassword();
     *
     * // 生成8位随机密码（不含特殊字符）
     * String password = passwordEncoderUtil.generateRandomPassword(8, false);
     * </pre>
     *
     * @param length          密码长度（从配置读取范围）
     * @param useSpecialChars 是否包含特殊字符
     * @return 随机密码
     */
    public String generateRandomPassword(int length, boolean useSpecialChars) {
        // 从配置读取密码长度限制（配置优先规则）
        int minLength = properties.getSecurity().getPassword().getMinLength();
        int maxLength = properties.getSecurity().getPassword().getMaxLength();

        if (length < minLength || length > maxLength) {
            throw new IllegalArgumentException("密码长度必须在" + minLength + "-" + maxLength + "之间");
        }

        // 字符集定义
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";

        StringBuilder allChars = new StringBuilder();
        allChars.append(upperCase);
        allChars.append(lowerCase);
        allChars.append(digits);
        if (useSpecialChars) {
            allChars.append(specialChars);
        }

        StringBuilder password = new StringBuilder();

        // 确保至少包含1个大写字母、1个小写字母、1个数字
        password.append(upperCase.charAt(RANDOM.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(RANDOM.nextInt(lowerCase.length())));
        password.append(digits.charAt(RANDOM.nextInt(digits.length())));

        // 如果需要特殊字符，确保至少包含1个特殊字符
        if (useSpecialChars) {
            password.append(specialChars.charAt(RANDOM.nextInt(specialChars.length())));
        }

        // 填充剩余长度
        for (int i = password.length(); i < length; i++) {
            password.append(allChars.charAt(RANDOM.nextInt(allChars.length())));
        }

        // 打乱字符顺序（避免固定模式）
        return shuffleString(password.toString());
    }

    /**
     * 生成随机密码（默认10位，含特殊字符）
     *
     * @param length 密码长度（从配置读取范围）
     * @return 随机密码
     */
    public String generateRandomPassword(int length) {
        return generateRandomPassword(length, true);
    }

    /**
     * 生成随机密码（默认10位，含特殊字符）
     *
     * @return 随机密码
     */
    public String generateRandomPassword() {
        return generateRandomPassword(10, true);
    }

    /**
     * 打乱字符串顺序
     *
     * @param input 输入字符串
     * @return 打乱后的字符串
     */
    private static String shuffleString(String input) {
        char[] characters = input.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            int randomIndex = RANDOM.nextInt(characters.length);
            char temp = characters[i];
            characters[i] = characters[randomIndex];
            characters[randomIndex] = temp;
        }
        return new String(characters);
    }

    /**
     * 密码强度分析结果
     */
    public static class PasswordStrength {
        /**
         * 强度得分（0-5）
         *
         * 0：不符合任何要求
         * 1：只满足长度
         * 2：满足长度+1种字符类型
         * 3：满足长度+2种字符类型（最低要求）
         * 4：满足长度+3种字符类型
         * 5：满足所有要求
         */
        private final int score;

        /**
         * 是否符合强度要求
         */
        private final boolean valid;

        /**
         * 强度描述或错误提示
         */
        private final String message;

        public PasswordStrength(int score, boolean valid, String message) {
            this.score = score;
            this.valid = valid;
            this.message = message;
        }

        public int getScore() {
            return score;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        /**
         * 获取强度等级
         *
         * @return 强度等级（弱/中/强）
         */
        public String getStrengthLevel() {
            if (score <= 2) {
                return "弱";
            } else if (score == 3 || score == 4) {
                return "中";
            } else {
                return "强";
            }
        }
    }

    /**
     * 常见弱密码列表
     */
    public static final String[] COMMON_WEAK_PASSWORDS = {
        "123456", "password", "123456789", "12345678",
        "111111", "qwerty", "abc123", "admin123",
        "123abc", "123123", "admin", "root", "user"
    };

    /**
     * 检查是否为常见弱密码
     *
     * @param password 待检查的密码
     * @return true-弱密码，false-非弱密码
     */
    public static boolean isWeakPassword(String password) {
        if (password == null || password.isEmpty()) {
            return true;
        }

        String lowerPassword = password.toLowerCase();
        for (String weak : COMMON_WEAK_PASSWORDS) {
            if (lowerPassword.equals(weak) || lowerPassword.contains(weak)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查密码是否包含个人信息
     *
     * @param password 密码
     * @param username 用户名（可选）
     * @param phone    手机号（可选）
     * @return true-包含个人信息，false-不包含个人信息
     */
    public static boolean containsPersonalInfo(String password, String username, String phone) {
        if (password == null || password.isEmpty()) {
            return true;
        }

        // 检查是否包含用户名
        if (username != null && !username.isEmpty()) {
            if (password.toLowerCase().contains(username.toLowerCase())) {
                return true;
            }
        }

        // 检查是否包含手机号
        if (phone != null && !phone.isEmpty()) {
            if (password.contains(phone)) {
                return true;
            }
        }

        return false;
    }
}
