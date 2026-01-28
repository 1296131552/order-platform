package com.company.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;

/**
 * Flyway 多数据源配置
 */
@Configuration
public class FlywayConfig {

    /**
     * 配置 user 数据源的 Flyway 迁移
     */
    @Bean
    public Flyway flywayUser(DataSource dataSource) {
        DynamicRoutingDataSource dynamicDataSource = (DynamicRoutingDataSource) dataSource;
        DataSource userDataSource = dynamicDataSource.getDataSource("user");

        Flyway flyway = Flyway.configure()
                .dataSource(userDataSource)
                .locations("db/migration/user")
                .baselineOnMigrate(true)
                .table("flyway_user_history")
                .validateOnMigrate(true)
                .load();

        flyway.migrate();
        return flyway;
    }

    /**
     * 配置 system 数据源的 Flyway 迁移
     */
    @Bean
    public Flyway flywaySystem(DataSource dataSource) {
        DynamicRoutingDataSource dynamicDataSource = (DynamicRoutingDataSource) dataSource;
        DataSource systemDataSource = dynamicDataSource.getDataSource("system");

        Flyway flyway = Flyway.configure()
                .dataSource(systemDataSource)
                .locations("db/migration/system")
                .baselineOnMigrate(true)
                .table("flyway_system_history")
                .validateOnMigrate(true)
                .load();

        flyway.migrate();
        return flyway;
    }
}
