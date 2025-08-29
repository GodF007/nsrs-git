package com.nsrs.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Web配置类
 * 配置静态资源处理、前端路由支持和CORS跨域配置
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        // 配置API接口的CORS
//        registry.addMapping("/*/**")
//                .allowedOriginPatterns("*")
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                .allowedHeaders("*")
//                .allowCredentials(true)
//                .maxAge(3600);
//
//        /*// 配置NSRS后台管理接口的CORS
//        registry.addMapping("/nsrs/**")
//                .allowedOriginPatterns("*")
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                .allowedHeaders("*")
//                .allowCredentials(true)
//                .maxAge(3600);*/
//    }

    /**
     * 配置静态资源处理
     * 支持前端SPA应用的路由
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 首先配置前端静态资源（具体路径优先）
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        
        // 配置favicon和robots.txt
        registry.addResourceHandler("/favicon.ico", "/robots.txt")
                .addResourceLocations("classpath:/static/");
        
        // 配置前端路由回退到 index.html（仅对非API请求）
        // 使用较低的优先级，确保不会拦截Controller处理的请求
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(false)  // 禁用资源链，避免拦截动态请求
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        log.debug("WebConfig - Processing resource path: {}", resourcePath);
                        
                        // 如果是API请求、druid、actuator请求，不处理
                        if (resourcePath.startsWith("api/") || resourcePath.startsWith("druid/") || resourcePath.startsWith("actuator/")) {
                            log.debug("WebConfig - Skipping API/druid/actuator request: {}", resourcePath);
                            return null;
                        }
                        
                        // 如果是nsrs模块的请求（包括busacc），不处理（让Controller处理）
                        if (resourcePath.startsWith("nsrs/")) {
                            log.debug("WebConfig - Skipping nsrs module request for Controller handling: {}", resourcePath);
                            return null;
                        }
                        
                        // 如果是busacc模块的请求，不处理（让Controller处理）
                        if (resourcePath.startsWith("busacc")) {
                            log.debug("WebConfig - Skipping busacc request for Controller handling: {}", resourcePath);
                            return null;
                        }
                        
                        Resource requestedResource = location.createRelative(resourcePath);
                        
                        // 如果请求的资源存在，直接返回
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            log.debug("WebConfig - Returning existing resource: {}", resourcePath);
                            return requestedResource;
                        }
                        
                        // 对于前端路由，返回index.html
                        log.debug("WebConfig - Returning index.html for SPA route: {}", resourcePath);
                        return new ClassPathResource("/static/index.html");
                    }
                });
    }

}