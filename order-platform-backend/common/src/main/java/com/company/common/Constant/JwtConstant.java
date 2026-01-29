package com.company.common.Constant;

public class JwtConstant {
    // JWT 相关常量
    public static final String JWT_HEADER_NAME = "Authorization";
    // 角色相关常量
    public static final String ROLE_PREFIX = "role:";

    // JWT相关常量
    public static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    
    // Authorization 头相关常量
    public static final String BEARER_PREFIX = "Bearer ";
    public static final int BEARER_PREFIX_LENGTH = 7;
    
    // JWT Token 类型常量
    public static final String TOKEN_TYPE_CLAIM = "type";
    public static final String USER_ID_CLAIM = "userId";
    public static final String ACCESS_TOKEN_TYPE = "access";
    public static final String REFRESH_TOKEN_TYPE = "refresh";
    public static final String REFRESH_TOKEN = "refreshToken";
    
    // 黑名单键后缀常量
    public static final String ACCESS_TOKEN_BLACKLIST_SUFFIX = "access:";
    public static final String REFRESH_TOKEN_BLACKLIST_SUFFIX = "refresh:";
    
    // Redis 黑名单值常量
    public static final String BLACKLIST_VALUE = "1";
}
