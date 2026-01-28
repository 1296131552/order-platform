package com.company.user;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成器 - 用于生成BCrypt加密密码
 *
 * 运行此测试类中的方法，控制台会输出加密后的密码
 */
class PasswordGeneratorTest {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void generatePassword_admin123() {
        String plainPassword = "admin123";
        String encryptedPassword = encoder.encode(plainPassword);
        System.out.println("===== 密码生成 =====");
        System.out.println("明文密码: " + plainPassword);
        System.out.println("加密密码: " + encryptedPassword);
    }

    @Test
    void generatePassword_123456() {
        String plainPassword = "123456";
        String encryptedPassword = encoder.encode(plainPassword);
        System.out.println("===== 密码生成 =====");
        System.out.println("明文密码: " + plainPassword);
        System.out.println("加密密码: " + encryptedPassword);
    }

    @Test
    void generatePassword_custom() {
        // 修改这里的密码
        String plainPassword = "Ww123456789";
        String encryptedPassword = encoder.encode(plainPassword);
        System.out.println("===== 密码生成 =====");
        System.out.println("明文密码: " + plainPassword);
        System.out.println("加密密码: " + encryptedPassword);
    }

    @Test
    void generatePassword_batch() {
        String[] passwords = {"admin123", "123456", "password", "admin"};

        System.out.println("===== 批量生成密码 =====");
        for (String plainPassword : passwords) {
            String encryptedPassword = encoder.encode(plainPassword);
            System.out.println("明文: " + plainPassword + "  =>  加密: " + encryptedPassword);
        }
    }
}
