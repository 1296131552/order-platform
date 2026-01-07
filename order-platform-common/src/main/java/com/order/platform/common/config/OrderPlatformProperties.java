package com.order.platform.common.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 订单可视化平台 - 统一配置属性类
 *
 * 功能说明：
 * - 集中管理所有业务模块的配置
 * - 从 application.yml 读取配置
 * - 提供类型安全的配置访问
 *
 * 配置结构：
 * order:
 *   platform:
 *     security（安全配置：密码、Token、锁定）
 *     jwt（JWT配置：密钥、过期时间）
 *     cache（缓存配置：TTL、启用状态）
 *
 * 使用方式：
 * <pre>
 * @Autowired
 * private OrderPlatformProperties properties;
 *
 * int maxAttempts = properties.getSecurity().getPassword().getMaxAttempts();
 * </pre>
 *
 * 配置文件示例：
 * <pre>
 * order:
 *   platform:
 *     security:
 *       password:
 *         max-attempts: 5
 *         lock-minutes: 30
 *         expire-days: 90
 *       token:
 *         expiration: 604800
 *     jwt:
 *       secret: ${JWT_SECRET:default-secret-key}
 *       expiration: 604800
 *     cache:
 *       permission-ttl: 300
 *       role-ttl: 300
 * </pre>
 *
 * 设计原则：
 * - 所有配置集中在 common 模块
 * - 避免在各业务模块中分散配置
 * - 配置文件在 API 入口模块统一管理
 *
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "order.platform")
public class OrderPlatformProperties {

    /**
     * 安全配置
     *
     * 说明：包含密码策略、Token策略、账户锁定策略等
     */
    private Security security = new Security();

    /**
     * JWT配置
     *
     * 说明：JWT Token 的生成和验证配置
     */
    private Jwt jwt = new Jwt();

    /**
     * 缓存配置
     *
     * 说明：权限缓存、角色缓存等配置
     */
    private Cache cache = new Cache();

    // ==================== Security 配置 ====================

    /**
     * 安全配置
     */
    @Data
    public static class Security {

        /**
         * 密码策略配置
         */
        private Password password = new Password();

        /**
         * Token策略配置
         */
        private Token token = new Token();

        /**
         * 密码策略
         */
        @Data
        public static class Password {

            /**
             * 密码错误最大尝试次数
             *
             * 说明：连续错误达到此次数后锁定账户
             * 默认：5次
             * 范围：1-10
             */
            @Min(value = 1, message = "密码错误最大尝试次数不能小于1")
            @Max(value = 10, message = "密码错误最大尝试次数不能大于10")
            private int maxAttempts = 5;

            /**
             * 账户锁定时长（分钟）
             *
             * 说明：账户锁定后，需要等待的时间
             * 默认：30分钟
             * 范围：5-120
             */
            @Min(value = 5, message = "锁定时长不能小于5分钟")
            @Max(value = 120, message = "锁定时长不能大于120分钟")
            private int lockMinutes = 30;

            /**
             * 密码过期天数
             *
             * 说明：密码有效期，过期后需要修改密码
             * 默认：90天
             * 范围：30-365
             */
            @Min(value = 30, message = "密码过期天数不能小于30天")
            @Max(value = 365, message = "密码过期天数不能大于365天")
            private int expireDays = 90;

            /**
             * 密码最小长度
             *
             * 默认：6位
             */
            @Min(value = 4, message = "密码最小长度不能小于4")
            @Max(value = 20, message = "密码最小长度不能大于20")
            private int minLength = 6;

            /**
             * 密码最大长度
             *
             * 默认：20位
             */
            @Min(value = 6, message = "密码最大长度不能小于6")
            @Max(value = 50, message = "密码最大长度不能大于50")
            private int maxLength = 20;

            /**
             * 密码最小强度得分
             *
             * 说明：1-5分，至少满足3种字符类型
             */
            @Min(value = 1, message = "密码最小强度不能小于1")
            @Max(value = 5, message = "密码最小强度不能大于5")
            private int minStrength = 3;
        }

        /**
         * Token策略
         */
        @Data
        public static class Token {

            /**
             * Token过期时间（秒）
             *
             * 默认：604800秒（7天）
             */
            @Min(value = 300, message = "Token过期时间不能小于5分钟")
            @Max(value = 2592000, message = "Token过期时间不能大于30天")
            private Long expiration = 604800L;
        }
    }

    // ==================== JWT 配置 ====================

    /**
     * JWT配置
     */
    @Data
    public static class Jwt {

        /**
         * JWT密钥
         *
         * 说明：用于签名和验证JWT Token的密钥
         * 注意：生产环境必须通过环境变量设置
         * 开发环境：使用默认值
         */
        @NotBlank(message = "JWT密钥不能为空")
        private String secret;

        /**
         * Token过期时间（秒）
         *
         * 默认：604800秒（7天）
         */
        @Min(value = 300, message = "Token过期时间不能小于5分钟")
        @Max(value = 2592000, message = "Token过期时间不能大于30天")
        private Long expiration = 604800L;

        /**
         * 刷新Token过期时间（秒）
         *
         * 默认：1209600秒（14天）
         */
        @Min(value = 300, message = "刷新Token过期时间不能小于5分钟")
        @Max(value = 2592000, message = "刷新Token过期时间不能大于30天")
        private Long refreshExpiration = 1209600L;
    }

    // ==================== Cache 配置 ====================

    /**
     * 缓存配置
     */
    @Data
    public static class Cache {

        /**
         * 权限缓存过期时间（秒）
         *
         * 默认：300秒（5分钟）
         */
        @Min(value = 60, message = "权限缓存TTL不能小于1分钟")
        @Max(value = 3600, message = "权限缓存TTL不能大于1小时")
        private int permissionTtl = 300;

        /**
         * 角色缓存过期时间（秒）
         *
         * 默认：300秒（5分钟）
         */
        @Min(value = 60, message = "角色缓存TTL不能小于1分钟")
        @Max(value = 3600, message = "角色缓存TTL不能大于1小时")
        private int roleTtl = 300;

        /**
         * 是否启用缓存
         *
         * 默认：true
         */
        private Boolean enabled = true;
    }
}
