package com.company.order.visual.common.security;

import io.jsonwebtoken.Claims;
import lombok.Getter;

import java.util.Date;

/**
 * Token 解析结果
 * <p>
 * 一次解析，包含所有需要的信息
 */
@Getter
public class TokenInfo {

    private final String rawToken;
    private final Long userId;
    private final String tokenId;
    private final Long version;
    private final Date expiration;
    private final boolean valid;

    private TokenInfo(String rawToken, Long userId, String tokenId, Long version, Date expiration, boolean valid) {
        this.rawToken = rawToken;
        this.userId = userId;
        this.tokenId = tokenId;
        this.version = version;
        this.expiration = expiration;
        this.valid = valid;
    }

    /**
     * 生成时创建有效的 TokenInfo（包含原始 token）
     */
    public static TokenInfo generated(String rawToken, Long userId, String tokenId, Long version, Date expiration) {
        return new TokenInfo(rawToken, userId, tokenId, version, expiration, true);
    }

    /**
     * 从 Claims 构建有效的 TokenInfo（解析时使用）
     * <p>
     * 添加NPE和异常防御，任何解析异常都返回无效Token
     */
    public static TokenInfo valid(String rawToken, Claims claims) {
        try {
            return new TokenInfo(
                    rawToken,
                    Long.parseLong(claims.getSubject()),
                    claims.getId(),
                    claims.get("version", Long.class),
                    claims.getExpiration(),
                    true
            );
        } catch (NullPointerException | NumberFormatException e) {
            // Claims字段缺失或格式错误，返回无效Token
            return invalid();
        }
    }

    /**
     * 无效的 TokenInfo
     */
    public static TokenInfo invalid() {
        return new TokenInfo(null, null, null, null, null, false);
    }

    /**
     * 获取剩余有效时间（毫秒）
     */
    public long getRemainingMillis() {
        if (!valid || expiration == null) {
            return 0L;
        }
        long remaining = expiration.getTime() - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
}
