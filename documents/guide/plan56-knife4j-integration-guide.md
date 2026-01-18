# Knife4j API 文档集成指南

> **目标读者**：后端开发工程师
> **阅读时间**：15 分钟
> **更新日期**：2026-01-17

---

## 一、Knife4j 简介

### 1.1 什么是 Knife4j

Knife4j 是一款基于 Swagger/OpenAPI 规范的 API 文档增强工具，它为 Swagger 提供了更友好的 UI 界面和更强大的功能。

```
┌─────────────────────────────────────────────────────────────┐
│                    Knife4j 架构图                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   +------------------+         +------------------+          │
│   |   Swagger Core   | <─────> |  Springdoc OpenAPI |       │
│   |   (OpenAPI 3.0)  |         |   (自动扫描注解)    |       │
│   +------------------+         +------------------+          │
│           ↑                                                    │
│           │                                                    │
│   +------------------+                                        │
│   |   Knife4j UI     │  ← 浏览器访问 doc.html                │
│   |   (增强界面)      │                                        │
│   +------------------+                                        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 为什么选择 Knife4j

| 特性 | Knife4j | Swagger UI |
|------|---------|------------|
| UI 美观度 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| 中文支持 | 原生支持 | 需配置 |
| 离线文档 | 支持 | 不支持 |
| 接口调试 | 强大 | 基础 |
| 分组管理 | 清晰 | 一般 |
| 全局参数 | 支持 | 需扩展 |

### 1.3 本项目的选择

- **版本**：Knife4j 4.4.0 + SpringDoc OpenAPI
- **原因**：Spring Boot 3.x 需要使用 `jakarta` 包名的 starter

---

## 二、快速开始

### 2.1 访问地址

| 访问入口 | URL | 说明 |
|----------|-----|------|
| Knife4j UI | http://localhost:8080/doc.html | **推荐**：增强版文档界面 |
| Swagger UI | http://localhost:8080/swagger-ui.html | 原生 Swagger 界面 |
| OpenAPI JSON | http://localhost:8080/v3/api-docs | OpenAPI 规范 JSON |

### 2.2 基础配置（已完成）

本项目已完成的配置：

#### 父 POM 依赖管理
```xml
<properties>
    <knife4j.version>4.4.0</knife4j.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
            <version>${knife4j.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

#### application.yml 配置
```yaml
# Knife4j API文档配置
knife4j:
  enable: true
  setting:
    language: zh_cn

springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
```

---

## 三、实施步骤

### 步骤 1：添加 API 模块依赖

**文件**：`order-platform-backend/order-platform-api/pom.xml`

在 `<dependencies>` 节点中添加：

```xml
<!-- Knife4j API文档 -->
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
</dependency>
```

**位置**：放在 Flyway 依赖之后，Spring Boot Test 之前

---

### 步骤 2：创建 OpenAPI 配置类

**文件**：`order-platform-backend/order-platform-api/src/main/java/com/company/order/visual/config/OpenApiConfig.java`

**完整代码**：

```java
package com.company.order.visual.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j OpenAPI 配置
 *
 * 文档访问地址：
 * - Knife4j UI: http://localhost:8080/doc.html
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    // ==================== 全局 OpenAPI 配置 ====================

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // 全局安全认证（JWT）
                .addSecurityItem(new SecurityRequirement().addList("Authorization"))
                .components(new Components()
                        .addSecuritySchemes("Authorization",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("请输入 JWT Token，无需加 Bearer 前缀"))
                        // 全局响应结构定义（Result<T>）
                        .addSchemas("Result", resultSchema())
                )
                // API 基本信息
                .info(new Info()
                        .title("订单可视化数字化管理平台 API")
                        .description("以销售订单为聚合根的领域驱动管理系统")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Order Platform Team")
                                .email("support@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }

    /**
     * 统一响应结构 Result<T> 的 Schema 定义
     */
    private Schema<?> resultSchema() {
        return new Schema<>()
                .type("object")
                .addProperty("code", new IntegerSchema()._default(200).description("响应码"))
                .addProperty("message", new StringSchema()._default("success").description("响应消息"))
                .addProperty("data", new Schema<>().description("业务数据"))
                .addProperty("timestamp", new IntegerSchema().description("时间戳"));
    }

    // ==================== API 分组配置 ====================

    /**
     * 系统分组 - 健康检查、登录登出等
     */
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("01-系统")
                .pathsToMatch("/api/**/health", "/api/auth/**")
                .build();
    }

    /**
     * 订单分组 - 订单管理相关接口
     */
    @Bean
    public GroupedOpenApi orderApi() {
        return GroupedOpenApi.builder()
                .group("02-订单")
                .pathsToMatch("/api/orders/**", "/api/order/**")
                .build();
    }

    /**
     * 发运分组 - 发运批次、签收相关接口
     */
    @Bean
    public GroupedOpenApi shipmentApi() {
        return GroupedOpenApi.builder()
                .group("03-发运")
                .pathsToMatch("/api/shipments/**", "/api/shipment/**")
                .build();
    }

    /**
     * 合作方分组 - 供应商、承运商管理接口
     */
    @Bean
    public GroupedOpenApi partnerApi() {
        return GroupedOpenApi.builder()
                .group("04-合作方")
                .pathsToMatch("/api/partners/**", "/api/partner/**")
                .build();
    }

    /**
     * 用户分组 - 用户、角色、权限管理接口
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("05-用户")
                .pathsToMatch("/api/users/**", "/api/roles/**", "/api/permissions/**")
                .build();
    }

    /**
     * 看板分组 - KPI 统计、数据聚合接口
     */
    @Bean
    public GroupedOpenApi dashboardApi() {
        return GroupedOpenApi.builder()
                .group("06-看板")
                .pathsToMatch("/api/dashboard/**")
                .build();
    }
}
```

**配置说明**：
- JWT 认证预留（使用 Bearer Token）
- 统一 Result<T> 响应格式
- 6 个业务分组，按路径前缀自动匹配

---

### 步骤 3：更新 Controller 添加 Swagger 注解

**文件**：`order-platform-backend/order-platform-api/src/main/java/com/company/order/visual/controller/HealthController.java`

**更新后代码**：

```java
package com.company.order.visual.controller;

import com.company.order.visual.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 *
 * @author Order Platform Team
 */
@Tag(name = "系统管理", description = "系统健康检查、监控等接口")
@RestController
public class HealthController {

    /**
     * 健康检查接口
     *
     * @return 系统状态信息
     */
    @Operation(
            summary = "健康检查",
            description = "获取系统当前状态，用于监控和负载均衡健康检查"
    )
    @ApiResponse(
            responseCode = "200",
            description = "系统正常运行",
            content = @Content(schema = @Schema(implementation = HealthResponse.class))
    )
    @GetMapping("/api/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("name", "order-platform");
        data.put("time", LocalDateTime.now());
        return Result.ok(data);
    }

    /**
     * 健康检查响应结构（用于文档展示）
     */
    @Schema(description = "健康检查响应")
    public record HealthResponse(
            @Schema(description = "状态：UP-正常, DOWN-异常")
            String status,
            @Schema(description = "系统名称")
            String name,
            @Schema(description = "当前时间")
            LocalDateTime time
    ) {}
}
```

---

### 步骤 4：验证

1. **编译项目**
   ```bash
   cd order-platform-backend
   mvn clean compile
   ```

2. **启动应用**
   ```bash
   mvn spring-boot:run -pl order-platform-api
   ```

3. **访问文档**
   - 浏览器打开：http://localhost:8080/doc.html
   - 检查"系统管理"分组是否显示
   - 检查"健康检查"接口是否显示完整

4. **测试接口调试**
   - 点击"健康检查"接口
   - 点击"调试"按钮
   - 点击"发送请求"
   - 确认返回正确的响应

---

## 四、注解规范

### 4.1 Controller 层注解

| 注解 | 用途 | 示例 |
|------|------|------|
| `@Tag` | 标识 Controller 分组 | `@Tag(name = "订单管理", description = "订单CRUD接口")` |
| `@Operation` | 接口描述 | `@Operation(summary = "创建订单", description = "创建新的销售订单")` |
| `@Parameter` | 参数说明 | `@Parameter(description = "订单ID", required = true) Long id` |
| `@ApiResponse` | 响应说明 | `@ApiResponse(responseCode = "200", description = "成功")` |
| `@SecurityRequirement` | 需要认证 | `@SecurityRequirement(name = "Authorization")` |

**完整示例**：

```java
@Tag(name = "订单管理", description = "订单CRUD接口")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Operation(
        summary = "创建订单",
        description = "创建新的销售订单，支持多产品明细"
    )
    @ApiResponse(responseCode = "200", description = "创建成功")
    @ApiResponse(responseCode = "400", description = "参数错误")
    @SecurityRequirement(name = "Authorization")  // 需要登录
    @PostMapping
    public Result<OrderVO> create(@RequestBody @Valid OrderCreateDTO dto) {
        // ...
    }

    @Operation(summary = "查询订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> getById(
        @Parameter(description = "订单ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        // ...
    }
}
```

### 4.2 Model 层注解

| 注解 | 用途 | 示例 |
|------|------|------|
| `@Schema` | 模型/字段描述 | `@Schema(description = "订单创建请求")` |
| `required = true` | 必填字段 | `@Schema(required = true, description = "客户ID")` |

**DTO 示例**：

```java
@Schema(description = "订单创建请求")
public record OrderCreateDTO(

    @Schema(required = true, description = "客户ID", example = "1")
    Long customerId,

    @Schema(required = true, description = "订单明细列表")
    List<OrderLineCreateDTO> lines,

    @Schema(description = "备注", example = "加急处理")
    String remark
) {}
```

### 4.3 分组规范

按业务模块分组，使用数字前缀保证显示顺序：

| 分组名称 | 路径匹配 | 说明 |
|----------|----------|------|
| 01-系统 | `/api/**/health`, `/api/auth/**` | 健康检查、登录登出 |
| 02-订单 | `/api/orders/**` | 订单管理 |
| 03-发运 | `/api/shipments/**` | 发运批次 |
| 04-合作方 | `/api/partners/**` | 供应商、承运商 |
| 05-用户 | `/api/users/**`, `/api/roles/**` | 用户、角色、权限 |
| 06-看板 | `/api/dashboard/**` | KPI 统计 |

---

## 五、接口调试指南

### 5.1 发送请求

1. 在 Knife4j UI 中找到目标接口
2. 点击"调试"标签
3. 填写请求参数（如果有）
4. 点击"发送请求"
5. 查看响应结果

### 5.2 认证配置

如果接口需要 JWT 认证：

1. 先调用登录接口获取 Token
2. 点击 Knife4j 右上角的"授权"按钮
3. 输入 Token（无需 `Bearer ` 前缀）
4. 保存后，后续请求自动携带 Token

```
┌─────────────────────────────────────┐
│  全局参数设置                        │
├─────────────────────────────────────┤
│  Authorization: __________________   │
│                  [  eyJhbGci...  ]  │
│                                     │
│  [ 保存 ]  [ 关闭 ]                 │
└─────────────────────────────────────┘
```

### 5.3 Mock 数据

Knife4j 支持 Mock 生成：

```java
@Schema(description = "用户信息", example = """
    {
        "id": 1,
        "username": "test_user",
        "realName": "测试用户",
        "email": "test@example.com"
    }
""")
public class UserVO {}
```

---

## 六、最佳实践

### 6.1 命名规范

| 场景 | 规范 | 好示例 | 坏示例 |
|------|------|--------|--------|
| 接口摘要 | 动词+名词，简洁 | "创建订单" | "createOrder" / "新增一个订单到系统中" |
| 参数描述 | 完整说明用途 | "客户ID" | "cid" / "id" |
| 响应描述 | 明确返回内容 | "返回订单详情" | "成功" |

### 6.2 响应格式

统一使用 `Result<T>` 包装：

```json
{
  "code": 200,
  "message": "success",
  "data": { /* 业务数据 */ },
  "timestamp": 1705334400000
}
```

错误码规范：

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

### 6.3 常见问题

#### 问题 1：依赖冲突

**症状**：启动报错 `ClassNotFoundException: jakarta.servlet...`

**解决**：确认使用 `knife4j-openapi3-jakarta-spring-boot-starter`（带 jakarta）

#### 问题 2：注解不生效

**症状**：访问 doc.html 空白或无接口

**排查**：
1. 检查 Controller 是否在 `@RestController` 下
2. 检查包扫描路径：`@SpringBootApplication(scanBasePackages = "...")`
3. 检查 `@Tag` 注解是否正确

#### 问题 3：分组不显示

**症状**：分组存在但接口不显示

**排查**：
1. 检查 `pathsToMatch` 路径是否与实际路径匹配
2. 检查接口 URL 是否以 `/api` 开头

---

## 七、附录

### 7.1 常用注解速查表

```java
// Controller 级别
@Tag(name = "分组名", description = "分组描述")

// 接口级别
@Operation(summary = "简短描述", description = "详细描述")
@Parameter(name = "参数名", description = "参数说明", required = true)
@ApiResponse(responseCode = "200", description = "响应说明")

// Model 级别
@Schema(description = "模型/字段说明", required = true, example = "示例值")

// 认证
@SecurityRequirement(name = "Authorization")  // 需要 JWT
```

### 7.2 参考资源

| 资源 | 链接 |
|------|------|
| Knife4j 官网 | https://doc.xiaominfo.com/ |
| SpringDoc 官网 | https://springdoc.org/ |
| OpenAPI 3.0 规范 | https://swagger.io/specification/ |

---

**文档版本**：v1.0
**最后更新**：2026-01-17
**维护者**：Order Platform Team
