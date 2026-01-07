# order-platform-api

> **订单可视化平台 - API 启动模块**

> Spring Boot 主入口，统一配置管理，聚合所有业务模块。

---

## 📋 目录

- [模块概述](#模块概述)
- [模块架构](#模块架构)
- [启动方式](#启动方式)
- [配置说明](#配置说明)
- [访问地址](#访问地址)
- [依赖说明](#依赖说明)
- [开发规范](#开发规范)

---

## 模块概述

### 定位

**API 模块**（order-platform-api）是系统的启动模块，提供：

- ✅ Spring Boot 主入口（ApiApplication）
- ✅ 统一配置文件（application.yml）
- ✅ RESTful API 接口聚合
- ✅ API 文档生成（Knife4j）
- ✅ 健康检查（Actuator）

### 设计原则

1. **启动入口**：唯一包含 Spring Boot 启动类的模块
2. **配置集中**：所有模块的配置文件在本模块统一管理
3. **接口聚合**：聚合所有业务模块的 RESTful API
4. **零业务逻辑**：本模块不包含业务代码，只负责启动和配置

---

## 模块架构

### 模块依赖关系

```
order-platform-api（启动模块）
    │
    ├─→ order-platform-user（用户模块）
    │       │
    │       └─→ order-platform-common（基础模块）
    │
    └─→ order-platform-common（基础模块）
```

### 模块职责

| 模块 | 职责 | 状态 |
|------|------|------|
| **order-platform-api** | 启动入口、配置管理、接口聚合 | ✅ 已创建 |
| **order-platform-common** | 基础组件、工具类、统一响应 | ✅ 已完成 |
| **order-platform-user** | 用户管理、认证授权、角色权限 | ✅ 已完成 |
| **order-platform-order** | 订单管理 | ⏳ 待开发 |
| **order-platform-shipment** | 发运管理 | ⏳ 待开发 |
| **order-platform-partner** | 合作方管理 | ⏳ 待开发 |

---

## 启动方式

### 方式 1：Maven 命令启动

```bash
# 进入 API 模块目录
cd order-platform-api

# 启动应用
mvn spring-boot:run
```

### 方式 2：IDE 启动

1. 在 IDE 中打开 `ApiApplication.java`
2. 右键点击，选择 `Run 'ApiApplication'`

### 方式 3：打包后启动

```bash
# 打包（跳过测试）
mvn clean package -DskipTests

# 启动 JAR 包
java -jar order-platform-api/target/order-platform-api-1.0.0.jar
```

---

## 配置说明

### 配置文件位置

```
order-platform-api/
└── src/main/resources/
    └── application.yml          ✅ 统一配置文件
```

### 配置结构

```yaml
# 业务配置
order:
  platform:
    security:
      password:
        max-attempts: 5
        lock-minutes: 30
        expire-days: 90
        min-length: 6
        max-length: 20
        min-strength: 3
      token:
        expiration: 604800
    jwt:
      secret: ${JWT_SECRET:dev-secret-key-do-not-use-in-production}
      expiration: 604800
      refresh-expiration: 1209600
    cache:
      permission-ttl: 300
      role-ttl: 300
      enabled: true

# Spring 配置
spring:
  application:
    name: order-platform-api
  datasource:
    url: jdbc:mysql://localhost:3306/order_platform
    username: root
    password: root
  data:
    redis:
      host: localhost
      port: 6379

# 服务器配置
server:
  port: 8080
```

### 环境变量覆盖

生产环境使用环境变量覆盖敏感配置：

```bash
# Linux/Mac
export JWT_SECRET=your-256-bit-secret-key

# Windows
set JWT_SECRET=your-256-bit-secret-key

# Docker
docker run -e JWT_SECRET=your-256-bit-secret-key ...

# Kubernetes
env:
  - name: JWT_SECRET
    valueFrom:
      secretKeyRef:
        name: jwt-secret
        key: secret
```

---

## 访问地址

### 本地环境

| 功能 | 地址 | 说明 |
|------|------|------|
| **API 文档** | http://localhost:8080/doc.html | Knife4j 增强版 Swagger |
| **健康检查** | http://localhost:8080/actuator/health | Actuator 健康检查 |
| **API 接口** | http://localhost:8080/api/** | RESTful API 前缀 |

### API 文档示例

启动成功后，访问 http://localhost:8080/doc.html 查看 API 文档：

- **认证登录**：POST /api/auth/login
- **用户登出**：POST /api/auth/logout
- **刷新 Token**：POST /api/auth/refresh
- **获取当前用户**：GET /api/auth/current

---

## 依赖说明

### Maven 依赖

```xml
<dependencies>
    <!-- Common 模块 -->
    <dependency>
        <groupId>com.order.platform</groupId>
        <artifactId>order-platform-common</artifactId>
    </dependency>

    <!-- User 模块 -->
    <dependency>
        <groupId>com.order.platform</groupId>
        <artifactId>order-platform-user</artifactId>
    </dependency>

    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Knife4j API 文档 -->
    <dependency>
        <groupId>com.github.xiaoymin</groupId>
        <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

### 模块扫描配置

```java
@SpringBootApplication
@ComponentScan(basePackages = "com.order.platform")
public class ApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
```

**扫描范围**：
- `com.order.platform.api`：API 模块
- `com.order.platform.user`：用户模块
- `com.order.platform.common`：基础模块
- `com.order.platform.order`：订单模块（TODO）
- 其他业务模块...

---

## 开发规范

### 代码规范

1. **本模块不写业务代码**
   - 只包含启动类和配置文件
   - 业务代码在各自业务模块中实现

2. **配置统一管理**
   - 所有模块的配置在 `application.yml` 中定义
   - 使用 `OrderPlatformProperties` 读取配置

3. **接口规范**
   - 所有 API 接口以 `/api` 为前缀
   - 使用 RESTful 风格
   - 统一响应格式（Result）

### 接口路径规划

```
/api/auth/*         认证登录（AuthController）
/api/user/*         用户管理（UserController，TODO）
/api/role/*         角色管理（RoleController，TODO）
/api/order/*        订单管理（OrderController，TODO）
/api/shipment/*     发运管理（ShipmentController，TODO）
/api/partner/*      合作方管理（PartnerController，TODO）
```

---

## 常见问题

### Q1: 启动时报错 "找不到 order-platform-common"

**原因**：没有先构建 common 模块

**解决方案**：
```bash
# 在项目根目录执行
mvn clean install
```

### Q2: 配置文件不生效

**原因**：配置文件在业务模块中，而不是在 API 模块中

**解决方案**：
- 确保 `application.yml` 在 `api/src/main/resources/` 目录下
- 业务模块不应包含 `application.yml`

### Q3: API 文档访问 404

**原因**：Knife4j 未正确配置

**解决方案**：
- 检查 `knife4j.enable` 是否为 `true`
- 检查依赖是否正确引入

---

## 更新记录

| 日期 | 版本 | 更新内容 | 更新人 |
|------|------|----------|--------|
| 2026-01-07 | v1.0.0 | 初始版本，创建 API 启动模块 | 开发组 |

---

## 相关文档

- [用户模块文档](../order-platform-user/README.md)
- [Common 模块文档](../order-platform-common/README.md)
- [配置管理方案](../docs/配置管理方案.md)
- [后端开发指导文档](../CLAUDE.md)

---

## 维护者

- **开发组** - 初始开发

---

## 许可证

本项目采用 MIT 许可证。
