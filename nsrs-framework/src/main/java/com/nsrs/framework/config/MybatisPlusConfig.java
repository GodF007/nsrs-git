package com.nsrs.framework.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 * 
 * @author NSRS
 * @since 2024-01-01
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 分页插件配置
     * 解决ShardingJDBC环境下分页查询total为0的问题
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 添加分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        
        // 设置请求的页面大于最大页后操作，true调回到首页，false继续请求，默认false
        paginationInterceptor.setOverflow(false);
        
        // 设置最大单页限制数量，默认500条，-1不受限制
        paginationInterceptor.setMaxLimit(1000L);
        
        // 开启count的join优化，只针对部分left join
        paginationInterceptor.setOptimizeJoin(true);
        
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        return interceptor;
    }
}