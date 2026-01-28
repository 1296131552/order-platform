package com.company.common.utils;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.company.common.Constant.JwtConstant;
import com.company.common.enums.result.AuthResultCode;
import com.company.common.exception.BusinessException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
@Component
@Slf4j
public class JwtUtil {
    @Value("${jwt.secret}")
    protected String secret;

    protected Key key;

    // 访问令牌有效期（毫秒）
    @Value("${jwt.expiration}")
    private long expiration;

    // 刷新令牌有效期（毫秒）
    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void init() {
        // 注意: secret 必须是 Base64 编码的字符串，不包含 _ 等非法字符
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    
    /**
     * 从token中提取用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从token中提取用户ID
     */
    public Integer extractUserId(String token) {
        return extractClaim(token, claims -> claims.get(JwtConstant.USER_ID_CLAIM, Integer.class));
    }

    /**
     * 从Authorization头中提取token,Bearer开头  
     */
    public String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(JwtConstant.BEARER_PREFIX)) {
            throw new BusinessException(AuthResultCode.AUTHORIZATION_HEADER_INVALID);
        }
        return authHeader.substring(JwtConstant.BEARER_PREFIX_LENGTH);
    }

    /**
     * 从Authorization头中提取用户名
     */
    public String getUsernameFromAuthHeader(String authHeader) {
        String token = extractToken(authHeader);
        return extractUsername(token);
    }

    /**
     * 从Authorization头中提取用户ID
     */
    public Integer getUserIdFromAuthHeader(String authHeader) {
        String token = extractToken(authHeader);
        return extractUserId(token);
    }

    /**
     * 从token中提取过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从token中提取指定的claim
     */
    protected <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从token中提取所有的claims
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查token是否过期
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 为用户生成访问令牌（包含用户ID）
     * @param username 用户名
     * @param userId 用户ID
     * @return 访问令牌
     */
    public String generateAccessToken(String username, Integer userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstant.TOKEN_TYPE_CLAIM, JwtConstant.ACCESS_TOKEN_TYPE);
        claims.put(JwtConstant.USER_ID_CLAIM, userId);
        return createToken(claims, username, expiration);
    }


    public String generateRefreshToken(String username, Integer userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstant.TOKEN_TYPE_CLAIM, JwtConstant.REFRESH_TOKEN_TYPE);
        claims.put(JwtConstant.USER_ID_CLAIM, userId);
        return createToken(claims, username, refreshExpiration);
    }

    /**
     * 创建token
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证token是否有效
     */
    public Boolean validateToken(String token) {
        // 检查token是否在黑名单中
        if (isAccessToken(token) && isAccessTokenInBlacklist(token)) {
            return false;
        }
        if (isRefreshToken(token) && isRefreshTokenInBlacklist(token)) {
            return false;
        }
        return !isTokenExpired(token);
    }
    
    /**
     * 检查是否是刷新令牌
     */
    public boolean isRefreshToken(String token) {
        try {
            final Claims claims = extractAllClaims(token);
            return JwtConstant.REFRESH_TOKEN_TYPE.equals(claims.get(JwtConstant.TOKEN_TYPE_CLAIM));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查是否是访问令牌
     */
    public boolean isAccessToken(String token) {
        try {
            final Claims claims = extractAllClaims(token);
            return JwtConstant.ACCESS_TOKEN_TYPE.equals(claims.get(JwtConstant.TOKEN_TYPE_CLAIM)) || claims.get(JwtConstant.TOKEN_TYPE_CLAIM) == null; // 兼容旧令牌
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 将访问令牌加入黑名单，过期时间为令牌过期时间
     * 无效的令牌将不会加入黑名单
     * @param token JWT访问令牌
     */
    public void addAccessTokenToBlacklist(String token) {
        String key = JwtConstant.TOKEN_BLACKLIST_PREFIX + JwtConstant.ACCESS_TOKEN_BLACKLIST_SUFFIX + token;
        try {
            long remainingTime = extractExpiration(token).getTime() - System.currentTimeMillis();
            if (remainingTime > 0) {
                redisTemplate.opsForValue().set(key, JwtConstant.BLACKLIST_VALUE, remainingTime, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            // JWT解析失败时，无需加入黑名单
            log.info("accessToken无效，无需加入黑名单");
        }
    }
    
    /**
     * 将刷新令牌加入黑名单，过期时间为令牌过期时间
     * 无效的令牌将不会加入黑名单
     * @param token JWT刷新令牌
     */
    public void addRefreshTokenToBlacklist(String token) {
        String key = JwtConstant.TOKEN_BLACKLIST_PREFIX + JwtConstant.REFRESH_TOKEN_BLACKLIST_SUFFIX + token;
        try {
            long remainingTime = extractExpiration(token).getTime() - System.currentTimeMillis();
            if (remainingTime > 0) {
                redisTemplate.opsForValue().set(key, JwtConstant.BLACKLIST_VALUE, remainingTime, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            // JWT解析失败时，无需加入黑名单
            log.info("refreshToken无效，无需加入黑名单");
        }
    }
    
    /**
     * 检查访问令牌是否在黑名单中
     * @param token JWT访问令牌
     * @return 是否在黑名单中
     */
    public boolean isAccessTokenInBlacklist(String token) {
        String key = JwtConstant.TOKEN_BLACKLIST_PREFIX + JwtConstant.ACCESS_TOKEN_BLACKLIST_SUFFIX + token;
        return redisTemplate.hasKey(key);
    }
    
    /**
     * 检查刷新令牌是否在黑名单中
     * @param token JWT刷新令牌
     * @return 是否在黑名单中
     */
    public boolean isRefreshTokenInBlacklist(String token) {
        String key = JwtConstant.TOKEN_BLACKLIST_PREFIX + JwtConstant.REFRESH_TOKEN_BLACKLIST_SUFFIX + token;
        return redisTemplate.hasKey(key);
    }

}
