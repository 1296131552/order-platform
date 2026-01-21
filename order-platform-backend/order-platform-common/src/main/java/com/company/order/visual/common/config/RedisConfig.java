package com.company.order.visual.common.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.lang.NonNull;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 * <p>
 * 职责:
 * - 配置 RedisTemplate 序列化方式（Key=String, Value=JSON）
 * - 配置 Spring Cache 缓存管理器
 *
 * @author Order Platform Team
 * @since 1.0.0
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 配置 RedisTemplate
     * <p>
     * Key 使用 String 序列化
     * Value 使用 JSON 序列化（支持复杂对象）
     *
     * @param connectionFactory Redis 连接工厂
     * @return RedisTemplate<String, Object>
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key 使用 String 序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value 使用 JSON 序列化
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置 Spring Cache 缓存管理器
     * <p>
     * 缓存有效期: 30分钟
     *
     * @param connectionFactory Redis 连接工厂
     * @return CacheManager
     */
    @Bean
    public CacheManager cacheManager(@NonNull RedisConnectionFactory connectionFactory) {
        // 缓存有效期: 30分钟
        final Duration ttl = Duration.ofMinutes(30);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
