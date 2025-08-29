package com.nsrs.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * WebMvc配置类，解决Springfox 3.0与Spring Boot 2.7.x的兼容性问题
 * Swagger相关配置已通过条件注解禁用
 */
@Configuration
@ConditionalOnProperty(name = "knife4j.enable", havingValue = "true", matchIfMissing = false)
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 解决Springfox与Spring MVC的路径匹配策略不兼容问题
        configurer.setUseTrailingSlashMatch(true);
        configurer.setUseRegisteredSuffixPatternMatch(true);
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Knife4j静态资源映射
        registry.addResourceHandler("/doc.html**")
                .addResourceLocations("classpath:/META-INF/resources/");
        
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    public InternalResourceViewResolver defaultViewResolver() {
        // 添加默认视图解析器，解决Swagger页面加载问题
        return new InternalResourceViewResolver();
    }

}