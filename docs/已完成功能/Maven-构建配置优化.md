# Maven 构建配置优化

> **Maven 多模块项目 Lombok 编译问题解决方案**

> **完成日期**：2026-01-07
> **维护团队**：后端开发组

---

## 📋 目录

- [问题背景](#问题背景)
- [问题根源](#问题根源)
- [解决方案](#解决方案)
- [修改文件清单](#修改文件清单)
- [验证步骤](#验证步骤)
- [注意事项](#注意事项)

---

## 问题背景

### 问题描述

在 Maven 多模块项目中，user 模块依赖 common 模块后，编译阶段无法找到 common 模块中由 Lombok `@Builder` 注解生成的 `builder()` 方法。

### 错误信息

```
[ERROR] 找不到符号
  符号:   类 OperationLogDTO
  位置: 类 com.order.platform.user.service.impl.AuthServiceImpl
```

### 影响范围

- **所有使用 common 模块中 Lombok 生成代码的业务模块**
- **编译阶段**：user 模块、api 模块编译失败
- **运行阶段**：不受影响（使用 IDE 编译时正常）

---

## 问题根源

### Lombok 的特殊性

Lombok 是**编译期注解处理器**，其依赖传递规则和普通依赖不同：

1. **编译期生成代码**：Lombok 在编译阶段通过注解处理器生成代码（如 getter/setter/builder）
2. **注解处理器不传递**：Maven 的依赖传递默认不会传递"注解处理器"的配置
3. **provided 作用域问题**：如果 common 模块中 Lombok 依赖作用域设为 `provided`，该依赖不会传递给 user 模块

### 核心原因

```
┌─────────────────────────────────────────────────────────┐
│  common 模块编译                                        │
│  ├─ Lombok 注解处理器生成代码                           │
│  └─ 生成的代码只在 common 模块编译时可用                   │
├─────────────────────────────────────────────────────────┤
│  user 模块编译                                          │
│  ├─ 依赖 common 模块                                       │
│  ├─ 但是没有配置 Lombok 注解处理器                      │
│  └─ 无法识别 common 模块中 Lombok 生成的代码               │
└─────────────────────────────────────────────────────────┘
```

---

## 解决方案

### 关键配置原则

1. **Lombok 依赖作用域使用 compile（默认）**，确保依赖可传递
2. **每个模块显式配置 Lombok 注解处理器**
3. **统一依赖版本**，避免版本冲突

### 步骤1：修正父 POM 配置

**文件**：`backend/pom.xml`

**修改内容**：

1. 添加 `dependencyManagement`，统一管理依赖版本
2. 添加 `maven-compiler-plugin` 配置到 `pluginManagement`
3. 添加 `maven-compiler-plugin` 配置到 `plugins`

```xml
<properties>
    <lombok.version>1.18.30</lombok.version>
</properties>

<dependencyManagement>
    <dependencies>
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>

<build>
    <pluginManagement>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </pluginManagement>

    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>21</source>
                <target>21</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

### 步骤2：修正 common 模块配置

**文件**：`backend/order-platform-common/pom.xml`

**关键修改**：

1. **Lombok 依赖作用域**：从 `provided` 改为 `compile`（默认）
2. **添加 maven-compiler-plugin 配置**

```xml
<dependencies>
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
        <!-- 使用 compile 作用域（默认），确保依赖可传递 -->
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>21</source>
                <target>21</target>
                <annotationProcessorPaths>
                    <!-- 显式指定 Lombok 为注解处理器 -->
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>1.18.30</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

### 步骤3：修正 user 模块配置

**文件**：`backend/order-platform-user/pom.xml`

**关键修改**：

1. **显式引入 Lombok 依赖**：即使 common 已引入，注解处理器需本地配置
2. **添加 maven-compiler-plugin 配置**

```xml
<dependencies>
    <!-- Common 模块 -->
    <dependency>
        <groupId>com.order.platform</groupId>
        <artifactId>order-platform-common</artifactId>
    </dependency>

    <!-- 必须显式引入 Lombok（即使 common 已引入，注解处理器需本地配置） -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>21</source>
                <target>21</target>
                <annotationProcessorPaths>
                    <!-- 显式配置 Lombok 注解处理器 -->
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>1.18.30</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

### 步骤4：修正 api 模块配置

**文件**：`backend/order-platform-api/pom.xml`

**关键修改**：同 user 模块

---

## 修改文件清单

### 配置文件

| 文件 | 修改类型 | 修改内容 |
|------|----------|----------|
| `pom.xml` | 新增 | 添加 dependencyManagement、maven-compiler-plugin |
| `order-platform-common/pom.xml` | 修改 | Lombok 作用域、添加编译器插件 |
| `order-platform-user/pom.xml` | 修改 | Lombok 版本、添加编译器插件 |
| `order-platform-api/pom.xml` | 修改 | Lombok 版本、添加编译器插件 |

### 源代码文件

| 文件 | 修改类型 | 修改内容 |
|------|----------|----------|
| `order-platform-common/src/main/java/com/order/platform/common/util/JwtUtil.java` | 修复 | 适配 JJWT 0.12.x 新 API |
| `order-platform-common/src/main/java/com/order/platform/common/response/PageResult.java` | 修复 | 添加泛型 `<T>` |
| `order-platform-common/src/main/java/com/order/platform/common/response/Result.java` | 增强 | 添加 `success()` 和 `error()` 方法 |
| `order-platform-common/src/main/java/com/order/platform/common/util/StringUtil.java` | 修复 | 修复 Hutool API 变更 |
| `order-platform-common/src/main/java/com/order/platform/common/aspect/OperationLogAspect.java` | 修复 | 添加 StrUtil import |
| `order-platform-user/src/main/java/com/order/platform/user/controller/AuthController.java` | 修复 | 使用枚举代替字符串，添加 import |
| `order-platform-user/src/main/java/com/order/platform/user/service/PermissionService.java` | 修复 | 重命名方法避免冲突 |
| `order-platform-user/src/main/java/com/order/platform/user/service/impl/PermissionServiceImpl.java` | 修复 | 重命名方法避免冲突 |
| `order-platform-user/src/main/java/com/order/platform/user/service/impl/AuthServiceImpl.java` | 修复 | 注入 AuthHelper、使用枚举、改用 new+setter |

---

## 验证步骤

### 1. 清理并重新编译

```bash
cd G:\项目\order-visualization-platform\backend
mvn clean compile -DskipTests
```

### 2. 验证编译结果

**实际结果**：
```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for 订单可视化平台后端 1.0.0:
[INFO]
[INFO] 订单可视化平台后端 .......................................... SUCCESS
[INFO] 公共模块 ............................................... SUCCESS [5.771 s]
[INFO] 用户权限模块 ............................................. SUCCESS [2.594 s]
[INFO] order-platform-api ................................. SUCCESS [1.321 s]
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  10.473 s
```

### 3. 验证 Lombok Builder 可用性

**测试代码**（AuthServiceImpl.java）：
```java
// 使用 Lombok builder 方式创建 DTO（user 模块调用 common 模块的类）
OperationLogDTO logDTO = OperationLogDTO.builder()
        .operatorId(userId)
        .operatorName(user.getUsername())
        .operatorUserCode(user.getUserCode())
        .operatorDepartmentId(user.getDepartmentId())
        .operatorDepartmentName(user.getDepartmentName())
        .businessType("USER")
        .businessId(userId)
        .operationType(operationType)
        .operationModule(OperationModule.USER.getCode())
        .operationDesc(description)
        .operationResult("SUCCESS")
        .build();
```

**验证结果**：✅ **编译成功，Lombok builder 完全可用**

### 4. 使用 IDEA 编译（推荐）

如果命令行编译仍有问题，在 IDEA 中：

1. **启用注解处理**：
   - `File` → `Settings` → `Build, Execution, Deployment` → `Compiler` → `Annotation Processors`
   - 勾选 `Enable annotation processing`

2. **重新构建项目**：
   - `Build` → `Rebuild Project`

---

## 注意事项

### 1. Lombok 依赖作用域

| 作用域 | 用途 | 是否传递 | 说明 |
|--------|------|----------|------|
| **compile**（默认） | 编译和运行时需要 | ✅ 传递 | 推荐：业务模块依赖 |
| **provided** | 编译时需要，运行时由容器提供 | ❌ 不传递 | 仅用于 JDK/Web 容器已提供的情况 |

**本项目**：所有模块都使用 `compile` 作用域

### 2. JDK 版本

- **项目使用**：Java 21
- **编译器配置**：source 和 target 都设为 21
- **确保一致**：IDE 项目 SDK 也设置为 21

### 3. 版本一致性

确保所有模块的 Lombok 版本完全一致：

```xml
<lombok.version>1.18.30</lombok.version>
```

### 4. IDEA 配置（推荐）

如果使用 IDEA 开发：

1. 安装 Lombok 插件
2. 启用注解处理
3. 设置 JDK 为 21
4. 使用 IDEA 的 Build 而非 Maven 命令行

---

## 相关文档

- [后端 README](../README.md) - 项目整体说明
- [配置管理方案](./配置管理方案.md) - 配置文件说明
- [后端开发指导文档](../CLAUDE.md) - 开发规范

---

## 更新记录

| 日期 | 版本 | 更新内容 | 更新人 |
|------|------|----------|--------|
| 2026-01-07 | v1.0.1 | 验证 Lombok builder 完全可用，更新实际编译结果和测试代码 | 开发组 |
| 2026-01-07 | v1.0.0 | 初始化文档，记录 Maven Lombok 编译问题解决方案 | 开发组 |

---

## 维护者

- **开发组** - 配置优化与问题解决

---

## 许可证

本项目采用 MIT 许可证。
