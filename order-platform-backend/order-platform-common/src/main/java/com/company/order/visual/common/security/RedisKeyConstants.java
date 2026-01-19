package com.company.order.visual.common.security;

/**
 * Redis Key 常量
 */
public final class RedisKeyConstants {

    private RedisKeyConstants() {}

    /**
     * Token 黑名单前缀：auth:blacklist:{tokenId}
     */
    public static final String BLACKLIST_PREFIX = "auth:blacklist:";

    /**
     * 用户 Token 版本号前缀：auth:version:user:{userId}
     */
    public static final String VERSION_PREFIX = "auth:version:user:";

    /**
     * 用户活跃 Tokens 集合前缀：auth:tokens:user:{userId}
     */
    public static final String ACTIVE_TOKENS_PREFIX = "auth:tokens:user:";

    /**
     * Token 版本号初始值
     */
    public static final Long INITIAL_TOKEN_VERSION = 1L;
}
