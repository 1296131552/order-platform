package com.company.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 配置类
 * 配置 Mapper 扫描路径
 */
@Configuration
@MapperScan({
    "com.company.user.mapper",
    "com.company.system.mapper"
})
public class MyBatisConfig {
}
