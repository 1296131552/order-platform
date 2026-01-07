package com.order.platform.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 订单可视化平台 - API 启动类
 *
 * 功能说明：
 * - Spring Boot 主入口
 * - 启动嵌入式 Tomcat 服务器
 * - 自动加载所有模块（common、user、其他业务模块）
 * - 扫描所有组件（Controller、Service、Mapper 等）
 *
 * 模块架构：
 * <pre>
 * order-platform-api（启动模块）  ← 本类
 *    ↓ 依赖
 * order-platform-user（用户模块）
 *    ↓ 依赖
 * order-platform-common（基础模块）
 * </pre>
 *
 * 启动方式：
 * <pre>
 * # Maven 命令启动
 * mvn spring-boot:run
 *
 * # Java 命令启动（打包后）
 * java -jar order-platform-api-1.0.0.jar
 *
 * # IDE 启动
 * 直接运行本类的 main 方法
 * </pre>
 *
 * 访问地址：
 * - API 文档（Knife4j）：http://localhost:8081/doc.html
 * - 健康检查（Actuator）：http://localhost:8081/actuator/health
 *
 * 配置文件：
 * - application.yml：统一配置文件（所有模块的配置）
 *
 * 依赖说明：
 * - common：基础组件（工具类、统一响应、异常处理等）
 * - user：用户模块（认证、授权、用户管理等）
 * - order：订单模块（TODO）
 * - shipment：发运模块（TODO）
 * - partner：合作方模块（TODO）
 * - attachment：附件模块（TODO）
 * - visualization：可视化模块（TODO）
 * - dashboard：看板模块（TODO）
 * - exception：异常模块（TODO）
 *
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.order.platform")
@MapperScan("com.order.platform.*.mapper")
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
        System.out.println("\n" +
                "██████████████████████████████████████████████████████\n" +
                "██                                                    ██\n" +
                "██    订单可视化数字化管理平台启动成功！                 ██\n" +
                "██                                                    ██\n" +
                "██    API 文档: http://localhost:8081/doc.html        ██\n" +
                "██    健康检查: http://localhost:8081/actuator/health  ██\n" +
                "██                                                    ██\n" +
                "██████████████████████████████████████████████████████\n");
    }
}
