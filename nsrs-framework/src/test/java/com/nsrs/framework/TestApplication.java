package com.nsrs.framework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 测试应用程序启动类
 * 用于Spring Boot集成测试
 *
 * @author NSRS
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}