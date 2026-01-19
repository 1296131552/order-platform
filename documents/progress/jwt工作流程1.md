# JWT 认证工作流程

## 一、完整认证流程（请求 → 响应）

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         请求进入系统                                      │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────┐
│  SecurityFilterChain (Spring Security)                                  │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │  1. 白名单检查                                                    │   │
│  │     ├── /auth/login       → 直接放行 → Controller                │   │
│  │     ├── /doc.html         → 直接放行 → Controller                │   │
│  │     ├── /swagger-ui/**    → 直接放行 → Controller                │   │
│  │     └── 其他白名单路径...                                          │   │
│  │                                                                  │   │
│  │  2. 非白名单 → 进入 JwtAuthenticationFilter                        │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────┐
│  JwtAuthenticationFilter.doFilterInternal()                             │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │  Step 1: 已认证检查                                               │   │
│  │     SecurityContextHolder 已有认证？→ 跳过，执行下一个 Filter      │   │
│  │                                                                  │   │
│  │  Step 2: 提取 Token                                              │   │
│  │     Authorization: Bearer xxx.yyy.zzz                             │   │
│  │     ├── 没有 Header？→ 放行 → 401（没有认证信息）                  │   │
│  │     └── 不是 Bearer 开头？→ 放行 → 401                            │   │
│  │                                                                  │   │
│  │  Step 3: 解析 Token                                              │   │
│  │     TokenInfo = jwtService.parseToken(jwt)                        │   │
│  │     ├── 签名错误？→ invalid → 放行 → 401                          │   │
│  │     ├── 格式错误？→ invalid → 放行 → 401                          │   │
│  │     ├── 已过期？→ invalid → 放行 → 401                            │   │
│  │     └── 成功 → 继续                                               │   │
│  │                                                                  │   │
│  │  Step 4: 黑名单检查                                               │   │
│  │     blacklistService.isBlacklisted(tokenId)                       │   │
│  │     ├── 在黑名单？→ 放行 → 401（已主动登出）                      │   │
│  │     └── 不在黑名单 → 继续                                         │   │
│  │                                                                  │   │
│  │  Step 5: 版本号检查（密码重置后批量失效）                          │   │
│  │     currentVersion = blacklistService.getUserTokenVersion(userId) │   │
│  │     tokenVersion != currentVersion？→ 放行 → 401                  │   │
│  │                                                                  │   │
│  │  Step 6: 加载用户详情                                             │   │
│  │     UserDetails = userDetailsService.loadUserById(userId)        │   │
│  │                                                                  │   │
│  │  Step 7: 设置 SecurityContext                                     │   │
│  │     UsernamePasswordAuthenticationToken auth = new Token(...)     │   │
│  │     SecurityContextHolder.getContext().setAuthentication(auth)    │   │
│  │                                                                  │   │
│  │  └── 执行下一个 Filter → Controller                              │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────┐
│  Controller                                                             │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │  @GetMapping("/api/users/me")                                     │   │
│  │  public Result<UserVO> getCurrentUser() {                         │   │
│  │      // 从 SecurityContext 获取当前用户                            │   │
│  │      Long userId = SecurityUtil.getCurrentUserId();               │   │
│  │      return Result.success(userService.getUserById(userId));      │   │
│  │  }                                                                │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────┐
│                         响应返回                                        │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 二、登录流程（Token 生成）

```
┌─────────────────────────────────────────────────────────────────────────┐
│  POST /auth/login                                                       │
│  { "account": "admin", "password": "123456" }                           │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────┐
│  UserServiceImpl.login()                                                │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │  1. 查询用户                                                     │   │
│  │     User = findUserByAccount(account)                            │   │
│  │     用户不存在？→ LOGIN_FAILED                                   │   │
│  │                                                                  │   │
│  │  2. 验证状态和密码                                                │   │
│  │     ├── isEnabled = false？→ USER_DISABLED                       │   │
│  │     ├── isLocked = true？→ USER_LOCKED                           │   │
│  │     └── passwordEncoder.matches(密码) = false？→ LOGIN_FAILED    │   │
│  │                                                                  │   │
│  │  3. 获取/初始化 Token 版本号                                      │   │
│  │     version = getUserTokenVersion(userId)                        │   │
│  │     version == null？→ 设为 1L，存入 Redis                        │   │
│  │                                                                  │   │
│  │  4. 生成 Token                                                   │   │
│  │     TokenInfo = jwtService.generateToken(userId, version)        │   │
│  │     ├── tokenId = UUID.randomUUID()                              │   │
│  │     ├── payload = { userId, tokenId, version, exp }              │   │
│  │     └── signWith(key) → xxx.yyy.zzz                              │   │
│  │                                                                  │   │
│  │  5. 记录活跃 Token（用于追踪）                                    │   │
│  │     addActiveToken(userId, tokenId) → Redis Set                  │   │
│  │                                                                  │   │
│  │  6. 更新登录信息                                                  │   │
│  │     user.lastLoginTime = now()                                   │   │
│  │     user.loginCount++                                            │   │
│  │                                                                  │   │
│  │  7. 返回                                                         │   │
│  │     return { user: UserVO, token: "xxx.yyy.zzz" }                │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 三、登出流程（Token 失效）

```
┌─────────────────────────────────────────────────────────────────────────┐
│  POST /auth/logout                                                      │
│  Header: Authorization: Bearer xxx.yyy.zzz                              │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────┐
│  UserServiceImpl.logout()                                               │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │  1. 解析当前 Token                                               │   │
│  │     TokenInfo = jwtService.parseToken(token)                     │   │
│  │                                                                  │   │
│  │  2. 加入黑名单                                                   │   │
│  │     addToBlacklist(tokenId, ttlMs)                               │   │
│  │     Redis Key: auth:blacklist:{tokenId}                          │   │
│  │     TTL: Token 剩余有效时间（避免永久堆积）                        │   │
│  │                                                                  │   │
│  │  3. 移除活跃 Token                                               │   │
│  │     removeActiveToken(userId, tokenId)                           │   │
│  │                                                                  │   │
│  │  4. 清除 SecurityContext                                         │   │
│  │     SecurityContextHolder.clearContext()                          │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 四、密码重置流程（批量失效所有 Token）

```
┌─────────────────────────────────────────────────────────────────────────┐
│  POST /auth/reset-password                                              │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────┐
│  UserServiceImpl.resetPassword()                                        │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │  1. 验证旧密码                                                   │   │
│  │     passwordEncoder.matches(旧密码)                              │   │
│  │                                                                  │   │
│  │  2. 更新新密码                                                   │   │
│  │     user.password = passwordEncoder.encode(新密码)               │   │
│  │     userMapper.updateById(user)                                  │   │
│  │                                                                  │   │
│  │  3. 递增 Token 版本号（关键！）                                   │   │
│  │     newVersion = incrementTokenVersion(userId)                   │   │
│  │     Redis Key: auth:version:user:{userId}                        │   │
│  │                                                                  │   │
│  │  结果：该用户所有旧 Token 的 version != currentVersion           │   │
│  │       下次请求会被 Filter 拦截 → 401                             │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 五、数据结构

### JWT Token 结构
```
┌─────────────────────────────────────────────────────────────┐
│  Header                      Payload              Signature  │
│  {                           {                   HMAC-SHA256│
│    "alg": "HS256",            "sub": "123",      (签名)     │
│    "typ": "JWT"               "id": "uuid-xxx",              │
│  }                            "version": 1,                  │
│                               "exp": 1234567890              │
│                              }                              │
└─────────────────────────────────────────────────────────────┘
        xxx.yyy.zzz
```

### Redis 存储结构
```
┌─────────────────────────────────────────────────────────────┐
│  auth:blacklist:{tokenId}     → "1"          (TTL: 剩余时间)  │
│  auth:version:user:{userId}   → "2"          (永久)          │
│  auth:tokens:user:{userId}    → Set{tokenId1, ...}          │
└─────────────────────────────────────────────────────────────┘
```

---

## 六、核心思想

```
Bad programmers: "我要在每个 Controller 里检查 Token！"
Good programmers: "让 Filter 统一处理，Controller 无感知。"

           登录一次 → 拿到 Token
                    ↓
           每次请求带 Token
                    ↓
           Filter 自动验证
                    ↓
           Controller 直接用 SecurityContext

      = 单一职责，关注点分离
```
