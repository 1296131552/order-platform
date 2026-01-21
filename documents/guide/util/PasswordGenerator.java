package com.company.order.visual.user.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具
 * <p> mvn test-compile exec:java -Dexec.mainClass="com.company.order.visual.user.util.PasswordGenerator"
 * 用于生成 BCrypt 加密后的密码，方便插入测试数据
 */
public class PasswordGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 生成不同密码的哈希值
        String[] passwords = {"admin123", "password123", "123456"};

        System.out.println("========================================");
        System.out.println("BCrypt 密码哈希生成");
        System.out.println("========================================");

        for (String pwd : passwords) {
            String hash = encoder.encode(pwd);
            System.out.println();
            System.out.println("明文密码: " + pwd);
            System.out.println("BCrypt哈希: " + hash);

            // 验证密码是否匹配
            boolean matches = encoder.matches(pwd, hash);
            System.out.println("验证结果: " + matches);
        }

        System.out.println();
        System.out.println("========================================");
    }
}
