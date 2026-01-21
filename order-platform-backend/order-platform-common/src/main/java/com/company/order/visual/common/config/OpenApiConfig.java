package com.company.order.visual.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 全局配置
 * <p>
 * 职责：
 * - 配置 API 基本信息（标题、描述、版本、许可）
 * - 配置 JWT Bearer 认证安全方案
 * - 全局应用认证要求
 * <p>
 * 访问地址：
 * - Knife4j UI: http://localhost:8081/api/doc.html
 * - Swagger UI: http://localhost:8081/api/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "JWT认证";
    public static final String BEARER_FORMAT = "JWT";
    public static final String SCHEME_NAME = "bearer";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API 基本信息
                .info(new Info()
                        .title("订单可视化数字化管理平台 API")
                        .description("""
                                以销售订单为聚合根的领域驱动管理系统。

                                **核心业务链：**
                                客户下单 → 对接产地供应商 → 安排第三方物流 → 多收货点签收

                                **认证方式：**
                                使用 JWT Bearer Token，在请求头中添加：`Authorization: Bearer {token}`
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Order Platform Team")
                                .email("support@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                // JWT 认证安全方案
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme(SCHEME_NAME)
                                        .bearerFormat(BEARER_FORMAT)
                                        .description("""
                                                请输入 JWT Token，无需添加 "Bearer " 前缀。

                                                **获取方式：**
                                                调用 /api/auth/login 接口登录成功后返回
                                                """)))
                // 全局应用认证要求（个别公开接口可通过 @io.swagger.v3.oas.annotations.security.SecurityRequirements 覆盖）
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
