package com.order.platform.user.test;

import com.order.platform.user.utils.PasswordEncoderUtil;

/**
 * 密码哈希生成器
 * 运行这个类来生成正确的BCrypt哈希
 */
public class GeneratePasswordHash {
    public static void main(String[] args) {
        String[] passwords = {"admin", "admin123", "123456"};

        System.out.println("-- BCrypt密码哈希（strength=10）");
        System.out.println("-- 使用Spring Security BCryptPasswordEncoder生成");
        System.out.println();

        for (String pwd : passwords) {
            String hash = PasswordEncoderUtil.encode(pwd);
            System.out.println("-- 密码: " + pwd);
            System.out.println("UPDATE t_user SET password = '" + hash + "' WHERE username = 'admin';");
            System.out.println();

            // 验证生成的哈希
            boolean matches = PasswordEncoderUtil.matches(pwd, hash);
            System.out.println("-- 验证: " + (matches ? "✓ 正确" : "✗ 错误"));
            System.out.println();
        }
    }
}
