# CRUD 开发指导文档

> **Order Platform CRUD 开发规范与模板**
>
> 本文档基于 User 模块实现总结，提供标准化的 CRUD 开发流程。

---

## 一、架构分层

```
┌─────────────────────────────────────────────────────────────────┐
│                        Controller 层                            │
│  职责：接收请求、参数校验、调用 Service、返回统一响应            │
├─────────────────────────────────────────────────────────────────┤
│                         Service 层                              │
│  职责：业务逻辑、事务管理、数据转换、异常抛出                    │
├─────────────────────────────────────────────────────────────────┤
│                         Mapper 层                               │
│  职责：数据访问、SQL 执行                                        │
├─────────────────────────────────────────────────────────────────┤
│                         Database                                │
└─────────────────────────────────────────────────────────────────┘
```

---

## 二、文件结构模板

```
order-platform-{module}/
├── src/main/java/com/company/order/visual/{module}/
│   ├── controller/
│   │   └── {Entity}Controller.java       # 控制器
│   ├── service/
│   │   ├── {Entity}Service.java          # 服务接口
│   │   └── impl/
│   │       └── {Entity}ServiceImpl.java  # 服务实现
│   ├── mapper/
│   │   └── {Entity}Mapper.java           # 数据访问
│   ├── entity/
│   │   └── {Entity}.java                 # 实体
│   ├── converter/
│   │   └── {Entity}Converter.java        # 转换器
│   └── dto/
│       ├── {Entity}CreateRequest.java    # 创建请求
│       ├── {Entity}UpdateRequest.java    # 更新请求
│       ├── {Entity}QueryRequest.java     # 查询请求
│       └── {Entity}VO.java               # 视图对象
```

---

## 三、Entity 实体模板

```java
package com.company.{module}.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * {实体名称}
 * <p>
 * 职责：数据库表映射
 * 关系：{关联关系说明}
 */
@Data
@TableName("t_{table_name}")
public class {Entity} {

    // ==================== 主键 ====================

    @TableId(type = IdType.AUTO)
    private Long id;

    // ==================== 业务字段 ====================

    private String field1;

    private Integer field2;

    private Boolean isEnabled;

    // ==================== 审计字段（自动填充）====================

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT)
    private Boolean isDeleted;
}
```

**关键要点：**
- `@TableName`：指定数据库表名
- `@TableId(type = IdType.AUTO)`：主键自增
- `@TableField(fill = FieldFill.INSERT)`：插入时自动填充
- `@TableField(fill = FieldFill.INSERT_UPDATE)`：插入和更新时自动填充

---

## 四、Mapper 模板

```java
package com.company.{module}.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.{module}.entity.{Entity};
import org.apache.ibatis.annotations.Mapper;

/**
 * {实体}数据访问接口
 */
@Mapper
public interface {Entity}Mapper extends BaseMapper<{Entity}> {
    // BaseMapper 已提供基础 CRUD 方法
    // - insert(Entity)
    // - deleteById(Serializable)
    // - updateById(Entity)
    // - selectById(Serializable)
    // - selectPage(Page, Wrapper)
    // - selectCount(Wrapper)
}
```

---

## 五、DTO 模板

### 5.1 创建请求 DTO

```java
package com.company.{module}.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class {Entity}CreateRequest {

    @NotBlank(message = "字段1不能为空")
    @Size(min = 3, max = 20, message = "字段1长度3-20位")
    private String field1;

    @NotNull(message = "字段2不能为空")
    private Integer field2;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
```

### 5.2 更新请求 DTO

```java
package com.company.{module}.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class {Entity}UpdateRequest {

    @NotNull(message = "ID不能为空")
    private Long id;

    @Size(max = 50, message = "字段1长度不能超过50位")
    private String field1;

    private Integer field2;

    private Boolean isEnabled;
}
```

### 5.3 查询请求 DTO

```java
package com.company.{module}.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "{实体}分页查询请求")
public class {Entity}QueryRequest {

    // ==================== 查询条件 ====================

    @Schema(description = "字段1（模糊查询）")
    private String field1;

    @Schema(description = "字段2（精确匹配）")
    private Integer field2;

    @Schema(description = "是否启用")
    private Boolean isEnabled;

    // ==================== 时间范围 ====================

    @Schema(description = "创建时间-开始")
    private LocalDateTime createdAtStart;

    @Schema(description = "创建时间-结束")
    private LocalDateTime createdAtEnd;

    // ==================== 分页参数 ====================

    @Schema(description = "页码（从1开始）", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", defaultValue = "10")
    private Integer pageSize = 10;
}
```

### 5.4 响应 VO

```java
package com.company.{module}.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "{实体}视图对象")
public class {Entity}VO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "字段1")
    private String field1;

    @Schema(description = "字段2")
    private Integer field2;

    @Schema(description = "是否启用")
    private Boolean isEnabled;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
```

