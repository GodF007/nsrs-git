package com.nsrs.framework.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;

/**
 * Knife4j API文档配置类
 * 统一使用Knife4j作为API文档工具
 */
@Configuration
@EnableKnife4j
@ConditionalOnProperty(name = "knife4j.enable", havingValue = "true", matchIfMissing = false)
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NSRS号码SIM卡资源管理系统API")
                        .version("1.0.0")
                        .description("号码SIM卡资源管理系统的RESTful API文档")
                        .contact(new Contact()
                                .name("NSRS Team")
                                .email("fyq@apac.com")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi msisdnApi() {
        return GroupedOpenApi.builder()
                .group("号码资源管理")
                .packagesToScan("com.nsrs.msisdn.controller")
                .build();
    }

    @Bean
    public GroupedOpenApi simcardApi() {
        return GroupedOpenApi.builder()
                .group("SIM卡管理")
                .packagesToScan("com.nsrs.simcard.controller")
                .build();
    }

    @Bean
    public GroupedOpenApi bindingApi() {
        return GroupedOpenApi.builder()
                .group("绑定管理")
                .packagesToScan("com.nsrs.binding.controller")
                .build();
    }

    @Bean
    public GroupedOpenApi busAccApi() {
        return GroupedOpenApi.builder()
                .group("营业受理")
                .packagesToScan("com.nsrs.busacc.controller")
                .build();
    }

    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("系统管理")
                .packagesToScan("com.nsrs.system.controller")
                .build();
    }

}