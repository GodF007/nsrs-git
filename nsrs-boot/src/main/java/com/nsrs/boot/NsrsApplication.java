package com.nsrs.boot;

import com.nsrs.busacc.config.SimCardSelectionProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 应用程序启动类
 */
@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
@ComponentScan(basePackages = "com.nsrs")
@MapperScan(basePackages = "com.nsrs.**.mapper")
@EnableConfigurationProperties({SimCardSelectionProperties.class})
public class NsrsApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(NsrsApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(NsrsApplication.class);
    }

}