---

## 六、Converter 转换器模板

```java
package com.company.{module}.converter;

import com.company.{module}.dto.{Entity}VO;
import com.company.{module}.entity.{Entity};
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {实体}实体与 VO 转换器
 * <p>
 * 职责：消除 Entity -> VO 转换的重复代码，统一转换逻辑
 */
@Component
public class {Entity}Converter {

    /**
     * Entity -> VO（单个转换）
     */
    public {Entity}VO toVO({Entity} entity) {
        return {Entity}VO.builder()
                .id(entity.getId())
                .field1(entity.getField1())
                .field2(entity.getField2())
                .isEnabled(entity.getIsEnabled())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * Entity List -> VO List（批量转换）
     */
    public List<{Entity}VO> toVO(List<{Entity}> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        return entities.stream()
                .map(this::toVO)
                .toList();
    }
}
```

---

## 七、Service 模板

### 7.1 Service 接口

```java
package com.company.{module}.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.{module}.dto.*;

/**
 * {实体}服务接口
 */
public interface {Entity}Service {

    /**
     * 创建{实体}
     */
    Long create{Entity}({Entity}CreateRequest request, Long operatorId);

    /**
     * 更新{实体}
     */
    void update{Entity}({Entity}UpdateRequest request, Long operatorId);

    /**
     * 删除{实体}（软删除）
     */
    void delete{Entity}(Long id, Long operatorId);

    /**
     * 根据ID获取{实体}详情
     */
    {Entity}VO get{Entity}ById(Long id);

    /**
     * 分页查询{实体}
     */
    Page<{Entity}VO> page{Entity}s({Entity}QueryRequest request);
}
```

### 7.2 Service 实现类

```java
package com.company.{module}.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.common.exception.BusinessException;
import com.company.common.response.ResponseCode;
import com.company.{module}.converter.{Entity}Converter;
import com.company.{module}.dto.*;
import com.company.{module}.entity.{Entity};
import com.company.{module}.mapper.{Entity}Mapper;
import com.company.{module}.service.{Entity}Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * {实体}服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class {Entity}ServiceImpl implements {Entity}Service {

    private final {Entity}Mapper {entity}Mapper;
    private final {Entity}Converter {entity}Converter;

    // ==================== 创建 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create{Entity}({Entity}CreateRequest request, Long operatorId) {
        // 1. 业务校验
        // TODO: 添加唯一性校验等

        // 2. 构建实体
        {Entity} entity = new {Entity}();
        entity.setField1(request.getField1());
        entity.setField2(request.getField2());
        // 审计字段由 MetaObjectHandler 自动填充

        // 3. 保存
        {entity}Mapper.insert(entity);

        log.info("创建{实体}成功, id={}, operatorId={}", entity.getId(), operatorId);
        return entity.getId();
    }

    // ==================== 更新 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update{Entity}({Entity}UpdateRequest request, Long operatorId) {
        // 1. 校验是否存在
        {Entity} entity = get{Entity}ByIdOrThrow(request.getId());

        // 2. 更新字段（只更新非空字段）
        if (StringUtils.hasText(request.getField1())) {
            entity.setField1(request.getField1());
        }
        if (request.getField2() != null) {
            entity.setField2(request.getField2());
        }
        // 审计字段由 MetaObjectHandler 自动填充

        // 3. 保存
        {entity}Mapper.updateById(entity);

        log.info("更新{实体}成功, id={}, operatorId={}", entity.getId(), operatorId);
    }

    // ==================== 删除 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete{Entity}(Long id, Long operatorId) {
        // 1. 校验是否存在
        get{Entity}ByIdOrThrow(id);

        // 2. 软删除
        {entity}Mapper.deleteById(id);

        log.info("删除{实体}成功, id={}, operatorId={}", id, operatorId);
    }

    // ==================== 查询 ====================

    @Override
    public {Entity}VO get{Entity}ById(Long id) {
        {Entity} entity = get{Entity}ByIdOrThrow(id);
        return {entity}Converter.toVO(entity);
    }

    @Override
    public Page<{Entity}VO> page{Entity}s({Entity}QueryRequest request) {
        // 1. 构建查询条件
        LambdaQueryWrapper<{Entity}> wrapper = buildQueryWrapper(request);

        // 2. 分页查询
        Page<{Entity}> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<{Entity}> entityPage = {entity}Mapper.selectPage(page, wrapper);

        // 3. 批量转换
        List<{Entity}VO> vos = {entity}Converter.toVO(entityPage.getRecords());

        // 4. 构建返回结果
        Page<{Entity}VO> voPage = new Page<>(
                entityPage.getCurrent(),
                entityPage.getSize(),
                entityPage.getTotal()
        );
        voPage.setRecords(vos);
        return voPage;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<{Entity}> buildQueryWrapper({Entity}QueryRequest request) {
        return new LambdaQueryWrapper<{Entity}>()
                // 固定条件：未删除
                .eq({Entity}::getIsDeleted, false)
                // 动态条件：有值才拼接
                .like(StringUtils.hasText(request.getField1()),
                      {Entity}::getField1, request.getField1())
                .eq(request.getField2() != null,
                    {Entity}::getField2, request.getField2())
                .eq(request.getIsEnabled() != null,
                    {Entity}::getIsEnabled, request.getIsEnabled())
                // 时间范围
                .ge(request.getCreatedAtStart() != null,
                    {Entity}::getCreatedAt, request.getCreatedAtStart())
                .le(request.getCreatedAtEnd() != null,
                    {Entity}::getCreatedAt, request.getCreatedAtEnd())
                // 排序
                .orderByDesc({Entity}::getCreatedAt);
    }

    /**
     * 根据 ID 获取实体，不存在或已删除时抛出异常
     */
    private {Entity} get{Entity}ByIdOrThrow(Long id) {
        {Entity} entity = {entity}Mapper.selectById(id);
        if (entity == null || Boolean.TRUE.equals(entity.getIsDeleted())) {
            throw new BusinessException(ResponseCode.{ENTITY}_NOT_FOUND);
        }
        return entity;
    }
}
```

