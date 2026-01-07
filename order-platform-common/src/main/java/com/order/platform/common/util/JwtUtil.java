package com.order.platform.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT 工具类
 *
 * 功能说明：
 * - 生成 JWT Token（支持用户信息、角色信息）
 * - 解析 Token 获取用户信息
 * - 验证 Token 有效性和过期时间
 *
 * Token Claims 结构：
 * - userId: 用户ID
 * - username: 用户名
 * - roles: 角色代码列表（可选，用于混合方案的角色快照）
 *
 * 使用场景：
 * - 用户登录认证
 * - 接口权限验证
 * - 角色信息传递
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 生成 Token（基础版本，不含角色）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT Token
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        return generateToken(claims);
    }

    /**
     * 生成 Token（增强版本，包含角色信息）
     *
     * 使用场景：混合方案，登录时将用户角色快照存入 Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param roles    角色代码列表（如 ["CUSTOMER_MANAGER", "DATA_ADMIN"]）
     * @return JWT Token
     */
    public String generateToken(Long userId, String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        if (roles != null && !roles.isEmpty()) {
            claims.put("roles", roles);
        }
        return generateToken(claims);
    }

    /**
     * 生成 Token（通用版本，自定义 Claims）
     *
     * @param claims 自定义 Claims
     * @return JWT Token
     */
    public String generateToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 从 Token 中获取 Claims
     *
     * @param token JWT Token
     * @return Claims 对象
     */
    public Claims getClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 Token 中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从 Token 中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 从 Token 中获取角色列表（混合方案使用）
     *
     * 注意事项：
     * - 如果 Token 中无角色信息，返回空列表（表示需要从数据库查询）
     * - 角色是登录时的快照，可能不是最新值
     *
     * @param token JWT Token
     * @return 角色代码列表，不存在时返回空列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Object rolesObj = claims.get("roles");
            if (rolesObj instanceof List) {
                return (List<String>) rolesObj;
            }
            return List.of();
        } catch (Exception e) {
            // 解析失败时返回空列表，让调用方决定是否从数据库查询
            return List.of();
        }
    }

    /**
     * 验证 Token 是否过期
     *
     * @param token JWT Token
     * @return true-已过期，false-未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证 Token
     *
     * @param token JWT Token
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查 Token 是否包含角色信息（混合方案使用）
     *
     * @param token JWT Token
     * @return true-包含角色信息，false-不包含
     */
    public boolean hasRolesInToken(String token) {
        List<String> roles = getRolesFromToken(token);
        return roles != null && !roles.isEmpty();
    }
}
