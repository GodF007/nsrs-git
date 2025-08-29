package com.nsrs.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 事务管理器配置
 * 用于解决ShardingSphere环境下事务管理器配置问题
 */
@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    /**
     * 配置事务管理器
     * 在ShardingSphere环境下，需要手动配置事务管理器
     * 
     * @param dataSource ShardingSphere代理数据源
     * @return 事务管理器
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}