---

## 八、Controller 模板

```java
package com.company.{module}.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.common.handler.MetaObjectHandlerImpl;
import com.company.common.response.Result;
import com.company.{module}.dto.*;
import com.company.{module}.service.{Entity}Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * {实体}管理控制器
 * <p>
 * 职责：{实体}的 CRUD 接口
 */
@Tag(name = "{实体}管理", description = "{实体} CRUD 接口")
@RestController
@RequestMapping("/{entity}")
public class {Entity}Controller {

    @Resource
    private {Entity}Service {entity}Service;

    @Operation(summary = "获取{实体}详情")
    @GetMapping("/{id}")
    public Result<{Entity}VO> get{Entity}ById(
            @Parameter(description = "{实体}ID", required = true)
            @PathVariable Long id) {
        {Entity}VO vo = {entity}Service.get{Entity}ById(id);
        return Result.ok(vo);
    }

    @Operation(summary = "分页查询{实体}")
    @GetMapping("/list")
    public Result<Page<{Entity}VO>> page{Entity}s(
            @Parameter(description = "查询条件")
            {Entity}QueryRequest request) {
        Page<{Entity}VO> page = {entity}Service.page{Entity}s(request);
        return Result.ok(page);
    }

    @Operation(summary = "创建{实体}")
    @PostMapping
    public Result<Long> create{Entity}(@Valid @RequestBody {Entity}CreateRequest request) {
        Long operatorId = MetaObjectHandlerImpl.getOperatorId();
        Long id = {entity}Service.create{Entity}(request, operatorId);
        return Result.ok(id);
    }

    @Operation(summary = "更新{实体}")
    @PutMapping
    public Result<Void> update{Entity}(@Valid @RequestBody {Entity}UpdateRequest request) {
        Long operatorId = MetaObjectHandlerImpl.getOperatorId();
        {entity}Service.update{Entity}(request, operatorId);
        return Result.ok();
    }

    @Operation(summary = "删除{实体}")
    @DeleteMapping("/{id}")
    public Result<Void> delete{Entity}(
            @Parameter(description = "{实体}ID", required = true)
            @PathVariable Long id) {
        Long operatorId = MetaObjectHandlerImpl.getOperatorId();
        {entity}Service.delete{Entity}(id, operatorId);
        return Result.ok();
    }
}
```

---

## 九、查询条件构建参考

### 9.1 LambdaQueryWrapper 常用方法

| 方法 | 说明 | 示例 |
|------|------|------|
| `eq` | 等于 | `.eq(Entity::getId, id)` |
| `ne` | 不等于 | `.ne(Entity::getStatus, 0)` |
| `like` | 模糊查询 | `.like(Entity::getName, name)` |
| `likeLeft` | 左模糊 | `.likeLeft(Entity::getName, name)` |
| `likeRight` | 右模糊 | `.likeRight(Entity::getName, name)` |
| `in` | IN 查询 | `.in(Entity::getId, ids)` |
| `gt` | 大于 | `.gt(Entity::getAge, 18)` |
| `ge` | 大于等于 | `.ge(Entity::getCreatedAt, startTime)` |
| `lt` | 小于 | `.lt(Entity::getAge, 60)` |
| `le` | 小于等于 | `.le(Entity::getCreatedAt, endTime)` |
| `between` | BETWEEN | `.between(Entity::getCreatedAt, start, end)` |
| `isNull` | IS NULL | `.isNull(Entity::getDeletedAt)` |
| `isNotNull` | IS NOT NULL | `.isNotNull(Entity::getPhone)` |
| `orderByAsc` | 升序排序 | `.orderByAsc(Entity::getCreatedAt)` |
| `orderByDesc` | 降序排序 | `.orderByDesc(Entity::getCreatedAt)` |

