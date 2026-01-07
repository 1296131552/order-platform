# JWT Token 角色管理

> **功能模块**：认证授权
> **完成日期**：2026-01-07
> **相关文件**：`order-platform-common/src/main/java/com/order/platform/common/util/JwtUtil.java`

---

## 📋 功能概述

扩展 JWT 工具类，支持在 Token 中存储和读取用户角色信息，实现角色快照功能。

### 核心价值

- **性能优化**：Token 中携带角色快照，避免每次请求都查库
- **混合方案**：支持 Token 快照 + 数据库查询的混合模式
- **向后兼容**：保持原有 API 不变，新增角色相关方法

---

## 🎯 实现方案

### Token Claims 结构

```json
{
  "userId": 1001,
  "username": "zhangsan",
  "roles": ["CUSTOMER_MANAGER", "DATA_ADMIN"],
  "iat": 1704600000,
  "exp": 1705184000
}
```

### 核心方法

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `generateToken(userId, username, roles)` | 生成含角色的 Token | 用户ID、用户名、角色列表 | JWT Token |
| `getRolesFromToken(token)` | 从 Token 获取角色 | JWT Token | 角色代码列表 |
| `hasRolesInToken(token)` | 检查 Token 是否包含角色 | JWT Token | boolean |

---

## 💻 使用示例

### 1. 生成含角色的 Token（登录时）

```java
@Autowired
private JwtUtil jwtUtil;

@Autowired
private UserRoleService userRoleService;

public String login(String username, String password) {
    // 1. 验证用户名密码
    User user = authenticate(username, password);

    // 2. 查询用户角色
    List<String> roles = userRoleService.getRoleCodesByUserId(user.getId());

    // 3. 生成含角色的 Token
    String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roles);

    return token;
}
```

### 2. 解析 Token 获取角色

```java
public List<String> getUserRolesFromToken(String token) {
    // 检查 Token 是否包含角色
    if (jwtUtil.hasRolesInToken(token)) {
        return jwtUtil.getRolesFromToken(token);
    }
    return Collections.emptyList();
}
```

### 3. 兼容旧版本 Token（不含角色）

```java
public List<String> getRolesFallback(String token, Long userId) {
    // 优先从 Token 获取
    if (jwtUtil.hasRolesInToken(token)) {
        return jwtUtil.getRolesFromToken(token);
    }

    // Token 无角色，从数据库查询
    return userRoleService.getRoleCodesByUserId(userId);
}
```

---

## 🔧 技术细节

### 1. 角色存储格式

```java
// JwtUtil.java
public String generateToken(Long userId, String username, List<String> roles) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);
    claims.put("username", username);

    // 角色列表存储到 claims
    if (roles != null && !roles.isEmpty()) {
        claims.put("roles", roles);
    }

    return generateToken(claims);
}
```

### 2. 角色读取逻辑

```java
@SuppressWarnings("unchecked")
public List<String> getRolesFromToken(String token) {
    try {
        Claims claims = getClaimsFromToken(token);
        Object rolesObj = claims.get("roles");

        if (rolesObj instanceof List) {
            return (List<String>) rolesObj;
        }
        return List.of();  // 无角色时返回空列表
    } catch (Exception e) {
        return List.of();  // 解析失败时返回空列表
    }
}
```

### 3. 类型安全处理

- 使用 `@SuppressWarnings("unchecked")` 抑制类型转换警告
- 使用 `instanceof` 检查确保类型安全
- 异常时返回空列表而非 null（避免空指针）

---

## ⚠️ 注意事项

### 角色快照特性

- Token 中的角色是**登录时的快照**，可能不是最新值
- 如果用户角色变更，需要重新登录或等待 Token 过期
- 适合角色变更不频繁的场景

### Token 大小限制

- 角色列表不宜过长（建议不超过 10 个角色）
- 每个角色代码长度建议不超过 50 字符
- 过大的 Token 会影响 HTTP 请求性能

### 向后兼容

- 旧版本 Token（不含 roles 字段）兼容处理
- `getRolesFromToken()` 返回空列表而非 null
- `hasRolesInToken()` 检查 roles 字段是否存在

---

## 🔄 后续优化

| 优化项 | 优先级 | 说明 |
|--------|--------|------|
| 角色版本号 | 中 | 在 Token 中添加角色版本号，支持角色变更检测 |
| 权限列表 | 低 | 将用户权限也存入 Token，进一步减少数据库查询 |
| Token 刷新 | 高 | 提供 refresh_token 机制，自动更新 Token 中的角色 |

---

## 📚 相关文档

- [用户角色查询服务](./用户角色查询服务.md)
- [认证拦截器优化](./认证拦截器优化.md)
- [代码质量改进](./代码质量改进.md)
