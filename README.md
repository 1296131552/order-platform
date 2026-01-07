# 订单可视化平台 - 后端项目

> **订单可视化数字化管理平台** - 后端服务

> 以销售订单为核心，实现业务全流程可视化、资料统一管理、数据看板决策支持的业务管理系统。

---

## 目录

- [项目概述](#项目概述)
- [技术栈](#技术栈)
- [模块架构](#模块架构)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [API 访问地址](#api-访问地址)
- [开发规范](#开发规范)
- [文档维护规范](#文档维护规范)
- [相关文档](#相关文档)
- [维护者](#维护者)

---

## 项目概述

### 项目定位

**订单可视化数字化管理平台** 是一个以销售订单为核心的综合性业务管理系统，实现：

- **业务全流程可视化**：从下单到对账归档的完整流程跟踪
- **资料统一管理**：订单、发运、签收、附件等资料集中管理
- **数据看板决策支持**：KPI 指标统计、趋势分析、异常预警

### 项目性质

**本项目是指导教程项目**，具有双重目标：

1. **指导与参考**：为开发者提供完整的功能实现参考，展示最佳实践
2. **项目兜底**：确保项目有可用的代码实现，作为实际部署的基础

### 核心特性

- **分层架构**：API 入口层 → 业务服务层 → 基础支撑层
- **模块化设计**：业务模块独立，职责明确，易于扩展
- **认证授权**：JWT Token + 角色权限 + 数据权限
- **操作日志**：完整的操作轨迹记录
- **统一响应**：RESTful API + 统一响应格式
- **异常处理**：全局异常处理 + 友好错误提示

---

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| **JDK** | 21 | Java 运行环境（LTS 版本） |
| **Spring Boot** | 3.2.1 | 核心框架 |
| **Spring MVC** | 内置 | Web 框架（RESTful API） |
| **Spring Security Crypto** | 内置 | 密码加密（BCrypt） |
| **MyBatis Plus** | 3.5.x | ORM 框架（推荐使用注解形式） |
| **MySQL** | 8.0+ | 关系数据库 |
| **Druid** | 1.2.20+ | 数据库连接池 |
| **Redis** | 6.2+ / 7.0+ | 缓存/Session 存储 |
| **JWT** | 0.12.3 | Token 认证（JJWT） |
| **Knife4j** | 4.4.0+ | API 文档（OpenAPI 3.0） |
| **Lombok** | 最新 | 简化 Java 代码 |
| **Hutool** | 5.8.x | Java 工具类库 |

---

## 模块架构

### 模块依赖关系

```
order-platform-backend（父项目）
    │
    ├─→ order-platform-api（启动模块）
    │       │
    │       ├─→ order-platform-user（用户模块）
    │       │       │
    │       │       └─→ order-platform-common（基础模块）
    │       │
    │       └─→ order-platform-common（基础模块）
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
| **order-platform-api** | 启动入口、配置管理、接口聚合 | ✅ 已完成 |
| **order-platform-common** | 基础组件、工具类、统一响应、异常处理 | ✅ 已完成 |
| **order-platform-user** | 用户管理、认证授权、角色权限 | ✅ 已完成 |
| **order-platform-order** | 订单管理 | ⏳ 待开发 |
| **order-platform-shipment** | 发运管理（含签收） | ⏳ 待开发 |
| **order-platform-partner** | 合作方管理 | ⏳ 待开发 |

---

## 快速开始

### 环境要求

| 软件 | 版本要求 | 说明 |
|------|---------|------|
| **JDK** | 21 或更高 | LTS 版本，项目使用 Java 21 特性 |
| **Maven** | 3.8+ 或更高 | 用于项目构建和依赖管理 |
| **MySQL** | 8.0+ 或更高 | 关系数据库，存储业务数据 |
| **Redis** | 6.2+ 或更高（可选） | 缓存，用于密码错误计数、权限缓存等 |

---

### 1. 克隆项目

```bash
git clone <repository-url>
cd order-visualization-platform/backend
```

---

### 2. 初始化数据库

**使用提供的初始化脚本（推荐）**

```bash
# 一键初始化测试数据库（包含表结构和测试数据）
mysql -u root -p < scripts/sql/init-test-db.sql
```

**脚本说明**：
- 数据库名称：`opv-test1`
- 字符集：`utf8mb4`
- 包含完整的表结构（t_user, t_role, t_user_role, t_role_permission, t_operation_log）
- 包含测试用户数据（admin, zhangsan）

**测试账号**：
- admin / admin （系统管理员）
- zhangsan / 123456 （客户经理）

---

### 3. 配置文件

配置文件位置：`order-platform-api/src/main/resources/application.yml`

**最小化配置**（使用默认值即可运行）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/opv-test1?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 你的MySQL密码
```

**生产环境配置**（必须修改 JWT 密钥）：

```bash
# Linux/Mac
export JWT_SECRET=your-256-bit-secret-key-must-be-very-long-and-secure

# Windows
set JWT_SECRET=your-256-bit-secret-key-must-be-very-long-and-secure
```

**重要提示**：生产环境的 JWT 密钥必须通过环境变量设置，不要直接写在配置文件中！

---

### 4. 构建项目

```bash
# 在项目根目录执行（clean 清理构建缓存，install 安装到本地仓库）
mvn clean install
```

**构建输出**：
```
[INFO] BUILD SUCCESS
[INFO] Total time:  XX.XXX s
[INFO] Finished at: 2026-01-08TXX:XX:XX+08:00
```

---

### 5. 启动应用

**方式一：Maven 命令启动（开发环境推荐）**

```bash
# 进入 API 模块
cd order-platform-api

# 启动应用（支持热重载）
mvn spring-boot:run
```

**方式二：打包后启动（生产环境）**

```bash
# 打包（跳过测试）
mvn clean package -DskipTests

# 启动 JAR 包
java -jar order-platform-api/target/order-platform-api-1.0.0.jar
```

**方式三：IDE 启动**

直接运行 `order-platform-api/src/main/java/com/order/platform/api/ApiApplication.java` 的 `main` 方法。

---

### 6. 验证启动

**启动成功标志**：

控制台输出：
```
██████████████████████████████████████████████████████
██                                                    ██
██    订单可视化数字化管理平台启动成功！                 ██
██                                                    ██
██    API 文档: http://localhost:8081/doc.html        ██
██    健康检查: http://localhost:8081/actuator/health  ██
██                                                    ██
██████████████████████████████████████████████████████
```

**访问地址**：

| 功能 | 地址 | 说明 |
|------|------|------|
| **API 文档（Knife4j）** | http://localhost:8081/doc.html | 增强版 Swagger，支持在线调试 |
| **Swagger UI** | http://localhost:8081/swagger-ui.html | 原生 Swagger UI |
| **健康检查** | http://localhost:8081/actuator/health | Actuator 健康检查 |
| **OpenAPI JSON** | http://localhost:8081/v3/api-docs | OpenAPI 3.0 JSON 格式 |

**测试登录**：

1. 打开 API 文档：http://localhost:8081/doc.html
2. 找到「认证登录」分组
3. 点击「POST /api/auth/login」→「调试」
4. 输入请求参数：
```json
{
  "account": "zhangsan",
  "password": "123456"
}
```
5. 点击「发送」，返回 Token 即为成功

---

### 7. 热重载开发（开发环境）

项目已集成 Spring Boot DevTools，支持热重载：

**触发热重载的步骤**：

1. 修改 Java 代码（Controller、Service 等）
2. 按 `Ctrl+F9`（IDEA）编译项目
3. DevTools 自动检测变化并重启应用（1-3秒）

**能热重载的场景** ✅：
- 修改已有 Java 类的方法内容
- 修改 Controller、Service 等

**不能热重载的场景** ❌：
- 新增 Java 文件
- 新增 @Configuration 类
- 修改 pom.xml
- 修改 application.yml 的某些配置

详见：[热重载功能文档](docs/已完成功能/热重载功能.md)

---

### 8. 常见问题

**问题1：启动报错 "Access denied for user"**

原因：数据库用户名或密码错误

解决方案：
```yaml
# 检查 application.yml 中的数据库配置
spring:
  datasource:
    username: root
    password: 你的正确密码
```

---

**问题2：启动报错 "Unknown database 'opv-test1'"**

原因：数据库未创建

解决方案：
```bash
mysql -u root -p < scripts/sql/init-test-db.sql
```

---

**问题3：API 文档页面显示不正常**

原因：Knife4j 配置错误

解决方案：确保 `application.yml` 中的配置正确：
```yaml
springdoc:
  api-docs:
    path: /v3/api-docs  # 注意不是 /doc.html
```

详见：[API 文档配置文档](docs/已完成功能/API文档配置.md)

---

**问题4：登录时报错 "用户不存在"**

原因：数据库中没有测试用户

解决方案：
```bash
# 重新执行初始化脚本
mysql -u root -p < scripts/sql/init-test-db.sql
```

---

**问题5：JWT Token 验证失败**

原因：JWT 密钥配置错误

解决方案：确保 `application.yml` 中的 JWT 密钥一致：
```yaml
order:
  platform:
    jwt:
      secret: dev-secret-key-do-not-use-in-production-change-in-production
```

---

## 配置说明

### 配置文件位置

```
order-platform-api/
└── src/main/resources/
    └── application.yml          ✅ 统一配置文件（所有模块）
```

**设计说明**：项目采用统一配置文件策略，所有模块的配置集中在 `application.yml` 中，通过 `OrderPlatformProperties` 类进行类型安全的配置管理。

---

### 核心配置结构

```yaml
order:                    # 业务配置前缀
  platform:              # 平台配置
    security:            # 安全配置（密码、Token）
    jwt:                 # JWT 配置（密钥、过期时间）
    cache:               # 缓存配置（TTL、启用状态）

spring:                  # Spring Boot 原生配置
  application:           # 应用名称
  datasource:            # 数据源配置
  data:                  # 数据存储（Redis）
  devtools:              # 开发工具（热重载）

mybatis-plus:            # MyBatis Plus 配置
logging:                 # 日志配置

springdoc:               # OpenAPI 文档配置
knife4j:                # Knife4j 增强配置

server:                  # 服务器配置（端口等）
```

---

### 1. JWT 密钥配置 ⚠️

**开发环境**（默认值，已配置）：

```yaml
order:
  platform:
    jwt:
      # 开发环境默认密钥（不安全，仅用于开发）
      secret: ${JWT_SECRET:dev-secret-key-do-not-use-in-production-change-in-production}
      expiration: 604800           # Token 过期时间（秒，7天）
      refresh-expiration: 1209600  # 刷新 Token 过期时间（秒，14天）
```

**生产环境设置**（必须修改）：

```bash
# Linux/Mac
export JWT_SECRET=your-256-bit-secret-key-must-be-very-long-and-secure

# Windows CMD
set JWT_SECRET=your-256-bit-secret-key-must-be-very-long-and-secure

# Windows PowerShell
$env:JWT_SECRET="your-256-bit-secret-key-must-be-very-long-and-secure"

# Docker Compose
environment:
  - JWT_SECRET=your-256-bit-secret-key-must-be-very-long-and-secure
```

**密钥生成方法**：

```bash
# 使用 OpenSSL 生成随机密钥
openssl rand -base64 32

# 或使用 Python
python -c "import secrets; print(secrets.token_urlsafe(32))"
```

---

### 2. 密码策略配置

```yaml
order:
  platform:
    security:
      password:
        # 密码错误最大尝试次数（默认：5次）
        max-attempts: 5

        # 账户锁定时长（单位：分钟，默认：30分钟）
        lock-minutes: 30

        # 密码过期天数（默认：90天）
        expire-days: 90

        # 密码最小长度（默认：6位）
        min-length: 6

        # 密码最大长度（默认：20位）
        max-length: 20

        # 密码最小强度得分：1-5（默认：3分）
        # 1分：只有长度  |  2分：长度+1种字符  |  3分：长度+2种字符
        # 4分：长度+3种字符  |  5分：长度+4种字符（大写、小写、数字、特殊字符）
        min-strength: 3

      token:
        # Token 过期时间（单位：秒，默认：604800秒 = 7天）
        expiration: 604800
```

**密码强度计算规则**：
- 满足长度要求：1分
- 包含小写字母：+1分
- 包含大写字母：+1分
- 包含数字：+1分
- 包含特殊字符：+1分

---

### 3. 缓存配置

```yaml
order:
  platform:
    cache:
      # 权限缓存过期时间（单位：秒，默认：300秒 = 5分钟）
      permission-ttl: 300

      # 角色缓存过期时间（单位：秒，默认：300秒 = 5分钟）
      role-ttl: 300

      # 是否启用缓存（默认：true）
      enabled: true
```

**缓存策略**：
- 权限缓存：减少数据库查询，提高接口性能
- 角色缓存：用户登录后角色信息缓存 5 分钟
- 缓存实现：Caffeine（本地缓存）+ Redis（分布式缓存）

---

### 4. 数据源配置

```yaml
spring:
  datasource:
    # MySQL 驱动
    driver-class-name: com.mysql.cj.jdbc.Driver

    # 数据库连接 URL
    # 参数说明：
    # - useUnicode=true&characterEncoding=utf8：使用 UTF-8 编码
    # - useSSL=false：不使用 SSL（开发环境）
    # - serverTimezone=Asia/Shanghai：时区设置
    # - allowPublicKeyRetrieval=true：允许公钥检索（解决 MySQL 8.0 连接问题）
    url: jdbc:mysql://localhost:3306/opv-test1?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true

    # 数据库用户名
    username: root

    # 数据库密码
    password: w123456789
```

**生产环境建议**：
- 使用 SSL 连接：`useSSL=true`
- 使用连接池：Druid（已集成）
- 配置连接池参数：
```yaml
spring:
  datasource:
    druid:
      initial-size: 5           # 初始连接数
      min-idle: 5               # 最小空闲连接数
      max-active: 20            # 最大活跃连接数
      max-wait: 60000           # 获取连接等待超时时间（毫秒）
```

---

### 5. Redis 配置

```yaml
spring:
  data:
    redis:
      host: localhost           # Redis 服务器地址
      port: 6379                # Redis 端口
      password:                 # Redis 密码（默认为空）
      database: 0               # Redis 数据库索引（0-15）
      timeout: 3000ms           # 连接超时时间
      lettuce:
        pool:
          max-active: 8         # 连接池最大连接数
          max-idle: 8           # 连接池最大空闲连接数
          min-idle: 0           # 连接池最小空闲连接数
```

**Redis 用途**：
- 密码错误计数器（登录失败锁定）
- 权限缓存（分布式环境）
- Token 黑名单（强制登出，TODO）

---

### 6. MyBatis Plus 配置

```yaml
mybatis-plus:
  configuration:
    # 下划线转驼峰（数据库字段 user_name → Java 属性 userName）
    map-underscore-to-camel-case: true

    # 日志实现（使用 Slf4j）
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl

  global-config:
    db-config:
      # 主键类型（AUTO：数据库自增）
      id-type: auto

      # 逻辑删除字段
      logic-delete-field: isDeleted
      logic-delete-value: 1      # 删除后的值
      logic-not-delete-value: 0  # 未删除的值
```

**自动填充配置**：
- `created_at`：插入时自动填充当前时间
- `updated_at`：插入和更新时自动填充当前时间
- `created_by`：插入时自动填充 -1（系统创建）
- `updated_by`：插入和更新时自动填充 -1（系统更新）

详见：`MybatisPlusMetaObjectHandler.java`

---

### 7. 日志配置

```yaml
logging:
  level:
    root: INFO                          # 全局日志级别
    com.order.platform: DEBUG           # 项目日志级别（开发环境用 DEBUG）
    org.springframework.security: DEBUG  # Spring Security 日志级别

  pattern:
    console: '%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n'
```

**日志级别说明**：
- **ERROR**：错误日志，需要立即处理
- **WARN**：警告日志，需要关注
- **INFO**：信息日志，记录关键业务流程
- **DEBUG**：调试日志，开发环境使用

---

### 8. Knife4j API 文档配置

```yaml
# OpenAPI 3.0 配置
springdoc:
  api-docs:
    enabled: true              # 启用 API 文档
    path: /v3/api-docs         # API 文档 JSON 路径

  swagger-ui:
    enabled: true              # 启用 Swagger UI
    path: /swagger-ui.html     # Swagger UI 路径

# Knife4j 增强配置
knife4j:
  enable: true                 # 启用 Knife4j
  setting:
    language: zh_cn            # 语言：中文
    swagger-model-name: 实体类列表  # 实体类列表名称
```

**API 文档访问地址**：
- Knife4j 文档：http://localhost:8081/doc.html
- Swagger UI：http://localhost:8081/swagger-ui.html
- OpenAPI JSON：http://localhost:8081/v3/api-docs

---

### 9. 服务器配置

```yaml
server:
  port: 8081                   # 服务端口（默认：8081）
  servlet:
    context-path: /            # 应用上下文路径（默认：/）
  tomcat:
    uri-encoding: UTF-8        # URI 编码
```

**生产环境建议**：
- 使用 8080 或其他标准端口
- 配置 HTTPS（使用 Nginx 反向代理）
- 配置 Gzip 压缩

---

### 10. DevTools 热重载配置

```yaml
spring:
  devtools:
    enabled: true                             # 启用热重载
    exclude: static/**,public/**,templates/**  # 排除不触发热重载的资源
    additional-paths: src/main/java           # 监控额外资源
    livereload:
      enabled: false                           # 禁用 LiveReload（浏览器自动刷新）
    restart:
      enabled: true                            # 启用重启
      exclude: static/**,public/**,templates/**,META-INF/maven/**,META-INF/resources/**
```

**热重载原理**：
- 使用双类加载器机制
- Base ClassLoader：加载第三方依赖 jar（不变）
- Restart ClassLoader：加载应用代码（可重新加载）

---

### 配置最佳实践

#### 开发环境配置

```yaml
# 使用默认配置，快速启动
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/opv-test1?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your-password

logging:
  level:
    com.order.platform: DEBUG

server:
  port: 8081
```

#### 生产环境配置

```bash
# 使用环境变量覆盖敏感配置
export JWT_SECRET=your-256-bit-secret-key
export DB_PASSWORD=your-production-db-password
export REDIS_PASSWORD=your-redis-password
```

```yaml
# application.yml 中使用环境变量
spring:
  datasource:
    password: ${DB_PASSWORD}

  data:
    redis:
      password: ${REDIS_PASSWORD}

order:
  platform:
    jwt:
      secret: ${JWT_SECRET}
```

---

### 配置文件优先级

Spring Boot 配置加载优先级（从高到低）：

1. 命令行参数：`--server.port=8082`
2. 环境变量：`JWT_SECRET=xxx`
3. 配置文件：`application.yml`
4. 默认值：`@Value("${key:defaultValue}")`

---

## API 访问地址

### 本地环境

| 功能 | 地址 | 说明 |
|------|------|------|
| **API 文档** | http://localhost:8080/doc.html | Knife4j 增强版 Swagger |
| **健康检查** | http://localhost:8080/actuator/health | Actuator 健康检查 |
| **API 接口** | http://localhost:8080/api/** | RESTful API 前缀 |

### API 接口示例

| 功能 | 方法 | 路径 | 说明 |
|------|------|------|------| 
| **用户登录** | POST | /api/auth/login | 支持用户名/邮箱/手机号登录 |
| **用户登出** | POST | /api/auth/logout | 清除 Token |
| **刷新 Token** | POST | /api/auth/refresh | 使用旧 Token 换取新 Token |
| **修改密码** | POST | /api/auth/change-password | 用户修改自己的密码 |
| **获取当前用户** | GET | /api/auth/current | 获取当前登录用户信息 |

---

## 开发规范

### 代码注释规范（指导教程项目）

**1. 类级注释**：

```java
/**
 * 订单服务实现类
 *
 * 功能说明：
 * - 订单 CRUD 操作
 * - 订单状态流转
 * - 订单统计
 *
 * @author 开发组
 * @since 1.0.0
 */
public class OrderServiceImpl implements OrderService {
    // ...
}
```

**2. 方法级注释**：

```java
/**
 * 根据订单号查询订单
 *
 * 业务规则：
 * - 订单号唯一性检查
 * - 逻辑删除的订单不返回
 * - 查询结果缓存 5 分钟
 *
 * @param orderNo 订单号
 * @return 订单实体，不存在返回 null
 */
public Order getByOrderNo(String orderNo) {
    // ...
}
```

**3. 关键业务逻辑注释**：

```java
// 1. 验证用户状态：只允许激活状态的用户登录
if (!"active".equals(user.getStatus())) {
    throw new BusinessException("用户已被禁用");
}

// 2. 密码加密：使用 BCrypt 算法加密
String encryptedPassword = passwordEncoder.encode(rawPassword);

// 3. 生成 JWT Token，有效期 7 天
String token = JwtUtil.generateToken(user.getId(), 7 * 24 * 60 * 60);
```

### MyBatis 使用规范

**推荐使用注解形式编写 SQL**：

```java
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT * FROM t_order " +
            "WHERE customer_id = #{customerId} " +
            "AND is_deleted = 0 " +
            "ORDER BY created_at DESC")
    List<Order> selectByCustomerId(@Param("customerId") Long customerId);
}
```

**优点**：
- SQL 与 Java 代码在同一文件，便于维护
- 无需维护 XML 映射文件
- 简单查询一目了然

### API 规范

**RESTful 风格**：

| HTTP 方法 | 用途 | 示例 |
|-----------|------|------|
| **GET** | 查询资源 | `GET /api/order/{id}` |
| **POST** | 创建资源 | `POST /api/order` |
| **PUT** | 全量更新资源 | `PUT /api/order/{id}` |
| **PATCH** | 部分更新资源 | `PATCH /api/order/{id}/status` |
| **DELETE** | 删除资源 | `DELETE /api/order/{id}` |

**统一响应格式**：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {...},
  "timestamp": "2026-01-07T12:00:00"
}
```

### 配置优先规则 ⭐

**重要**：适合用配置管理的变量必须使用配置，禁止硬编码

```java
// ❌ 错误：硬编码
public static final int MIN_LENGTH = 6;
public static final int MAX_LENGTH = 20;

// ✅ 正确：从配置读取
@Autowired
private OrderPlatformProperties properties;

int minLength = properties.getSecurity().getPassword().getMinLength();
```

---

## 文档维护规范

### 必须维护的文档

| 文档 | 路径 | 说明 |
|------|------|------|
| **项目根 README** | `backend/README.md` | 本文档，项目整体说明 |
| **已完成功能** | `backend/docs/已完成的功能.md` | 功能实现进度追踪 |
| **API 接口文档** | `backend/API接口文档.md` | RESTful API 接口规范 |
| **各模块 README** | `order-platform-*/README.md` | 模块功能说明 |

### 文档修改规范 ⭐

**1. 修改前确认**

在修改文档前，先确认：

- 文档类型（可修改 / 禁止修改）
- 影响范围（需要同步更新的其他文档）
- 修改类型（新增功能 / 修改功能 / 修复 Bug）

**2. 修改流程**

```bash
# 1. 修改代码
# 2. 更新相关文档
# 3. 添加更新记录
# 4. 提交代码
```

**3. 更新记录格式**

在文档末尾添加更新记录：

```markdown
## 更新记录

| 日期 | 版本 | 更新内容 | 更新人 |
|------|------|----------|--------|
| 2026-01-07 | v1.0.1 | 新增 API 启动模块（order-platform-api） | 开发组 |
| 2026-01-07 | v1.0.0 | 初始化文档 | 开发组 |
```

**4. 禁止修改的文档** ⚠️

以下文档为甲方提供的设计文档，**禁止修改**：

- `docs/业务和流程/可视化数字化管理平台v1217.md` - 甲方解决方案文档
- `docs/数据库/0.数据库设计文档.md` - 数据库设计文档
- `docs/数据库/1.数据库命名规范.md` - 数据库命名规范
- `docs/数据库/数据库评估报告.md` - 数据库评估报告
- `docs/技术栈和架构/技术栈和架构v1.md` - 技术架构设计
- `docs/技术栈和架构/架构评估报告.md` - 架构评估报告
- `docs/技术栈和架构/文件布局设计.md` - 文件布局设计
- `docs/ig/` - 废弃文件目录

**5. 可修改的文档**

- `docs/项目计划.md` - 项目进度计划
- `docs/已完成的功能.md` - 功能实现进度
- `docs/已完成功能/*.md` - 各功能详细文档
- `order-platform-*/README.md` - 各模块 README
- `backend/README.md` - 本文档

**6. 文档命名规范**

- 使用中文描述，简洁明了
- 使用 `-` 连接多个词，如 `认证登录核心功能.md`
- 避免使用特殊字符

---

## 相关文档

### 核心设计文档

| 文档 | 路径 | 说明 |
|------|------|------|
| **设计分析文档** | `设计分析文档.md` | 设计符合度分析与优化方案 ⭐ |
| **数据库设计文档** | `数据库设计文档.md` | 27 张表的完整设计 |
| **API 接口文档** | `API接口文档.md` | RESTful API 接口规范 |
| **系统架构设计** | `系统架构设计.md` | 分层架构与模块设计 |
| **后端开发指导** | `CLAUDE.md` | 后端开发规范与规则 |

### 项目文档

| 文档 | 路径 | 说明 |
|------|------|------|
| **已完成功能** | `docs/已完成的功能.md` | 功能实现进度追踪 |
| **父级项目文档** | `../CLAUDE.md` | 整体项目说明 |
| **项目计划** | `docs/项目计划.md` | 40 天详细开发计划 |

### 甲方文档（不可修改）

| 文档 | 路径 | 说明 |
|------|------|------|
| **解决方案文档** | `docs/业务和流程/可视化数字化管理平台解决方案v1217.md` | 甲方需求文档 |

---

## 核心业务概念

### 订单状态流转

```
草稿 → 执行中 → 部分到货 → 完成
```

### 发运状态流转

```
待提货 → 在途 → 已到货
```

### 签收状态流转

```
待签收 → 已签收 → 有差异 → 已处理
```

### 核心指标定义（统一口径）

- **订单数量**：按创建时间统计
- **在途订单**：发运已启动但未完成签收的订单
- **准时率**：按时签收订单数/总完成订单数×100%
- **异常件数**：存在到货差异或运输异常的订单数

---

## 常用命令

### 后端开发

```bash
# 构建项目
mvn clean install

# 运行 API 模块（主入口）
cd order-platform-api
mvn spring-boot:run

# 打包
mvn clean package -DskipTests

# 跳过测试打包
mvn clean package -Dmaven.test.skip=true
```

### 数据库

```bash
# 连接数据库
mysql -u root -p order_platform

# 查看表结构
SHOW TABLES;
DESC t_user;
```

### Git 提交

```bash
# 添加所有更改
git add .

# 提交（使用规范格式）
git commit -m "[模块] 类型: 简短描述"

# 示例
git commit -m "[common] feat: 新增 UserRoleProvider 接口"
git commit -m "[order] fix: 修复订单状态流转问题"
```

---

## 已知问题与优化计划

### 当前已知问题

| 优先级 | 问题 | 文件 | 说明 |
|--------|------|------|------|
| **P0** | 🔴 登录接口 updated_at 字段为 null | `AuthServiceImpl.updateLoginInfo` | MyBatis Plus 自动填充未触发，更新时传递 null 值（见下方详细说明） |
| **P2** | 密码错误计数器未实现 | `AuthServiceImpl.java` | handlePasswordError 方法（需要 Redis） |
| **P2** | 缓存未实现 | `PermissionServiceImpl.java` | clearCache / clearCacheBatch 方法（需要 Redis） |

---

### 🔴 P0 问题：登录接口 updated_at 字段为 null

**问题描述**：
用户登录时更新登录信息，`updated_at` 和 `updated_by` 字段被显式设置为 null，导致数据库更新失败。

**错误日志**：
```sql
UPDATE t_user SET last_login_time=?, login_count=?, updated_at=?, updated_by=?
Parameters: ..., null, null, 1(Long)
Column 'updated_at' cannot be null
```

**已尝试的修复**（均未生效）：

1. ✅ 修复 `MybatisPlusMetaObjectHandler.java`
   - 添加 `createdAt/createTime` 和 `updatedAt/updateTime` 双重支持
   - 添加 `createdBy` 和 `updatedBy` 自动填充

2. ✅ 修改数据库表结构
   - 添加 `updated_at` 默认值：`DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)`
   - 修改 `result_desc` 为 TEXT 类型（避免数据截断）

3. ✅ 修改 User 实体字段策略
   - 设置 `updateStrategy = FieldStrategy.NOT_NULL`

4. ✅ 修改 updateLoginInfo 方法
   - 使用 `LambdaUpdateWrapper` 只更新指定字段
   - 避免 `updated_at` 和 `updated_by` 被设置为 null

**根本原因**（推测）：
- MyBatis Plus 的 MetaObjectHandler 需要在 `updateById` 调用时触发
- 但由于某种原因未触发，导致自动填充未执行
- 需要重新编译并重启后端才能验证修复是否生效

**下一步**：
- 重新编译并重启后端
- 验证 MetaObjectHandler 是否正常工作
- 如仍失败，考虑使用数据库触发器或其他方案

### 已修复问题

| 日期 | 优先级 | 问题 | 修复方案 |
|------|--------|------|----------|
| 2026-01-07 | **架构** | Maven 多模块 Lombok 编译失败 | 统一配置依赖版本和编译器插件，修正 Lombok 作用域 |
| 2026-01-07 | **P0** | PasswordEncoderUtil 硬编码 | 注入 OrderPlatformProperties，从配置读取参数 |
| 2026-01-07 | **P0** | JwtUtil 配置方式不一致 | 改用 OrderPlatformProperties 读取配置 |
| 2026-01-07 | **P1** | AuthController 测试代码 | 使用 CurrentUserHolder 获取当前用户 ID |
| 2026-01-07 | **P1** | PasswordEncoderUtil 静态调用 | 改为实例方法调用 |
| 2026-01-07 | **P1** | 操作日志未集成 | 调用 operationLogService.saveAsync() |

### 优化计划

项目完成后，按照 **6 个阶段** 进行优化：

| 阶段 | 内容 | 预计时间 |
|------|------|----------|
| 第一阶段 | 补全核心业务表（来单、确认、对账） | 1 周 |
| 第二阶段 | 完善缓存功能（Redis 集成） | 1 周 |
| 第三阶段 | 完善可视化功能（地图、时间线） | 2 周 |
| 第四阶段 | 完善看板功能 | 1 周 |
| 第五阶段 | 完善导入导出 | 1 周 |

详见：[设计分析文档](./设计分析文档.md)

---

## 维护者

- **开发组** - 初始开发与维护

---

## 许可证

本项目采用 MIT 许可证。

---

## 更新记录

| 日期 | 版本 | 更新内容 | 更新人 |
|------|------|----------|--------|
| 2026-01-07 | v1.0.4 | 验证 Maven 多模块 Lombok 编译问题完全解决，Lombok @Builder 完全可用 | 开发组 |
| 2026-01-07 | v1.0.3 | 修复 Maven 多模块 Lombok 编译问题，统一配置依赖版本和编译器插件 | 开发组 |
| 2026-01-07 | v1.0.2 | 修复 P1 优先级问题：AuthController 测试代码、PasswordEncoderUtil 静态调用、操作日志集成 | 开发组 |
| 2026-01-07 | v1.0.1 | 修复 P0 优先级问题：PasswordEncoderUtil 硬编码、JwtUtil 配置方式不一致（遵循配置优先规则） | 开发组 |
| 2026-01-07 | v1.0.0 | 初始化文档，完成项目整体说明和开发规范 | 开发组 |
