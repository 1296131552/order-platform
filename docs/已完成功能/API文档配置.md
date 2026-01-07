# API 文档配置

> **功能文档**：Knife4j API 文档配置与分组管理
>
> **完成日期**：2026-01-08
>
> **维护团队**：后端开发组

---

## 📋 功能概述

### 问题描述

项目集成了 Knife4j API 文档工具，但缺少 OpenAPI 配置类，导致：
- API 文档页面显示不正常（右侧空白）
- 只显示一个接口，接口列表不完整
- 访问 `http://localhost:8080/doc.html` 无法正常使用

### 解决方案

创建 `OpenApiConfig` 配置类，配置 API 分组、JWT 认证、基本信息等。

---

## 🔧 实现细节

### 1. 创建 OpenAPI 配置类

**文件**：`order-platform-common/src/main/java/com/order/platform/common/config/OpenApiConfig.java`

```java
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
                        .description("...")
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
     * 用户模块 API 分组
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("02-用户模块")
                .pathsToMatch("/api/user/**")
                .build();
    }

    // ... 其他模块分组（订单、发运、合作方、附件、可视化、看板、异常）
}
```

### 2. 修复 application.yml 配置

**文件**：`order-platform-api/src/main/resources/application.yml`

**修复前**：
```yaml
springdoc:
  api-docs:
    path: /doc.html  # ❌ 错误：这是 JSON 路径，不是 HTML
```

**修复后**：
```yaml
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs  # ✅ 正确：API 文档 JSON 路径
  swagger-ui:
    enabled: true
    path: /swagger-ui.html

knife4j:
  enable: true
  setting:
    language: zh_cn
    swagger-model-name: 实体类列表
```

### 3. 添加 common 模块依赖

**文件**：`order-platform-common/pom.xml`

```xml
<!-- Knife4j API 文档（用于配置类） -->
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
    <optional>true</optional>
</dependency>
```

---

## ✅ 测试结果

### 访问地址

- **Knife4j 文档**：`http://localhost:8081/doc.html`
- **Swagger UI**：`http://localhost:8081/swagger-ui.html`
- **OpenAPI JSON**：`http://localhost:8081/v3/api-docs`

### API 分组

| 分组 | 路径 | 状态 |
|------|------|------|
| 01-认证模块 | `/api/auth/**` | ✅ |
| 02-用户模块 | `/api/user/**` | ✅ |
| 03-订单模块 | `/api/order/**` | ⏳ |
| 04-发运模块 | `/api/shipment/**` | ⏳ |
| 05-合作方模块 | `/api/partner/**` | ⏳ |
| 06-附件模块 | `/api/attachment/**` | ⏳ |
| 07-可视化模块 | `/api/visualization/**` | ⏳ |
| 08-看板模块 | `/api/dashboard/**` | ⏳ |
| 09-异常模块 | `/api/exception/**` | ⏳ |

### JWT 认证

点击文档右上角 **"Authorize"** 按钮，输入格式：
```
Bearer {token}
```

---

## 📝 相关文件

| 文件 | 说明 |
|------|------|
| `common/config/OpenApiConfig.java` | OpenAPI 配置类 |
| `api/pom.xml` | 添加 spring-boot-devtools 依赖 |
| `api/src/main/resources/application.yml` | 修复 springdoc 配置 |
| `common/pom.xml` | 添加 Knife4j 依赖 |

---

## 🔗 相关文档

- [API接口文档](../../API接口文档.md)
- [order-platform-api/README.md](../../order-platform-api/README.md)
- [Knife4j 官方文档](https://doc.xiaominfo.com/)
