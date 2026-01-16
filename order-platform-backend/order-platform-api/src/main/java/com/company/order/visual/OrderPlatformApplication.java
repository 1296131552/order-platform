package com.company.order.visual;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 订单可视化数字化管理平台 - 启动类
 *
 * @author Order Platform Team
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.company.order.visual")
public class OrderPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderPlatformApplication.class, args);
    }
}
