package com.order.platform.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 配置类
 *
 * 功能说明：
 * - 配置 Knife4j API 文档（基于 OpenAPI 3.0）
 * - 支持多模块 API 分组显示
 * - 支持 JWT Token 认证
 * - 配置 API 基本信息（标题、版本、描述等）
 *
 * 访问地址：
 * - Knife4j 文档：http://localhost:8081/doc.html
 * - Swagger UI：http://localhost:8081/swagger-ui.html
 * - OpenAPI JSON：http://localhost:8081/v3/api-docs
 *
 * 分组说明：
 * - 认证模块：/api/auth/**
 * - 用户模块：/api/user/**
 * - 订单模块：/api/order/**
 *
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * 配置 OpenAPI 基本信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API 基本信息
                .info(new Info()
                        .title("订单可视化平台 API 文档")
                        .version("1.0.0")
                        .description("""
                                ## 订单可视化数字化管理平台

                                ### 技术栈
                                - Spring Boot 3.2.x
                                - Java 21
                                - MySQL 8.0+
                                - MyBatis Plus
                                - Knife4j (OpenAPI 3.0)

                                ### 认证说明
                                除登录/注册接口外，所有接口都需要在请求头中携带 Token：

                                ```
                                Authorization: Bearer {token}
                                ```

                                ### 错误码说明
                                - 200: 操作成功
                                - 401: 未登录或 Token 过期
                                - 403: 无权限访问
                                - 500: 服务器内部错误
                                """)
                        .contact(new Contact()
                                .name("开发组")
                                .email("dev@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                // 服务器地址
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("本地开发环境"),
                        new Server().url("/").description("当前服务器")
                ))
                // JWT 认证配置
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("请输入 JWT Token，格式：Bearer {token}")));
    }

    /**
     * 认证模块 API 分组
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("01-认证模块")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    /**
     * 用户管理 API 分组（/api/users/**）
     */
    @Bean
    public GroupedOpenApi usersApi() {
        return GroupedOpenApi.builder()
                .group("02-用户管理")
                .pathsToMatch("/api/users/**")
                .build();
    }

    /**
     * 用户模块 API 分组（/api/user/**）
     * 注意：当前项目中使用的是 /api/users/**，此分组预留
     */
    /*@Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("03-用户模块")
                .pathsToMatch("/api/user/**")
                .build();
    }*/

    /**
     * 订单模块 API 分组
     */
    @Bean
    public GroupedOpenApi orderApi() {
        return GroupedOpenApi.builder()
                .group("03-订单模块")
                .pathsToMatch("/api/order/**")
                .build();
    }

    /**
     * 发运模块 API 分组
     */
    @Bean
    public GroupedOpenApi shipmentApi() {
        return GroupedOpenApi.builder()
                .group("04-发运模块")
                .pathsToMatch("/api/shipment/**")
                .build();
    }

    /**
     * 合作方模块 API 分组
     */
    @Bean
    public GroupedOpenApi partnerApi() {
        return GroupedOpenApi.builder()
                .group("05-合作方模块")
                .pathsToMatch("/api/partner/**")
                .build();
    }

    /**
     * 附件模块 API 分组
     */
    @Bean
    public GroupedOpenApi attachmentApi() {
        return GroupedOpenApi.builder()
                .group("06-附件模块")
                .pathsToMatch("/api/attachment/**")
                .build();
    }

    /**
     * 可视化模块 API 分组
     */
    @Bean
    public GroupedOpenApi visualizationApi() {
        return GroupedOpenApi.builder()
                .group("07-可视化模块")
                .pathsToMatch("/api/visualization/**")
                .build();
    }

    /**
     * 看板模块 API 分组
     */
    @Bean
    public GroupedOpenApi dashboardApi() {
        return GroupedOpenApi.builder()
                .group("08-看板模块")
                .pathsToMatch("/api/dashboard/**")
                .build();
    }

    /**
     * 异常模块 API 分组
     */
    @Bean
    public GroupedOpenApi exceptionApi() {
        return GroupedOpenApi.builder()
                .group("09-异常模块")
                .pathsToMatch("/api/exception/**")
                .build();
    }
}