### 9.2 动态条件拼接模式

```java
// 条件拼接格式：condition(条件是否为true), field, value
// 当条件为 false 时，该条件不会被拼接到 SQL 中

.like(StringUtils.hasText(request.getName()),
      Entity::getName, request.getName())

.eq(request.getStatus() != null,
    Entity::getStatus, request.getStatus())

.ge(request.getStartTime() != null,
    Entity::getCreatedAt, request.getStartTime())

.le(request.getEndTime() != null,
    Entity::getCreatedAt, request.getEndTime())
```

---

## 十、统一响应格式

### 10.1 响应结构

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1705334400000
}
```

### 10.2 使用方式

```java
// 成功响应（无数据）
return Result.ok();

// 成功响应（带数据）
return Result.ok(data);

// 失败响应（自定义消息）
return Result.fail("错误消息");

// 失败响应（使用响应码枚举）
return Result.fail(ResponseCode.USER_NOT_FOUND);
```

### 10.3 常用响应码

```java
// ========== 通用 ==========
SUCCESS(200, "success"),
BAD_REQUEST(400, "参数错误"),
UNAUTHORIZED(401, "未认证"),
FORBIDDEN(403, "无权限"),
NOT_FOUND(404, "资源不存在"),
INTERNAL_ERROR(500, "服务器错误"),

// ========== 用户模块 ==========
USER_NOT_FOUND(4001, "用户不存在"),
USER_DISABLED(4002, "用户已禁用"),
LOGIN_FAILED(4004, "用户名或密码错误"),
```

---

## 十一、异常处理

### 11.1 抛出业务异常

```java
// 使用响应码枚举
throw new BusinessException(ResponseCode.USER_NOT_FOUND);

// 使用响应码枚举 + 自定义消息
throw new BusinessException(ResponseCode.BAD_REQUEST, "合作方编码已存在");
```

### 11.2 全局异常处理

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    // 参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = extractFieldErrors(e.getBindingResult());
        return Result.fail(ResponseCode.BAD_REQUEST.getCode(), message);
    }

    // 兜底异常处理
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(ResponseCode.INTERNAL_ERROR);
    }
}
```

---

## 十二、开发清单

### 12.1 新建模块 CRUD 步骤

- [ ] 1. 创建 Entity 实体类
- [ ] 2. 创建 Mapper 接口
- [ ] 3. 创建 DTO 类（CreateRequest, UpdateRequest, QueryRequest, VO）
- [ ] 4. 创建 Converter 转换器
- [ ] 5. 创建 Service 接口和实现类
- [ ] 6. 创建 Controller 控制器
- [ ] 7. 在 ResponseCode 中添加响应码
- [ ] 8. 配置 Security 放行路径（如需要）
- [ ] 9. 编写单元测试

### 12.2 代码质量检查

- [ ] 函数长度 < 20 行
- [ ] 缩进层级不超过 3 层
- [ ] 命名符合规范（PascalCase/camelCase）
- [ ] 注释使用中文
- [ ] 事务只在 Service 层使用
- [ ] 敏感数据不输出到日志

### 12.3 安全检查

- [ ] 使用 @Valid 校验参数
- [ ] 密码使用 BCrypt 加密
- [ ] SQL 使用参数化查询
- [ ] JWT 认证集成
- [ ] 权限校验（如需要）

---

## 十三、参考文件

| 类型 | 路径 |
|------|------|
| Entity 示例 | `order-platform-user/src/main/java/com/company/order/visual/user/entity/User.java` |
| Mapper 示例 | `order-platform-user/src/main/java/com/company/order/visual/user/mapper/UserMapper.java` |
| DTO 示例 | `order-platform-user/src/main/java/com/company/order/visual/user/dto/` |
| Converter 示例 | `order-platform-user/src/main/java/com/company/order/visual/user/converter/UserConverter.java` |
| Service 示例 | `order-platform-user/src/main/java/com/company/order/visual/user/service/impl/UserServiceImpl.java` |
| Controller 示例 | `order-platform-user/src/main/java/com/company/order/visual/user/controller/UserController.java` |
| 统一响应 | `order-platform-common/src/main/java/com/company/order/visual/common/response/Result.java` |
| 响应码 | `order-platform-common/src/main/java/com/company/order/visual/common/response/ResponseCode.java` |
| 自动填充 | `order-platform-common/src/main/java/com/company/order/visual/common/handler/MetaObjectHandlerImpl.java` |

---

> **文档版本**: v1.0
> **最后更新**: 2026-01-21
> **维护者**: AI Assistant
