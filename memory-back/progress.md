# 项目进度记录

## 2026-01-16

### plan_53 后端项目初始化（已完成）

**完成内容**：

1. **Maven 多模块项目骨架**
   - 父 POM：统一管理 10 个子模块
   - 10 个子模块：api, common, order, shipment, partner, dashboard, attachment, exception, user, visualization
 
2. **公共模块基础类**
   - `Result<T>`：统一响应封装
   - `ResponseCode`：响应码枚举（含订单、发运、合作方等模块码段）
   - `BusinessException`：业务异常类
   - `GlobalExceptionHandler`：全局异常处理器
   - 状态枚举：`OrderStatus`, `PartnerType`, `ShipmentStatus`, `ShipmentLineStatus`

3. **聚合模块实体类**
   - `Order`, `OrderLine`（订单聚合）
   - `Shipment`, `ShipmentLine`, `ReceiptDetail`（发运聚合）
   - `Partner`（合作方聚合 - 统一表设计）
   - `Attachment`（附件聚合）
   - `ExceptionRecord`（异常聚合）
   - `User`（用户聚合）

4. **API 启动模块**
   - `OrderPlatformApplication`：启动类
   - `HealthController`：健康检查接口
   - `application.yml`：配置文件

**技术决策**：

| 决策 | 选择 | 理由 |
|------|------|------|
| 枚举存储 | `@EnumValue` + `@JsonValue` | 数据库和 API 统一使用小写值（draft），Java 代码使用枚举常量（DRAFT） |
| 异常处理 | `@RestControllerAdvice` | 统一捕获，避免 try-catch 吞没异常 |
| 响应格式 | `Result<T>` | 统一返回格式 {code, message, data, timestamp} |
| 状态类型 | 枚举而非 String | 编译期类型检查，防止拼写错误 |

**代码审查反馈**：

1. ✅ 修复：状态字段从 `String` 改为枚举类型
2. ✅ 修复：消除 `GlobalExceptionHandler` 中的重复代码（提取 `extractFieldErrors()`）
3. ✅ 修复：移除 `Result.ok(message, data)` 方法，统一 API 签名
4. ✅ 新增：枚举类实现 `@EnumValue` 和 `@JsonValue`，统一存储策略
5. ✅ 新增：状态枚举注释明确业务语义（DELIVERED vs RECEIVED）

**待完成**：
- [ ] plan_54 前端项目初始化
- [ ] plan_55 数据库建表（DDL 脚本）
- [ ] plan_56 API 启动模块配置（Knife4j 文档）
