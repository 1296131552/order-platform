package com.company.order.visual.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 服务
 * <p>
 * 职责：
 * - 生成 JWT Token
 * - 解析 JWT Token（一次解析，返回所有信息）
 */
@Slf4j
@Component
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.jwtProperties = properties;
        this.key = Keys.hmacShaKeyFor(
                properties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
        log.info("JWT 密钥初始化成功，算法={}", key.getAlgorithm());
    }

    /**
     * 生成 JWT Token
     *
     * @param userId       用户 ID
     * @param tokenVersion Token 版本号
     * @return TokenInfo（包含原始 token 和所有信息）
     */
    public TokenInfo generateToken(Long userId, Long tokenVersion) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());
        String tokenId = UUID.randomUUID().toString();

        String rawToken = Jwts.builder()
                .setId(tokenId)
                .setSubject(String.valueOf(userId))
                .claim("version", tokenVersion)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return TokenInfo.generated(rawToken, userId, tokenId, tokenVersion, expiryDate);
    }

    /**
     * 解析 Token
     * <p>
     * 一次解析，返回所有信息
     *
     * @param token JWT Token
     * @return TokenInfo
     */
    public TokenInfo parseToken(String token) {
        if (token == null || token.isEmpty()) {
            return TokenInfo.invalid();
        }
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return TokenInfo.valid(token, claims);
        } catch (ExpiredJwtException e) {
            log.debug("Token 已过期");  // 如果你需要知道过期
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token 无效");    // 所有其他情况
        }
        return TokenInfo.invalid();
    }

}
