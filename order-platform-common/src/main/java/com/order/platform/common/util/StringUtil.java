package com.order.platform.common.util;

import cn.hutool.core.util.StrUtil;

/**
 * 字符串工具类
 *
 * 功能说明：
 * - 字符串判空
 * - 敏感信息脱敏（手机号、身份证、邮箱）
 * - 字符串截断
 * - 随机字符串生成
 *
 * 设计说明：
 * - 基于 Hutool 的 StrUtil 进行封装
 * - 增加项目特有的脱敏规则
 * - 统一项目字符串处理规范
 *
 * @since 1.0.1
 */
public class StringUtil {

    // ==================== 判空 ====================

    /**
     * 判断字符串是否为空
     *
     * @param str 字符串
     * @return true-为空，false-不为空
     */
    public static boolean isEmpty(String str) {
        return StrUtil.isEmpty(str);
    }

    /**
     * 判断字符串是否为空白（null、空串、纯空格）
     *
     * @param str 字符串
     * @return true-为空白，false-不为空白
     */
    public static boolean isBlank(String str) {
        return StrUtil.isBlank(str);
    }

    /**
     * 判断字符串是否不为空
     *
     * @param str 字符串
     * @return true-不为空，false-为空
     */
    public static boolean isNotEmpty(String str) {
        return StrUtil.isNotEmpty(str);
    }

    /**
     * 判断字符串是否不为空白
     *
     * @param str 字符串
     * @return true-不为空白，false-为空白
     */
    public static boolean isNotBlank(String str) {
        return StrUtil.isNotBlank(str);
    }

    // ==================== 脱敏 ====================

    /**
     * 手机号脱敏
     *
     * 脱敏规则：保留前3位和后4位，中间用****代替
     * 示例：13812345678 → 138****5678
     *
     * @param phone 手机号
     * @return 脱敏后的手机号，非手机号格式原样返回
     */
    public static String maskPhone(String phone) {
        if (isBlank(phone)) {
            return phone;
        }
        // 去除空格
        phone = phone.replaceAll("\\s+", "");
        // 验证手机号格式（1开头的11位数字）
        if (!phone.matches("^1\\d{10}$")) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 身份证号脱敏
     *
     * 脱敏规则：保留前6位和后4位，中间用********代替
     * 示例：330102199001011234 → 330102********1234
     *
     * @param idCard 身份证号
     * @return 脱敏后的身份证号，非身份证号格式原样返回
     */
    public static String maskIdCard(String idCard) {
        if (isBlank(idCard)) {
            return idCard;
        }
        // 去除空格和横杠
        idCard = idCard.replaceAll("[\\s-]+", "");
        // 支持15位或18位身份证号
        if (idCard.length() == 15) {
            return idCard.replaceAll("(\\d{6})\\d{5}(\\d{4})", "$1*****$2");
        } else if (idCard.length() == 18) {
            return idCard.replaceAll("(\\d{6})\\d{8}(\\d{4})", "$1********$2");
        }
        return idCard;
    }

    /**
     * 邮箱脱敏
     *
     * 脱敏规则：保留首字母和@之后的域名，中间用***代替
     * 示例：abc@example.com → a***@example.com
     *
     * @param email 邮箱地址
     * @return 脱敏后的邮箱，非邮箱格式原样返回
     */
    public static String maskEmail(String email) {
        if (isBlank(email) || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return email;
        }
        String username = parts[0];
        String domain = parts[1];
        // 用户名保留首字母，后面用***
        if (username.length() > 1) {
            username = username.charAt(0) + "***";
        }
        return username + "@" + domain;
    }

    /**
     * 银行卡号脱敏
     *
     * 脱敏规则：只显示后4位，其余用****代替
     * 示例：6222021234567890123 → ************0123
     *
     * @param cardNo 银行卡号
     * @return 脱敏后的卡号，非卡号格式原样返回
     */
    public static String maskBankCard(String cardNo) {
        if (isBlank(cardNo)) {
            return cardNo;
        }
        // 去除空格
        cardNo = cardNo.replaceAll("\\s+", "");
        // 验证银行卡号格式（12-19位数字）
        if (!cardNo.matches("^\\d{12,19}$")) {
            return cardNo;
        }
        // 只显示后4位
        int length = cardNo.length();
        String last4 = cardNo.substring(length - 4);
        return "*".repeat(length - 4) + last4;
    }

    // ==================== 截断 ====================

    /**
     * 字符串截断（超长用省略号）
     *
     * 截断规则：
     * - 长度小于等于 maxLength，原样返回
     * - 长度大于 maxLength，截取前 maxLength-3 个字符，加上 "..."
     *
     * @param str        字符串
     * @param maxLength  最大长度
     * @return 截断后的字符串
     */
    public static String truncate(String str, int maxLength) {
        if (isBlank(str) || str.length() <= maxLength) {
            return str;
        }
        if (maxLength <= 3) {
            return "...";
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * 字符串截取（不添加省略号）
     *
     * @param str    字符串
     * @param length 截取长度
     * @return 截取后的字符串
     */
    public static String substring(String str, int length) {
        if (isBlank(str)) {
            return str;
        }
        return StrUtil.sub(str, 0, length);
    }

    // ==================== 随机生成 ====================

    /**
     * 生成随机数字字符串
     *
     * @param length 长度
     * @return 随机数字字符串
     */
    public static String randomNumeric(int length) {
        return StrUtil.randomNumbers(length);
    }

    /**
     * 生成随机字母数字字符串
     *
     * @param length 长度
     * @return 随机字母数字字符串
     */
    public static String randomString(int length) {
        return StrUtil.randomString(length);
    }

    // ==================== 其他 ====================

    /**
     * 去除字符串两端空白
     *
     * @param str 字符串
     * @return 去除两端空白后的字符串
     */
    public static String trim(String str) {
        return StrUtil.trim(str);
    }

    /**
     * 下划线转驼峰
     *
     * 示例：order_no → orderNo
     *
     * @param str 下划线命名的字符串
     * @return 驼峰命名的字符串
     */
    public static String toCamelCase(String str) {
        return StrUtil.toCamelCase(str);
    }

    /**
     * 驼峰转下划线
     *
     * 示例：orderNo → order_no
     *
     * @param str 驼峰命名的字符串
     * @return 下划线命名的字符串
     */
    public static String toSnakeCase(String str) {
        return StrUtil.toUnderlineCase(str);
    }
}
